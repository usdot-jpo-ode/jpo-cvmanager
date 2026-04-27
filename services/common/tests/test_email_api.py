import pytest
import datetime
from unittest.mock import Mock, patch
from common.email_api import EmailApi
from common.keycloak_api import KeycloakServiceAccountApi


@pytest.fixture
def mock_kc_api():
    """Fixture to create a mock KeycloakServiceAccountApi instance."""
    mock = Mock(spec=KeycloakServiceAccountApi)
    return mock


@pytest.fixture
def email_api(mock_kc_api):
    """Fixture to create an EmailApi instance for testing."""
    return EmailApi(iapi_base_url="http://localhost:8089", kc_api=mock_kc_api)


@pytest.fixture
def mock_token():
    """Fixture for a mock Keycloak token."""
    return {
        "access_token": "mock_access_token",
        "expires_in": 300,  # 5 minutes in seconds
        "refresh_expires_in": 1800,  # 30 minutes in seconds
        "refresh_token": "mock_refresh_token",
        "token_type": "Bearer",
        "id_token": "mock_id_token",
        "not_before_policy": "0",
        "session_state": "mock_session",
        "scope": "openid profile email",
    }


class TestEmailApiInitialization:
    """Tests for EmailApi initialization."""

    def test_init_with_keycloak_api(self, mock_kc_api):
        """Test EmailApi initialization with KeycloakServiceAccountApi."""
        email_api = EmailApi(iapi_base_url="http://localhost:8089", kc_api=mock_kc_api)

        assert email_api.iapi_endpoint == "http://localhost:8089"
        assert email_api.kc_api == mock_kc_api

    def test_init_with_different_base_url(self, mock_kc_api):
        """Test EmailApi initialization with different base URL."""
        email_api = EmailApi(
            iapi_base_url="https://api.example.com", kc_api=mock_kc_api
        )

        assert email_api.iapi_endpoint == "https://api.example.com"


class TestSendMessageCounts:
    """Tests for send_message_counts method."""

    @patch("common.email_api.requests.post")
    def test_send_message_counts_success(
        self, mock_post, email_api, mock_kc_api, mock_token
    ):
        """Test successful message counts email send."""
        mock_kc_api.get_kc_token.return_value = mock_token

        mock_response = Mock()
        mock_response.status_code = 200
        mock_response.json.return_value = {"status": "sent"}
        mock_post.return_value = mock_response

        start_date = datetime.datetime(2025, 1, 1)
        end_date = datetime.datetime(2025, 1, 2)

        status_code, response = email_api.send_message_counts(
            org_name="Test Org",
            deployment_title="Test Deployment",
            start_date=start_date,
            end_date=end_date,
            message_type_list=["BSM", "TIM"],
            rsu_counts=[{"rsu": "192.168.1.1", "count": 100}],
        )

        assert status_code == 200
        assert response["status"] == "sent"
        mock_kc_api.get_kc_token.assert_called_once()
        mock_post.assert_called_once()

        call_args = mock_post.call_args
        assert call_args[0][0] == "http://localhost:8089/emails/message-counts"
        assert call_args[1]["headers"]["Authorization"] == "Bearer mock_access_token"
        assert call_args[1]["json"]["org_name"] == "Test Org"
        assert call_args[1]["json"]["deployment_title"] == "Test Deployment"
        assert call_args[1]["json"]["message_type_list"] == ["BSM", "TIM"]

    @patch("common.email_api.requests.post")
    def test_send_message_counts_no_token(self, mock_post, email_api, mock_kc_api):
        """Test message counts email when token cannot be obtained."""
        mock_kc_api.get_kc_token.return_value = None

        status_code, response = email_api.send_message_counts(
            org_name="Test Org",
            deployment_title="Test Deployment",
            start_date=datetime.datetime.now(),
            end_date=datetime.datetime.now(),
            message_type_list=["BSM"],
            rsu_counts=[],
        )

        assert status_code == 500
        assert "error" in response
        assert response["error"] == "Unable to obtain Keycloak token."
        mock_post.assert_not_called()

    @patch("common.email_api.requests.post")
    def test_send_message_counts_api_failure(
        self, mock_post, email_api, mock_kc_api, mock_token
    ):
        """Test message counts email when API returns error."""
        mock_kc_api.get_kc_token.return_value = mock_token

        mock_response = Mock()
        mock_response.status_code = 500
        mock_response.text = "Internal Server Error"
        mock_response.json.return_value = {"error": "failed to send"}
        mock_post.return_value = mock_response

        status_code, response = email_api.send_message_counts(
            org_name="Test Org",
            deployment_title="Test Deployment",
            start_date=datetime.datetime.now(),
            end_date=datetime.datetime.now(),
            message_type_list=["BSM"],
            rsu_counts=[],
        )

        assert status_code == 500
        assert "error" in response

    @patch("common.email_api.requests.post")
    def test_send_message_counts_with_empty_counts(
        self, mock_post, email_api, mock_kc_api, mock_token
    ):
        """Test sending message counts with empty counts list."""
        mock_kc_api.get_kc_token.return_value = mock_token

        mock_response = Mock()
        mock_response.status_code = 200
        mock_response.json.return_value = {"status": "sent"}
        mock_post.return_value = mock_response

        status_code, response = email_api.send_message_counts(
            org_name="Test Org",
            deployment_title="Test Deployment",
            start_date=datetime.datetime.now(),
            end_date=datetime.datetime.now(),
            message_type_list=["BSM"],
            rsu_counts=[],
        )

        assert status_code == 200
        call_args = mock_post.call_args
        assert call_args[1]["json"]["rsu_counts"] == []


class TestSendFirmwareUpgradeFailure:
    """Tests for send_firmware_upgrade_failure method."""

    @patch("common.email_api.requests.post")
    def test_send_firmware_upgrade_failure_success(
        self, mock_post, email_api, mock_kc_api, mock_token
    ):
        """Test successful firmware upgrade failure email send."""
        mock_kc_api.get_kc_token.return_value = mock_token

        mock_response = Mock()
        mock_response.status_code = 201
        mock_response.json.return_value = {"message": "email sent"}
        mock_post.return_value = mock_response

        status_code, response = email_api.send_firmware_upgrade_failure(
            rsu_ip="192.168.1.100",
            error_message="SNMP timeout",
            failure_type="ConnectionError",
            stack_trace="Traceback...",
        )

        assert status_code == 201
        assert response["message"] == "email sent"
        mock_kc_api.get_kc_token.assert_called_once()

        call_args = mock_post.call_args
        assert (
            call_args[0][0] == "http://localhost:8089/emails/firmware-upgrade-failures"
        )
        assert call_args[1]["headers"]["Authorization"] == "Bearer mock_access_token"
        assert call_args[1]["json"]["rsu_ip"] == "192.168.1.100"
        assert call_args[1]["json"]["error_message"] == "SNMP timeout"

    @patch("common.email_api.requests.post")
    def test_send_firmware_upgrade_failure_no_token(
        self, mock_post, email_api, mock_kc_api
    ):
        """Test firmware upgrade failure email when token cannot be obtained."""
        mock_kc_api.get_kc_token.return_value = None

        status_code, response = email_api.send_firmware_upgrade_failure(
            rsu_ip="192.168.1.100",
            error_message="Error",
            failure_type="Error",
            stack_trace="",
        )

        assert status_code == 500
        assert "error" in response
        assert response["error"] == "Unable to obtain Keycloak token."
        mock_post.assert_not_called()

    @patch("common.email_api.requests.post")
    def test_send_firmware_upgrade_failure_api_error(
        self, mock_post, email_api, mock_kc_api, mock_token
    ):
        """Test firmware upgrade failure email with API error."""
        mock_kc_api.get_kc_token.return_value = mock_token

        mock_response = Mock()
        mock_response.status_code = 400
        mock_response.text = "Invalid RSU IP"
        mock_response.json.return_value = {"error": "validation_error"}
        mock_post.return_value = mock_response

        status_code, response = email_api.send_firmware_upgrade_failure(
            rsu_ip="invalid_ip",
            error_message="Error",
            failure_type="Error",
            stack_trace="",
        )

        assert status_code == 400

    @patch("common.email_api.requests.post")
    def test_send_firmware_upgrade_failure_with_long_stack_trace(
        self, mock_post, email_api, mock_kc_api, mock_token
    ):
        """Test firmware upgrade failure email with long stack trace."""
        mock_kc_api.get_kc_token.return_value = mock_token

        mock_response = Mock()
        mock_response.status_code = 200
        mock_response.json.return_value = {"status": "sent"}
        mock_post.return_value = mock_response

        long_stack_trace = "Traceback (most recent call last):\n" * 100

        status_code, response = email_api.send_firmware_upgrade_failure(
            rsu_ip="192.168.1.100",
            error_message="Connection timeout",
            failure_type="TimeoutError",
            stack_trace=long_stack_trace,
        )

        assert status_code == 200
        call_args = mock_post.call_args
        assert len(call_args[1]["json"]["stack_trace"]) > 1000


class TestSendApiErrorEmail:
    """Tests for send_api_error_email method."""

    @patch("common.email_api.requests.post")
    def test_send_api_error_email_success(
        self, mock_post, email_api, mock_kc_api, mock_token
    ):
        """Test successful API error email send."""
        mock_kc_api.get_kc_token.return_value = mock_token

        mock_response = Mock()
        mock_response.status_code = 200
        mock_response.json.return_value = {"status": "sent"}
        mock_post.return_value = mock_response

        status_code, response = email_api.send_api_error_email(
            error_message="Database connection failed",
            stack_trace="Traceback (most recent call last)...",
            timestamp="2025-01-05T12:00:00Z",
            logs_link="https://logs.example.com",
        )

        assert status_code == 200
        assert response["status"] == "sent"
        mock_kc_api.get_kc_token.assert_called_once()

        call_args = mock_post.call_args
        assert call_args[0][0] == "http://localhost:8089/emails/api-errors"
        assert call_args[1]["headers"]["Authorization"] == "Bearer mock_access_token"

    @patch("common.email_api.requests.post")
    def test_send_api_error_email_no_token(self, mock_post, email_api, mock_kc_api):
        """Test API error email when token cannot be obtained."""
        mock_kc_api.get_kc_token.return_value = None

        status_code, response = email_api.send_api_error_email(
            error_message="Error",
            stack_trace="Trace",
            timestamp="2025-01-05T12:00:00Z",
            logs_link="https://logs.example.com",
        )

        assert status_code == 500
        assert "error" in response
        assert response["error"] == "Unable to obtain Keycloak token."
        mock_post.assert_not_called()

    @patch("common.email_api.requests.post")
    def test_send_api_error_email_with_all_fields(
        self, mock_post, email_api, mock_kc_api, mock_token
    ):
        """Test API error email with all fields populated."""
        mock_kc_api.get_kc_token.return_value = mock_token

        mock_response = Mock()
        mock_response.status_code = 200
        mock_response.json.return_value = {"message_id": "12345"}
        mock_post.return_value = mock_response

        status_code, response = email_api.send_api_error_email(
            error_message="ValueError: Invalid latitude",
            stack_trace="Traceback...\nValueError: Invalid latitude",
            timestamp="2025-01-05T14:32:18.456Z",
            logs_link="https://cvmanager.example.com/logs?level=error",
        )

        assert status_code == 200
        call_args = mock_post.call_args
        json_data = call_args[1]["json"]
        assert json_data["error_message"] == "ValueError: Invalid latitude"
        assert json_data["stack_trace"] == "Traceback...\nValueError: Invalid latitude"
        assert json_data["timestamp"] == "2025-01-05T14:32:18.456Z"
        assert (
            json_data["logs_link"] == "https://cvmanager.example.com/logs?level=error"
        )

    @patch("common.email_api.requests.post")
    def test_send_api_error_email_api_failure(
        self, mock_post, email_api, mock_kc_api, mock_token
    ):
        """Test API error email when API returns error."""
        mock_kc_api.get_kc_token.return_value = mock_token

        mock_response = Mock()
        mock_response.status_code = 503
        mock_response.text = "Service Unavailable"
        mock_response.json.return_value = {"error": "service_unavailable"}
        mock_post.return_value = mock_response

        status_code, response = email_api.send_api_error_email(
            error_message="Error",
            stack_trace="Trace",
            timestamp="2025-01-05T12:00:00Z",
            logs_link="https://logs.example.com",
        )

        assert status_code == 503


class TestEmailApiIntegration:
    """Integration tests for EmailApi with KeycloakServiceAccountApi."""

    @patch("common.email_api.requests.post")
    def test_token_refresh_between_calls(self, mock_post, mock_kc_api, mock_token):
        """Test that token is refreshed between email calls."""
        email_api = EmailApi(iapi_base_url="http://localhost:8089", kc_api=mock_kc_api)

        # First call uses initial token
        mock_kc_api.get_kc_token.return_value = mock_token
        mock_response = Mock()
        mock_response.status_code = 200
        mock_response.json.return_value = {"status": "sent"}
        mock_post.return_value = mock_response

        email_api.send_api_error_email(
            error_message="Error 1",
            stack_trace="Trace 1",
            timestamp="2025-01-05T12:00:00Z",
            logs_link="https://logs.example.com",
        )

        # Second call uses refreshed token
        refreshed_token = {**mock_token, "access_token": "refreshed_access_token"}
        mock_kc_api.get_kc_token.return_value = refreshed_token

        email_api.send_api_error_email(
            error_message="Error 2",
            stack_trace="Trace 2",
            timestamp="2025-01-05T12:05:00Z",
            logs_link="https://logs.example.com",
        )

        assert mock_kc_api.get_kc_token.call_count == 2

        # Verify second call used refreshed token
        second_call_args = mock_post.call_args_list[1]
        assert (
            second_call_args[1]["headers"]["Authorization"]
            == "Bearer refreshed_access_token"
        )

    @patch("common.email_api.requests.post")
    def test_multiple_email_types_same_token(self, mock_post, mock_kc_api, mock_token):
        """Test that multiple email types can use the same token."""
        email_api = EmailApi(iapi_base_url="http://localhost:8089", kc_api=mock_kc_api)

        mock_kc_api.get_kc_token.return_value = mock_token
        mock_response = Mock()
        mock_response.status_code = 200
        mock_response.json.return_value = {"status": "sent"}
        mock_post.return_value = mock_response

        # Send different types of emails
        email_api.send_api_error_email(
            error_message="Error",
            stack_trace="Trace",
            timestamp="2025-01-05T12:00:00Z",
            logs_link="https://logs.example.com",
        )

        email_api.send_firmware_upgrade_failure(
            rsu_ip="192.168.1.100",
            error_message="Firmware error",
            failure_type="UpgradeError",
            stack_trace="Trace",
        )

        email_api.send_message_counts(
            org_name="Test Org",
            deployment_title="Test Deployment",
            start_date=datetime.datetime.now(),
            end_date=datetime.datetime.now(),
            message_type_list=["BSM"],
            rsu_counts=[],
        )

        # Each email send requests a token; all three email types should send successfully.
        assert mock_kc_api.get_kc_token.call_count == 3
        assert mock_post.call_count == 3
