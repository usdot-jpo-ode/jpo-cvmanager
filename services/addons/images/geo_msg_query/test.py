from unittest.mock import MagicMock, patch
from pymongo import DESCENDING, GEOSPHERE
from geo_msg_query import set_indexes


@patch("geo_msg_query.logging")
def test_set_indexes_create_new_indexes(mock_logging):
    # Mock the necessary objects
    db = MagicMock()
    collection = "output_collection"
    mongo_ttl = "7"

    # Mock the index_information method to return an empty dictionary
    output_collection_obj = MagicMock()
    output_collection_obj.index_information.return_value = {}
    db[collection] = output_collection_obj

    # Call the set_indexes function
    set_indexes(db, collection, mongo_ttl)

    # Assert that the expected indexes are created
    output_collection_obj.create_index.assert_any_call(
        [
            ("properties.timestamp", DESCENDING),
            ("properties.msg_type", DESCENDING),
            ("geometry", GEOSPHERE),
        ],
        name="timestamp_geosphere_index",
    )
    output_collection_obj.create_index.assert_any_call(
        [("properties.timestamp", DESCENDING)],
        name="ttl_index",
        expireAfterSeconds=7 * 24 * 60 * 60,
    )

    # Assert that the logging messages are called correctly
    mock_logging.info.assert_any_call("Creating indexes for the output collection")
    mock_logging.info.assert_any_call("Creating timestamp_geosphere_index")
    mock_logging.info.assert_any_call("Creating ttl_index")


@patch("geo_msg_query.logging")
def test_set_indexes_existing_indexes_with_same_ttl(mock_logging):
    # Mock the necessary objects
    db = MagicMock()
    collection = "output_collection"
    mongo_ttl = "7"

    # Mock the index_information method to return the existing indexes
    output_collection_obj = MagicMock()
    output_collection_obj.index_information.return_value = {
        "timestamp_geosphere_index": {},
        "ttl_index": {"expireAfterSeconds": 7 * 24 * 60 * 60},
    }
    db[collection] = output_collection_obj

    # Call the set_indexes function
    set_indexes(db, collection, mongo_ttl)

    # Assert that the existing indexes are not recreated
    output_collection_obj.create_index.assert_not_called()

    # Assert that the logging messages are called correctly
    mock_logging.info.assert_any_call("Creating indexes for the output collection")
    mock_logging.info.assert_any_call("timestamp_geosphere_index already exists")
    mock_logging.info.assert_any_call(
        "ttl_index already exists with the correct TTL value"
    )


@patch("geo_msg_query.logging")
def test_set_indexes_existing_indexes_with_different_ttl(mock_logging):
    # Mock the necessary objects
    db = MagicMock()
    collection = "output_collection"
    mongo_ttl = "7"

    # Mock the index_information method to return the existing indexes with different TTL
    output_collection_obj = MagicMock()
    output_collection_obj.index_information.return_value = {
        "timestamp_geosphere_index": {},
        "ttl_index": {"expireAfterSeconds": 10 * 24 * 60 * 60},
    }
    db[collection] = output_collection_obj

    # Call the set_indexes function
    set_indexes(db, collection, mongo_ttl)

    # Assert that the existing ttl_index is dropped and recreated with the correct TTL
    output_collection_obj.drop_index.assert_called_with("ttl_index")
    output_collection_obj.create_index.assert_called_with(
        [("properties.timestamp", DESCENDING)],
        name="ttl_index",
        expireAfterSeconds=7 * 24 * 60 * 60,
    )

    # Assert that the logging messages are called correctly
    mock_logging.info.assert_any_call("Creating indexes for the output collection")
    mock_logging.info.assert_any_call("timestamp_geosphere_index already exists")
    mock_logging.info.assert_any_call(
        "ttl_index exists but with different TTL value. Recreating..."
    )
