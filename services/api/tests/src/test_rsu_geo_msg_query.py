from copy import deepcopy
from datetime import datetime, timedelta
from unittest.mock import patch, MagicMock
import os
from api.src.rsu_geo_msg_query import (
    query_geo_data_mongo,
    geo_hash,
    get_collection,
    create_geo_filter,
)
import json
import api.tests.data.rsu_geo_msg_query_data as rsu_geo_msg_query_data


def test_geo_hash():
    result = geo_hash("test_id_001", 1616636734, 123.4567, 234.5678)
    assert result is not None


@patch.dict(
    os.environ,
    {
        "MONGO_DB_URI": "uri",
        "MONGO_DB_NAME": "name",
        "MONGO_PROCESSED_BSM_COLLECTION_NAME": "col",
        "MAX_GEO_QUERY_RECORDS": "10000",
    },
)
@patch("api.src.rsu_geo_msg_query.MongoClient")
def test_query_geo_data_mongo_bsm(mock_mongo):
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_mongo.return_value.__getitem__.return_value = mock_db
    mock_db.__getitem__.return_value = mock_collection

    mongo_bsm_response = deepcopy(rsu_geo_msg_query_data.mongo_geo_bsm_data_response)
    mock_collection.find.return_value = iter(mongo_bsm_response)

    start = "2023-07-01T00:00:00Z"
    end = "2023-07-02T00:00:00Z"
    response = query_geo_data_mongo(
        rsu_geo_msg_query_data.point_list, start, end, "bSm"
    )

    expected_response = deepcopy(rsu_geo_msg_query_data.mongo_geo_bsm_data_response)
    expected_response[0].pop("_id")
    expected_response[0].pop("recordGeneratedAt")

    mock_mongo.assert_called()
    mock_collection.find.assert_called()

    # Compare each field in the response
    assert len(response) == len(expected_response)
    assert response == expected_response


@patch.dict(
    os.environ,
    {
        "MONGO_DB_URI": "uri",
        "MONGO_DB_NAME": "name",
        "MONGO_PROCESSED_BSM_COLLECTION_NAME": "col",
        "MAX_GEO_QUERY_RECORDS": "10000",
    },
)
@patch("api.src.rsu_geo_msg_query.MongoClient")
def test_query_geo_data_mongo_filter_failed(mock_mongo):
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_mongo.return_value.__getitem__.return_value = mock_db
    mock_db.__getitem__.return_value = mock_collection

    mock_collection.find.side_effect = Exception("Failed to find")

    start = "2023-07-01T00:00:00Z"
    end = "2023-07-02T00:00:00Z"
    response, code = query_geo_data_mongo(
        rsu_geo_msg_query_data.point_list, start, end, "BsM"
    )
    expected_response = []

    mock_mongo.assert_called()
    mock_collection.find.assert_called()
    assert code == 500
    assert response == expected_response


@patch.dict(
    os.environ,
    {
        "MONGO_DB_URI": "uri",
        "MONGO_DB_NAME": "name",
        "MONGO_PROCESSED_BSM_COLLECTION_NAME": "col",
    },
)
@patch("api.src.rsu_geo_msg_query.MongoClient")
def test_query_geo_data_mongo_failed_to_connect(mock_mongo):
    mock_mongo.side_effect = Exception("Failed to connect")

    start = "2023-07-01T00:00:00Z"
    end = "2023-07-02T00:00:00Z"
    response, code = query_geo_data_mongo(
        rsu_geo_msg_query_data.point_list, start, end, "bsM"
    )
    expected_response = []

    mock_mongo.assert_called()
    assert code == 503
    assert response == expected_response


@patch.dict(
    os.environ,
    {
        "MONGO_DB_URI": "uri",
        "MONGO_DB_NAME": "name",
        "MONGO_PROCESSED_PSM_COLLECTION_NAME": "col",
        "MAX_GEO_QUERY_RECORDS": "10000",
    },
)
@patch("api.src.rsu_geo_msg_query.MongoClient")
def test_query_geo_data_mongo_psm(mock_mongo):
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_mongo.return_value.__getitem__.return_value = mock_db
    mock_db.__getitem__.return_value = mock_collection

    mongo_psm_response = deepcopy(rsu_geo_msg_query_data.mongo_geo_psm_data_response)
    mock_collection.find.return_value = iter(mongo_psm_response)

    start = "2023-07-01T00:00:00Z"
    end = "2023-07-02T00:00:00Z"
    response = query_geo_data_mongo(
        rsu_geo_msg_query_data.point_list, start, end, "PsM"
    )
    expected_response = deepcopy(rsu_geo_msg_query_data.mongo_geo_psm_data_response)
    expected_response[0].pop("_id")
    expected_response[0].pop("recordGeneratedAt")

    mock_mongo.assert_called()
    mock_collection.find.assert_called()

    # Compare each field in the response
    assert len(response) == len(expected_response)
    for resp, exp in zip(response, expected_response):
        assert resp["type"] == exp["type"]
        assert resp["geometry"] == exp["geometry"]
        assert resp["properties"] == exp["properties"]


@patch.dict(
    os.environ,
    {
        "MONGO_DB_URI": "uri",
        "MONGO_DB_NAME": "name",
        "MAX_GEO_QUERY_RECORDS": "10000",
    },
)
@patch("api.src.rsu_geo_msg_query.MongoClient")
def test_query_geo_data_mongo_unsupported_msg_type(mock_mongo):
    start = "2023-07-01T00:00:00Z"
    end = "2023-07-02T00:00:00Z"
    response, code = query_geo_data_mongo(
        rsu_geo_msg_query_data.point_list, start, end, "msg_type"
    )

    assert code == 400
    assert response == []


@patch.dict(
    os.environ,
    {
        "MONGO_DB_URI": "uri",
        "MONGO_DB_NAME": "name",
        "MONGO_PROCESSED_BSM_COLLECTION_NAME": "bsm_col",
        "MONGO_PROCESSED_PSM_COLLECTION_NAME": "psm_col",
    },
)
@patch("api.src.rsu_geo_msg_query.MongoClient")
def test_get_collection(mock_mongo):
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_mongo.return_value.__getitem__.return_value = mock_db
    mock_db.__getitem__.return_value = mock_collection

    # Test BSM collection
    collection, name, code = get_collection("BSM")
    assert collection == mock_collection
    assert name == "bsm_col"
    assert code == 200

    # Test PSM collection
    collection, name, code = get_collection("PSM")
    assert collection == mock_collection
    assert name == "psm_col"
    assert code == 200

    # Test invalid message type
    collection, name, code = get_collection("INVALID")
    assert collection is None
    assert name is None
    assert code == 400

    # Test connection failure
    mock_mongo.side_effect = Exception("Connection failed")
    collection, name, code = get_collection("BSM")
    assert collection is None
    assert name is None
    assert code == 503


def test_create_geo_filter():
    point_list = [[1.0, 2.0], [3.0, 4.0], [5.0, 6.0]]
    start = "2023-07-01T00:00:00Z"
    end = "2023-07-02T00:00:00Z"

    filter = create_geo_filter(point_list, start, end)

    assert filter["properties.timeStamp"]["$gte"] == "2023-07-01T00:00:00Z"
    assert filter["properties.timeStamp"]["$lte"] == "2023-07-02T00:00:00Z"
    assert filter["geometry"]["$geoWithin"]["$geometry"]["type"] == "Polygon"
    assert filter["geometry"]["$geoWithin"]["$geometry"]["coordinates"] == [point_list]


@patch.dict(
    os.environ,
    {
        "MONGO_DB_URI": "uri",
        "MONGO_DB_NAME": "name",
        "MONGO_PROCESSED_BSM_COLLECTION_NAME": "col",
        "MAX_GEO_QUERY_RECORDS": "10000",
    },
)
@patch("api.src.rsu_geo_msg_query.MongoClient")
def test_query_geo_data_mongo_schema_version_filter(mock_mongo):
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_mongo.return_value.__getitem__.return_value = mock_db
    mock_db.__getitem__.return_value = mock_collection

    mock_collection.find.return_value = iter(
        rsu_geo_msg_query_data.geo_msg_data_new_schema
    )
    mock_collection.count_documents.return_value = len(
        rsu_geo_msg_query_data.geo_msg_data_new_schema
    )

    start = "2023-07-01T00:00:00Z"
    end = "2023-07-02T00:00:00Z"
    response = query_geo_data_mongo(
        rsu_geo_msg_query_data.point_list, start, end, "BSM"
    )

    # assert that the other schema versions are not processed
    assert len(response) == 0


# checks deduplication and sorting by timestamp
@patch.dict(
    os.environ,
    {
        "MONGO_DB_URI": "uri",
        "MONGO_DB_NAME": "name",
        "MONGO_PROCESSED_BSM_COLLECTION_NAME": "col",
        "MAX_GEO_QUERY_RECORDS": "10000",
    },
)
@patch("api.src.rsu_geo_msg_query.MongoClient")
def test_order_by_time_stamp(mock_mongo):
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_mongo.return_value.__getitem__.return_value = mock_db
    mock_db.__getitem__.return_value = mock_collection

    mock_collection.find.return_value = iter(
        rsu_geo_msg_query_data.mongo_geo_data_not_time_sorted
    )

    start = "2023-07-01T00:00:00Z"
    end = "2023-07-02T00:00:00Z"
    response = query_geo_data_mongo(
        rsu_geo_msg_query_data.point_list, start, end, "BSM"
    )

    assert len(response) == 3
    assert (
        response[0]["properties"]["timeStamp"] < response[1]["properties"]["timeStamp"]
    )


@patch.dict(
    os.environ,
    {
        "MONGO_DB_URI": "uri",
        "MONGO_DB_NAME": "name",
        "MONGO_PROCESSED_BSM_COLLECTION_NAME": "col",
        "MAX_GEO_QUERY_RECORDS": "5",
    },
)
@patch("api.src.rsu_geo_msg_query.MongoClient")
def test_query_limit(mock_mongo):
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_mongo.return_value.__getitem__.return_value = mock_db
    mock_db.__getitem__.return_value = mock_collection

    # create 10 records with 10 minute intervals
    data = []
    for i in range(10):
        temp = deepcopy(rsu_geo_msg_query_data.mongo_geo_bsm_data_response[0])
        last_time_stamp = datetime.strptime(
            temp["properties"]["timeStamp"], "%Y-%m-%dT%H:%M:%SZ"
        )
        new_time_stamp = last_time_stamp + timedelta(seconds=10 * i)
        temp["properties"]["timeStamp"] = new_time_stamp.strftime("%Y-%m-%dT%H:%M:%SZ")
        data.append(temp)

    mock_collection.find.return_value = iter(data)

    start = "2023-07-01T00:00:00Z"
    end = "2023-07-02T00:00:00Z"
    response = query_geo_data_mongo(
        rsu_geo_msg_query_data.point_list, start, end, "BSM"
    )
    # assert that the 5 extra records are not returned
    assert len(response) == 5
