from unittest.mock import patch, MagicMock
import os
from api.src.rsu_geo_msg_query import query_geo_data_mongo, geo_hash
import api.tests.data.rsu_geo_msg_query_data as rsu_geo_msg_query_data


def test_geo_hash():
    result = geo_hash("192.168.1.1", 1616636734, 123.4567, 234.5678)
    assert result is not None


@patch.dict(
    os.environ, {"MONGO_DB_URI": "uri", "MONGO_DB_NAME": "name", "GEO_DB_NAME": "col"}
)
@patch("api.src.rsu_geo_msg_query.MongoClient")
def test_query_geo_data_mongo(mock_mongo):
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_mongo.return_value.__getitem__.return_value = mock_db
    mock_db.__getitem__.return_value = mock_collection

    mock_collection.find.return_value = iter(
        rsu_geo_msg_query_data.mongo_geo_data_response
    )

    start = "2023-07-01T00:00:00Z"
    end = "2023-07-02T00:00:00Z"
    response, code = query_geo_data_mongo(rsu_geo_msg_query_data.point_list, start, end)
    expected_response = rsu_geo_msg_query_data.processed_geo_message_data

    mock_mongo.assert_called()
    mock_collection.find.assert_called()
    assert code == 200
    assert response == expected_response


@patch.dict(
    os.environ, {"MONGO_DB_URI": "uri", "MONGO_DB_NAME": "name", "GEO_DB_NAME": "col"}
)
@patch("api.src.rsu_geo_msg_query.MongoClient")
def test_query_geo_data_mongo_filter_failed(mock_mongo):
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_mongo.return_value.__getitem__.return_value = mock_db
    mock_db.__getitem__.return_value = mock_collection
    mock_db.validate_collection.return_value = "valid"

    mock_collection.find.side_effect = Exception("Failed to find")

    start = "2023-07-01T00:00:00Z"
    end = "2023-07-02T00:00:00Z"
    response, code = query_geo_data_mongo(rsu_geo_msg_query_data.point_list, start, end)
    expected_response = []

    mock_mongo.assert_called()
    mock_collection.find.assert_called()
    assert code == 500
    assert response == expected_response


@patch.dict(
    os.environ, {"MONGO_DB_URI": "uri", "MONGO_DB_NAME": "name", "GEO_DB_NAME": "col"}
)
@patch("api.src.rsu_geo_msg_query.MongoClient")
def test_query_geo_data_mongo_failed_to_connect(mock_mongo):
    mock_mongo.side_effect = Exception("Failed to connect")

    start = "2023-07-01T00:00:00Z"
    end = "2023-07-02T00:00:00Z"
    response, code = query_geo_data_mongo(rsu_geo_msg_query_data.point_list, start, end)
    expected_response = []

    mock_mongo.assert_called()
    assert code == 503
    assert response == expected_response
