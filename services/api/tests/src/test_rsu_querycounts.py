from unittest.mock import patch, MagicMock
import pytest
import api.src.rsu_querycounts as rsu_querycounts
from api.src.rsu_querycounts import query_rsu_counts_aggregated
import api.tests.data.rsu_querycounts_data as querycounts_data
from api.tests.data import auth_data
from werkzeug.exceptions import Forbidden, InternalServerError
from datetime import datetime

user_valid = auth_data.get_request_environ()


# #################################### Testing Requests ###########################################
def test_options_request():
    counts = rsu_querycounts.RsuQueryCounts()
    (body, code, headers) = counts.options()
    assert body == ""
    assert code == 204
    assert headers["Access-Control-Allow-Methods"] == "GET"


##################################### Test query_rsu_counts_aggregated ###########################################
@patch("api.src.rsu_querycounts.MongoClient")
@patch("api.src.rsu_querycounts.util.format_date_utc")
def test_query_rsu_counts_aggregated_success(mock_format_date, mock_mongo):
    # Mock date formatting
    mock_start_dt = datetime(2022, 1, 1, 0, 0, 0)
    mock_end_dt = datetime(2023, 1, 1, 0, 0, 0)
    mock_format_date.side_effect = [mock_start_dt, mock_end_dt]

    # Mock MongoDB connection and collection
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_mongo.return_value.__getitem__.return_value = mock_db
    mock_db.__getitem__.return_value = mock_collection

    # Mock aggregation cursor result
    mock_cursor = [
        {
            "rsuIp": "192.168.0.1",
            "messageTypeCounts": {"BSM": 100, "SPAT": 50, "SSM": 25},
        },
        {
            "rsuIp": "192.168.0.2",
            "messageTypeCounts": {"BSM": 200, "SPAT": 75},
        },
    ]
    mock_collection.aggregate.return_value = iter(mock_cursor)

    allowed_ips_dict = {
        "192.168.0.1": "Route 1",
        "192.168.0.2": "Route 2",
        "192.168.0.3": "Route 3",
    }
    start = "2022-01-01T00:00:00"
    end = "2023-01-01T00:00:00"

    expected_result = {
        "192.168.0.1": {
            "road": "Route 1",
            "messageTypeCounts": {"BSM": 100, "SPAT": 50, "SSM": 25},
        },
        "192.168.0.2": {
            "road": "Route 2",
            "messageTypeCounts": {"BSM": 200, "SPAT": 75},
        },
        "192.168.0.3": {"road": "Route 3", "messageTypeCounts": {}},
    }

    result = query_rsu_counts_aggregated(allowed_ips_dict, start, end)

    assert result == expected_result
    # Verify the aggregation pipeline was called with allowDiskUse=False
    mock_collection.aggregate.assert_called_once()
    call_args = mock_collection.aggregate.call_args
    assert call_args[1]["allowDiskUse"] is False


@patch("api.src.rsu_querycounts.MongoClient")
@patch("api.src.rsu_querycounts.util.format_date_utc")
def test_query_rsu_counts_aggregated_empty_results(mock_format_date, mock_mongo):
    # Mock date formatting
    mock_start_dt = datetime(2022, 1, 1, 0, 0, 0)
    mock_end_dt = datetime(2023, 1, 1, 0, 0, 0)
    mock_format_date.side_effect = [mock_start_dt, mock_end_dt]

    # Mock MongoDB connection and collection
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_mongo.return_value.__getitem__.return_value = mock_db
    mock_db.__getitem__.return_value = mock_collection

    # Mock empty aggregation cursor result
    mock_collection.aggregate.return_value = iter([])

    allowed_ips_dict = {
        "192.168.0.1": "Route 1",
        "192.168.0.2": "Route 2",
    }
    start = "2022-01-01T00:00:00"
    end = "2023-01-01T00:00:00"

    expected_result = {
        "192.168.0.1": {"road": "Route 1", "messageTypeCounts": {}},
        "192.168.0.2": {"road": "Route 2", "messageTypeCounts": {}},
    }

    result = query_rsu_counts_aggregated(allowed_ips_dict, start, end)

    assert result == expected_result


@patch("api.src.rsu_querycounts.MongoClient")
@patch("api.src.rsu_querycounts.logging")
def test_query_rsu_counts_aggregated_connection_failure(mock_logging, mock_mongo):
    # Mock the MongoDB connection to throw an exception
    mock_mongo.side_effect = Exception("Connection timeout")

    allowed_ips_dict = {"192.168.0.1": "Route 1"}
    start = "2022-01-01T00:00:00"
    end = "2023-01-01T00:00:00"

    with pytest.raises(Forbidden) as exc_info:
        query_rsu_counts_aggregated(allowed_ips_dict, start, end)

    assert str(exc_info.value) == "403 Forbidden: Failed to connect to Mongo"
    mock_logging.error.assert_called_once()


@patch("api.src.rsu_querycounts.MongoClient")
@patch("api.src.rsu_querycounts.util.format_date_utc")
@patch("api.src.rsu_querycounts.logging")
def test_query_rsu_counts_aggregated_aggregation_failure(
    mock_logging, mock_format_date, mock_mongo
):
    # Mock date formatting
    mock_start_dt = datetime(2022, 1, 1, 0, 0, 0)
    mock_end_dt = datetime(2023, 1, 1, 0, 0, 0)
    mock_format_date.side_effect = [mock_start_dt, mock_end_dt]

    # Mock MongoDB connection and collection
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_mongo.return_value.__getitem__.return_value = mock_db
    mock_db.__getitem__.return_value = mock_collection

    # Mock aggregation to throw an exception
    mock_collection.aggregate.side_effect = Exception("Aggregation error")

    allowed_ips_dict = {"192.168.0.1": "Route 1"}
    start = "2022-01-01T00:00:00"
    end = "2023-01-01T00:00:00"

    with pytest.raises(InternalServerError) as exc_info:
        query_rsu_counts_aggregated(allowed_ips_dict, start, end)

    assert str(exc_info.value) == "500 Internal Server Error: Encountered unknown issue"
    mock_logging.error.assert_called()


@patch("api.src.rsu_querycounts.MongoClient")
@patch("api.src.rsu_querycounts.util.format_date_utc")
def test_query_rsu_counts_aggregated_partial_results(mock_format_date, mock_mongo):
    # Mock date formatting
    mock_start_dt = datetime(2022, 1, 1, 0, 0, 0)
    mock_end_dt = datetime(2023, 1, 1, 0, 0, 0)
    mock_format_date.side_effect = [mock_start_dt, mock_end_dt]

    # Mock MongoDB connection and collection
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_mongo.return_value.__getitem__.return_value = mock_db
    mock_db.__getitem__.return_value = mock_collection

    # Mock aggregation cursor with only one RSU having results
    mock_cursor = [
        {
            "rsuIp": "192.168.0.1",
            "messageTypeCounts": {"BSM": 150},
        }
    ]
    mock_collection.aggregate.return_value = iter(mock_cursor)

    allowed_ips_dict = {
        "192.168.0.1": "Route 1",
        "192.168.0.2": "Route 2",
        "192.168.0.3": "Route 3",
    }
    start = "2022-01-01T00:00:00"
    end = "2023-01-01T00:00:00"

    expected_result = {
        "192.168.0.1": {"road": "Route 1", "messageTypeCounts": {"BSM": 150}},
        "192.168.0.2": {"road": "Route 2", "messageTypeCounts": {}},
        "192.168.0.3": {"road": "Route 3", "messageTypeCounts": {}},
    }

    result = query_rsu_counts_aggregated(allowed_ips_dict, start, end)

    assert result == expected_result


@patch("api.src.rsu_querycounts.MongoClient")
@patch("api.src.rsu_querycounts.util.format_date_utc")
def test_query_rsu_counts_aggregated_pipeline_structure(mock_format_date, mock_mongo):
    # Mock date formatting
    mock_start_dt = datetime(2022, 1, 1, 0, 0, 0)
    mock_end_dt = datetime(2023, 1, 1, 0, 0, 0)
    mock_format_date.side_effect = [mock_start_dt, mock_end_dt]

    # Mock MongoDB connection and collection
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_mongo.return_value.__getitem__.return_value = mock_db
    mock_db.__getitem__.return_value = mock_collection
    mock_collection.aggregate.return_value = iter([])

    allowed_ips_dict = {"192.168.0.1": "Route 1", "192.168.0.2": "Route 2"}
    start = "2022-01-01T00:00:00"
    end = "2023-01-01T00:00:00"

    query_rsu_counts_aggregated(allowed_ips_dict, start, end)

    # Verify the pipeline structure
    call_args = mock_collection.aggregate.call_args
    pipeline = call_args[0][0]

    # Check match stage
    assert pipeline[0]["$match"]["rsuIp"]["$in"] == ["192.168.0.1", "192.168.0.2"]
    assert pipeline[0]["$match"]["timestamp"]["$gte"] == mock_start_dt
    assert pipeline[0]["$match"]["timestamp"]["$lt"] == mock_end_dt

    # Check project stage
    assert "$project" in pipeline[1]
    assert pipeline[1]["$project"]["rsuIp"] == 1
    assert pipeline[1]["$project"]["messageType"] == 1
    assert pipeline[1]["$project"]["count"] == 1
    assert pipeline[1]["$project"]["_id"] == 0

    # Check first group stage
    assert "$group" in pipeline[2]
    assert pipeline[2]["$group"]["_id"] == {
        "rsuIp": "$rsuIp",
        "messageType": "$messageType",
    }

    # Check second group stage
    assert "$group" in pipeline[3]
    assert pipeline[3]["$group"]["_id"] == "$_id.rsuIp"

    # Check final project stage
    assert "$project" in pipeline[4]
    assert pipeline[4]["$project"]["_id"] == 0
    assert pipeline[4]["$project"]["rsuIp"] == "$_id"
