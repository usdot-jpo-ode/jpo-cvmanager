import os
from datetime import datetime, timedelta
from mock import MagicMock, patch
from addons.images.count_metric import mongo_counter


def test_write_counts():
    mock_collection = MagicMock()
    mock_mongo_db = {"CVCounts": mock_collection}

    mongo_counter.write_counts(mock_mongo_db, ["test"])

    mock_collection.insert_many.assert_called_with(["test"])


def test_write_counts_empty():
    mock_collection = MagicMock()
    mock_mongo_db = {"CVCounts": mock_collection}

    mongo_counter.write_counts(mock_mongo_db, [])

    mock_collection.insert_many.assert_not_called()


@patch.dict(os.environ, {"MONGO_DB_URI": "uri", "MONGO_DB_NAME": "name"})
def test_count_query_bsm():
    mock_collection = MagicMock()
    mock_collection.aggregate.return_value = [
        {
            "_id": "10.0.0.1",
            "count": 5,
        }
    ]
    mock_mongo_db = {"OdeBsmJson": mock_collection}

    start_dt = datetime.now().replace(
        year=2024, month=1, day=1, minute=0, second=0, microsecond=0
    )
    end_dt = datetime.now().replace(
        year=2024, month=1, day=2, minute=0, second=0, microsecond=0
    )

    result = mongo_counter.count_query(mock_mongo_db, "bsm", start_dt, end_dt)

    expected_result = [
        {
            "messageType": "bsm",
            "rsuIp": "10.0.0.1",
            "timestamp": start_dt,
            "count": 5,
        }
    ]
    assert result == expected_result


@patch.dict(os.environ, {"MONGO_DB_URI": "uri", "MONGO_DB_NAME": "name"})
@patch("addons.images.count_metric.mongo_counter.write_counts")
@patch("addons.images.count_metric.mongo_counter.count_query")
def test_run_mongo_counter(mock_count_query, mock_write_counts):
    start_dt = datetime.now().replace(
        year=2024, month=1, day=1, minute=0, second=0, microsecond=0
    )
    mock_count_query.return_value = [
        {
            "messageType": "test",
            "rsuIp": "10.0.0.1",
            "timestamp": start_dt,
            "count": 5,
        }
    ]
    mock_mongo_db = MagicMock()
    mongo_counter.message_types = ["test"]

    mongo_counter.run_mongo_counter(mock_mongo_db)

    expected = [
        {
            "messageType": "test",
            "rsuIp": "10.0.0.1",
            "timestamp": start_dt,
            "count": 5,
        }
    ]
    mock_count_query.assert_called_once()
    mock_write_counts.assert_called_with(mock_mongo_db, expected)
