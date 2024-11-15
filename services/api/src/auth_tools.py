import logging
from services.common import pgquery


class ORG_ROLE_LITERAL:
    USER = "user"
    OPERATOR = "operator"
    ADMIN = "admin"


class UserOrgAssociation:
    def __init__(self, name: str, role: str):
        self.name = name
        self.role = role


class UserInfo:
    def __init__(self, token_user_info: dict):
        self.email = token_user_info.get("email")
        self.organizations: list[UserOrgAssociation] = token_user_info.get(
            "cvmanager_data", {}
        ).get("organizations", [])
        self.super_user = (
            token_user_info.get("cvmanager_data", {}).get("super_user") == "1"
        )
        self.first_name = token_user_info.get("given_name")
        self.last_name = token_user_info.get("family_name")
        self.name = token_user_info.get("name")

    def to_dict(self):
        return {
            "email": self.email,
            "organizations": [org.__dict__ for org in self.organizations],
            "super_user": self.super_user,
            "first_name": self.first_name,
            "last_name": self.last_name,
            "name": self.name,
        }


ENVIRON_USER_KEY = "user"


class EnvironNoAuth:
    pass


class EnvironWithoutOrg:
    def __init__(self, user_info: UserInfo):
        self.user_info = user_info


class EnvironWithOrg(EnvironWithoutOrg):
    def __init__(self, user_info: UserInfo, organization: str, role: str):
        self.user_info = user_info
        self.organization = organization
        self.role = role


####################################### Restrictions By Organization #######################################
def check_rsu_with_org(rsu_ip: str, organization: str) -> bool:
    query = (
        "SELECT rd.ipv4_address "
        "FROM public.rsus rd "
        "JOIN public.rsu_organization_name AS ron_v ON ron_v.rsu_id = rd.rsu_id "
        f"WHERE ron_v.name = '{organization}'"
    )

    logging.debug(f'Executing query: "{query};"')
    data = pgquery.query_db(query)

    rsu_dict = {}
    for row in data:
        row = dict(row[0])
        rsu_dict[row["ipv4_address"]] = row["primary_route"]
    return rsu_ip in rsu_dict


def check_role_above(
    user_role: ORG_ROLE_LITERAL, required_role: ORG_ROLE_LITERAL
) -> bool:
    roles = [ORG_ROLE_LITERAL.USER, ORG_ROLE_LITERAL.OPERATOR, ORG_ROLE_LITERAL.ADMIN]
    return roles.index(user_role) <= roles.index(required_role)
