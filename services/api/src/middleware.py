from enum import Enum
from werkzeug.wrappers import Request
from keycloak import KeycloakOpenID
import logging
import os
from werkzeug.exceptions import Forbidden, Unauthorized, NotImplemented

from common.auth_tools import (
    ENVIRON_USER_KEY,
    EnvironNoAuth,
    EnvironWithOrg,
    EnvironWithoutOrg,
    UserInfo,
)


class FEATURE_KEYS_LITERAL(Enum):
    RSU = "rsu"
    INTERSECTION = "intersection"
    WZDX = "wzdx"
    MOOVE_AI = "moove_ai"


# Feature flag environment variables
ENABLE_RSU_FEATURES = os.getenv("ENABLE_RSU_FEATURES", "true").lower() != "false"
ENABLE_INTERSECTION_FEATURES = (
    os.getenv("ENABLE_INTERSECTION_FEATURES", "true").lower() != "false"
)
ENABLE_WZDX_FEATURES = os.getenv("ENABLE_WZDX_FEATURES", "true").lower() != "false"
ENABLE_MOOVE_AI_FEATURES = (
    os.getenv("ENABLE_MOOVE_AI_FEATURES", "true").lower() != "false"
)


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
    "/rsu-config-geo-query": True,
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
feature_tags: dict[str, FEATURE_KEYS_LITERAL | None] = {
    "/": None,
    "/user-auth": None,
    "/rsuinfo": FEATURE_KEYS_LITERAL.RSU,
    "/rsu-online-status": FEATURE_KEYS_LITERAL.RSU,
    "/rsucounts": FEATURE_KEYS_LITERAL.RSU,
    "/rsu-msgfwd-query": FEATURE_KEYS_LITERAL.RSU,
    "/rsu-command": FEATURE_KEYS_LITERAL.RSU,
    "/rsu-map-info": FEATURE_KEYS_LITERAL.RSU,
    "/iss-scms-status": FEATURE_KEYS_LITERAL.RSU,
    "/wzdx-feed": FEATURE_KEYS_LITERAL.WZDX,
    "/rsu-geo-msg-data": FEATURE_KEYS_LITERAL.RSU,
    "/rsu-ssm-srm-data": FEATURE_KEYS_LITERAL.RSU,
    "/admin-new-rsu": FEATURE_KEYS_LITERAL.RSU,
    "/admin-rsu": FEATURE_KEYS_LITERAL.RSU,
    "/admin-new-intersection": FEATURE_KEYS_LITERAL.INTERSECTION,
    "/admin-intersection": FEATURE_KEYS_LITERAL.INTERSECTION,
    "/admin-new-user": None,
    "/admin-user": None,
    "/admin-new-org": None,
    "/admin-org": None,
    "/rsu-config-geo-query": FEATURE_KEYS_LITERAL.RSU,
    "/rsu-geo-query": FEATURE_KEYS_LITERAL.RSU,
    "/moove-ai-data": FEATURE_KEYS_LITERAL.MOOVE_AI,
    "/admin-new-notification": None,
    "/admin-notification": None,
    "/rsu-error-summary": FEATURE_KEYS_LITERAL.RSU,
}


def check_auth_exempt(method, path):
    # Do not bother authorizing a CORS check
    if method == "OPTIONS":
        return True

    exempt_paths = ["/", "/contact-support"]
    if path in exempt_paths:
        return True

    return False


def is_tag_disabled(tag: FEATURE_KEYS_LITERAL | None) -> bool:
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
    elif not ENABLE_MOOVE_AI_FEATURES and tag == FEATURE_KEYS_LITERAL.MOOVE_AI:
        return True
    return False


def is_endpoint_disabled(feature_tags: dict, path: str) -> bool:
    """
    Check if the endpoint/method is disabled by feature flags

    Args:
        feature_tags: dict: The dictionary of feature tags
        path: str: The path of the request
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
        return is_tag_disabled(feature_tags.get(path))
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
        try:
            # Enforce Feature Flags from environment variables
            if is_endpoint_disabled(feature_tags, request.path):
                # Return 501, Not Implemented
                raise NotImplemented("Feature disabled by feature flag")  # noqa: F901

            environ[ENVIRON_USER_KEY] = EnvironNoAuth()

            # Check if the method and path is exempt from authorization
            if check_auth_exempt(request.method, request.path):
                return self.app(environ, start_response)

            # Verify user token ID is a real token
            token_id = request.headers.get("Authorization")
            if not token_id:
                raise Unauthorized("Authorization header not found")
            logging.warning(f"Authorization Header: {token_id}")
            # Verify authorized user
            user_info = get_user_role(token_id)
            logging.warning(f"User info: {user_info}")
            if not user_info:
                raise Unauthorized("Failed to parse Authorization token")
            environ[ENVIRON_USER_KEY] = EnvironWithoutOrg(user_info)

            # If endpoint requires, check if user is permitted for the specified organization
            permitted = False
            requested_org = request.headers.get("Organization")
            if requested_org:
                for org_name, org_role in user_info.organizations.items():
                    if org_name == requested_org:
                        org_name = True
                        environ[ENVIRON_USER_KEY] = EnvironWithOrg(
                            user_info, org_name, org_role
                        )
                        permitted = True
            elif organization_required.get(request.path):
                raise Forbidden("Organization Required")
            else:
                permitted = True

            if not permitted:
                raise Forbidden("User unauthorized")

            return self.app(environ, start_response)
        except Unauthorized:
            raise
        except Forbidden:
            raise
        except NotImplemented:
            raise
        except Exception as e:
            # Throws an exception if not valid
            logging.exception(f"Invalid token for reason: {e}")
            raise Unauthorized(f"Authorization failed: {e}")
