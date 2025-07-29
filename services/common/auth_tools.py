from enum import Enum
from functools import wraps
import logging
from typing import Optional, Protocol

from flask import request
from common import pgquery
import json
from werkzeug.exceptions import Forbidden, Unauthorized


class ORG_ROLE_LITERAL(str, Enum):
    USER = "user"
    OPERATOR = "operator"
    ADMIN = "admin"


class RESOURCE_TYPE(Enum):
    USER = "user"
    RSU = "rsu"
    INTERSECTION = "intersection"
    ORGANIZATION = "organization"


class UserInfo:
    def __init__(self, token_user_info: dict):
        self.email = token_user_info.get("email")
        self.organizations: dict[str, ORG_ROLE_LITERAL] = {
            org["org"]: ORG_ROLE_LITERAL(org["role"])
            for org in token_user_info.get("cvmanager_data", {}).get(
                "organizations", []
            )
        }
        self.super_user = (
            token_user_info.get("cvmanager_data", {}).get("super_user") == "1"
        )
        self.first_name = token_user_info.get("given_name")
        self.last_name = token_user_info.get("family_name")
        self.name = token_user_info.get("name")

    # This method is exposed to users. It should not include any confidential information.
    def to_dict(self):
        return {
            "email": self.email,
            "organizations": [
                {"name": name, "role": role}
                for name, role in self.organizations.items()
            ],
            "super_user": self.super_user,
            "first_name": self.first_name,
            "last_name": self.last_name,
            "name": self.name,
        }

    def __repr__(self):
        return json.dumps(self, default=lambda o: o.__dict__, sort_keys=True, indent=4)


ENVIRON_USER_KEY = "user"


class EnvironNoAuth:
    def __init__(self):
        self.user_info = None
        self.organization = None
        self.role = None


class EnvironWithoutOrg:
    def __init__(self, user_info: UserInfo):
        self.user_info = user_info
        self.organization = None
        self.role = None


class EnvironWithOrg:
    def __init__(self, user_info: UserInfo, organization: str, role: ORG_ROLE_LITERAL):
        self.user_info = user_info
        self.organization = organization
        self.role = role


####################################### Restrictions By Organization #######################################
def get_rsu_set_for_org(organizations: list[str]) -> set[str]:
    if not organizations:
        return set()

    query = (
        "SELECT rsu.ipv4_address::text AS ipv4_address "
        "FROM public.rsus rsu "
        "JOIN public.rsu_organization AS rsu_org ON rsu_org.rsu_id = rsu.rsu_id "
        "JOIN public.organizations AS org ON org.organization_id = rsu_org.organization_id "
        "WHERE org.name = ANY (:allowed_orgs)"
    )

    logging.debug(f'Executing query: "{query};"')
    params = {"allowed_orgs": organizations}
    data = pgquery.query_db(query, params=params)

    return set([rsu["ipv4_address"] for rsu in data])


def check_rsu_with_org(rsu_ip: str, organizations: list[str]) -> bool:
    if not organizations:
        return False

    query = (
        "SELECT rsu.ipv4_address::text AS ipv4_address "
        "FROM public.rsus rsu "
        "JOIN public.rsu_organization AS rsu_org ON rsu_org.rsu_id = rsu.rsu_id "
        "JOIN public.organizations AS org ON org.organization_id = rsu_org.organization_id "
        "WHERE org.name = ANY (:allowed_orgs) "
        "AND rsu.ipv4_address = :rsu_ip"
    )

    logging.debug(f'Executing query: "{query};"')
    params = {"allowed_orgs": organizations, "rsu_ip": rsu_ip}
    data = pgquery.query_db(query, params=params)

    return data[0]["ipv4_address"] == rsu_ip if data else False


def check_intersection_with_org(intersection_id: str, organizations: list[str]) -> bool:
    if not organizations:
        return False
    query = (
        "SELECT intersection.intersection_number as intersection_number "
        "FROM public.intersections intersection "
        "JOIN public.intersection_organization AS intersection_org ON intersection_org.intersection_id = intersection.intersection_id "
        "JOIN public.organizations AS org ON org.organization_id = intersection_org.organization_id "
        "WHERE org.name = ANY (:allowed_orgs)"
        "AND intersection.intersection_number = :intersection_id"
    )

    logging.debug(f'Executing query: "{query};"')
    params = {"allowed_orgs": organizations, "intersection_id": intersection_id}
    data = pgquery.query_db(query, params=params)

    return data[0]["intersection_number"] == intersection_id if data else False


def check_user_with_org(user_email: str, organizations: list[str]) -> bool:
    if not organizations:
        return False
    query = (
        "SELECT u.email as email "
        "FROM public.users u "
        "JOIN public.user_organization AS user_org ON user_org.user_id = u.user_id "
        "JOIN public.organizations AS org ON org.organization_id = user_org.organization_id "
        "WHERE org.name = ANY (:allowed_orgs)"
        "AND u.email = :user_email"
    )

    logging.debug(f'Executing query: "{query};"')
    params = {"allowed_orgs": organizations, "user_email": user_email}
    data = pgquery.query_db(query, params=params)

    return data[0]["email"] == user_email if data else False


def get_user_info(email: str) -> Optional[UserInfo]:
    user_info_query = (
        "SELECT jsonb_build_object('email', email, 'given_name', first_name, 'family_name', last_name, 'super_user', super_user) "
        "FROM public.users "
        "WHERE email = :email"
    )
    user_info_rows = pgquery.query_db(user_info_query, params={"email": email})
    if not user_info_rows:
        return None
    print(f"User info for {email} found: {user_info_rows[0][0]}")
    user_info_dict = dict(user_info_rows[0][0])
    org_query = (
        "SELECT jsonb_build_object('org', org.name, 'role', roles.name) "
        "FROM public.users u "
        "JOIN public.user_organization uo on u.user_id = uo.user_id "
        "JOIN public.organizations org on uo.organization_id = org.organization_id "
        "JOIN public.roles on uo.role_id = roles.role_id "
        "WHERE u.email = :email"
    )
    org_rows = pgquery.query_db(org_query, params={"email": email})
    user_info_dict["cvmanager_data"] = {
        "organizations": [dict(row[0]) for row in org_rows]
    }  # matching the structure in jwt the JWT token UserInfo is designed to read from
    return UserInfo(user_info_dict)


def get_index_or_default(
    lst: list[ORG_ROLE_LITERAL], item: ORG_ROLE_LITERAL, default=-1
) -> int:
    try:
        return lst.index(item)
    except ValueError:
        return default


def check_role_above(
    user_role: ORG_ROLE_LITERAL, required_role: ORG_ROLE_LITERAL
) -> bool:
    roles = [
        ORG_ROLE_LITERAL.USER,
        ORG_ROLE_LITERAL.OPERATOR,
        ORG_ROLE_LITERAL.ADMIN,
    ]
    return get_index_or_default(roles, user_role) >= get_index_or_default(
        roles, required_role
    )


def get_qualified_org_list(
    user: EnvironWithOrg, required_role: ORG_ROLE_LITERAL, include_super_user=False
) -> list[str]:
    if include_super_user and user.user_info.super_user:
        return pgquery.query_and_return_list(
            "SELECT name FROM public.organizations ORDER BY name ASC"
        )
    allowed_orgs = []
    for org_name, org_role in user.user_info.organizations.items():
        if check_role_above(org_role, required_role):
            allowed_orgs.append(org_name)
    return allowed_orgs


def protect_user_access(user_email: str, user: EnvironWithOrg):
    if user_email != user.user_info.email:
        qualified_orgs = get_qualified_org_list(
            user, ORG_ROLE_LITERAL.ADMIN, include_super_user=False
        )
        if not user.user_info.super_user and not check_user_with_org(
            user_email, qualified_orgs
        ):
            raise Forbidden(
                f"403 Forbidden: User does not have access to modify notifications for user {user_email}"
            )
    return True


def enforce_organization_restrictions(
    user: EnvironWithOrg,
    qualified_orgs: list[str],
    spec: dict,
    keys_to_check: list[str],
):
    """
    Validates that the user has the necessary permissions to modify specific organizational relationships.

    This function ensures that the user is authorized to modify particular organizations.
    If the user attempts to modify organizations they are not authorized for, a `Forbidden` exception is raised.

    Args:
        user (EnvironWithOrg): The user making the request, including their organizational context.
        qualified_orgs (list[str]): A list of organizations the user is authorized to modify.
        spec (dict): A dictionary containing the organizations to modify.
        keys_to_check (list[str]): A list of keys in the intersection_spec that should be authorized.

    Raises:
        Forbidden: If the user attempts to modify organizations they are not authorized to modify.
    """
    if not user.user_info.super_user:
        for key in keys_to_check:
            # Collect list of organizations the user doesn't have enough permissions to modify
            unqualified_orgs = [
                org for org in spec.get(key, []) if org not in qualified_orgs
            ]
            # If the user tries to add organizations they are not authorized for, raise Forbidden
            if unqualified_orgs:
                raise Forbidden(
                    f"Unauthorized organization modification through {key}: {','.join(unqualified_orgs)}"
                )


class PermissionResult:
    def __init__(
        self,
        allowed: bool,
        qualified_orgs: list[str],
        message: Optional[str],
        user: EnvironWithOrg,
    ):
        self.allowed = allowed
        self.qualified_orgs = qualified_orgs
        self.message = message
        self.user = user

    def to_dict(self):
        return self.__dict__


class PermissionChecker(Protocol):
    def check(
        self,
        user: EnvironWithOrg,
        required_role: ORG_ROLE_LITERAL | None,
        resource_type: Optional[RESOURCE_TYPE] = None,
        resource_id: Optional[str] = None,
    ): ...


class DefaultPermissionChecker:
    def check(
        self,
        user: EnvironWithOrg,
        required_role: ORG_ROLE_LITERAL | None,
        resource_type: Optional[RESOURCE_TYPE] = None,
        resource_id: Optional[str] = None,
    ) -> PermissionResult:
        if required_role is None:
            return PermissionResult(
                allowed=True,
                qualified_orgs=list(user.user_info.organizations.keys()),
                message=None,
                user=user,
            )

        qualified_orgs = get_qualified_org_list(
            user, required_role, include_super_user=False
        )

        if user.user_info.super_user:
            return PermissionResult(
                allowed=True,
                qualified_orgs=qualified_orgs,
                message=None,
                user=user,
            )

        if user.organization is not None:
            if not check_role_above(user.role, required_role):
                return PermissionResult(
                    allowed=False,
                    qualified_orgs=qualified_orgs,
                    message=f"User does not have sufficient access to organization {user.organization}. Require role: {required_role}",
                    user=user,
                )

        if required_role and not qualified_orgs:
            return PermissionResult(
                allowed=False,
                qualified_orgs=qualified_orgs,
                message=f"User does not have sufficient access in any organization. Require role: {required_role}",
                user=user,
            )

        if resource_type and resource_id is not None and resource_id != "all":
            match resource_type:
                case RESOURCE_TYPE.USER:
                    if resource_id is not None and resource_id != user.user_info.email:
                        if not check_user_with_org(resource_id, qualified_orgs):
                            return PermissionResult(
                                allowed=False,
                                qualified_orgs=qualified_orgs,
                                message=f"User does not have access to modify data for user {resource_id}",
                                user=user,
                            )
                case RESOURCE_TYPE.RSU:
                    if not check_rsu_with_org(resource_id, qualified_orgs):
                        return PermissionResult(
                            allowed=False,
                            qualified_orgs=qualified_orgs,
                            message=f"User does not have access to modify data for RSU {resource_id}",
                            user=user,
                        )
                case RESOURCE_TYPE.INTERSECTION:
                    if not check_intersection_with_org(resource_id, qualified_orgs):
                        return PermissionResult(
                            allowed=False,
                            qualified_orgs=qualified_orgs,
                            message=f"User does not have access to modify data for intersection {resource_id}",
                            user=user,
                        )
                case RESOURCE_TYPE.ORGANIZATION:
                    if resource_id not in qualified_orgs:
                        return PermissionResult(
                            allowed=False,
                            qualified_orgs=qualified_orgs,
                            message=f"User does not have access to modify data for organization {resource_id}",
                            user=user,
                        )

        return PermissionResult(
            allowed=True,
            qualified_orgs=qualified_orgs,
            message=None,
            user=user,
        )


def require_permission(
    required_role: ORG_ROLE_LITERAL | None,
    resource_type: Optional[RESOURCE_TYPE] = None,
    checker: Optional[PermissionChecker] = None,
):
    """Decorator that requires a specific permission check to pass and passes results to wrapped function"""
    if checker is None:
        checker = DefaultPermissionChecker()

    def decorator(f):
        @wraps(f)
        def wrapper(*args, **kwargs):
            user: EnvironWithOrg = request.environ[ENVIRON_USER_KEY]
            if not user.user_info:
                raise Unauthorized("User is not authenticated")

            resource_id = (
                args[0]
                if len(args) > 0 and isinstance(args[0], str)
                else kwargs.get("resource_id", None)
            )

            # Perform permission check
            result = checker.check(user, required_role, resource_type, resource_id)

            if not result.allowed:
                raise Forbidden(
                    result.message or "User does not have required permissions"
                )
            # Only add permission_result to kwargs if the function accepts it
            from inspect import signature

            sig = signature(f)
            if "permission_result" in sig.parameters:
                kwargs["permission_result"] = result

            return f(*args, **kwargs)

        return wrapper

    return decorator
