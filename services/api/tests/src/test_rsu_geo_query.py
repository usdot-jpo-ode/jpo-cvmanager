from unittest.mock import patch, MagicMock
import pytest
import api.src.rsu_geo_query as rsu_geo_query
import api.tests.data.rsu_geo_query_data as rsu_geo_query_data


##################################### Testing Requests ###########################################


def test_options_request():
    counts = rsu_geo_query.RsuGeoQuery()
    (body, code, headers) = counts.options()
    assert body == ""
    assert code == 204
    assert headers["Access-Control-Allow-Methods"] == "POST"


@patch("api.src.rsu_geo_query.query_org_rsus")
@patch("api.src.rsu_geo_query.query_rsu_devices")
def test_post_request(mock_query, mock_rsus):
    req = MagicMock()
    req.args = rsu_geo_query_data.request_args_good
    req.environ = rsu_geo_query_data.request_params_good
    geo_query = rsu_geo_query.RsuGeoQuery()
    mock_rsus.return_value = ["10.0.0.1", "10.0.0.2", "10.0.0.3"], 200
    mock_query.return_value = ["10.0.0.1"], 200
    with patch("api.src.rsu_geo_query.request", req):
        (data, code, headers) = geo_query.post()
        assert code == 200
        assert headers["Access-Control-Allow-Origin"] == "test.com"
        assert headers["Content-Type"] == "application/json"
        assert data == ["10.0.0.1"]


################################### Testing Data Validation #########################################


def test_schema_validate_invalid_message():
    req = MagicMock()
    req.args = rsu_geo_query_data.request_args_bad_message
    geo_query = rsu_geo_query.RsuGeoQuery()
    with patch("api.src.rsu_geo_query.request", req):
        with pytest.raises(Exception):
            assert geo_query.post()


def test_schema_validate_bad_type():
    req = MagicMock()
    req.args = rsu_geo_query_data.request_args_bad_type
    geo_query = rsu_geo_query.RsuGeoQuery()
    with patch("api.src.rsu_geo_query.request", req):
        with pytest.raises(Exception):
            assert geo_query.post()


################################### Test query_org_rsus ########################################


@patch("api.src.rsu_geo_query.pgquery")
def test_query_org_rsus(mock_pgquery):
    mock_pgquery.query_db.return_value = [
        ({"10.11.81.12"},),
        ({"10.11.81.13"},),
        ({"10.11.81.14"},),
    ]
    actual_result = rsu_geo_query.query_org_rsus("Test")
    mock_pgquery.query_db.assert_called_with(rsu_geo_query_data.rsu_org_query)

    assert actual_result == {"{10.11.81.12}", "{10.11.81.13}", "{10.11.81.14}"}


@patch("api.src.rsu_geo_query.pgquery")
def test_query_org_rsus_empty(mock_pgquery):
    mock_pgquery.query_db.return_value = []
    actual_result = rsu_geo_query.query_org_rsus("Test")

    assert actual_result == set()


################################### Test query_rsu_devices ########################################


@patch("api.src.rsu_commands.pgquery.query_db")
def test_query_rsu_devices(mock_query_db):
    mock_query_db.return_value = [
        ({"ip": "10.11.81.12"},),
    ]
    actual_result, code = rsu_geo_query.query_rsu_devices(
        {"10.11.81.12"},
        rsu_geo_query_data.point_list,
    )
    mock_query_db.assert_called_with(rsu_geo_query_data.rsu_devices_query)

    assert actual_result == ["10.11.81.12"]
    assert code == 200

@patch("api.src.rsu_commands.pgquery.query_db")
def test_query_rsu_devices_with_vendor(mock_query_db):
    mock_query_db.return_value = [
        ({"ip": "10.11.81.12"},),
    ]
    actual_result, code = rsu_geo_query.query_rsu_devices(
        {"10.11.81.12"},
        rsu_geo_query_data.point_list_vendor,
        vendor="Test"
    )
    mock_query_db.assert_called_with(rsu_geo_query_data.rsu_devices_query_vendor)

    assert actual_result == ["10.11.81.12"]
    assert code == 200