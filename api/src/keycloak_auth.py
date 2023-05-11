import traceback
from keycloak import KeycloakOpenID
import logging


class KeycloakProvider:
    def __init__(
        self,
        KEYCLOAK_ENDPOINT,
        KEYCLOAK_REALM,
        KEYCLOAK_CLIENT,
        KEYCLOAK_SECRET_KEY,
    ):
        self.keycloak_openid = KeycloakOpenID(
            server_url=f"{KEYCLOAK_ENDPOINT}/",
            client_id=KEYCLOAK_CLIENT,
            realm_name=KEYCLOAK_REALM,
            client_secret_key=KEYCLOAK_SECRET_KEY,
        )
        token = self.keycloak_openid.token("admin@cimms.com", "12345")
        userinfo = self.keycloak_openid.userinfo(token["access_token"])
        print(userinfo)

    def get_access_token_user(self, username: str, password: str):
        try:
            return self.keycloak_openid.token(username, password)
        except:
            return None

    def get_user_info(self, token):
        try:
            response = self.keycloak_openid.userinfo(token)
            token = self.keycloak_openid.token("admin@cimms.com", "12345")
            userinfo = self.keycloak_openid.userinfo(token["access_token"])
            print(userinfo)

            return userinfo
        except Exception as e:
            traceback.print_exc()
            print(e)
            return False

    def validate_token_user(self, token):
        if not self.get_user_info(token):
            return False
        return True
