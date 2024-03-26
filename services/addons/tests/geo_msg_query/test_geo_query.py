import os
from concurrent.futures import ThreadPoolExecutor
from unittest import mock
from pymongo import MongoClient
from datetime import datetime
from unittest.mock import MagicMock, patch
import json
import pytest
import logging
from images.geo_msg_query import geo_msg_query
from images.geo_msg_query.geo_msg_query import (
    create_message,
    process_message,
    process_topic,
    create_collection_and_indexes,
)


@pytest.fixture
def mock_mongo_client():
    mock_client = MagicMock(spec=MongoClient)
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_client.__getitem__.return_value = mock_db
    mock_db.__getitem__.return_value = mock_collection
    mock_db.list_collection_names.return_value = ["name"]
    mock_db.create_collection.return_value = True
    mock_db.get_collection.return_value = mock_collection
    mock_collection.create_index.return_value = True
    return mock_client


def test_create_collection_and_indexes_already_exists(mock_mongo_client, caplog):
    caplog.set_level(logging.INFO)

    mock_db = mock_mongo_client["item"]

    create_collection_and_indexes(mock_db, "name", 30)

    assert "Database Already Exists" in caplog.text
    assert mock_db.list_collection_names.called


def test_create_collection_and_indexes_creation(mock_mongo_client, caplog):
    caplog.set_level(logging.INFO)

    mock_db = mock_mongo_client["item"]

    create_collection_and_indexes(mock_db, "new_name", 30)

    assert "Collection created." in caplog.text
    assert "Complex index created." in caplog.text
    assert "Message Type index created." in caplog.text
    assert "TTL index created." in caplog.text
    assert mock_db.list_collection_names.called


def test_create_message_bsm():
    original_message = {
        "payload": {"data": {"coreData": {"position": {"longitude": 123.45, "latitude": 67.89}}}},
        "metadata": {
            "originIp": "127.0.0.1",
            "odeReceivedAt": "2022-01-01T12:00:00.000Z",
        },
    }

    expected_result = {
        "type": "Feature",
        "geometry": {"type": "Point", "coordinates": [123.45, 67.89]},
        "properties": {"id": "127.0.0.1", "timestamp": datetime(2022, 1, 1, 12, 0, 0), "msg_type": "Bsm"},
    }

    assert create_message(original_message, "Bsm") == expected_result


def test_create_message_psm():
    original_message = {
        "payload": {"data": {"position": {"longitude": 123.45, "latitude": 67.89}}},
        "metadata": {
            "originIp": "127.0.0.1",
            "odeReceivedAt": "2022-01-01T12:00:00.000Z",
        },
    }

    expected_result = {
        "type": "Feature",
        "geometry": {"type": "Point", "coordinates": [123.45, 67.89]},
        "properties": {"id": "127.0.0.1", "timestamp": datetime(2022, 1, 1, 12, 0, 0), "msg_type": "Psm"},
    }

    assert create_message(original_message, "Psm") == expected_result


@patch("images.geo_msg_query.geo_msg_query.json.loads")
def test_process_message(mock_json_loads):
    message = {
        "payload": {"data": {"coreData": {"position": {"longitude": 123.45, "latitude": 67.89}}}},
        "metadata": {
            "originIp": "127.0.0.1",
            "odeReceivedAt": "2022-01-01T12:00:00.000Z",
        },
    }
    collection_name = "test_collection"

    msg = MagicMock()
    msg.value.decode.return_value = json.dumps(message)
    mock_json_loads.return_value = message

    mock_collection = MagicMock()
    process_message(msg, mock_collection, collection_name, "Bsm")

    mock_collection.insert_one.assert_called_once_with(
        {
            "type": "Feature",
            "geometry": {"type": "Point", "coordinates": [123.45, 67.89]},
            "properties": {"id": "127.0.0.1", "timestamp": datetime(2022, 1, 1, 12, 0, 0), "msg_type": "Bsm"},
        }
    )


@patch("images.geo_msg_query.geo_msg_query.kafka_helper")
@patch("images.geo_msg_query.geo_msg_query.process_message")
def test_process_topic(mock_process_message, mock_kafka_helper, caplog):
    caplog.set_level(logging.INFO)

    mock_executor = MagicMock()
    collection = "test"
    msg = "msg"
    mock_consumer = MagicMock()
    mock_consumer.__iter__.return_value = [msg]
    mock_kafka_helper.create_consumer.return_value = mock_consumer
    process_topic("psM", collection, mock_executor)

    assert "Starting Psm processing service." in caplog.text
    assert "Listening for messages on Kafka topic topic.OdePsmJson..." in caplog.text
    assert mock_executor.submit.called
    mock_executor.submit.assert_called_with(mock_process_message, msg, collection, 0, "Psm")


@patch.dict(
    os.environ,
    {
        "MONGO_DB_URI": "mongodb://localhost:27017",
        "MONGO_DB_NAME": "test_db",
        "GEO_MSG_TYPES": '["bSm","PsM"]',
        "MONGO_GEO_COLLECTION": "geo_output",
        "MONGO_TTL": "30",
    },
)
@patch("images.geo_msg_query.geo_msg_query.ThreadPoolExecutor")
def test_run(mock_thread_pool_executor, mock_mongo_client):
    mock_collection = mock_mongo_client.__getitem__.return_value
    geo_msg_query.set_mongo_client = MagicMock(return_value=[mock_mongo_client, mock_collection])
    geo_msg_query.create_collection_and_indexes = MagicMock(return_value=None)

    geo_msg_query.run()

    mock_thread_pool_executor.assert_called_once_with(max_workers=5)
