from unittest.mock import patch, MagicMock, ANY
import pytest
from flask import Flask, request
from rsu_snmp_fwd_fetch import RsuSnmpFwdFetch
from common.auth_tools import PermissionResult, EnvironWithOrg, ORG_ROLE_LITERAL, ENVIRON_USER_KEY, UserInfo

from werkzeug.exceptions import InternalServerError, BadRequest, NotFound

@pytest.fixture
def app():
    app = Flask(__name__)
    return app

@pytest.fixture
def permission_result():
    mock_user_info = MagicMock(spec=UserInfo)
    mock_user_info.super_user = False
    mock_user_info.organizations = {"TestOrg": "admin"}

    user = EnvironWithOrg(mock_user_info, "TestOrg", ORG_ROLE_LITERAL.USER)

    return PermissionResult(allowed=True, qualified_orgs=["TestOrg"], message=None, user=user)

# #################################### Testing Requests ###########################################

def test_options_request():
    resource = RsuSnmpFwdFetch()
    (body, code, headers) = resource.options()
    assert body == ""
    assert code == 204
    assert headers["Access-Control-Allow-Methods"] == "GET"

@patch("rsu_snmp_fwd_fetch.fetch_rsu_info")
@patch("rsu_snmp_fwd_fetch.UpdatePostgresRsuMessageForward")
@patch("rsu_snmp_fwd_fetch.rsu_message_forward_helpers")
def test_get_request_success(mock_helpers, mock_update_pg, mock_fetch_info, app, permission_result):
    with app.test_request_context(query_string={"rsu_ip": "10.0.0.1"}):
        request.environ[ENVIRON_USER_KEY] = permission_result.user
        rsu_info = {
            "rsu_id": 1,
            "snmp_username": "user",
            "snmp_password": "pw",
            "snmp_encrypt_pw": "enc",
            "snmp_version": "v3"
        }
        mock_fetch_info.return_value = rsu_info
        
        mock_updater = MagicMock()
        mock_update_pg.return_value = mock_updater
        mock_updater.get_snmp_configs.return_value = {1: "some_configs"}
        
        mock_helpers.format_snmp_msgfwd_configs.return_value = {"formatted": "data"}
        
        resource = RsuSnmpFwdFetch()
        (data, code, headers) = resource.get()
        
        assert code == 200
        assert data == {"formatted": "data"}
        mock_fetch_info.assert_called_once_with("10.0.0.1", ANY)
        mock_updater.get_snmp_configs.assert_called_once()
        args, _ = mock_updater.get_snmp_configs.call_args
        assert args[0][0]["ipv4_address"] == "10.0.0.1"

def test_get_request_invalid_schema(app, permission_result):
    with app.test_request_context(query_string={"rsu_ip": "invalid-ip"}):
        request.environ[ENVIRON_USER_KEY] = permission_result.user
        resource = RsuSnmpFwdFetch()
        with pytest.raises(Exception) as excinfo:
            resource.get()
        assert "400" in str(excinfo.value)

@patch("rsu_snmp_fwd_fetch.fetch_rsu_info")
def test_get_request_rsu_not_found(mock_fetch_info, app, permission_result):
    with app.test_request_context(query_string={"rsu_ip": "10.0.0.1"}):
        request.environ[ENVIRON_USER_KEY] = permission_result.user
        mock_fetch_info.return_value = None
        
        resource = RsuSnmpFwdFetch()
        with pytest.raises(NotFound) as excinfo:
            resource.get()
        assert "404" in str(excinfo.value)
        assert "not found in organization" in str(excinfo.value)

@patch("rsu_snmp_fwd_fetch.fetch_rsu_info")
def test_get_request_missing_required_fields(mock_fetch_info, app, permission_result):
    with app.test_request_context(query_string={"rsu_ip": "10.0.0.1"}):
        request.environ[ENVIRON_USER_KEY] = permission_result.user
        # Missing snmp_version
        rsu_info = {
            "rsu_id": 1,
            "snmp_username": "user",
            "snmp_password": "pw",
            "snmp_encrypt_pw": "enc"
        }
        mock_fetch_info.return_value = rsu_info
        
        resource = RsuSnmpFwdFetch()
        with pytest.raises(InternalServerError) as excinfo:
            resource.get()
        assert excinfo.value.code == 500
        assert "RSU info missing required fields" in str(excinfo.value)

@patch("rsu_snmp_fwd_fetch.fetch_rsu_info")
@patch("rsu_snmp_fwd_fetch.UpdatePostgresRsuMessageForward")
@patch("rsu_snmp_fwd_fetch.rsu_message_forward_helpers")
def test_get_request_missing_snmp_encrypt_pw_field(mock_helpers, mock_update_pg, mock_fetch_info, app, permission_result):
    with app.test_request_context(query_string={"rsu_ip": "10.0.0.1"}):
        request.environ[ENVIRON_USER_KEY] = permission_result.user
        rsu_info = {
            "rsu_id": 1,
            "snmp_username": "user",
            "snmp_password": "pw",
            "snmp_version": "v3"
        }
        mock_fetch_info.return_value = rsu_info

        mock_updater = MagicMock()
        mock_update_pg.return_value = mock_updater
        mock_updater.get_snmp_configs.return_value = {1: "some_configs"}

        mock_helpers.format_snmp_msgfwd_configs.return_value = {"formatted": "data"}

        resource = RsuSnmpFwdFetch()
        (data, code, headers) = resource.get()

        assert code == 200
        assert data == {"formatted": "data"}
        mock_fetch_info.assert_called_once_with("10.0.0.1", ANY)
        mock_updater.get_snmp_configs.assert_called_once()
        args, _ = mock_updater.get_snmp_configs.call_args
        assert args[0][0]["ipv4_address"] == "10.0.0.1"

@patch("rsu_snmp_fwd_fetch.fetch_rsu_info")
@patch("rsu_snmp_fwd_fetch.UpdatePostgresRsuMessageForward")
def test_get_request_unable_to_retrieve(mock_update_pg, mock_fetch_info, app, permission_result):
    with app.test_request_context(query_string={"rsu_ip": "10.0.0.1"}):
        request.environ[ENVIRON_USER_KEY] = permission_result.user
        rsu_info = {
            "rsu_id": 1,
            "snmp_username": "user",
            "snmp_password": "pw",
            "snmp_encrypt_pw": "enc",
            "snmp_version": "v3"
        }
        mock_fetch_info.return_value = rsu_info
        
        mock_updater = MagicMock()
        mock_update_pg.return_value = mock_updater
        mock_updater.get_snmp_configs.return_value = {1: "Unable to retrieve latest SNMP config"}
        
        resource = RsuSnmpFwdFetch()
        with pytest.raises(InternalServerError) as excinfo:
            resource.get()
        assert excinfo.value.code == 500
        assert "Error fetching SNMP configs" in str(excinfo.value)

@patch("rsu_snmp_fwd_fetch.fetch_rsu_info")
@patch("rsu_snmp_fwd_fetch.UpdatePostgresRsuMessageForward")
def test_get_request_unsupported_version(mock_update_pg, mock_fetch_info, app, permission_result):
    with app.test_request_context(query_string={"rsu_ip": "10.0.0.1"}):
        request.environ[ENVIRON_USER_KEY] = permission_result.user
        rsu_info = {
            "rsu_id": 1,
            "snmp_username": "user",
            "snmp_password": "pw",
            "snmp_encrypt_pw": "enc",
            "snmp_version": "v3"
        }
        mock_fetch_info.return_value = rsu_info
        
        mock_updater = MagicMock()
        mock_update_pg.return_value = mock_updater
        mock_updater.get_snmp_configs.return_value = {1: "Unsupported SNMP version"}
        
        resource = RsuSnmpFwdFetch()
        with pytest.raises(InternalServerError) as excinfo:
            resource.get()
        assert excinfo.value.code == 500
        assert "Error fetching SNMP configs" in str(excinfo.value)

@patch("rsu_snmp_fwd_fetch.fetch_rsu_info")
@patch("rsu_snmp_fwd_fetch.UpdatePostgresRsuMessageForward")
def test_get_request_exception(mock_update_pg, mock_fetch_info, app, permission_result):
    with app.test_request_context(query_string={"rsu_ip": "10.0.0.1"}):
        request.environ[ENVIRON_USER_KEY] = permission_result.user
        rsu_info = {
            "rsu_id": 1,
            "snmp_username": "user",
            "snmp_password": "pw",
            "snmp_encrypt_pw": "enc",
            "snmp_version": "v3"
        }
        mock_fetch_info.return_value = rsu_info
        
        mock_updater = MagicMock()
        mock_update_pg.return_value = mock_updater
        mock_updater.get_snmp_configs.side_effect = Exception("Test Exception")
        
        resource = RsuSnmpFwdFetch()
        with pytest.raises(InternalServerError) as excinfo:
            resource.get()
        assert excinfo.value.code == 500
        assert "Error fetching SNMP configs" in str(excinfo.value)
