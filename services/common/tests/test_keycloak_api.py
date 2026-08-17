import pytest
import datetime
from unittest.mock import MagicMock, patch
from common.keycloak_api import KeycloakServiceAccountApi


@pytest.fixture
def mock_keycloak_openid():
    """
    Fixture to mock the KeycloakOpenID class from python-keycloak library.
    
    This fixture patches the KeycloakOpenID class to prevent actual network calls
    to Keycloak during testing. The mock is yielded and automatically cleaned up
    after each test function completes.
    
    Yields:
        MagicMock: A mock object replacing the KeycloakOpenID class.
    """
    with patch("common.keycloak_api.KeycloakOpenID") as mock:
        yield mock


@pytest.fixture
def keycloak_api(mock_keycloak_openid):
    """
    Fixture to create a KeycloakServiceAccountApi instance with mocked dependencies.
    
    This fixture creates a fresh instance of KeycloakServiceAccountApi for each test,
    using the mocked KeycloakOpenID class to prevent real authentication attempts.
    All configuration values are test doubles.
    
    Args:
        mock_keycloak_openid: The mocked KeycloakOpenID class from the fixture above.
    
    Returns:
        KeycloakServiceAccountApi: A configured instance ready for testing with:
            - endpoint: "https://keycloak.example.com"
            - realm: "test-realm"
            - client_id: "test-client"
            - client_secret: "test-secret"
    """
    return KeycloakServiceAccountApi(
        endpoint="https://keycloak.example.com",
        realm="test-realm",
        client_id="test-client",
        client_secret="test-secret",
    )


@pytest.fixture
def sample_token():
    """
    Fixture to provide a sample Keycloak token response dictionary.
    
    This fixture returns a realistic token response structure that mimics what
    the actual Keycloak server would return. It includes all standard OAuth2/OIDC
    fields with test values.
    
    Returns:
        dict: A dictionary containing:
            - access_token: JWT access token for API authentication
            - expires_in: Access token validity period (300 seconds = 5 minutes)
            - refresh_expires_in: Refresh token validity period (1800 seconds = 30 minutes)
            - refresh_token: Token used to obtain new access tokens
            - token_type: OAuth2 token type (Bearer)
            - id_token: OIDC identity token
            - not_before_policy: Keycloak security policy timestamp
            - session_state: Keycloak session identifier
            - scope: Requested OAuth2 scopes (openid, email, profile)
    """
    return {
        "access_token": "sample_access_token",
        "expires_in": 300,  # 5 minutes
        "refresh_expires_in": 1800,  # 30 minutes
        "refresh_token": "sample_refresh_token",
        "token_type": "Bearer",
        "id_token": "sample_id_token",
        "not_before_policy": "0",
        "session_state": "sample_session_state",
        "scope": "openid email profile",
    }


class TestKeycloakServiceAccountApi:
    """Test suite for KeycloakServiceAccountApi class."""

    def test_init(self, keycloak_api, mock_keycloak_openid):
        """Test initialization of KeycloakServiceAccountApi."""
        assert keycloak_api.endpoint == "https://keycloak.example.com"
        assert keycloak_api.realm == "test-realm"
        assert keycloak_api.client_id == "test-client"
        assert keycloak_api.client_secret == "test-secret"
        assert keycloak_api.token is None
        assert keycloak_api.token_expiration_date is None
        assert keycloak_api.refresh_token_expiration_date is None

        # Verify KeycloakOpenID was initialized correctly
        mock_keycloak_openid.assert_called_once_with(
            server_url="https://keycloak.example.com",
            realm_name="test-realm",
            client_id="test-client",
            client_secret_key="test-secret",
        )

    def test_gen_keycloak_token(self, keycloak_api, sample_token):
        """Test generating a new Keycloak token."""
        keycloak_api.keycloak_openid.token = MagicMock(return_value=sample_token)

        result = keycloak_api._gen_keycloak_token()

        assert result == sample_token
        keycloak_api.keycloak_openid.token.assert_called_once_with(
            grant_type="client_credentials",
            client_id="test-client",
            client_secret="test-secret",
            scope="openid",
        )

    def test_refresh_keycloak_token_success(self, keycloak_api, sample_token):
        """Test successfully refreshing a Keycloak token."""
        refreshed_token = {**sample_token, "access_token": "new_access_token"}
        keycloak_api.keycloak_openid.refresh_token = MagicMock(
            return_value=refreshed_token
        )

        result = keycloak_api._refresh_keycloak_token("old_refresh_token")

        assert result == refreshed_token
        keycloak_api.keycloak_openid.refresh_token.assert_called_once_with(
            "old_refresh_token"
        )

    def test_refresh_keycloak_token_failure(self, keycloak_api):
        """Test refresh token failure handling."""
        keycloak_api.keycloak_openid.refresh_token = MagicMock(
            side_effect=Exception("Invalid refresh token")
        )

        result = keycloak_api._refresh_keycloak_token("invalid_refresh_token")

        assert result is None

    def test_is_current_token_valid_no_token(self, keycloak_api):
        """Test token validation when no token exists."""
        assert keycloak_api._is_current_token_valid() is False

    def test_is_current_token_valid_expired(self, keycloak_api, sample_token):
        """Test token validation when token is expired."""
        keycloak_api.token = sample_token
        keycloak_api.token_expiration_date = (
            datetime.datetime.now() - datetime.timedelta(minutes=1)
        )

        assert keycloak_api._is_current_token_valid() is False

    def test_is_current_token_valid_not_expired(self, keycloak_api, sample_token):
        """Test token validation when token is still valid."""
        keycloak_api.token = sample_token
        keycloak_api.token_expiration_date = (
            datetime.datetime.now() + datetime.timedelta(minutes=5)
        )

        assert keycloak_api._is_current_token_valid() is True

    def test_is_refresh_token_valid_no_token(self, keycloak_api):
        """Test refresh token validation when no token exists."""
        assert keycloak_api._is_refresh_token_valid() is False

    def test_is_refresh_token_valid_expired(self, keycloak_api, sample_token):
        """Test refresh token validation when refresh token is expired."""
        keycloak_api.token = sample_token
        keycloak_api.refresh_token_expiration_date = (
            datetime.datetime.now() - datetime.timedelta(minutes=1)
        )

        assert keycloak_api._is_refresh_token_valid() is False

    def test_is_refresh_token_valid_not_expired(self, keycloak_api, sample_token):
        """Test refresh token validation when refresh token is still valid."""
        keycloak_api.token = sample_token
        keycloak_api.refresh_token_expiration_date = (
            datetime.datetime.now() + datetime.timedelta(minutes=30)
        )

        assert keycloak_api._is_refresh_token_valid() is True

    def test_update_token(self, keycloak_api, sample_token):
        """Test _update_token method correctly sets token and expiration dates."""
        current_time = datetime.datetime.now()

        with patch("common.keycloak_api.datetime") as mock_datetime:
            mock_datetime.datetime.now.return_value = current_time
            mock_datetime.timedelta = datetime.timedelta

            keycloak_api._update_token(sample_token)

        assert keycloak_api.token == sample_token
        assert keycloak_api.token_expiration_date == current_time + datetime.timedelta(
            seconds=300
        )
        assert (
            keycloak_api.refresh_token_expiration_date
            == current_time + datetime.timedelta(seconds=1800)
        )

    def test_get_kc_token_no_existing_token(self, keycloak_api, sample_token):
        """Test get_kc_token when no token exists."""
        keycloak_api.keycloak_openid.token = MagicMock(return_value=sample_token)

        result = keycloak_api.get_kc_token()

        assert result == sample_token
        assert keycloak_api.token == sample_token

    def test_get_kc_token_valid_token_exists(self, keycloak_api, sample_token):
        """Test get_kc_token when valid token already exists."""
        keycloak_api.token = sample_token
        keycloak_api.token_expiration_date = (
            datetime.datetime.now() + datetime.timedelta(minutes=5)
        )

        result = keycloak_api.get_kc_token()

        assert result == sample_token
        # Should not call token generation
        keycloak_api.keycloak_openid._token.assert_not_called()

    def test_get_kc_token_expired_but_refresh_valid(self, keycloak_api, sample_token):
        """Test get_kc_token when access token expired but refresh token is valid."""
        refreshed_token = {**sample_token, "access_token": "new_access_token"}

        keycloak_api.token = sample_token
        keycloak_api.token_expiration_date = (
            datetime.datetime.now() - datetime.timedelta(minutes=1)
        )
        keycloak_api.refresh_token_expiration_date = (
            datetime.datetime.now() + datetime.timedelta(minutes=20)
        )
        keycloak_api.keycloak_openid.refresh_token = MagicMock(
            return_value=refreshed_token
        )

        result = keycloak_api.get_kc_token()

        assert result == refreshed_token
        assert keycloak_api.token == refreshed_token
        keycloak_api.keycloak_openid.refresh_token.assert_called_once_with(
            sample_token["refresh_token"]
        )

    def test_get_kc_token_refresh_fails_generates_new(self, keycloak_api, sample_token):
        """Test get_kc_token falls back to generating new token when refresh fails."""
        new_token = {**sample_token, "access_token": "brand_new_access_token"}

        keycloak_api.token = sample_token
        keycloak_api.token_expiration_date = (
            datetime.datetime.now() - datetime.timedelta(minutes=1)
        )
        keycloak_api.refresh_token_expiration_date = (
            datetime.datetime.now() + datetime.timedelta(minutes=20)
        )
        keycloak_api.keycloak_openid.refresh_token = MagicMock(return_value=None)
        keycloak_api.keycloak_openid.token = MagicMock(return_value=new_token)

        result = keycloak_api.get_kc_token()

        assert result == new_token
        keycloak_api.keycloak_openid.token.assert_called_once()

    def test_get_kc_token_both_tokens_expired(self, keycloak_api, sample_token):
        """Test get_kc_token when both access and refresh tokens are expired."""
        new_token = {**sample_token, "access_token": "brand_new_access_token"}

        keycloak_api.token = sample_token
        keycloak_api.token_expiration_date = (
            datetime.datetime.now() - datetime.timedelta(minutes=1)
        )
        keycloak_api.refresh_token_expiration_date = (
            datetime.datetime.now() - datetime.timedelta(minutes=1)
        )
        keycloak_api.keycloak_openid.token = MagicMock(return_value=new_token)

        result = keycloak_api.get_kc_token()

        assert result == new_token
        # Should not attempt refresh
        keycloak_api.keycloak_openid.refresh_token.assert_not_called()

    def test_get_kc_token_generation_fails(self, keycloak_api):
        """Test get_kc_token when token generation fails."""
        keycloak_api.keycloak_openid.token = MagicMock(return_value=None)

        result = keycloak_api.get_kc_token()

        assert result is None

    def test_get_kc_token_refresh_exception_then_generate(
        self, keycloak_api, sample_token
    ):
        """Test get_kc_token handles refresh exception and generates new token."""
        new_token = {**sample_token, "access_token": "brand_new_access_token"}

        keycloak_api.token = sample_token
        keycloak_api.token_expiration_date = (
            datetime.datetime.now() - datetime.timedelta(minutes=1)
        )
        keycloak_api.refresh_token_expiration_date = (
            datetime.datetime.now() + datetime.timedelta(minutes=20)
        )
        keycloak_api.keycloak_openid.refresh_token = MagicMock(
            side_effect=Exception("Network error")
        )
        keycloak_api.keycloak_openid.token = MagicMock(return_value=new_token)

        result = keycloak_api.get_kc_token()

        assert result == new_token

    def test_token_expiration_calculation_accuracy(self, keycloak_api, sample_token):
        """Test that token expiration dates are calculated accurately."""
        freeze_time = datetime.datetime(2024, 1, 15, 12, 0, 0)

        with patch("common.keycloak_api.datetime") as mock_datetime:
            mock_datetime.datetime.now.return_value = freeze_time
            mock_datetime.timedelta = datetime.timedelta

            keycloak_api._update_token(sample_token)

        expected_token_expiry = freeze_time + datetime.timedelta(seconds=300)
        expected_refresh_expiry = freeze_time + datetime.timedelta(seconds=1800)

        assert keycloak_api.token_expiration_date == expected_token_expiry
        assert keycloak_api.refresh_token_expiration_date == expected_refresh_expiry

    def test_multiple_token_refreshes(self, keycloak_api, sample_token):
        """Test multiple sequential token refreshes."""
        tokens = [{**sample_token, "access_token": f"token_{i}"} for i in range(3)]

        keycloak_api.keycloak_openid.refresh_token = MagicMock(side_effect=tokens)

        for i, expected_token in enumerate(tokens):
            keycloak_api.token = sample_token
            keycloak_api.token_expiration_date = (
                datetime.datetime.now() - datetime.timedelta(minutes=1)
            )
            keycloak_api.refresh_token_expiration_date = (
                datetime.datetime.now() + datetime.timedelta(minutes=20)
            )

            result = keycloak_api.get_kc_token()
            assert result["access_token"] == f"token_{i}"

    @pytest.mark.parametrize(
        "expires_in,refresh_expires_in",
        [
            (60, 300),  # 1 min, 5 min
            (300, 1800),  # 5 min, 30 min
            (3600, 86400),  # 1 hour, 24 hours
        ],
    )
    def test_various_expiration_times(
        self, keycloak_api, sample_token, expires_in, refresh_expires_in
    ):
        """Test token handling with various expiration times."""
        token = {
            **sample_token,
            "expires_in": expires_in,
            "refresh_expires_in": refresh_expires_in,
        }

        current_time = datetime.datetime.now()
        with patch("common.keycloak_api.datetime") as mock_datetime:
            mock_datetime.datetime.now.return_value = current_time
            mock_datetime.timedelta = datetime.timedelta

            keycloak_api._update_token(token)

        assert keycloak_api.token_expiration_date == current_time + datetime.timedelta(
            seconds=expires_in
        )
        assert (
            keycloak_api.refresh_token_expiration_date
            == current_time + datetime.timedelta(seconds=refresh_expires_in)
        )
