from typing import TypedDict
from werkzeug.wrappers import Request, Response
from flask import Request as FlaskRequest
from keycloak import KeycloakOpenID
import logging
import os
import common.pgquery as pgquery


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


def get_user_role(token) -> UserInfo | None:
    keycloak_openid = KeycloakOpenID(
        server_url=os.getenv("KEYCLOAK_ENDPOINT"),
        realm_name=os.getenv("KEYCLOAK_REALM"),
        client_id=os.getenv("KEYCLOAK_API_CLIENT_ID"),
        client_secret_key=os.getenv("KEYCLOAK_API_CLIENT_SECRET_KEY"),
    )
    logging.debug(f"Middleware get_user_role introspect token {token}")
    introspect = keycloak_openid.introspect(token)
    data = None

    if introspect["active"]:
        user_info = keycloak_openid.userinfo(token)

        # Pull all user data from authenticated token
        data = UserInfo(user_info)

        logging.debug(f"Middleware get_user_role get user info of {data['email']}")
    else:
        logging.error("User token does not exist", token)

    return None


organization_required = {
    "/user-auth": False,
    "/rsuinfo": True,
    "/rsu-online-status": True,
    "/rsucounts": True,
    "/rsu-msgfwd-query": True,
    "/rsu-command": True,
    "/rsu-map-info": True,
    "/iss-scms-status": True,
    "/wzdx-feed": False,
    "/rsu-geo-msg-data": False,
    "/rsu-ssm-srm-data": False,
    "/admin-new-rsu": True,
    "/admin-rsu": True,
    "/admin-new-intersection": True,
    "/admin-intersection": True,
    "/admin-new-user": True,
    "/admin-user": True,
    "/admin-new-org": True,
    "/admin-org": True,
    "/rsu-geo-query": True,
    "/admin-new-notification": True,
    "/admin-notification": True,
    "/rsu-error-summary": True,
}


def check_auth_exempt(method, path):
    # Do not bother authorizing a CORS check
    if method == "OPTIONS":
        return True

    exempt_paths = ["/", "/contact-support", "/rsu-error-summary"]
    if path in exempt_paths:
        return True

    return False


class Middleware:
    def __init__(self, app):
        self.app = app
        self.default_headers = {
            "Access-Control-Allow-Origin": os.environ["CORS_DOMAIN"],
            "Content-Type": "application/json",
        }

    def __call__(self, environ, start_response):
        request = Request(environ)
        logging.info(f"Request - {request.method} {request.path}")

        # Check if the method and path is exempt from authorization
        if check_auth_exempt(request.method, request.path):
            return self.app(environ, start_response)

        environ["user"] = EnvironNoAuth()
        try:
            # Verify user token ID is a real token
            token_id = request.headers["Authorization"]
            # Verify authorized user
            user_info = get_user_role(token_id)
            if user_info:
                environ["user"] = EnvironWithoutOrg(user_info)
                # environ["user_info"] = user_info

                # If endpoint requires, check if user is permitted for the specified organization
                permitted = False
                if organization_required[request.path]:
                    requested_org = request.headers["Organization"]
                    for org in user_info.organizations:
                        if org.name == requested_org:
                            permitted = True
                            environ["user"] = EnvironWithOrg(
                                user_info, org.name, org.role
                            )
                elif "admin" in request.path:
                    if user_info.super_user:
                        permitted = True
                else:
                    permitted = True

                if permitted:
                    return self.app(environ, start_response)

            res = Response(
                "User unauthorized", status=401, headers=self.default_headers
            )
            logging.debug(f"User unauthorized, returning a 401")
            return res(environ, start_response)
        except Exception as e:
            # Throws an exception if not valid
            logging.exception(f"Invalid token for reason: {e}")
            res = Response(
                "Authorization failed", status=401, headers=self.default_headers
            )
            return res(environ, start_response)


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
