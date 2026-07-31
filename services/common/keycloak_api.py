import logging
import datetime
from typing import TypedDict
from keycloak import KeycloakOpenID


class KeycloakToken(TypedDict):
    access_token: str
    expires_in: int
    refresh_expires_in: int
    refresh_token: str
    token_type: str
    id_token: str
    not_before_policy: str
    session_state: str
    scope: str


class KeycloakServiceAccountApi:
    def __init__(self, endpoint, realm, client_id, client_secret):
        """
        Initialize the KeycloakServiceAccountApi with the base URL, client ID, and client secret.

        Args:
            endpoint (str): The Keycloak server URL.
            realm (str): The Keycloak realm name.
            client_id (str): The Keycloak client ID for authentication.
            client_secret (str): The Keycloak client secret for authentication.
        """
        self.endpoint = endpoint
        self.realm = realm
        self.client_id = client_id
        self.client_secret = client_secret
        self.keycloak_openid = KeycloakOpenID(
            server_url=endpoint,
            realm_name=realm,
            client_id=client_id,
            client_secret_key=client_secret,
        )

        self.token: KeycloakToken | None = None
        self.token_expiration_date: datetime.datetime | None = None
        self.refresh_token_expiration_date: datetime.datetime | None = None

    def _gen_keycloak_token(self) -> KeycloakToken | None:
        """
        Request a new Keycloak token from the authentication endpoint.

        Returns:
            KeycloakToken | None: The token dictionary, or None if generation fails.
        """
        try:
            return self.keycloak_openid.token(
                grant_type="client_credentials",
                client_id=self.client_id,
                client_secret=self.client_secret,
                scope="openid",
            )
        except Exception as e:
            logging.warning(f"Failed to generate Keycloak token: {e}")
            return None

    def _refresh_keycloak_token(self, refresh_token: str) -> KeycloakToken | None:
        """
        Refresh an existing Keycloak token using the refresh token.

        Args:
            refresh_token (str): The refresh token to use for obtaining a new access token.

        Returns:
            KeycloakToken | None: The refreshed token dictionary, or None if refresh fails.
        """
        try:
            return self.keycloak_openid.refresh_token(refresh_token)
        except Exception as e:
            logging.warning(f"Failed to refresh Keycloak token: {e}")
            return None

    def _is_current_token_valid(self) -> bool:
        """
        Check if the current Keycloak token is valid (not expired).

        Returns:
            bool: True if the token exists and is not expired, False otherwise.
        """
        return (
            self.token is not None
            and self.token_expiration_date is not None
            and datetime.datetime.now() < self.token_expiration_date
        )

    def _is_refresh_token_valid(self) -> bool:
        """
        Check if the refresh token is still valid (not expired).

        Returns:
            bool: True if the refresh token exists and is not expired, False otherwise.
        """
        return (
            self.token is not None
            and self.refresh_token_expiration_date is not None
            and datetime.datetime.now() < self.refresh_token_expiration_date
        )

    def get_kc_token(self) -> KeycloakToken | None:
        """
        Get a valid Keycloak token, refreshing or regenerating it if necessary.

        Returns:
            KeycloakToken | None: The valid token dictionary, or None if unable to obtain one.
        """
        # If current token is valid, return it
        if self._is_current_token_valid():
            return self.token

        # If access token expired but refresh token is still valid, try to refresh
        if self.token is not None and self._is_refresh_token_valid():
            logging.info("Access token expired. Attempting to refresh token.")
            refreshed_token = self._refresh_keycloak_token(self.token["refresh_token"])
            if refreshed_token:
                self._update_token(refreshed_token)
                logging.info("Successfully refreshed Keycloak token.")
                return self.token
            else:
                logging.warning("Token refresh failed. Generating new token.")

        # If no token exists, or refresh failed, generate a new token
        logging.info("Generating new Keycloak token.")
        new_token = self._gen_keycloak_token()
        if new_token:
            self._update_token(new_token)
            logging.info("Successfully generated new Keycloak token.")
        else:
            logging.error("Failed to obtain Keycloak token.")
            return None

        return self.token

    def _update_token(self, token: KeycloakToken) -> None:
        """
        Update the stored token and calculate expiration timestamps.

        Args:
            token (KeycloakToken): The new token to store.
        """
        self.token = token
        current_time = datetime.datetime.now()

        # expires_in is in seconds, convert to timedelta
        self.token_expiration_date = current_time + datetime.timedelta(
            seconds=token["expires_in"]
        )

        # refresh_expires_in is also in seconds
        self.refresh_token_expiration_date = current_time + datetime.timedelta(
            seconds=token["refresh_expires_in"]
        )
