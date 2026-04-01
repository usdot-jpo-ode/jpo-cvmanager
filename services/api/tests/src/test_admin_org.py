from unittest.mock import patch, MagicMock, call, PropertyMock
import pytest
import api.src.admin_org as admin_org
import api.tests.data.admin_org_data as admin_org_data
from sqlalchemy.exc import IntegrityError, SQLAlchemyError
from werkzeug.exceptions import HTTPException
from api.tests.data import auth_data
from werkzeug.exceptions import BadRequest, Conflict, InternalServerError
from flask import Flask, request
from common.auth_tools import (
    ENVIRON_USER_KEY,
    EnvironWithOrg,
    ORG_ROLE_LITERAL,
    UserInfo,
)

user_valid = auth_data.get_request_environ()


@pytest.fixture
def app():
    app = Flask(__name__)
    return app


@pytest.fixture
def permission_result():
    mock_user_info = MagicMock(spec=UserInfo)
    mock_user_info.super_user = True
    mock_user_info.organizations = {"Test Org": "admin"}

    user = EnvironWithOrg(mock_user_info, "Test Org", ORG_ROLE_LITERAL.ADMIN)

    return user


# ##################################### Testing Requests ##########################################
# OPTIONS endpoint test
def test_request_options():
    info = admin_org.AdminOrg()
    (body, code, headers) = info.options()
    assert body == ""
    assert code == 204
    assert headers["Access-Control-Allow-Methods"] == "GET,PATCH,DELETE"


# GET endpoint tests
@patch("api.src.admin_org.get_modify_org_data_authorized")
def test_entry_get(mock_get_modify_org_data, app):
    mock_get_modify_org_data.return_value = {}
    with app.test_request_context(query_string=admin_org_data.request_args_get_delete_good):
        request.environ[ENVIRON_USER_KEY] = user_valid
        status = admin_org.AdminOrg()
        (body, code, headers) = status.get()

        mock_get_modify_org_data.assert_called_once()
        assert code == 200
        assert headers["Access-Control-Allow-Origin"] == "test.com"
        assert body == {}


# Test schema for string value
def test_entry_get_schema_str(app):
    with app.test_request_context(query_string=admin_org_data.request_args_get_delete_bad):
        request.environ[ENVIRON_USER_KEY] = user_valid
        status = admin_org.AdminOrg()
        with pytest.raises(HTTPException):
            status.get()


# PATCH endpoint tests
@patch("api.src.admin_org.modify_org_authorized")
def test_entry_patch(mock_modify_org, app):
    mock_modify_org.return_value = {}
    with app.test_request_context(json=admin_org_data.request_json_good):
        request.environ[ENVIRON_USER_KEY] = user_valid
        status = admin_org.AdminOrg()
        (body, code, headers) = status.patch()

        mock_modify_org.assert_called_once_with(
            admin_org_data.request_json_good["orig_name"],
            admin_org_data.request_json_good,
            is_bulk_update=False,
        )
        assert code == 200
        assert headers["Access-Control-Allow-Origin"] == "test.com"
        assert body == {}


def test_entry_patch_schema(app):
    with app.test_request_context(json=admin_org_data.request_json_bad):
        request.environ[ENVIRON_USER_KEY] = user_valid
        status = admin_org.AdminOrg()
        with pytest.raises(HTTPException):
            status.patch()


# DELETE endpoint tests
@patch("api.src.admin_org.delete_org_authorized")
def test_entry_delete_user(mock_delete_org, app):
    mock_delete_org.return_value = {"message": "Organization successfully deleted"}
    with app.test_request_context(query_string=admin_org_data.request_args_get_delete_good):
        request.environ[ENVIRON_USER_KEY] = user_valid
        status = admin_org.AdminOrg()
        (body, code, headers) = status.delete()

        mock_delete_org.assert_called_once()
        assert code == 200
        assert headers["Access-Control-Allow-Origin"] == "test.com"
        assert body == {"message": "Organization successfully deleted"}


def test_entry_delete_schema(app):
    with app.test_request_context(query_string={}):
        request.environ[ENVIRON_USER_KEY] = user_valid
        status = admin_org.AdminOrg()
        with pytest.raises(HTTPException):
            status.delete()


# ##################################### Testing Functions ##########################################
# get_all_orgs
@patch("api.src.admin_org.pgquery.query_db")
def test_get_all_orgs(mock_query_db):
    mock_query_db.return_value = admin_org_data.get_all_orgs_pgdb_return
    expected_result = admin_org_data.get_all_orgs_result
    actual_result = admin_org.get_all_orgs(
        list(user_valid.user_info.organizations.keys())
    )

    mock_query_db.assert_called_with(
        admin_org_data.get_all_orgs_sql[0], params=admin_org_data.get_all_orgs_sql[1]
    )
    assert actual_result == expected_result


# get_org_data
@patch("api.src.admin_org.pgquery.query_db")
def test_get_org_data(mock_query_db):
    mock_query_db.side_effect = [
        admin_org_data.get_org_data_user_return,
        admin_org_data.get_org_data_rsu_return,
        admin_org_data.get_org_data_intersection_return,
    ]
    expected_result = admin_org_data.get_org_data_result
    actual_result = admin_org.get_org_data("Test Org", user_valid)

    calls = [
        call(admin_org_data.get_org_data_user_sql, params={"org_name": "Test Org"}),
        call(admin_org_data.get_org_data_rsu_sql, params={"org_name": "Test Org"}),
        call(
            admin_org_data.get_org_data_intersection_sql,
            params={"org_name": "Test Org"},
        ),
    ]
    mock_query_db.assert_has_calls(calls)
    assert actual_result == expected_result


# get_allowed_selections
@patch("api.src.admin_org.pgquery.query_db")
def test_get_allowed_selections(mock_query_db):
    mock_query_db.return_value = admin_org_data.get_allowed_selections_return
    expected_result = admin_org_data.get_allowed_selections_result
    actual_result = admin_org.get_allowed_selections()

    mock_query_db.assert_called_with(admin_org_data.get_allowed_selections_sql)
    assert actual_result == expected_result


# get_modify_org_data
@patch("api.src.admin_org.get_all_orgs")
def test_get_modify_org_data_all(mock_get_all_orgs, app):
    mock_get_all_orgs.return_value = ["Test Org data"]
    expected_rsu_data = {"org_data": ["Test Org data"]}
    mock_permission_result = MagicMock()
    mock_permission_result.user.user_info.super_user = True
    with app.test_request_context():
        request.environ[ENVIRON_USER_KEY] = user_valid
        actual_result = admin_org.get_modify_org_data_authorized(
            "all", permission_result=mock_permission_result
        )

    mock_get_all_orgs.assert_called_with(None)
    assert actual_result == expected_rsu_data


@patch("api.src.admin_org.get_allowed_selections")
@patch("api.src.admin_org.get_org_data")
def test_get_modify_org_data_specific(
    mock_get_org_data, mock_get_allowed_selections, app
):
    mock_get_org_data.return_value = "Test Org data"
    mock_get_allowed_selections.return_value = ["allowed_selections"]
    expected_rsu_data = {
        "org_data": "Test Org data",
        "allowed_selections": ["allowed_selections"],
    }
    mock_permission_result = MagicMock()
    mock_permission_result.user.user_info.super_user = True
    with app.test_request_context():
        request.environ[ENVIRON_USER_KEY] = user_valid
        actual_result = admin_org.get_modify_org_data_authorized(
            "Test Org", permission_result=mock_permission_result
        )

    mock_get_org_data.assert_called_with("Test Org", True)
    mock_get_allowed_selections.assert_called_with()
    assert actual_result == expected_rsu_data


# check_safe_input
def test_check_safe_input():
    expected_result = True
    actual_result = admin_org.check_safe_input(admin_org_data.request_json_good)
    assert actual_result == expected_result


def test_check_safe_input_bad():
    expected_result = False
    actual_result = admin_org.check_safe_input(admin_org_data.request_json_unsafe_input)
    assert actual_result == expected_result


# modify_org
@patch("api.src.admin_org.get_modify_org_data_authorized")
@patch("api.src.admin_org.check_safe_input")
@patch("api.src.admin_org.pgquery.write_db")
def test_modify_organization_success(
    mock_pgquery, mock_check_safe_input, mock_get_modify_org_data, app
):
    mock_check_safe_input.return_value = True
    mock_get_modify_org_data.return_value = {"org_data": "mocked_data"}
    expected_msg = {"org_data": "mocked_data"}
    with app.test_request_context():
        request.environ[ENVIRON_USER_KEY] = user_valid
        actual_msg = admin_org.modify_org_authorized(
            "Test Org", admin_org_data.request_json_good, is_bulk_update=False
        )

    calls = [
        call(admin_org_data.modify_org_sql[0], params=admin_org_data.modify_org_sql[1]),
        call(
            admin_org_data.modify_org_add_user_sql[0],
            params=admin_org_data.modify_org_add_user_sql[1],
        ),
        call(
            admin_org_data.modify_org_modify_user_sql[0],
            params=admin_org_data.modify_org_modify_user_sql[1],
        ),
        call(
            admin_org_data.modify_org_remove_user_sql[0],
            params=admin_org_data.modify_org_remove_user_sql[1],
        ),
        call(
            admin_org_data.modify_org_add_rsu_sql[0],
            params=admin_org_data.modify_org_add_rsu_sql[1],
        ),
        call(
            admin_org_data.modify_org_remove_rsu_sql[0],
            params=admin_org_data.modify_org_remove_rsu_sql[1],
        ),
        call(
            admin_org_data.modify_org_add_intersection_sql[0],
            params=admin_org_data.modify_org_add_intersection_sql[1],
        ),
        call(
            admin_org_data.modify_org_remove_intersection_sql[0],
            params=admin_org_data.modify_org_remove_intersection_sql[1],
        ),
    ]
    mock_pgquery.assert_has_calls(calls)
    assert actual_msg == expected_msg


@patch("api.src.admin_org.check_safe_input")
@patch("api.src.admin_org.pgquery.write_db")
def test_modify_org_check_fail(mock_pgquery, mock_check_safe_input, app):
    mock_check_safe_input.return_value = False

    expected_message = "400 Bad Request: No special characters are allowed: !\"#$%&'()*+,./:;<=>?@[\\]^`{|}~. No sequences of '-' characters are allowed"
    with app.test_request_context():
        request.environ[ENVIRON_USER_KEY] = user_valid
        with pytest.raises(BadRequest) as exc_info:
            admin_org.modify_org_authorized("Test Org", admin_org_data.request_json_good)

    mock_pgquery.assert_has_calls([])
    assert str(exc_info.value) == expected_message


@patch("api.src.admin_org.check_safe_input")
@patch("api.src.admin_org.pgquery.write_db")
def test_modify_org_generic_exception(mock_pgquery, mock_check_safe_input, app):
    mock_check_safe_input.return_value = True
    mock_pgquery.side_effect = SQLAlchemyError("Test")

    expected_message = (
        "500 Internal Server Error: Encountered unknown issue executing query"
    )
    with app.test_request_context():
        request.environ[ENVIRON_USER_KEY] = user_valid
        with pytest.raises(InternalServerError) as exc_info:
            admin_org.modify_org_authorized("Test Org", admin_org_data.request_json_good)

    assert str(exc_info.value) == expected_message


@patch("api.src.admin_org.check_safe_input")
@patch("api.src.admin_org.pgquery.write_db")
def test_modify_org_sql_exception(mock_pgquery, mock_check_safe_input, app):
    mock_check_safe_input.return_value = True
    orig = MagicMock()
    orig.args = ({"D": "SQL issue encountered"},)
    mock_pgquery.side_effect = IntegrityError("", {}, orig)

    expected_message = "500 Internal Server Error: SQL issue encountered"
    with app.test_request_context():
        request.environ[ENVIRON_USER_KEY] = user_valid
        with pytest.raises(InternalServerError) as exc_info:
            admin_org.modify_org_authorized("Test Org", admin_org_data.request_json_good)

    assert str(exc_info.value) == expected_message


# delete_org
@patch("api.src.admin_org.pgquery.write_db")
@patch("api.src.admin_org.pgquery.query_db")
def test_delete_org(mock_query_db, mock_write_db, app):
    mock_query_db.return_value = []
    expected_result = {"message": "Organization successfully deleted"}
    with app.test_request_context():
        request.environ[ENVIRON_USER_KEY] = user_valid
        actual_result = admin_org.delete_org_authorized("Test Org")

    calls = [
        call(
            admin_org_data.delete_org_calls[0][0],
            params=admin_org_data.delete_org_calls[0][1],
        ),
        call(
            admin_org_data.delete_org_calls[1][0],
            params=admin_org_data.delete_org_calls[1][1],
        ),
        call(
            admin_org_data.delete_org_calls[2][0],
            params=admin_org_data.delete_org_calls[2][1],
        ),
    ]
    mock_write_db.assert_has_calls(calls)
    assert actual_result == expected_result


@patch("api.src.admin_org.pgquery.query_db")
def test_delete_org_failure_orphan_rsu(mock_query_db, app):
    mock_query_db.return_value = [
        [{"user_id": 1, "count": 2}],
        [{"user_id": 2, "count": 1}],
    ]
    expected_message = "409 Conflict: Cannot delete organization that has one or more RSUs only associated with this organization"
    with app.test_request_context():
        request.environ[ENVIRON_USER_KEY] = user_valid
        with pytest.raises(Conflict) as exc_info:
            admin_org.delete_org_authorized("Test Org")

    assert str(exc_info.value) == expected_message


@patch("api.src.admin_org.pgquery.query_db")
@patch("api.src.admin_org.check_orphan_rsus")
@patch("api.src.admin_org.check_orphan_intersections")
def test_delete_org_failure_orphan_user(
    mock_orphan_intersections, mock_orphan_rsus, mock_query_db, app
):
    mock_orphan_intersections.return_value = False
    mock_orphan_rsus.return_value = False
    mock_query_db.return_value = [
        [{"user_id": 1, "count": 2}],
        [{"user_id": 2, "count": 1}],
    ]
    expected_message = "409 Conflict: Cannot delete organization that has one or more users only associated with this organization"
    with app.test_request_context():
        request.environ[ENVIRON_USER_KEY] = user_valid
        with pytest.raises(Conflict) as exc_info:
            admin_org.delete_org_authorized("Test Org")

    assert str(exc_info.value) == expected_message


@patch("api.src.admin_org.pgquery.query_db")
@patch("api.src.admin_org.check_orphan_rsus")
def test_delete_org_failure_orphan_intersection(mock_orphan_rsus, mock_query_db, app):
    mock_orphan_rsus.return_value = False
    mock_query_db.return_value = [
        [{"user_id": 1, "count": 2}],
        [{"user_id": 2, "count": 1}],
    ]
    expected_message = "409 Conflict: Cannot delete organization that has one or more Intersections only associated with this organization"
    with app.test_request_context():
        request.environ[ENVIRON_USER_KEY] = user_valid
        with pytest.raises(Conflict) as exc_info:
            admin_org.delete_org_authorized("Test Org")

    assert str(exc_info.value) == expected_message


# ##################################### Bulk Update Tests #########################################


def test_modify_org_bulk_tim_deposit_success(app, permission_result):
    org_spec = {
        "orig_name": "Test Org",
        "name": "Test Org",
        "email": "test@gmail.com",
        "users_to_add": [],
        "users_to_modify": [],
        "users_to_remove": [],
        "rsus_to_add": [],
        "rsus_to_remove": [],
        "intersections_to_add": [],
        "intersections_to_remove": [],
        "tim_deposit": True,
    }

    with app.test_request_context():
        request.environ[ENVIRON_USER_KEY] = permission_result
        with patch("api.src.admin_org.pgquery.write_db") as mock_write_db, patch(
            "api.src.admin_org.check_safe_input", return_value=True
        ), patch(
            "api.src.admin_org.get_modify_org_data_authorized",
            return_value={"org_data": "mocked_data"},
        ):

            result = admin_org.modify_org_authorized(
                "Test Org", org_spec, is_bulk_update=True
            )

            assert result == {"org_data": "mocked_data"}

            # Should have 2 write_db calls: 1 for org update, 1 for bulk RSU options update
            assert mock_write_db.call_count == 2

            # Check bulk update call
            bulk_query = (
                "INSERT INTO public.rsu_options (rsu_id, tim_deposit) "
                "SELECT ro.rsu_id, :tim_deposit "
                "FROM public.rsu_organization ro "
                "JOIN public.organizations org ON ro.organization_id = org.organization_id "
                "WHERE org.name = :name "
                "ON CONFLICT (rsu_id) DO UPDATE SET tim_deposit = EXCLUDED.tim_deposit"
            )
            bulk_params = {"name": "Test Org", "tim_deposit": True}

            mock_write_db.assert_any_call(bulk_query, params=bulk_params)


def test_modify_org_bulk_snmp_monitoring_success(app, permission_result):
    org_spec = {
        "orig_name": "Test Org",
        "name": "Test Org",
        "email": "test@gmail.com",
        "users_to_add": [],
        "users_to_modify": [],
        "users_to_remove": [],
        "rsus_to_add": [],
        "rsus_to_remove": [],
        "intersections_to_add": [],
        "intersections_to_remove": [],
        "snmp_monitoring": True,
    }

    with app.test_request_context():
        request.environ[ENVIRON_USER_KEY] = permission_result
        with patch("api.src.admin_org.pgquery.write_db") as mock_write_db, patch(
            "api.src.admin_org.check_safe_input", return_value=True
        ), patch(
            "api.src.admin_org.get_modify_org_data_authorized",
            return_value={"org_data": "mocked_data"},
        ):

            result = admin_org.modify_org_authorized(
                "Test Org", org_spec, is_bulk_update=True
            )

            assert result == {"org_data": "mocked_data"}

            # Check bulk update call
            bulk_query = (
                "INSERT INTO public.rsu_options (rsu_id, snmp_monitoring) "
                "SELECT ro.rsu_id, :snmp_monitoring "
                "FROM public.rsu_organization ro "
                "JOIN public.organizations org ON ro.organization_id = org.organization_id "
                "WHERE org.name = :name "
                "ON CONFLICT (rsu_id) DO UPDATE SET snmp_monitoring = EXCLUDED.snmp_monitoring"
            )
            bulk_params = {"name": "Test Org", "snmp_monitoring": True}

            mock_write_db.assert_any_call(bulk_query, params=bulk_params)


def test_modify_org_bulk_both_success(app, permission_result):
    org_spec = {
        "orig_name": "Test Org",
        "name": "Test Org",
        "email": "test@gmail.com",
        "users_to_add": [],
        "users_to_modify": [],
        "users_to_remove": [],
        "rsus_to_add": [],
        "rsus_to_remove": [],
        "intersections_to_add": [],
        "intersections_to_remove": [],
        "tim_deposit": True,
        "snmp_monitoring": False,
    }

    with app.test_request_context():
        request.environ[ENVIRON_USER_KEY] = permission_result
        with patch("api.src.admin_org.pgquery.write_db") as mock_write_db, patch(
            "api.src.admin_org.check_safe_input", return_value=True
        ), patch(
            "api.src.admin_org.get_modify_org_data_authorized",
            return_value={"org_data": "mocked_data"},
        ):

            result = admin_org.modify_org_authorized(
                "Test Org", org_spec, is_bulk_update=True
            )

            assert result == {"org_data": "mocked_data"}

            # Check bulk update call
            bulk_query = (
                "INSERT INTO public.rsu_options (rsu_id, tim_deposit, snmp_monitoring) "
                "SELECT ro.rsu_id, :tim_deposit, :snmp_monitoring "
                "FROM public.rsu_organization ro "
                "JOIN public.organizations org ON ro.organization_id = org.organization_id "
                "WHERE org.name = :name "
                "ON CONFLICT (rsu_id) DO UPDATE SET tim_deposit = EXCLUDED.tim_deposit, snmp_monitoring = EXCLUDED.snmp_monitoring"
            )
            bulk_params = {
                "name": "Test Org",
                "tim_deposit": True,
                "snmp_monitoring": False,
            }

            mock_write_db.assert_any_call(bulk_query, params=bulk_params)


def test_modify_org_no_bulk_endpoint(app, permission_result):
    org_spec = {
        "orig_name": "Test Org",
        "name": "Test Org",
        "email": "test@gmail.com",
        "users_to_add": [],
        "users_to_modify": [],
        "users_to_remove": [],
        "rsus_to_add": [],
        "rsus_to_remove": [],
        "intersections_to_add": [],
        "intersections_to_remove": [],
        "tim_deposit": True,
    }

    with app.test_request_context():
        request.environ[ENVIRON_USER_KEY] = permission_result
        with patch("api.src.admin_org.pgquery.write_db") as mock_write_db, patch(
            "api.src.admin_org.check_safe_input", return_value=True
        ), patch(
            "api.src.admin_org.get_modify_org_data_authorized",
            return_value={"org_data": "mocked_data"},
        ):

            result = admin_org.modify_org_authorized(
                "Test Org", org_spec, is_bulk_update=False
            )

            assert result == {"org_data": "mocked_data"}
            # Should only have 1 call (for org update)
            assert mock_write_db.call_count == 1
            assert (
                "INSERT INTO public.rsu_options"
                not in mock_write_db.call_args_list[0][0][0]
            )


def test_admin_org_tim_deposit_patch(app, permission_result):
    resource = admin_org.AdminOrgTimDeposit()
    org_spec = {
        "orig_name": "Test Org",
        "name": "Test Org",
        "email": "test@gmail.com",
        "users_to_add": [],
        "users_to_modify": [],
        "users_to_remove": [],
        "rsus_to_add": [],
        "rsus_to_remove": [],
        "intersections_to_add": [],
        "intersections_to_remove": [],
        "tim_deposit": True,
    }
    with app.test_request_context(json=org_spec):
        request.environ[ENVIRON_USER_KEY] = permission_result
        with patch("api.src.admin_org.modify_org_authorized") as mock_modify:
            mock_modify.return_value = {"message": "success"}
            (body, code, headers) = resource.patch()
            assert code == 200
            assert body == {"message": "success"}
            mock_modify.assert_called_once_with("Test Org", org_spec, is_bulk_update=True)


def test_admin_org_snmp_monitoring_patch(app, permission_result):
    resource = admin_org.AdminOrgSnmpMonitoring()
    org_spec = {
        "orig_name": "Test Org",
        "name": "Test Org",
        "email": "test@gmail.com",
        "users_to_add": [],
        "users_to_modify": [],
        "users_to_remove": [],
        "rsus_to_add": [],
        "rsus_to_remove": [],
        "intersections_to_add": [],
        "intersections_to_remove": [],
        "snmp_monitoring": True,
    }
    with app.test_request_context(json=org_spec):
        request.environ[ENVIRON_USER_KEY] = permission_result
        with patch("api.src.admin_org.modify_org_authorized") as mock_modify:
            mock_modify.return_value = {"message": "success"}
            (body, code, headers) = resource.patch()
            assert code == 200
            assert body == {"message": "success"}
            mock_modify.assert_called_once_with("Test Org", org_spec, is_bulk_update=True)
