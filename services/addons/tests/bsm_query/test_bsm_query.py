import os
from pymongo import MongoClient
from datetime import datetime
from unittest.mock import MagicMock, patch

import pytest
from addons.images.bsm_query import bsm_query

from addons.images.bsm_query.bsm_query import create_message, process_message, run


@pytest.fixture
def mock_mongo_client():
    mock_client = MagicMock(spec=MongoClient)
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_client.__getitem__.return_value = mock_db
    mock_db.__getitem__.return_value = mock_collection
    return mock_client


def test_create_message():
    original_message = {
        "payload": {
            "data": {"coreData": {"position": {"longitude": 123.45, "latitude": 67.89}}}
        },
        "metadata": {
            "originIp": "127.0.0.1",
            "odeReceivedAt": "2022-01-01T12:00:00.000Z",
        },
    }

    expected_result = {
        "type": "Feature",
        "geometry": {"type": "Point", "coordinates": [123.45, 67.89]},
        "properties": {"id": "127.0.0.1", "timestamp": datetime(2022, 1, 1, 12, 0, 0)},
    }

    assert create_message(original_message) == expected_result


def test_process_message(mock_mongo_client):
    message = {
        "payload": {
            "data": {"coreData": {"position": {"longitude": 123.45, "latitude": 67.89}}}
        },
        "metadata": {
            "originIp": "127.0.0.1",
            "odeReceivedAt": "2022-01-01T12:00:00.000Z",
        },
    }
    collection_name = "test_collection"

    process_message(message, mock_mongo_client, collection_name)

    mock_collection = mock_mongo_client.__getitem__.return_value
    mock_collection.insert_one.assert_called_once_with(
        {
            "type": "Feature",
            "geometry": {"type": "Point", "coordinates": [123.45, 67.89]},
            "properties": {
                "id": "127.0.0.1",
                "timestamp": datetime(2022, 1, 1, 12, 0, 0),
            },
        }
    )


@patch.dict(
    os.environ,
    {
        "MONGO_DB_URI": "mongodb://localhost:27017",
        "MONGO_DB_NAME": "test_db",
        "MONGO_BSM_INPUT_COLLECTION": "bsm_input",
        "MONGO_GEO_OUTPUT_COLLECTION": "geo_output",
    },
)
@patch("addons.images.bsm_query.bsm_query.ThreadPoolExecutor")
def test_run(mock_thread_pool_executor, mock_mongo_client):
    mock_collection = mock_mongo_client.__getitem__.return_value
    bsm_query.set_mongo_client = MagicMock(
        return_value=[mock_mongo_client, mock_collection]
    )

    mock_stream = MagicMock()

    mock_stream.return_value = "hi"

    mock_stream.__iter__.return_value = [
        {"fullDocument": "document1"},
        {"fullDocument": "document2"},
        {"fullDocument": "document3"},
    ]

    mock_collection.watch.return_value.__enter__.return_value = mock_stream

    bsm_query.run()

    mock_thread_pool_executor.assert_called_once_with(max_workers=5)


if __name__ == "__main__":
    pytest.main()
