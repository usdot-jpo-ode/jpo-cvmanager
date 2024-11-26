from common.auth_tools import ORG_ROLE_LITERAL, EnvironWithOrg, UserInfo


jwt_token_data_good = {
    "exp": 1731624083,
    "iat": 1731622283,
    "jti": "eb188cbd-d376-4184-bc77-e1bd6db558d7",
    "iss": "http://cvmanager.auth.com:8084/realms/cvmanager",
    "aud": "account",
    "sub": "f:a6d0ac0a-279e-40f6-a226-81cc7d5be501:fc3d8729-8526-4aaa-805b-d64bf3b93860",
    "typ": "Bearer",
    "azp": "cvmanager-gui",
    "session_state": "6681248d-b190-4d27-a160-b2e896cbafb1",
    "acr": "1",
    "allowed-origins": ["http://cvmanager.local.com", "http://localhost:3000"],
    "realm_access": {"roles": ["offline_access", "uma_authorization", "USER"]},
    "resource_access": {
        "account": {"roles": ["manage-account", "manage-account-links", "view-profile"]}
    },
    "scope": "profile openid email",
    "sid": "6681248d-b190-4d27-a160-b2e896cbafb1",
    "email_verified": False,
    "name": "Test User",
    "preferred_username": "test@gmail.com",
    "given_name": "Test",
    "cvmanager_data": {
        "super_user": "1",
        "organizations": [
            {"org": "Test Org", "role": "admin"},
            {"org": "Test Org 2", "role": "operator"},
            {"org": "Test Org 3", "role": "user"},
        ],
        "user_created_timestamp": 1731618005319,
    },
    "family_name": "User",
    "email": "test@gmail.com",
}


def get_user_info_good():
    return UserInfo(jwt_token_data_good)


def get_request_environ(org, role):
    return EnvironWithOrg(get_user_info_good(), org, role)
