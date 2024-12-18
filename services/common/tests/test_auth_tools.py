from mock import MagicMock, Mock, patch
import pytest
from common import auth_tools
from common.auth_tools import (
    ENVIRON_USER_KEY,
    ORG_ROLE_LITERAL,
    RESOURCE_TYPE,
    PermissionResult,
    UserInfo,
    require_permission,
)
from api.tests.data import auth_data
from common.tests.data import auth_tools_data
from werkzeug.exceptions import Forbidden


######################### User Info #########################
def test_user_info():
    user = UserInfo(auth_data.jwt_token_data_good)
    assert user.email == "test@gmail.com"
    assert user.first_name == "Test"
    assert user.last_name == "User"
    assert user.name == "Test User"
    assert user.super_user == True
    assert user.organizations == {
        "Test Org": "admin",
        "Test Org 2": "operator",
        "Test Org 3": "user",
    }

    assert user.to_dict() == {
        "email": "test@gmail.com",
        "organizations": [
            {"name": "Test Org", "role": "admin"},
            {"name": "Test Org 2", "role": "operator"},
            {"name": "Test Org 3", "role": "user"},
        ],
        "super_user": True,
        "first_name": "Test",
        "last_name": "User",
        "name": "Test User",
    }


######################### RSUs #########################
@patch("common.pgquery.query_db")
def test_get_rsu_dict_for_org(mock_query_db):
    mock_query_db.return_value = auth_tools_data.rsu_query_return
    valid_rsus = auth_tools.get_rsu_dict_for_org(auth_tools_data.query_organizations)
    assert "1.1.1.1" in valid_rsus
    assert len(valid_rsus) == 3

    assert mock_query_db.call_count == 1
    assert mock_query_db.call_args[0][0] == auth_tools_data.rsu_query_statement


@patch("common.pgquery.query_db")
def test_get_rsu_dict_for_org_no_orgs(mock_query_db):
    mock_query_db.return_value = []
    valid_rsus = auth_tools.get_rsu_dict_for_org([])
    assert len(valid_rsus) == 0

    assert mock_query_db.call_count == 0


@patch("common.pgquery.query_db")
def test_check_rsu_with_org(mock_query_db):
    mock_query_db.return_value = auth_tools_data.rsu_query_return

    # Valid RSUs
    assert auth_tools.check_rsu_with_org("1.1.1.1", ["a"])

    # Invalid RSUs
    assert not auth_tools.check_rsu_with_org("1.1.1.1a", ["a"])
    assert not auth_tools.check_rsu_with_org("1.1.1.4", ["a"])
    assert not auth_tools.check_rsu_with_org("1.1.1.", ["a"])
    assert not auth_tools.check_rsu_with_org("1", ["a"])
    assert not auth_tools.check_rsu_with_org(None, ["a"])


######################### Intersections #########################
@patch("common.pgquery.query_db")
def test_check_intersection_with_org(mock_query_db):
    mock_query_db.return_value = auth_tools_data.intersection_query_return

    # Valid intersections
    assert auth_tools.check_intersection_with_org("1", ["a"])

    # Invalid intersections
    assert not auth_tools.check_intersection_with_org("a", ["a"])
    assert not auth_tools.check_intersection_with_org("4", ["a"])
    assert not auth_tools.check_intersection_with_org(None, ["a"])


######################### Users #########################
@patch("common.pgquery.query_db")
def test_check_user_with_org(mock_query_db):
    mock_query_db.return_value = auth_tools_data.user_query_return

    # Valid users
    assert auth_tools.check_user_with_org("test1@gmail.com", ["a"])

    # Invalid users
    assert not auth_tools.check_user_with_org("invalid@gmail.com", ["a"])
    assert not auth_tools.check_user_with_org("", ["a"])
    assert not auth_tools.check_user_with_org(None, ["a"])


######################### Role Checks #########################
def test_check_role_above():
    assert auth_tools.check_role_above(ORG_ROLE_LITERAL.ADMIN, ORG_ROLE_LITERAL.USER)
    assert auth_tools.check_role_above(ORG_ROLE_LITERAL.OPERATOR, ORG_ROLE_LITERAL.USER)
    assert not auth_tools.check_role_above(
        ORG_ROLE_LITERAL.USER, ORG_ROLE_LITERAL.ADMIN
    )
    assert not auth_tools.check_role_above(
        ORG_ROLE_LITERAL.USER, ORG_ROLE_LITERAL.OPERATOR
    )


######################### Qualified Org List #########################
@patch("common.pgquery.query_and_return_list")
def test_get_qualified_org_list(mock_query_and_return_list):
    mock_query_and_return_list.return_value = [
        "Test Org",
        "Test Org 2",
        "Test Org 3",
        "Test Org 4",
    ]
    user = auth_data.get_request_environ()

    # super_user
    user.user_info.super_user = True
    assert auth_tools.get_qualified_org_list(
        user, ORG_ROLE_LITERAL.ADMIN, include_super_user=True
    ) == [
        "Test Org",
        "Test Org 2",
        "Test Org 3",
        "Test Org 4",
    ]
    assert auth_tools.get_qualified_org_list(
        user, ORG_ROLE_LITERAL.ADMIN, include_super_user=False
    ) == ["Test Org"]
    assert auth_tools.get_qualified_org_list(
        user, ORG_ROLE_LITERAL.OPERATOR, include_super_user=False
    ) == [
        "Test Org",
        "Test Org 2",
    ]
    assert auth_tools.get_qualified_org_list(
        user, ORG_ROLE_LITERAL.USER, include_super_user=False
    ) == [
        "Test Org",
        "Test Org 2",
        "Test Org 3",
    ]

    # no super_user
    user.user_info.super_user = False
    assert auth_tools.get_qualified_org_list(user, ORG_ROLE_LITERAL.ADMIN) == [
        "Test Org",
    ]
    assert auth_tools.get_qualified_org_list(user, ORG_ROLE_LITERAL.OPERATOR) == [
        "Test Org",
        "Test Org 2",
    ]
    assert auth_tools.get_qualified_org_list(user, ORG_ROLE_LITERAL.USER) == [
        "Test Org",
        "Test Org 2",
        "Test Org 3",
    ]


def test_require_permission():
    @require_permission(required_role=ORG_ROLE_LITERAL.OPERATOR)
    def test_function():
        return None

    user_valid = auth_data.get_request_environ()
    req = MagicMock()
    req.environ = {ENVIRON_USER_KEY: user_valid}

    # Mock the environment
    with patch("common.auth_tools.request", req):
        test_function()  # Should pass without error


def test_require_permission_with_result():
    @require_permission(required_role=ORG_ROLE_LITERAL.OPERATOR)
    def test_function(permission_result: PermissionResult):
        return permission_result

    user_valid = auth_data.get_request_environ()
    req = MagicMock()
    req.environ = {ENVIRON_USER_KEY: user_valid}

    # Mock the environment
    with patch("common.auth_tools.request", req):
        result = test_function()
        assert result.allowed == True


@patch("common.auth_tools.get_qualified_org_list")
@patch("common.auth_tools.check_role_above")
@patch("common.auth_tools.check_user_with_org")
@patch("common.auth_tools.check_rsu_with_org")
@patch("common.auth_tools.check_intersection_with_org")
def test_require_permission_calls_super_user(
    mock_check_intersection_with_org,
    mock_check_rsu_with_org,
    mock_check_user_with_org,
    mock_check_role_above,
    mock_get_qualified_org_list,
):
    additional_check = Mock()

    @require_permission(
        required_role=ORG_ROLE_LITERAL.OPERATOR,
        resource_type=RESOURCE_TYPE.USER,
        additional_check=additional_check,
    )
    def test_function(email: str, permission_result: PermissionResult):
        return permission_result

    user_valid = auth_data.get_request_environ()
    user_valid.user_info.email = "test@example.com"
    user_valid.user_info.super_user = True

    req = MagicMock()
    req.environ = {ENVIRON_USER_KEY: user_valid}

    # Mock the environment
    with patch("common.auth_tools.request", req):
        test_function("different@example.com")
        mock_get_qualified_org_list.assert_called_once()
        mock_check_user_with_org.assert_not_called()
        mock_check_rsu_with_org.assert_not_called()
        mock_check_intersection_with_org.assert_not_called()
        mock_check_role_above.assert_not_called()
        additional_check.assert_not_called()


@patch("common.auth_tools.get_qualified_org_list", return_value=["Test Org"])
@patch("common.auth_tools.check_role_above")
@patch("common.auth_tools.check_user_with_org")
@patch("common.auth_tools.check_rsu_with_org")
@patch("common.auth_tools.check_intersection_with_org")
def test_require_permission_calls_user_self(
    mock_check_intersection_with_org,
    mock_check_rsu_with_org,
    mock_check_user_with_org,
    mock_check_role_above,
    mock_get_qualified_org_list,
):
    additional_check = Mock()

    @require_permission(
        required_role=ORG_ROLE_LITERAL.OPERATOR,
        resource_type=RESOURCE_TYPE.USER,
        additional_check=additional_check,
    )
    def test_function(email: str, permission_result: PermissionResult):
        return permission_result

    user_valid = auth_data.get_request_environ()
    user_valid.user_info.email = "test@example.com"
    user_valid.user_info.super_user = False

    req = MagicMock()
    req.environ = {ENVIRON_USER_KEY: user_valid}

    # Mock the environment
    with patch("common.auth_tools.request", req):
        test_function("test@example.com")
        mock_get_qualified_org_list.assert_called_once()
        mock_get_qualified_org_list.assert_called_with(
            user_valid, ORG_ROLE_LITERAL.OPERATOR, include_super_user=False
        )
        mock_check_user_with_org.assert_not_called()
        mock_check_rsu_with_org.assert_not_called()
        mock_check_intersection_with_org.assert_not_called()
        mock_check_role_above.assert_not_called()
        additional_check.assert_called_once()
        additional_check.assert_called_with(
            "test@example.com",
            user=user_valid,
            required_role=ORG_ROLE_LITERAL.OPERATOR,
            resource_type=RESOURCE_TYPE.USER,
            resource_id="test@example.com",
        )


@patch("common.auth_tools.get_qualified_org_list", return_value=["Test Org"])
@patch("common.auth_tools.check_role_above")
@patch("common.auth_tools.check_user_with_org", return_value=True)
@patch("common.auth_tools.check_rsu_with_org")
@patch("common.auth_tools.check_intersection_with_org")
def test_require_permission_calls_user_other(
    mock_check_intersection_with_org,
    mock_check_rsu_with_org,
    mock_check_user_with_org,
    mock_check_role_above,
    mock_get_qualified_org_list,
):
    additional_check = Mock()

    @require_permission(
        required_role=ORG_ROLE_LITERAL.OPERATOR,
        resource_type=RESOURCE_TYPE.USER,
        additional_check=additional_check,
    )
    def test_function(email: str, permission_result: PermissionResult):
        return permission_result

    user_valid = auth_data.get_request_environ()
    user_valid.user_info.email = "test@example.com"
    user_valid.user_info.super_user = False
    user_valid.organization = "Test Org"
    user_valid.role = ORG_ROLE_LITERAL.OPERATOR

    req = MagicMock()
    req.environ = {ENVIRON_USER_KEY: user_valid}

    # Mock the environment
    with patch("common.auth_tools.request", req):
        test_function("different@example.com")
        mock_get_qualified_org_list.assert_called_once()
        mock_get_qualified_org_list.assert_called_with(
            user_valid, ORG_ROLE_LITERAL.OPERATOR, include_super_user=False
        )
        mock_check_user_with_org.assert_called_once()
        mock_check_user_with_org.assert_called_with(
            "different@example.com", ["Test Org"]
        )
        mock_check_rsu_with_org.assert_not_called()
        mock_check_intersection_with_org.assert_not_called()
        mock_check_role_above.assert_called_once()
        mock_check_role_above.assert_called_with(
            ORG_ROLE_LITERAL.OPERATOR, ORG_ROLE_LITERAL.OPERATOR
        )
        additional_check.assert_called_once()
        additional_check.assert_called_with(
            "different@example.com",
            user=user_valid,
            required_role=ORG_ROLE_LITERAL.OPERATOR,
            resource_type=RESOURCE_TYPE.USER,
            resource_id="different@example.com",
        )


@patch("common.auth_tools.get_qualified_org_list", return_value=["Test Org"])
@patch("common.auth_tools.check_role_above")
@patch("common.auth_tools.check_user_with_org", return_value=True)
def test_require_permission_additional_check(
    mock_check_user_with_org,
    mock_check_role_above,
    mock_get_qualified_org_list,
):
    user_valid = auth_data.get_request_environ()
    user_valid.user_info.email = "test@example.com"
    user_valid.user_info.super_user = False
    user_valid.organization = "Test Org"
    user_valid.role = ORG_ROLE_LITERAL.OPERATOR

    additional_check_result = PermissionResult(
        user=user_valid,
        allowed=False,
        qualified_orgs=["qualified org"],
        message="additional check message",
    )
    additional_check = Mock()
    additional_check.return_value = additional_check_result

    @require_permission(
        required_role=ORG_ROLE_LITERAL.ADMIN,
        resource_type=RESOURCE_TYPE.USER,
        additional_check=additional_check,
    )
    def test_function(email: str, permission_result: PermissionResult):
        return permission_result

    req = MagicMock()
    req.environ = {ENVIRON_USER_KEY: user_valid}

    # Mock the environment
    with patch("common.auth_tools.request", req):
        with pytest.raises(Forbidden) as e:
            test_function("different@example.com")
        assert str(e.value) == "403 Forbidden: additional check message"
        mock_check_user_with_org.assert_called_once()
        mock_check_user_with_org.assert_called_with(
            "different@example.com", ["Test Org"]
        )
        mock_check_role_above.assert_called_once()
        mock_check_role_above.assert_called_with(
            ORG_ROLE_LITERAL.OPERATOR, ORG_ROLE_LITERAL.ADMIN
        )
        mock_get_qualified_org_list.assert_called_once()
        mock_get_qualified_org_list.assert_called_with(
            user_valid, ORG_ROLE_LITERAL.ADMIN, include_super_user=False
        )
        additional_check.assert_called_once()
        additional_check.assert_called_with(
            "different@example.com",
            user=user_valid,
            required_role=ORG_ROLE_LITERAL.ADMIN,
            resource_type=RESOURCE_TYPE.USER,
            resource_id="different@example.com",
        )


@patch("common.auth_tools.check_user_with_org")
def test_require_permission_user_self(mock_check_user_with_org):
    @require_permission(
        required_role=ORG_ROLE_LITERAL.OPERATOR, resource_type=RESOURCE_TYPE.USER
    )
    def test_function(email: str, permission_result: dict):
        return permission_result

    user_valid = auth_data.get_request_environ()
    user_valid.user_info.email = "test@example.com"
    user_valid.user_info.super_user = False

    req = MagicMock()
    req.environ = {ENVIRON_USER_KEY: user_valid}

    # Mock the environment
    with patch("common.auth_tools.request", req):
        result: PermissionResult = test_function("test@example.com")
        assert result.allowed == True
        mock_check_user_with_org.assert_not_called()


@patch("common.auth_tools.check_user_with_org", return_value=True)
def test_require_permission_user_other(mock_check_user_with_org):
    @require_permission(
        required_role=ORG_ROLE_LITERAL.OPERATOR, resource_type=RESOURCE_TYPE.USER
    )
    def test_function(email: str):
        return None

    user_valid = auth_data.get_request_environ()
    user_valid.user_info.email = "test@example.com"
    user_valid.user_info.super_user = False

    req = MagicMock()
    req.environ = {ENVIRON_USER_KEY: user_valid}

    # Mock the environment
    with patch("common.auth_tools.request", req):
        test_function("different@example.com")
        assert mock_check_user_with_org.call_count == 1


@patch("common.auth_tools.check_user_with_org", return_value=False)
def test_require_permission_user_unauthorized(mock_check_user_with_org):
    @require_permission(
        required_role=ORG_ROLE_LITERAL.OPERATOR, resource_type=RESOURCE_TYPE.USER
    )
    def test_function(email: str):
        return None

    user_valid = auth_data.get_request_environ()
    user_valid.user_info.email = "test@example.com"
    user_valid.user_info.super_user = False
    user_valid.user_info.organizations = {}

    req = MagicMock()
    req.environ = {ENVIRON_USER_KEY: user_valid}

    # Mock the environment
    with patch("common.auth_tools.request", req):
        with pytest.raises(Forbidden):
            test_function("different@example.com")


@patch("common.auth_tools.check_rsu_with_org", return_value=True)
def test_require_permission_rsu_authorized(mock_check_rsu_with_org):
    @require_permission(
        required_role=ORG_ROLE_LITERAL.OPERATOR, resource_type=RESOURCE_TYPE.RSU
    )
    def test_function(rsu_ip: str):
        return None

    user_valid = auth_data.get_request_environ()
    user_valid.user_info.super_user = False
    req = MagicMock()
    req.environ = {ENVIRON_USER_KEY: user_valid}

    # Mock the environment
    with patch("common.auth_tools.request", req):
        test_function("1.1.1.1")


@patch("common.auth_tools.check_rsu_with_org", return_value=False)
def test_require_permission_rsu_unauthorized(mock_check_rsu_with_org):
    @require_permission(
        required_role=ORG_ROLE_LITERAL.OPERATOR, resource_type=RESOURCE_TYPE.RSU
    )
    def test_function(rsu_ip: str):
        return None

    user_valid = auth_data.get_request_environ()
    user_valid.user_info.super_user = False
    req = MagicMock()
    req.environ = {ENVIRON_USER_KEY: user_valid}

    # Mock the environment
    with patch("common.auth_tools.request", req):
        with pytest.raises(Forbidden):
            test_function("1.1.1.1")


@patch("common.auth_tools.check_intersection_with_org", return_value=True)
def test_require_permission_intersection_authorized(mock_check_intersection_with_org):
    @require_permission(
        required_role=ORG_ROLE_LITERAL.OPERATOR,
        resource_type=RESOURCE_TYPE.INTERSECTION,
    )
    def test_function(intersection_id: str):
        return None

    user_valid = auth_data.get_request_environ()
    user_valid.user_info.super_user = False
    req = MagicMock()
    req.environ = {ENVIRON_USER_KEY: user_valid}

    # Mock the environment
    with patch("common.auth_tools.request", req):
        test_function("1")


@patch("common.auth_tools.check_intersection_with_org", return_value=False)
def test_require_permission_intersection_unauthorized(mock_check_intersection_with_org):
    @require_permission(
        required_role=ORG_ROLE_LITERAL.OPERATOR,
        resource_type=RESOURCE_TYPE.INTERSECTION,
    )
    def test_function(intersection_id: str):
        return None

    user_valid = auth_data.get_request_environ()
    user_valid.user_info.super_user = False
    req = MagicMock()
    req.environ = {ENVIRON_USER_KEY: user_valid}

    # Mock the environment
    with patch("common.auth_tools.request", req):
        with pytest.raises(Forbidden):
            test_function("1")


@patch("common.auth_tools.check_role_above", return_value=True)
@patch("common.auth_tools.check_rsu_with_org", return_value=True)
def test_require_permission_org_role_above(
    mock_check_rsu_with_org, mock_check_role_above
):
    @require_permission(
        required_role=ORG_ROLE_LITERAL.OPERATOR, resource_type=RESOURCE_TYPE.RSU
    )
    def test_function(rsu_ip: str, permission_result: dict):
        return None

    user_valid = auth_data.get_request_environ()
    user_valid.user_info.super_user = False
    user_valid.role = ORG_ROLE_LITERAL.OPERATOR
    user_valid.organization = "Test Org"

    req = MagicMock()
    req.environ = {ENVIRON_USER_KEY: user_valid}

    # Mock the environment
    with patch("common.auth_tools.request", req):
        test_function("1.1.1.1")
