import os
from unittest.mock import patch, MagicMock
import api.src.rsu_ssm_srm as rsu_ssm_srm
import api.tests.data.rsu_ssm_srm_data as ssm_srm_data
from datetime import datetime
from pytz import UTC


# #################################### Testing Requests ###########################################
def test_options_request():
    counts = rsu_ssm_srm.RsuSsmSrmData()
    (body, code, headers) = counts.options()
    assert body == ""
    assert code == 204
    assert headers["Access-Control-Allow-Methods"] == "GET"


@patch.dict(
    os.environ,
    {
        "MONGO_DB_URI": "uri",
        "MONGO_DB_NAME": "db",
        "SSM_DB_NAME": "collection",
        "SRM_DB_NAME": "srm_collection",
    },
)
@patch("api.src.rsu_ssm_srm.query_ssm_data_mongo")
@patch("api.src.rsu_ssm_srm.query_srm_data_mongo")
def test_get_request(mock_srm, mock_ssm):
    ssm_srm = rsu_ssm_srm.RsuSsmSrmData()
    mock_ssm.return_value = []
    mock_srm.return_value = [
        ssm_srm_data.srm_processed_one,
        ssm_srm_data.srm_processed_two,
        ssm_srm_data.srm_processed_one,
        ssm_srm_data.srm_processed_three,
    ]
    (data, code, headers) = ssm_srm.get()
    assert code == 200
    assert headers["Access-Control-Allow-Origin"] == "test.com"
    assert headers["Content-Type"] == "application/json"
    assert data == [
        ssm_srm_data.srm_processed_two,
        ssm_srm_data.srm_processed_one,
        ssm_srm_data.srm_processed_one,
        ssm_srm_data.srm_processed_three,
    ]


# ################################### Test query_ssm_data ########################################
@patch.dict(
    os.environ,
    {"MONGO_DB_URI": "uri", "MONGO_DB_NAME": "db", "SSM_DB_NAME": "collection"},
)
@patch("api.src.rsu_ssm_srm.MongoClient")
@patch("api.src.rsu_ssm_srm.datetime")
def test_query_ssm_data_query(mock_date, mock_mongo):
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_mongo.return_value.__getitem__.return_value = mock_db
    mock_db.__getitem__.return_value = mock_collection

    mock_collection.find.return_value = []

    mock_date.now.return_value = datetime.strptime(
        "2022/12/14 00:00:00", "%Y/%m/%d %H:%M:%S"
    ).astimezone(UTC)

    rsu_ssm_srm.query_ssm_data_mongo()

    mock_mongo.assert_called()
    mock_collection.find.assert_called()


@patch.dict(
    os.environ,
    {"MONGO_DB_URI": "uri", "MONGO_DB_NAME": "db", "SSM_DB_NAME": "collection"},
)
@patch("api.src.rsu_ssm_srm.MongoClient")
def test_query_ssm_data_no_data(mock_mongo):
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_mongo.return_value.__getitem__.return_value = mock_db
    mock_db.__getitem__.return_value = mock_collection

    mock_collection.find.return_value = []
    with patch.dict("api.src.rsu_ssm_srm.os.environ", {"SSM_DB_NAME": "Fake_table"}):
        data = rsu_ssm_srm.query_ssm_data_mongo()
        assert data == []


@patch.dict(
    os.environ,
    {"MONGO_DB_URI": "uri", "MONGO_DB_NAME": "db", "SSM_DB_NAME": "collection"},
)
@patch("api.src.rsu_ssm_srm.MongoClient")
def test_query_ssm_data_single_result(mock_mongo):
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_mongo.return_value.__getitem__.return_value = mock_db
    mock_db.__getitem__.return_value = mock_collection

    mock_collection.find.return_value = [ssm_srm_data.ssm_record_one]
    with patch.dict("api.src.rsu_ssm_srm.os.environ", {"SSM_DB_NAME": "Fake_table"}):
        data = rsu_ssm_srm.query_ssm_data_mongo()
        assert data == ssm_srm_data.ssm_single_result_expected


@patch.dict(
    os.environ,
    {"MONGO_DB_URI": "uri", "MONGO_DB_NAME": "db", "SSM_DB_NAME": "collection"},
)
@patch("api.src.rsu_ssm_srm.MongoClient")
def test_query_ssm_data_multiple_result(mock_mongo):
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_mongo.return_value.__getitem__.return_value = mock_db
    mock_db.__getitem__.return_value = mock_collection

    mock_collection.find.return_value = [
        ssm_srm_data.ssm_record_one,
        ssm_srm_data.ssm_record_two,
        ssm_srm_data.ssm_record_three,
    ]
    with patch.dict("api.src.rsu_ssm_srm.os.environ", {"SSM_DB_NAME": "Fake_table"}):
        data = rsu_ssm_srm.query_ssm_data_mongo()
        assert data == ssm_srm_data.ssm_multiple_result_expected


# #################################### Test query_srm_data ###########################################
@patch.dict(
    os.environ,
    {"MONGO_DB_URI": "uri", "MONGO_DB_NAME": "db", "SRM_DB_NAME": "collection"},
)
@patch("api.src.rsu_ssm_srm.MongoClient")
@patch("api.src.rsu_ssm_srm.datetime")
def test_query_srm_data_query(mock_date, mock_mongo):
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_mongo.return_value.__getitem__.return_value = mock_db
    mock_db.__getitem__.return_value = mock_collection

    mock_collection.find.return_value = []
    mock_date.now.return_value = datetime.strptime(
        "2022/12/14 00:00:00", "%Y/%m/%d %H:%M:%S"
    ).astimezone(UTC)
    with patch.dict("api.src.rsu_ssm_srm.os.environ", {"SRM_DB_NAME": "Fake_table"}):
        rsu_ssm_srm.query_srm_data_mongo()
        mock_mongo.assert_called()
        mock_collection.find.assert_called()


@patch.dict(
    os.environ,
    {"MONGO_DB_URI": "uri", "MONGO_DB_NAME": "db", "SRM_DB_NAME": "collection"},
)
@patch("api.src.rsu_ssm_srm.MongoClient")
def test_query_srm_data_no_data(mock_mongo):
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_mongo.return_value.__getitem__.return_value = mock_db
    mock_db.__getitem__.return_value = mock_collection

    mock_collection.find.return_value = []
    with patch.dict("api.src.rsu_ssm_srm.os.environ", {"SRM_DB_NAME": "Fake_table"}):
        data = rsu_ssm_srm.query_srm_data_mongo()
        assert data == []


@patch.dict(
    os.environ,
    {"MONGO_DB_URI": "uri", "MONGO_DB_NAME": "db", "SRM_DB_NAME": "collection"},
)
@patch("api.src.rsu_ssm_srm.MongoClient")
def test_query_srm_data_single_result(mock_mongo):
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_mongo.return_value.__getitem__.return_value = mock_db
    mock_db.__getitem__.return_value = mock_collection

    mock_collection.find.return_value = [ssm_srm_data.srm_record_one]
    with patch.dict("api.src.rsu_ssm_srm.os.environ", {"SRM_DB_NAME": "Fake_table"}):
        data = rsu_ssm_srm.query_srm_data_mongo()
        assert data == ssm_srm_data.srm_single_result_expected


@patch.dict(
    os.environ,
    {"MONGO_DB_URI": "uri", "MONGO_DB_NAME": "db", "SRM_DB_NAME": "collection"},
)
@patch("api.src.rsu_ssm_srm.MongoClient")
def test_query_srm_data_multiple_result(mock_mongo):
    mock_db = MagicMock()
    mock_collection = MagicMock()
    mock_mongo.return_value.__getitem__.return_value = mock_db
    mock_db.__getitem__.return_value = mock_collection

    mock_collection.find.return_value = [
        ssm_srm_data.srm_record_one,
        ssm_srm_data.srm_record_two,
        ssm_srm_data.srm_record_three,
    ]
    with patch.dict("api.src.rsu_ssm_srm.os.environ", {"SRM_DB_NAME": "Fake_table"}):
        data = rsu_ssm_srm.query_srm_data_mongo()
        assert data == ssm_srm_data.srm_multiple_result_expected
