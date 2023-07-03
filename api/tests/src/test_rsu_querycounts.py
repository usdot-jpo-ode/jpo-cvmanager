from unittest.mock import patch, MagicMock
import pytest
import os
import src.rsu_querycounts as rsu_querycounts
from src.rsu_querycounts import query_rsu_counts_mongo
import tests.data.rsu_querycounts_data as querycounts_data
from datetime import datetime

##################################### Testing Requests ###########################################

def test_options_request():
    counts = rsu_querycounts.RsuQueryCounts()
    (body, code, headers)= counts.options()
    assert body == ''
    assert code == 204
    assert headers['Access-Control-Allow-Methods'] == 'GET'

@patch.dict(os.environ, {
    'MSG_COUNTS_DB': 'BIGQUERY'
})
@patch('src.rsu_querycounts.get_organization_rsus')
@patch('src.rsu_querycounts.query_rsu_counts_bq')
def test_get_request_bq(mock_query, mock_rsus):
    req = MagicMock()
    req.args = querycounts_data.request_args_good
    req.environ = querycounts_data.request_params_good
    counts = rsu_querycounts.RsuQueryCounts()
    mock_rsus.return_value = ['10.0.0.1', '10.0.0.2', '10.0.0.3']
    mock_query.return_value = {"Some Data"}, 200
    with patch("src.rsu_querycounts.request", req):
        (data, code, headers) = counts.get()
        assert code == 200
        assert headers['Access-Control-Allow-Origin'] == "*"
        assert headers['Content-Type'] == "application/json"
        assert data == {"Some Data"}

@patch.dict(os.environ, {
    'MSG_COUNTS_DB': 'MONGODB'
})
@patch('src.rsu_querycounts.get_organization_rsus')
@patch('src.rsu_querycounts.query_rsu_counts_mongo')
def test_get_request_mongo(mock_query, mock_rsus):
    req = MagicMock()
    req.args = querycounts_data.request_args_good
    req.environ = querycounts_data.request_params_good
    counts = rsu_querycounts.RsuQueryCounts()
    mock_rsus.return_value = ['10.0.0.1', '10.0.0.2', '10.0.0.3']
    mock_query.return_value = {"Some Data"}, 200
    with patch("src.rsu_querycounts.request", req):
        (data, code, headers) = counts.get()
        assert code == 200
        assert headers['Access-Control-Allow-Origin'] == "*"
        assert headers['Content-Type'] == "application/json"
        assert data == {"Some Data"}

################################### Testing Data Validation #########################################

def test_get_request_invalid_message():
    req = MagicMock()
    req.args = querycounts_data.request_args_bad_message
    counts = rsu_querycounts.RsuQueryCounts()
    with patch("src.rsu_querycounts.request", req):
        (data, code, headers)= counts.get()
        assert code == 400
        assert headers['Access-Control-Allow-Origin'] == "*"
        assert data == "Invalid Message Type.\nValid message types: SSM, BSM, SPAT, SRM, MAP"

def test_schema_validate_bad_data():
    req = MagicMock()
    req.args = querycounts_data.request_args_bad_type
    counts = rsu_querycounts.RsuQueryCounts()
    with patch("src.rsu_querycounts.request", req):
        with pytest.raises(Exception):
            assert counts.get()

################################### Test get_organization_rsus ########################################

@patch('src.rsu_querycounts.pgquery')
def test_rsu_counts_get_organization_rsus(mock_pgquery):
    mock_pgquery.query_db.return_value = [
        ({'ip': '10.11.81.12'},),
        ({'ip': '10.11.81.13'},), 
        ({'ip': '10.11.81.14'},)
        ]
    expected_query = "SELECT jsonb_build_object('ip', rd.ipv4_address) " \
                    "FROM public.rsus AS rd " \
                    "JOIN public.rsu_organization_name AS ron_v ON ron_v.rsu_id = rd.rsu_id " \
                    f"WHERE ron_v.name = 'Test' " \
                    "ORDER BY rd.ipv4_address"
    actual_result = rsu_querycounts.get_organization_rsus('Test')
    mock_pgquery.query_db.assert_called_with(expected_query)

    assert actual_result == ['10.11.81.12', '10.11.81.13', '10.11.81.14']

@patch('src.rsu_querycounts.pgquery')
def test_rsu_counts_get_organization_rsus_empty(mock_pgquery):
    mock_pgquery.query_db.return_value = []
    expected_query = "SELECT jsonb_build_object('ip', rd.ipv4_address) " \
                    "FROM public.rsus AS rd " \
                    "JOIN public.rsu_organization_name AS ron_v ON ron_v.rsu_id = rd.rsu_id " \
                    f"WHERE ron_v.name = 'Test' " \
                    "ORDER BY rd.ipv4_address"
    actual_result = rsu_querycounts.get_organization_rsus('Test')
    mock_pgquery.query_db.assert_called_with(expected_query)

    assert actual_result == []

##################################### Test query_rsu_counts ###########################################
@patch.dict(os.environ, {
    'MONGO_DB_URI': 'uri',
    'MONGO_DB_NAME': 'name',
    'MONGO_COUNTS_COLLECTION': 'col'
})
@patch('pymongo.collection.Collection.find')
def test_query_rsu_counts_mongo(mock_find):
    start_date = "2023-01-01T00:00:00"
    end_date = start_date
    allowed_ips = ['192.168.0.1']
    message_type = 'BSM'
    doc = {"ip": "192.168.0.1", "road": "road1", "count": 1}
    mock_find.return_value = [doc]

    with patch('pymongo.MongoClient') as mock_client:
        mock_db = MagicMock()
        mock_client.return_value.__getitem__.return_value = mock_db
        mock_collection = MagicMock()
        mock_db.__getitem__.return_value = mock_collection
        print("collectionmock: ", mock_find)
        result, status_code = query_rsu_counts_mongo(allowed_ips, message_type, start_date, end_date)
        assert status_code == 200
        assert result == {doc['ip']: {"road": doc["road"], "count": doc["count"]}}
        mock_find.assert_called()
        mock_find.assert_called_with(filter=querycounts_data.rsu_counts_query_mongo)

@patch('src.rsu_querycounts.bigquery')
def test_rsu_counts_bq_query(mock_bigquery):
    expected_query = querycounts_data.rsu_counts_query_bq
    with patch.dict('src.rsu_querycounts.os.environ', {'COUNT_DB_NAME': 'Fake_table'}):
        rsu_querycounts.query_rsu_counts_bq(['10.11.81.24'], 'BSM', '2022-05-23T12:00:00', '2022-05-24T12:00:00')
        mock_bigquery.Client.return_value.query.assert_called_with(expected_query)

@patch('src.rsu_querycounts.bigquery')
def test_rsu_counts_bq_no_data(mock_bigquery):
    mock_bigquery.Client.return_value.query.return_value = {}
    expected_rsu_data = {}
    with patch.dict('src.rsu_querycounts.os.environ', {'COUNT_DB_NAME': 'Fake_table'}):
        (data, code) = rsu_querycounts.query_rsu_counts_bq(['10.11.81.24'], 'BSM', '2022-05-23T12:00:00', '2022-05-24T12:00:00')
        assert data == expected_rsu_data
        assert code == 200


@patch('src.rsu_querycounts.bigquery')
def test_rsu_counts_bq_single_result(mock_bigquery):
    mock_bigquery.Client.return_value.query.return_value = [querycounts_data.rsu_one]
    expected_rsu_data = querycounts_data.rsu_counts_expected_single
    with patch.dict('src.rsu_querycounts.os.environ', {'COUNT_DB_NAME': 'Fake_table'}):
        (data, code) = rsu_querycounts.query_rsu_counts_bq(['172.16.28.23', '10.11.81.24', '172.16.28.136'], 'BSM', '2022-05-23T12:00:00', '2022-05-24T12:00:00')
        assert data == expected_rsu_data
        assert code == 200


@patch('src.rsu_querycounts.bigquery')
def test_rsu_counts_bq_multiple_result(mock_bigquery):
    mock_bigquery.Client.return_value.query.return_value = [querycounts_data.rsu_one, 
                                                            querycounts_data.rsu_two, 
                                                            querycounts_data.rsu_three]
    expected_rsu_data = querycounts_data.rsu_counts_expected_multiple
    with patch.dict('src.rsu_querycounts.os.environ', {'COUNT_DB_NAME': 'Fake_table'}):
        (data, code) = rsu_querycounts.query_rsu_counts_bq(['10.11.81.24', '172.16.28.23', '172.16.28.136'], 'BSM', '2022-05-23T12:00:00', '2022-05-24T12:00:00')
        assert data == expected_rsu_data
        assert code == 200


@patch('src.rsu_querycounts.bigquery')
def test_rsu_counts_bq_limited_rsus(mock_bigquery):
    mock_bigquery.Client.return_value.query.return_value = [querycounts_data.rsu_one, 
                                                            querycounts_data.rsu_two, 
                                                            querycounts_data.rsu_three]
    expected_rsu_data = querycounts_data.rsu_counts_expected_limited_rsus
    with patch.dict('src.rsu_querycounts.os.environ', {'COUNT_DB_NAME': 'Fake_table'}):
        (data, code) = rsu_querycounts.query_rsu_counts_bq(['172.16.28.23', '172.16.28.136'], 'BSM', '2022-05-23T12:00:00', '2022-05-24T12:00:00')
        assert data == expected_rsu_data
        assert code == 200