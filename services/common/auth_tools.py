from dataclasses import dataclass
from functools import wraps
import logging
from typing import Optional, Protocol

from flask import request
from common import pgquery
import json

from api.src.errors import UnauthorizedException


class ORG_ROLE_LITERAL:
    USER = "user"
    OPERATOR = "operator"
    ADMIN = "admin"


class RESOURCE_TYPE:
    USER = "user"
    RSU = "rsu"
    INTERSECTION = "intersection"


class UserInfo:
    def __init__(self, token_user_info: dict):
        logging.warning("Token User Info: " + str(token_user_info))
        self.email = token_user_info.get("email")
        self.organizations: dict[str, ORG_ROLE_LITERAL] = {
            org["org"]: org["role"]
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
                {"org": name, "role": role} for name, role in self.organizations.items()
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


class EnvironWithOrg(EnvironWithoutOrg):
    def __init__(self, user_info: UserInfo, organization: str, role: ORG_ROLE_LITERAL):
        self.user_info = user_info
        self.organization = organization
        self.role = role


####################################### Restrictions By Organization #######################################
def check_rsu_with_org(rsu_ip: str, organizations: list[str]) -> bool:
    if not organizations:
        return {}
    allowed_orgs_str = ", ".join(f"'{org}'" for org in organizations)
    query = (
        "SELECT rsu.ipv4_address::text AS ipv4_address"
        "FROM public.rsus rsu"
        "JOIN public.rsu_organization rsu_org ON rsu.rsu_id = rsu_org.rsu_id"
        "JOIN public.organizations org ON rsu_org.organization_id = org.organization_id"
        f"WHERE org.name = ANY (ARRAY[{allowed_orgs_str}])"
        f"AND rsu.ipv4_address = '{rsu_ip}'"
    )

    logging.debug(f'Executing query: "{query};"')
    data = pgquery.query_db(query)

    return data[0]["ipv4_address"] == rsu_ip if data else False


def check_intersection_with_org(intersection_id: str, organizations: list[str]) -> bool:
    if not organizations:
        return {}
    allowed_orgs_str = ", ".join(f"'{org}'" for org in organizations)
    query = (
        "SELECT intersection.intersection_number as intersection_number "
        "FROM public.intersections rsu "
        "JOIN public.intersection_organization AS intersection_org ON intersection_org.intersection_id = intersection.intersection_id "
        "JOIN public.organizations AS org ON org.organization_id = intersection_org.organization_id "
        f"WHERE org.name = ANY (ARRAY[{allowed_orgs_str}])"
        f"AND intersection.intersection_number = '{intersection_id}'"
    )

    logging.debug(f'Executing query: "{query};"')
    data = pgquery.query_db(query)

    return data[0]["intersection_number"] == intersection_id if data else False


def check_user_with_org(user_email: str, organizations: list[str]) -> bool:
    if not organizations:
        return {}
    allowed_orgs_str = ", ".join(f"'{org}'" for org in organizations)
    query = (
        "SELECT u.email as email "
        "FROM public.users u "
        "JOIN public.user_organization AS user_org ON user_org.user_id = intersection.user_id "
        "JOIN public.organizations AS org ON org.organization_id = user_org.organization_id "
        f"WHERE org.name = ANY (ARRAY[{allowed_orgs_str}])"
        f"AND u.email = '{user_email}'"
    )

    logging.debug(f'Executing query: "{query};"')
    data = pgquery.query_db(query)

    return data[0]["email"] == user_email if data else False


def check_role_above(
    user_role: ORG_ROLE_LITERAL, required_role: ORG_ROLE_LITERAL
) -> bool:
    roles = [
        None,
        ORG_ROLE_LITERAL.USER,
        ORG_ROLE_LITERAL.OPERATOR,
        ORG_ROLE_LITERAL.ADMIN,
    ]
    return roles.index(user_role) >= roles.index(required_role)


def get_qualified_org_list(
    user: EnvironWithOrg, required_role: ORG_ROLE_LITERAL, include_super_user=True
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
            raise UnauthorizedException(
                f"User does not have access to modify notifications for user {user_email}"
            )
    return True


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
        required_role: ORG_ROLE_LITERAL,
        resource_type: Optional[RESOURCE_TYPE] = None,
        resource_id: Optional[str] = None,
    ): ...


@dataclass
class DefaultPermissionChecker:
    def check(
        self,
        user: EnvironWithOrg,
        required_role: ORG_ROLE_LITERAL,
        resource_type: Optional[RESOURCE_TYPE] = None,
        resource_id: Optional[str] = None,
    ) -> PermissionResult:
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
                    message=f"User does not have access to modify data for {resource_type} {resource_id}",
                    user=user,
                )

        if resource_type:
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

        return PermissionResult(
            allowed=True,
            qualified_orgs=qualified_orgs,
            message=None,
            user=user,
        )


class AdditionalCheck(Protocol):
    # Define types here, as if __call__ were a function (ignore self).
    def __call__(
        self,
        user: EnvironWithOrg,
        required_role: ORG_ROLE_LITERAL,
        resource_type: RESOURCE_TYPE,
        resource_id: str,
    ) -> PermissionResult: ...


def require_permission(
    required_role: ORG_ROLE_LITERAL,
    resource_type: Optional[RESOURCE_TYPE] = None,
    checker: Optional[PermissionChecker] = None,
    additional_check: Optional[AdditionalCheck] = None,
):
    """Decorator that requires a specific permission check to pass and passes results to wrapped function"""
    if checker is None:
        checker = DefaultPermissionChecker()

    def decorator(f):
        @wraps(f)
        def wrapper(*args, **kwargs):
            user: EnvironWithOrg = request.environ[ENVIRON_USER_KEY]

            resource_id = (
                args[0]
                if len(args) > 0 and type(args[0]) == str
                else kwargs.get("resource_id", None)
            )

            # Perform permission check
            result = checker.check(user, required_role, resource_type, resource_id)

            # If additional check is provided and the permission check is allowed, run the additional check
            result = (
                additional_check(
                    *args,
                    **kwargs,
                    user=user,
                    required_role=required_role,
                    resource_type=resource_type,
                    resource_id=resource_id,
                )
                if (additional_check and result.allowed)
                else result
            )

            if not result.allowed:
                raise UnauthorizedException(
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
