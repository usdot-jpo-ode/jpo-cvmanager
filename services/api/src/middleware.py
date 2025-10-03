from werkzeug.wrappers import Request, Response
from keycloak import KeycloakOpenID
import logging
import api_environment
import common.pgquery as pgquery


class FEATURE_KEYS_LITERAL:
    RSU = "rsu"
    INTERSECTION = "intersection"
    WZDX = "wzdx"
    MOOVE_AI = "moove_ai"


def get_user_role(token):
    keycloak_openid = KeycloakOpenID(
        server_url=api_environment.KEYCLOAK_ENDPOINT,
        realm_name=api_environment.KEYCLOAK_REALM,
        client_id=api_environment.KEYCLOAK_API_CLIENT_ID,
        client_secret_key=api_environment.KEYCLOAK_API_CLIENT_SECRET_KEY,
    )
    logging.debug(f"Middleware get_user_role introspect token {token}")
    introspect = keycloak_openid.introspect(token)
    data = []

    if introspect["active"]:
        userinfo = keycloak_openid.userinfo(token)
        logging.debug(f"Middleware get_user_role get user info of {userinfo['email']}")

        email = userinfo["email"]

        # TODO: Eventually convert this query to allow users without organizations. This involves changing the query to use LEFT JOIN(s).
        query = (
            "SELECT jsonb_build_object('email', u.email, 'first_name', u.first_name, 'last_name', u.last_name, 'organization', org.name, 'role', roles.name, 'super_user', u.super_user) "
            "FROM public.users u "
            "JOIN public.user_organization uo on u.user_id = uo.user_id "
            "JOIN public.organizations org on uo.organization_id = org.organization_id "
            "JOIN public.roles on uo.role_id = roles.role_id "
            f"WHERE u.email = '{email}'"
        )

        logging.debug(f'Executing query "{query};"...')
        data = pgquery.query_db(query)
    else:
        logging.error("User token does not exist", token)

    if len(data) != 0:
        return data
    return None


organization_required = {
    "/user-auth": False,
    "/rsuinfo": True,
    "/rsu-online-status": True,
    "/rsucounts": True,
    "/rsu-msgfwd-query": True,
    "/rsu-command": True,
    "/iss-scms-status": True,
    "/wzdx-feed": False,
    "/rsu-geo-msg-data": False,
    "/rsu-ssm-srm-data": False,
    "/admin-new-rsu": False,
    "/admin-rsu": False,
    "/admin-new-intersection": False,
    "/admin-intersection": False,
    "/admin-new-user": False,
    "/admin-user": False,
    "/admin-new-org": False,
    "/admin-org": False,
    "/rsu-geo-query": True,
    "/moove-ai-data": False,
    "/admin-new-notification": False,
    "/admin-notification": False,
    "/rsu-error-summary": False,
}

# Tag endpoints with the feature they require. The tagged endpoints will automatically be disabled if the feature is disabled
# None: No feature required
# String: Feature required
# Dictionary: Method specific feature required (e.g. {"GET": "rsu", "POST": "intersection"})
feature_tags = {
    "/": None,
    "/user-auth": None,
    "/rsuinfo": "rsu",
    "/rsu-online-status": "rsu",
    "/rsucounts": "rsu",
    "/rsu-msgfwd-query": "rsu",
    "/rsu-command": "rsu",
    "/rsu-map-info": "rsu",
    "/iss-scms-status": "rsu",
    "/wzdx-feed": "wzdx",
    "/rsu-geo-msg-data": "rsu",
    "/rsu-ssm-srm-data": "rsu",
    "/admin-new-rsu": "rsu",
    "/admin-rsu": "rsu",
    "/admin-new-intersection": "intersection",
    "/admin-intersection": "intersection",
    "/admin-new-user": None,
    "/admin-user": None,
    "/admin-new-org": None,
    "/admin-org": None,
    "/rsu-geo-query": "rsu",
    "/moove-ai-data": "moove_ai",
    "/admin-new-notification": None,
    "/admin-notification": None,
    "/rsu-error-summary": "rsu",
}


def check_auth_exempt(method, path):
    # Do not bother authorizing a CORS check
    if method == "OPTIONS":
        return True

    exempt_paths = ["/", "/contact-support", "/rsu-error-summary"]
    if path in exempt_paths:
        return True

    return False


def is_tag_disabled(tag: FEATURE_KEYS_LITERAL) -> bool:
    """
    Evaluate the tag to determine if the feature should be disabled

    Args:
        tag: ["rsu", "intersection", "wzdx"]: The feature tag to evaluate
    Returns:
        bool: True if the feature should be disabled, False otherwise
    """
    if not api_environment.ENABLE_RSU_FEATURES and tag == FEATURE_KEYS_LITERAL.RSU:
        return True
    elif (
        not api_environment.ENABLE_INTERSECTION_FEATURES
        and tag == FEATURE_KEYS_LITERAL.INTERSECTION
    ):
        return True
    elif not api_environment.ENABLE_WZDX_FEATURES and tag == FEATURE_KEYS_LITERAL.WZDX:
        return True
    elif (
        not api_environment.ENABLE_MOOVE_AI_FEATURES
        and tag == FEATURE_KEYS_LITERAL.MOOVE_AI
    ):
        return True
    return False


def is_endpoint_disabled(feature_tags: dict, path: str, method: str) -> bool:
    """
    Check if the endpoint/method is disabled by feature flags

    Args:
        path: str: The path of the request
        method: str: The HTTP method of the request
    Returns:
        bool: True if the endpoint/method is disabled, False otherwise

    **Logic**:
        Check if the endpoint path or method is tagged with a feature
        1. if path tag is None, the endpoint is not tagged
        2. if path tag is a string, the path is tagged with a feature
        3. if path tag is a dictionary, methods are individually tagged. Check current method against dictionary
    """

    if path not in feature_tags:
        logging.warning(f"Feature tag not found for endpoint path: {path}")
        return False
    if feature_tags.get(path) is not None:
        if type(feature_tags.get(path)) is dict:
            tag = feature_tags.get(path).get(method)
            if tag is not None:
                return is_tag_disabled(tag)
        else:
            tag = feature_tags.get(path)
            return is_tag_disabled(tag)
    return False


class Middleware:
    def __init__(self, app):
        self.app = app
        self.default_headers = {
            "Access-Control-Allow-Origin": api_environment.CORS_DOMAIN,
            "Content-Type": "application/json",
        }

    def __call__(self, environ, start_response):
        request = Request(environ)
        logging.info(f"Request - {request.method} {request.path}")

        # Enforce Feature Flags from environment variables
        if is_endpoint_disabled(feature_tags, request.path, request.method):
            res = Response("Feature disabled", status=405, headers=self.default_headers)
            res(environ, start_response)

        # Check if the method and path is exempt from authorization
        if check_auth_exempt(request.method, request.path):
            return self.app(environ, start_response)

        try:
            # Verify user token ID is a real token
            token_id = request.headers["Authorization"]
            # Verify authorized user
            data = get_user_role(token_id)
            if data:
                user_info = {
                    "name": f'{data[0][0]["first_name"]} {data[0][0]["last_name"]}',
                    "email": data[0][0]["email"],
                    "organizations": [],
                    "super_user": True if data[0][0]["super_user"] == "1" else False,
                }

                # Parse the organization permissions
                for org in data:
                    user_info["organizations"].append(
                        {"name": org[0]["organization"], "role": org[0]["role"]}
                    )
                environ["user_info"] = user_info

                # If endpoint requires, check if user is permitted for the specified organization
                permitted = False
                if organization_required[request.path]:
                    requested_org = request.headers["Organization"]
                    for permission in user_info["organizations"]:
                        if permission["name"] == requested_org:
                            permitted = True
                            environ["organization"] = permission["name"]
                            environ["role"] = permission["role"]
                elif "admin" in request.path:
                    if user_info["super_user"]:
                        permitted = True
                else:
                    permitted = True

                if permitted:
                    return self.app(environ, start_response)

            res = Response(
                "User unauthorized", status=401, headers=self.default_headers
            )
            logging.debug("User unauthorized, returning a 401")
            return res(environ, start_response)
        except Exception as e:
            # Throws an exception if not valid
            logging.exception(f"Invalid token for reason: {e}")
            res = Response(
                "Authorization failed", status=401, headers=self.default_headers
            )
            return res(environ, start_response)
