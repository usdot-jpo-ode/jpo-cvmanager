from unittest.mock import patch, MagicMock
import pytest
import api.src.rsu_querycounts as rsu_querycounts
from api.src.rsu_querycounts import query_rsu_counts_mongo
import api.tests.data.rsu_querycounts_data as querycounts_data
from api.tests.data import auth_data
from werkzeug.exceptions import Forbidden

user_valid = auth_data.get_request_environ()


# #################################### Testing Requests ###########################################
def test_options_request():
    counts = rsu_querycounts.RsuQueryCounts()
    (body, code, headers) = counts.options()
    assert body == ""
    assert code == 204
    assert headers["Access-Control-Allow-Methods"] == "GET"


@patch("api.src.api_environment.COUNTS_MSG_TYPES", ["BSM", "SSM", "SPAT"])
@patch("api.src.rsu_querycounts.get_organization_rsus")
@patch("api.src.rsu_querycounts.query_rsu_counts_mongo")
@patch(
    "api.src.rsu_querycounts.request",
    MagicMock(
        args=querycounts_data.request_args_good,
    ),
)
def test_get_request(mock_query, mock_rsus):
    counts = rsu_querycounts.RsuQueryCounts()
    mock_rsus.return_value = ["10.0.0.1", "10.0.0.2", "10.0.0.3"]
    mock_query.return_value = {"Some Data"}
    (data, code, headers) = counts.get()
    assert code == 200
    assert headers["Access-Control-Allow-Origin"] == "test.com"
    assert headers["Content-Type"] == "application/json"
    assert data == {"Some Data"}


@patch(
    "api.src.rsu_querycounts.request",
    MagicMock(
        args=querycounts_data.request_args_bad_type,
    ),
)
def test_schema_validate_bad_data():
    counts = rsu_querycounts.RsuQueryCounts()
    with pytest.raises(Exception):
        assert counts.get()


# ################################## Test get_organization_rsus ########################################
@patch("api.src.rsu_querycounts.pgquery")
def test_rsu_counts_get_organization_rsus(mock_pgquery):
    mock_pgquery.query_db.return_value = [
        ({"ipv4_address": "10.11.81.12", "primary_route": "Route 1"},),
        ({"ipv4_address": "10.11.81.13", "primary_route": "Route 1"},),
        ({"ipv4_address": "10.11.81.14", "primary_route": "Route 1"},),
    ]
    expected_query = (
        "SELECT to_jsonb(row) "
        "FROM ("
        "SELECT rd.ipv4_address, rd.primary_route "
        "FROM public.rsus rd "
        "JOIN public.rsu_organization_name AS ron_v ON ron_v.rsu_id = rd.rsu_id "
        "ORDER BY primary_route ASC, milepost ASC"
        ") as row"
    )

    actual_result = rsu_querycounts.get_organization_rsus(user_valid, [])

    mock_pgquery.query_db.assert_called_with(expected_query, params={})
    assert actual_result == {
        "10.11.81.12": "Route 1",
        "10.11.81.13": "Route 1",
        "10.11.81.14": "Route 1",
    }


@patch("api.src.rsu_querycounts.pgquery")
def test_rsu_counts_get_organization_rsus_empty(mock_pgquery):
    mock_pgquery.query_db.return_value = []
    expected_query = (
        "SELECT to_jsonb(row) "
        "FROM ("
        "SELECT rd.ipv4_address, rd.primary_route "
        "FROM public.rsus rd "
        "JOIN public.rsu_organization_name AS ron_v ON ron_v.rsu_id = rd.rsu_id "
        "ORDER BY primary_route ASC, milepost ASC"
        ") as row"
    )
    actual_result = rsu_querycounts.get_organization_rsus(user_valid, [])
    mock_pgquery.query_db.assert_called_with(expected_query, params={})

    assert actual_result == {}


##################################### Test query_rsu_counts ###########################################
@patch("api.src.rsu_querycounts.MongoClient")
def test_query_rsu_counts_mongo_success(mock_mongo):
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_mongo.return_value.__getitem__.return_value = mock_db
    mock_db.__getitem__.return_value = mock_collection
    mock_db.validate_collection.return_value = "valid"

    # Mock data that would be returned from MongoDB
    mock_collection.find_one.return_value = {"count": 5}

    allowed_ips = {"192.168.0.1": "A1", "192.168.0.2": "A2"}
    message_type = "BSM"
    start = "2022-01-01T00:00:00"
    end = "2023-01-01T00:00:00"

    expected_result = {
        "192.168.0.1": {"road": "A1", "count": 5},
        "192.168.0.2": {"road": "A2", "count": 5},
    }

    result = query_rsu_counts_mongo(allowed_ips, message_type, start, end)

    assert result == expected_result


@patch("api.src.rsu_querycounts.MongoClient")
@patch("api.src.rsu_querycounts.logging")
def test_query_rsu_counts_mongo_failure(mock_logging, mock_mongo):
    # Mock the MongoDB connection to throw an exception
    mock_mongo.side_effect = Exception("Failed to connect")

    allowed_ips = ["192.168.0.1", "192.168.0.2"]
    message_type = "TYPE_A"
    start = "2022-01-01T00:00:00"
    end = "2023-01-01T00:00:00"

    with pytest.raises(Forbidden) as exc_info:
        query_rsu_counts_mongo(allowed_ips, message_type, start, end)

    assert str(exc_info.value) == "403 Forbidden: Failed to connect to Mongo"
