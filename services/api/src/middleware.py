from werkzeug.wrappers import Request, Response
from keycloak import KeycloakOpenID
import logging
import os

from common.auth_tools import (
    EnvironNoAuth,
    EnvironWithOrg,
    EnvironWithoutOrg,
    UserInfo,
)


class FEATURE_KEYS_LITERAL:
    RSU = "rsu"
    INTERSECTION = "intersection"
    WZDX = "wzdx"


# Feature flag environment variables
ENABLE_RSU_FEATURES = os.getenv("ENABLE_RSU_FEATURES", "true").lower() != "false"
ENABLE_INTERSECTION_FEATURES = (
    os.getenv("ENABLE_INTERSECTION_FEATURES", "true").lower() != "false"
)
ENABLE_WZDX_FEATURES = os.getenv("ENABLE_WZDX_FEATURES", "true").lower() != "false"


def get_user_role(token) -> UserInfo | None:
    # TODO: Consider using pythjon-jose or PyJWT to locally validate the token, instead of calling the Keycloak server
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
        # Pull all user data from authenticated token
        data = UserInfo(introspect)

        logging.debug(f"Middleware get_user_role get user info of {data.email}")
    else:
        logging.error("User token does not exist", token)

    return data


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
    "/admin-new-rsu": False,
    "/admin-rsu": False,
    "/admin-new-intersection": False,
    "/admin-intersection": False,
    "/admin-new-user": False,
    "/admin-user": False,
    "/admin-new-org": False,
    "/admin-org": False,
    "/rsu-geo-query": False,
    "/admin-new-notification": False,
    "/admin-notification": False,
    "/rsu-error-summary": False,
}

# Tag endpoints with the feature they require. The tagged endpoints will automatically be disabled if the feature is disabled
# None: No feature required
# String: Feature required
# Dictionary: Method specific feature required (e.g. {"GET": "rsu", "POST": "intersection"})
feature_tags = {
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
    "/admin-new-notification": None,
    "/admin-notification": None,
    "/rsu-error-summary": "rsu",
}


def check_auth_exempt(method, path):
    # Do not bother authorizing a CORS check
    if method == "OPTIONS":
        return True

    # TODO: check rsu-error-summary authentication required
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
    if not ENABLE_RSU_FEATURES and tag == FEATURE_KEYS_LITERAL.RSU:
        return True
    elif not ENABLE_INTERSECTION_FEATURES and tag == FEATURE_KEYS_LITERAL.INTERSECTION:
        return True
    elif not ENABLE_WZDX_FEATURES and tag == FEATURE_KEYS_LITERAL.WZDX:
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
            "Access-Control-Allow-Origin": os.environ["CORS_DOMAIN"],
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

        environ["user"] = EnvironNoAuth()
        try:
            # Verify user token ID is a real token
            token_id = request.headers["Authorization"]
            logging.warning(f"Authorization Header: {token_id}")
            # Verify authorized user
            user_info = get_user_role(token_id)
            logging.warning(f"User info: {user_info}")
            if user_info:
                environ["user"] = EnvironWithoutOrg(user_info)
                # environ["user_info"] = user_info

                # If endpoint requires, check if user is permitted for the specified organization
                permitted = False
                requested_org = request.headers.get("Organization")
                if requested_org:
                    for name, org in user_info.organizations.items():
                        if name == requested_org:
                            permitted = True
                            environ["user"] = EnvironWithOrg(user_info, name, org.role)
                elif organization_required[request.path]:
                    permitted = False
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
