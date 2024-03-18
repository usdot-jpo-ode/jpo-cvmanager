from unittest.mock import patch, MagicMock
import os
from api.src.rsu_bsmdata import query_bsm_data_mongo, bsm_hash
import api.tests.data.rsu_bsmdata_data as rsu_bsmdata_data


def test_bsm_hash():
    result = bsm_hash("192.168.1.1", 1616636734, 123.4567, 234.5678)
    assert result is not None


@patch.dict(
    os.environ, {"MONGO_DB_URI": "uri", "MONGO_DB_NAME": "name", "BSM_DB_NAME": "col"}
)
@patch("api.src.rsu_bsmdata.MongoClient")
def test_query_bsm_data_mongo(mock_mongo):
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_mongo.return_value.__getitem__.return_value = mock_db
    mock_db.__getitem__.return_value = mock_collection
    mock_db.validate_collection.return_value = "valid"

    mock_collection.find.return_value = rsu_bsmdata_data.mongo_bsm_data_response

    start = "2023-07-01T00:00:00Z"
    end = "2023-07-02T00:00:00Z"
    response, code = query_bsm_data_mongo(rsu_bsmdata_data.point_list, start, end)
    expected_response = rsu_bsmdata_data.processed_bsm_message_data

    mock_mongo.assert_called()
    mock_collection.find.assert_called()
    assert code == 200
    assert response == expected_response


@patch.dict(
    os.environ, {"MONGO_DB_URI": "uri", "MONGO_DB_NAME": "name", "BSM_DB_NAME": "col"}
)
@patch("api.src.rsu_bsmdata.MongoClient")
def test_query_bsm_data_mongo_filter_failed(mock_mongo):
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_mongo.return_value.__getitem__.return_value = mock_db
    mock_db.__getitem__.return_value = mock_collection
    mock_db.validate_collection.return_value = "valid"

    mock_collection.find.side_effect = Exception("Failed to find")

    start = "2023-07-01T00:00:00Z"
    end = "2023-07-02T00:00:00Z"
    response, code = query_bsm_data_mongo(rsu_bsmdata_data.point_list, start, end)
    expected_response = []

    mock_mongo.assert_called()
    mock_collection.find.assert_called()
    assert code == 500
    assert response == expected_response


@patch.dict(
    os.environ, {"MONGO_DB_URI": "uri", "MONGO_DB_NAME": "name", "BSM_DB_NAME": "col"}
)
@patch("api.src.rsu_bsmdata.MongoClient")
def test_query_bsm_data_mongo_failed_to_connect(mock_mongo):
    mock_mongo.side_effect = Exception("Failed to connect")

    start = "2023-07-01T00:00:00Z"
    end = "2023-07-02T00:00:00Z"
    response, code = query_bsm_data_mongo(rsu_bsmdata_data.point_list, start, end)
    expected_response = []

    mock_mongo.assert_called()
    assert code == 503
    assert response == expected_response
