import logging
from common import pgquery
import json


class ORG_ROLE_LITERAL:
    USER = "user"
    OPERATOR = "operator"
    ADMIN = "admin"


class UserOrgAssociation:
    def __init__(self, name: str, role: str):
        self.name = name
        self.role = role

    def to_dict(self):
        return {"name": self.name, "role": self.role}

    def __repr__(self):
        return json.dumps(self, default=lambda o: o.__dict__, sort_keys=True, indent=4)


class UserInfo:
    def __init__(self, token_user_info: dict):
        logging.warning("Token User Info: " + str(token_user_info))
        self.email = token_user_info.get("email")
        self.organizations: dict[str, UserOrgAssociation] = {
            org["org"]: UserOrgAssociation(org["org"], org["role"])
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
            "organizations": [org.__dict__ for org in self.organizations.values()],
            "super_user": self.super_user,
            "first_name": self.first_name,
            "last_name": self.last_name,
            "name": self.name,
        }

    def __repr__(self):
        return json.dumps(self, default=lambda o: o.__dict__, sort_keys=True, indent=4)


ENVIRON_USER_KEY = "user"


class EnvironNoAuth:
    pass


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
def get_rsu_dict_for_org(organizations: list[str]) -> dict:
    if not organizations:
        return {}
    allowed_orgs_str = ", ".join(f"'{org}'" for org in organizations)
    query = (
        "SELECT rsu.ipv4_address::text AS ipv4_address "
        "FROM public.rsus rsu "
        "JOIN public.rsu_organization AS rsu_org ON rsu_org.rsu_id = rsu.rsu_id "
        "JOIN public.organizations AS org ON org.organization_id = rsu_org.organization_id "
        f"WHERE org.name IN ({allowed_orgs_str})"
    )

    logging.debug(f'Executing query: "{query};"')
    data = pgquery.query_db(query)

    return {row["ipv4_address"]: True for row in data}


def check_rsu_with_org(rsu_ip: str, organizations: list[str]) -> bool:
    rsu_dict = get_rsu_dict_for_org(organizations)
    return rsu_ip in rsu_dict


def get_intersection_dict_for_org(organizations: list[str]) -> dict:
    allowed_orgs_str = ", ".join(f"'{org}'" for org in organizations)
    query = (
        "SELECT intersection.intersection_number as intersection_number "
        "FROM public.intersections rsu "
        "JOIN public.intersection_organization AS intersection_org ON intersection_org.intersection_id = intersection.intersection_id "
        "JOIN public.organizations AS org ON org.organization_id = intersection_org.organization_id "
        f"WHERE org.name IN ({allowed_orgs_str})"
    )

    logging.debug(f'Executing query: "{query};"')
    data = pgquery.query_db(query)

    return {row["intersection_number"]: True for row in data}


def check_intersection_with_org(intersection_id: str, organizations: list[str]) -> bool:
    intersection_dict = get_rsu_dict_for_org(organizations)
    return intersection_id in intersection_dict


def get_user_dict_for_org(organizations: list[str]) -> dict:
    allowed_orgs_str = ", ".join(f"'{org}'" for org in organizations)
    query = (
        "SELECT u.email as email "
        "FROM public.users u "
        "JOIN public.user_organization AS user_org ON user_org.user_id = intersection.user_id "
        "JOIN public.organizations AS org ON org.organization_id = user_org.organization_id "
        f"WHERE org.name IN ({allowed_orgs_str})"
    )

    logging.debug(f'Executing query: "{query};"')
    data = pgquery.query_db(query)

    return {row["email"]: True for row in data}


def check_user_with_org(user_email: str, organizations: list[str]) -> bool:
    user_dict = get_user_dict_for_org(organizations)
    return user_email in user_dict


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
    for org in user.user_info.organizations:
        if check_role_above(org.role, required_role):
            allowed_orgs.append(org.name)
    return allowed_orgs
