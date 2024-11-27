from mock import patch
from common import auth_tools
from common.auth_tools import ORG_ROLE_LITERAL, UserInfo
from api.tests.data import auth_data
from common.tests.data import auth_tools_data


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
            {"org": "Test Org", "role": "admin"},
            {"org": "Test Org 2", "role": "operator"},
            {"org": "Test Org 3", "role": "user"},
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
    assert "1.1.1.2" in valid_rsus
    assert "1.1.1.3" in valid_rsus
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
    assert auth_tools.check_rsu_with_org("1.1.1.2", ["a"])
    assert auth_tools.check_rsu_with_org("1.1.1.3", ["a"])

    # Invalid RSUs
    assert not auth_tools.check_rsu_with_org("1.1.1.1a", ["a"])
    assert not auth_tools.check_rsu_with_org("1.1.1.4", ["a"])
    assert not auth_tools.check_rsu_with_org("1.1.1.", ["a"])
    assert not auth_tools.check_rsu_with_org("1", ["a"])
    assert not auth_tools.check_rsu_with_org(None, ["a"])


######################### Intersections #########################
@patch("common.pgquery.query_db")
def test_get_intersection_dict_for_org(mock_query_db):
    mock_query_db.return_value = auth_tools_data.intersection_query_return
    valid_intersections = auth_tools.get_intersection_dict_for_org(
        auth_tools_data.query_organizations
    )
    assert "1" in valid_intersections
    assert "2" in valid_intersections
    assert "3" in valid_intersections
    assert len(valid_intersections) == 3

    assert mock_query_db.call_count == 1
    assert mock_query_db.call_args[0][0] == auth_tools_data.intersection_query_statement


@patch("common.pgquery.query_db")
def test_get_intersection_dict_for_org_no_orgs(mock_query_db):
    mock_query_db.return_value = []
    valid_intersections = auth_tools.get_intersection_dict_for_org([])
    assert len(valid_intersections) == 0

    assert mock_query_db.call_count == 0


@patch("common.pgquery.query_db")
def test_check_intersection_with_org(mock_query_db):
    mock_query_db.return_value = auth_tools_data.intersection_query_return

    # Valid intersections
    assert auth_tools.check_intersection_with_org("1", ["a"])
    assert auth_tools.check_intersection_with_org("2", ["a"])
    assert auth_tools.check_intersection_with_org("3", ["a"])

    # Invalid intersections
    assert not auth_tools.check_intersection_with_org("a", ["a"])
    assert not auth_tools.check_intersection_with_org("4", ["a"])
    assert not auth_tools.check_intersection_with_org(None, ["a"])


######################### Users #########################
@patch("common.pgquery.query_db")
def test_get_user_dict_for_org(mock_query_db):
    mock_query_db.return_value = auth_tools_data.user_query_return
    valid_users = auth_tools.get_user_dict_for_org(auth_tools_data.query_organizations)
    assert "test1@gmail.com" in valid_users
    assert "test2@gmail.com" in valid_users
    assert "test3@gmail.com" in valid_users
    assert len(valid_users) == 3

    assert mock_query_db.call_count == 1
    assert mock_query_db.call_args[0][0] == auth_tools_data.user_query_statement


@patch("common.pgquery.query_db")
def test_get_user_dict_for_org_no_orgs(mock_query_db):
    mock_query_db.return_value = []
    valid_users = auth_tools.get_user_dict_for_org([])
    assert len(valid_users) == 0

    assert mock_query_db.call_count == 0


@patch("common.pgquery.query_db")
def test_check_user_with_org(mock_query_db):
    mock_query_db.return_value = auth_tools_data.user_query_return

    # Valid users
    assert auth_tools.check_user_with_org("test1@gmail.com", ["a"])
    assert auth_tools.check_user_with_org("test2@gmail.com", ["a"])
    assert auth_tools.check_user_with_org("test3@gmail.com", ["a"])

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
    assert auth_tools.get_qualified_org_list(user, ORG_ROLE_LITERAL.ADMIN) == [
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
