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
    set_indexes,
)


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


@patch("logging.info")
def test_set_indexes_empty(mock_logging):
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_db.__getitem__.return_value = mock_collection
    mock_index_info = {}
    mock_collection.index_information.return_value = mock_index_info

    set_indexes(mock_db, "output_collection", "7")

    mock_collection.create_index.assert_any_call(
        [
            ("properties.timestamp", DESCENDING),
            ("properties.msg_type", DESCENDING),
            ("geometry", GEOSPHERE),
        ],
        name="timestamp_geosphere_index",
    )

    mock_collection.create_index.assert_any_call(
        [("properties.timestamp", DESCENDING)],
        name="ttl_index",
        expireAfterSeconds=604800,
    )
    mock_logging.assert_any_call("Creating timestamp_geosphere_index")
    mock_logging.assert_any_call("Creating ttl_index")
    assert mock_logging.call_count == 3


@patch("logging.info")
def test_set_indexes_ttl_recreate(mock_logging):
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_db.__getitem__.return_value = mock_collection
    mock_index_info = {
        "ttl_index": {"expireAfterSeconds": 86400},
        "timestamp_geosphere_index": "timestamp_geosphere_index",
    }
    mock_collection.index_information.return_value = mock_index_info

    set_indexes(mock_db, "output_collection", "7")

    mock_collection.create_index.assert_any_call(
        [("properties.timestamp", DESCENDING)],
        name="ttl_index",
        expireAfterSeconds=604800,
    )
    mock_logging.assert_any_call("timestamp_geosphere_index already exists")
    mock_logging.assert_any_call(
        "ttl_index exists but with different TTL value. Recreating..."
    )
    assert mock_logging.call_count == 3


@patch("logging.info")
def test_set_indexes_exists(mock_logging):
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_db.__getitem__.return_value = mock_collection
    mock_index_info = {
        "ttl_index": {"expireAfterSeconds": 604800},
        "timestamp_geosphere_index": "timestamp_geosphere_index",
    }
    mock_collection.index_information.return_value = mock_index_info

    set_indexes(mock_db, "output_collection", "7")

    mock_logging.assert_any_call("timestamp_geosphere_index already exists")
    mock_logging.assert_any_call("ttl_index already exists with the correct TTL value")
    assert mock_logging.call_count == 3


# watch_collection method unit tests
@patch("logging.debug")
@patch("addons.images.geo_msg_query.geo_msg_query.process_message")
def test_watch_collection_success(mock_process_message, mock_logging):
    geo_msg_query.process_message = mock_process_message

    mock_db = MagicMock()
    mock_input_collection = "OdeBsmJson"
    mock_output_collection = "GeoMsg"
    mock_change = {"fullDocument": {"message": "Test message"}}
    mock_stream = MagicMock()
    mock_stream.__iter__.return_value = [mock_change]
    mock_db.__getitem__.return_value.watch.return_value.__enter__.return_value = (
        mock_stream
    )

    geo_msg_query.watch_collection(
        mock_db, mock_input_collection, mock_output_collection
    )

    mock_process_message.assert_called_once_with(
        mock_change["fullDocument"], mock_db, mock_output_collection, "Bsm"
    )
    mock_logging.assert_any_call("Bsm Count: 1")


@patch("logging.error")
def test_watch_collection_exception(mock_logging):
    mock_db = MagicMock()
    mock_input_collection = "OdeBsmJson"
    mock_output_collection = "GeoMsg"
    mock_error = Exception("Test error")
    mock_db.__getitem__.side_effect = mock_error

    geo_msg_query.watch_collection(
        mock_db, mock_input_collection, mock_output_collection
    )

    mock_logging.assert_any_call(
        "An error occurred while watching collection: OdeBsmJson"
    )
    mock_logging.assert_any_call(str(mock_error))


# run method unit tests
@patch.dict(
    os.environ,
    {
        "MONGO_DB_URI": "mongodb://localhost:27017",
        "MONGO_DB_NAME": "test_db",
        "MONGO_INPUT_COLLECTIONS": "OdeBsmJson,OdePsmJson",
        "MONGO_GEO_OUTPUT_COLLECTION": "GeoMsg",
        "MONGO_TTL": "7",
    },
)
@patch("addons.images.geo_msg_query.geo_msg_query.watch_collection")
@patch("addons.images.geo_msg_query.geo_msg_query.set_indexes")
@patch("addons.images.geo_msg_query.geo_msg_query.set_mongo_client")
@patch("addons.images.geo_msg_query.geo_msg_query.ThreadPoolExecutor")
def test_run(
    mock_thread_pool_executor,
    mock_set_mongo_client,
    mock_set_indexes,
    mock_watch_collection,
):

    mock_db = MagicMock()

    geo_msg_query.set_mongo_client = mock_set_mongo_client
    mock_set_mongo_client.return_value = mock_db
    geo_msg_query.set_indexes = mock_set_indexes
    geo_msg_query.watch_collection = mock_watch_collection

    geo_msg_query.run()

    mock_set_mongo_client.assert_called_with("mongodb://localhost:27017", "test_db")
    mock_set_indexes.assert_called_with(mock_db, "GeoMsg", "7")

    mock_thread_pool_executor.assert_called_once_with(max_workers=5)
    mock_executer = mock_thread_pool_executor.return_value.__enter__.return_value
    mock_executer.submit.assert_any_call(
        mock_watch_collection, mock_db, "OdePsmJson", "GeoMsg"
    )
    mock_executer.submit.assert_any_call(
        mock_watch_collection, mock_db, "OdeBsmJson", "GeoMsg"
    )


@patch("addons.images.geo_msg_query.geo_msg_query.set_indexes")
@patch("addons.images.geo_msg_query.geo_msg_query.set_mongo_client")
@patch("addons.images.geo_msg_query.geo_msg_query.ThreadPoolExecutor")
@patch("logging.error")
def test_run_exit(
    mock_logging, mock_thread_pool_executor, mock_set_mongo_client, mock_set_indexes
):
    with pytest.raises(SystemExit) as e:
        geo_msg_query.run()

    assert str(e.value) == "Environment variables are not set! Exiting."
    mock_logging.assert_any_call("Environment variables are not set! Exiting.")
    mock_thread_pool_executor.assert_not_called()
    mock_set_mongo_client.assert_not_called()
    mock_set_indexes.assert_not_called()
    mock_thread_pool_executor.assert_not_called()


if __name__ == "__main__":
    pytest.main()
