from unittest.mock import patch, MagicMock
import pytest
import src.rsu_geo_query as rsu_geo_query
import tests.data.rsu_geo_query_data as rsu_geo_query_data


##################################### Testing Requests ###########################################

def test_options_request():
    counts = rsu_geo_query.RsuQueryCounts()
    (body, code, headers)= counts.options()
    assert body == ''
    assert code == 204
    assert headers['Access-Control-Allow-Methods'] == 'GET'

@patch('src.rsu_geo_query.get_organization_rsus')
@patch('src.rsu_geo_query.query_rsu_counts')
def test_get_request(mock_query, mock_rsus):
    req = MagicMock()
    req.args = rsu_geo_query_data.request_args_good
    req.environ = rsu_geo_query_data.request_params_good
    counts = rsu_geo_query.RsuQueryCounts()
    mock_rsus.return_value = ['10.0.0.1', '10.0.0.2', '10.0.0.3']
    mock_query.return_value = {"Some Data"}, 200
    with patch("src.rsu_geo_query.request", req):
        (data, code, headers)= counts.get()
        assert code == 200
        assert headers['Access-Control-Allow-Origin'] == "*"
        assert headers['Content-Type'] == "application/json"
        assert data == {"Some Data"}

################################### Testing Data Validation #########################################

def test_get_request_invalid_message():
    req = MagicMock()
    req.args = rsu_geo_query_data.request_args_bad_message
    counts = rsu_geo_query.RsuQueryCounts()
    with patch("src.rsu_geo_query.request", req):
        (data, code, headers)= counts.get()
        assert code == 400
        assert headers['Access-Control-Allow-Origin'] == "*"
        assert data == "Invalid Message Type.\nValid message types: SSM, BSM, SPAT, SRM, MAP"

def test_schema_validate_bad_data():
    req = MagicMock()
    req.args = rsu_geo_query_data.request_args_bad_type
    counts = rsu_geo_query.RsuQueryCounts()
    with patch("src.rsu_geo_query.request", req):
        with pytest.raises(Exception):
            assert counts.get()

################################### Test get_organization_rsus ########################################

@patch('src.rsu_geo_query.pgquery')
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
    actual_result = rsu_geo_query.get_organization_rsus('Test')
    mock_pgquery.query_db.assert_called_with(expected_query)

    assert actual_result == ['10.11.81.12', '10.11.81.13', '10.11.81.14']

@patch('src.rsu_geo_query.pgquery')
def test_rsu_counts_get_organization_rsus_empty(mock_pgquery):
    mock_pgquery.query_db.return_value = []
    expected_query = "SELECT jsonb_build_object('ip', rd.ipv4_address) " \
                    "FROM public.rsus AS rd " \
                    "JOIN public.rsu_organization_name AS ron_v ON ron_v.rsu_id = rd.rsu_id " \
                    f"WHERE ron_v.name = 'Test' " \
                    "ORDER BY rd.ipv4_address"
    actual_result = rsu_geo_query.get_organization_rsus('Test')
    mock_pgquery.query_db.assert_called_with(expected_query)

    assert actual_result == []

##################################### Test query_rsu_counts ###########################################

@patch('src.rsu_geo_query.bigquery')
def test_rsu_counts_query(mock_bigquery):
    expected_query = rsu_geo_query_data.rsu_counts_query
    with patch.dict('src.rsu_geo_query.os.environ', {'COUNT_DB_NAME': 'Fake_table'}):
        rsu_geo_query.query_rsu_counts(['10.11.81.24'], 'BSM', '2022-05-23T12:00:00', '2022-05-24T12:00:00')
        mock_bigquery.Client.return_value.query.assert_called_with(expected_query)

@patch('src.rsu_geo_query.bigquery')
def test_rsu_counts_no_data(mock_bigquery):
    mock_bigquery.Client.return_value.query.return_value = {}
    expected_rsu_data = {}
    with patch.dict('src.rsu_geo_query.os.environ', {'COUNT_DB_NAME': 'Fake_table'}):
        (data, code) = rsu_geo_query.query_rsu_counts(['10.11.81.24'], 'BSM', '2022-05-23T12:00:00', '2022-05-24T12:00:00')
        assert data == expected_rsu_data
        assert code == 200


@patch('src.rsu_geo_query.bigquery')
def test_rsu_counts_single_result(mock_bigquery):
    mock_bigquery.Client.return_value.query.return_value = [rsu_geo_query_data.rsu_one]
    expected_rsu_data = rsu_geo_query_data.rsu_counts_expected_single
    with patch.dict('src.rsu_geo_query.os.environ', {'COUNT_DB_NAME': 'Fake_table'}):
        (data, code) = rsu_geo_query.query_rsu_counts(['172.16.28.23', '10.11.81.24', '172.16.28.136'], 'BSM', '2022-05-23T12:00:00', '2022-05-24T12:00:00')
        assert data == expected_rsu_data
        assert code == 200


@patch('src.rsu_geo_query.bigquery')
def test_rsu_counts_multiple_result(mock_bigquery):
    mock_bigquery.Client.return_value.query.return_value = [rsu_geo_query_data.rsu_one, 
                                                            rsu_geo_query_data.rsu_two, 
                                                            rsu_geo_query_data.rsu_three]
    expected_rsu_data = rsu_geo_query_data.rsu_counts_expected_multiple
    with patch.dict('src.rsu_geo_query.os.environ', {'COUNT_DB_NAME': 'Fake_table'}):
        (data, code) = rsu_geo_query.query_rsu_counts(['10.11.81.24', '172.16.28.23', '172.16.28.136'], 'BSM', '2022-05-23T12:00:00', '2022-05-24T12:00:00')
        assert data == expected_rsu_data
        assert code == 200


@patch('src.rsu_geo_query.bigquery')
def test_rsu_counts_limited_rsus(mock_bigquery):
    mock_bigquery.Client.return_value.query.return_value = [rsu_geo_query_data.rsu_one, 
                                                            rsu_geo_query_data.rsu_two, 
                                                            rsu_geo_query_data.rsu_three]
    expected_rsu_data = rsu_geo_query_data.rsu_counts_expected_limited_rsus
    with patch.dict('src.rsu_geo_query.os.environ', {'COUNT_DB_NAME': 'Fake_table'}):
        (data, code) = rsu_geo_query.query_rsu_counts(['172.16.28.23', '172.16.28.136'], 'BSM', '2022-05-23T12:00:00', '2022-05-24T12:00:00')
        assert data == expected_rsu_data
        assert code == 200