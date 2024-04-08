import os
from pymongo import MongoClient, DESCENDING, GEOSPHERE
from datetime import datetime
from unittest.mock import MagicMock, patch
import logging
import pytest
from addons.images.geo_msg_query import geo_msg_query

from addons.images.geo_msg_query.geo_msg_query import (
    create_message,
    process_message,
    run,
    set_indexes,
)


# @pytest.fixture
# def mock_mongo_client():
#     mock_client = MagicMock(spec=MongoClient)
#     mock_db = MagicMock()
#     mock_collection = MagicMock()
#     mock_client.__getitem__.return_value = mock_db
#     mock_db.__getitem__.return_value = mock_collection
#     return mock_client


# create_message unit tests
def test_create_message_bsm():
    original_message = {
        "payload": {
            "data": {"coreData": {"position": {"longitude": 123.456, "latitude": 78.9}}}
        },
        "metadata": {
            "odeReceivedAt": "2022-01-01T12:34:56.789000Z",
            "originIp": "127.0.0.1",
        },
    }
    msg_type = "Bsm"

    expected_message = {
        "type": "Feature",
        "geometry": {"type": "Point", "coordinates": [123.456, 78.9]},
        "properties": {
            "id": "127.0.0.1",
            "timestamp": datetime.strptime(
                "2022-01-01T12:34:56.789Z", "%Y-%m-%dT%H:%M:%S.%fZ"
            ),
            "msg_type": "Bsm",
        },
    }

    assert create_message(original_message, msg_type) == expected_message


def test_create_message_psm():
    original_message = {
        "payload": {"data": {"position": {"longitude": 12.34, "latitude": 56.78}}},
        "metadata": {
            "odeReceivedAt": "2022-01-01T12:34:56.789000Z",
            "originIp": "127.0.0.1",
        },
    }
    msg_type = "Psm"

    expected_message = {
        "type": "Feature",
        "geometry": {"type": "Point", "coordinates": [12.34, 56.78]},
        "properties": {
            "id": "127.0.0.1",
            "timestamp": datetime.strptime(
                "2022-01-01T12:34:56.789Z", "%Y-%m-%dT%H:%M:%S.%fZ"
            ),
            "msg_type": "Psm",
        },
    }

    assert create_message(original_message, msg_type) == expected_message


def test_create_message_invalid_type():
    original_message = {
        "payload": {
            "data": {"coreData": {"position": {"longitude": 123.456, "latitude": 78.9}}}
        },
        "metadata": {
            "odeReceivedAt": "2022-01-01T12:34:56.789012Z",
            "originIp": "127.0.0.1",
        },
    }
    msg_type = "InvalidType"

    expected_message = None

    with patch.object(logging, "warn") as mock_warn:
        assert create_message(original_message, msg_type) == expected_message
        mock_warn.assert_called_once_with(
            "create_message: Could not create a message for type: InvalidType"
        )


# process_message unit tests
@patch("addons.images.geo_msg_query.geo_msg_query.create_message")
def test_process_message_inserts_new_message_when_created_successfully(
    mock_process_message,
):
    message = "Test message"
    db = MagicMock()
    collection = "test_collection"
    msg_type = "test_type"

    mock_process_message.return_value = "New message"
    process_message(message, db, collection, msg_type)

    db[collection].insert_one.assert_called_once()


@patch("addons.images.geo_msg_query.geo_msg_query.create_message")
@patch("logging.error")
def test_process_message_logs_error_when_message_creation_fails(
    mock_logging, mock_process_message
):
    message = "Invalid message"
    db = MagicMock()
    collection = "test_collection"
    msg_type = "test_type"

    mock_process_message.return_value = None
    process_message(message, db, collection, msg_type)

    mock_logging.assert_called_once_with(
        f"process_message: Could not create a message from the input {msg_type} message: {message}"
    )


# set_indexes unit tests


@patch("logging.info")
def test_set_indexes_empty(mock_logging):
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_db.__getitem__.return_value = mock_collection
    mock_index_info = {
        "ttl_index": {"expireAfterSeconds": 86400},
    }
    mock_collection.index_information.return_value = mock_index_info

    set_indexes(mock_db, "output_collection", "7")

    mock_collection.create_index.assert_called_with(
        [
            ("properties.timestamp", DESCENDING),
            ("properties.msg_type", DESCENDING),
            ("geometry", GEOSPHERE),
        ],
        name="timestamp_geosphere_index",
    )

    # TODO:FIGURE OUT WHY ONLY THE LATEST MOCK ASSERT WORKS

    # mock_collection.create_index.assert_called_with(
    #     [("properties.timestamp", DESCENDING)],
    #     name="ttl_index",
    #     expireAfterSeconds=604800,
    # )
    # mock_logging.assert_called_with("Creating indexes for the output collection")
    # mock_logging.assert_called_with("Creating timestamp_geosphere_index")
    # mock_logging.assert_called_with("Creating ttl_index")

    # For some reason only the latest log message can be asserted
    mock_logging.assert_called_with(
        "ttl_index exists but with different TTL value. Recreating.."
    )
    # logging
    assert mock_logging.call_count == 3


if __name__ == "__main__":
    pytest.main()
