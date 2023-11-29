from unittest.mock import patch, MagicMock
import api.src.rsu_ssm_srm as rsu_ssm_srm
import api.tests.data.rsu_ssm_srm_data as ssm_srm_data
from datetime import datetime


##################################### Testing Requests ###########################################

def test_options_request():
    counts = rsu_ssm_srm.RsuSsmSrmData()
    (body, code, headers)= counts.options()
    assert body == ''
    assert code == 204
    assert headers['Access-Control-Allow-Methods'] == 'GET'

@patch('api.src.rsu_ssm_srm.query_ssm_data')
@patch('api.src.rsu_ssm_srm.query_srm_data')
def test_get_request(mock_srm, mock_ssm):
    req = MagicMock()
    ssm_srm = rsu_ssm_srm.RsuSsmSrmData()
    mock_ssm.return_value = 200, []
    mock_srm.return_value = 200, [
        ssm_srm_data.ssm_record_one, 
        ssm_srm_data.ssm_record_two, 
        ssm_srm_data.srm_record_one,
        ssm_srm_data.srm_record_three,
    ]
    with patch("api.src.rsu_ssm_srm.request", req):
        (data, code, headers)= ssm_srm.get()
        assert code == 200
        assert headers['Access-Control-Allow-Origin'] == "test.com"
        assert headers['Content-Type'] == "application/json"
        assert data == [
            ssm_srm_data.ssm_record_one,
            ssm_srm_data.srm_record_one,
            ssm_srm_data.ssm_record_two,
            ssm_srm_data.srm_record_three,
        ]

#################################### Test query_ssm_data ########################################

@patch('api.src.rsu_ssm_srm.bigquery')
@patch('api.src.rsu_ssm_srm.datetime')
def test_query_ssm_data_query(mock_date, mock_bigquery):
    mock_bigquery.Client.return_value.query.return_value = []
    mock_date.now.return_value = datetime.strptime('2022/12/14 00:00:00', '%Y/%m/%d %H:%M:%S')
    with patch.dict('api.src.rsu_ssm_srm.os.environ', {'SSM_DB_NAME': 'Fake_table'}):
        rsu_ssm_srm.query_ssm_data([])
        mock_bigquery.Client.return_value.query.assert_called_with(ssm_srm_data.ssm_expected_query)

@patch('api.src.rsu_ssm_srm.bigquery')
def test_query_ssm_data_no_data(mock_bigquery):
    mock_bigquery.Client.return_value.query.return_value = []
    with patch.dict('api.src.rsu_ssm_srm.os.environ', {'SSM_DB_NAME': 'Fake_table'}):
        (code, data) = rsu_ssm_srm.query_ssm_data([])
        assert data == []
        assert code == 200

@patch('api.src.rsu_ssm_srm.bigquery')
def test_query_ssm_data_single_result(mock_bigquery):
    mock_bigquery.Client.return_value.query.return_value = [ssm_srm_data.ssm_record_one]
    with patch.dict('api.src.rsu_ssm_srm.os.environ', {'SSM_DB_NAME': 'Fake_table'}):
        (code, data) = rsu_ssm_srm.query_ssm_data([])
        assert data == ssm_srm_data.ssm_single_result_expected
        assert code == 200

@patch('api.src.rsu_ssm_srm.bigquery')
def test_query_ssm_data_multiple_result(mock_bigquery):
    mock_bigquery.Client.return_value.query.return_value = [
    ssm_srm_data.ssm_record_one, 
    ssm_srm_data.ssm_record_two,
    ssm_srm_data.ssm_record_three
    ]
    with patch.dict('api.src.rsu_ssm_srm.os.environ', {'SSM_DB_NAME': 'Fake_table'}):
        (code, data) = rsu_ssm_srm.query_ssm_data([])
        assert data == ssm_srm_data.ssm_multiple_result_expected
        assert code == 200

##################################### Test query_srm_data ###########################################

@patch('api.src.rsu_ssm_srm.bigquery')
@patch('api.src.rsu_ssm_srm.datetime')
def test_query_srm_data_query(mock_date, mock_bigquery):
    mock_bigquery.Client.return_value.query.return_value = []
    mock_date.now.return_value = datetime.strptime('2022/12/14 00:00:00', '%Y/%m/%d %H:%M:%S')
    with patch.dict('api.src.rsu_ssm_srm.os.environ', {'SRM_DB_NAME': 'Fake_table'}):
        rsu_ssm_srm.query_srm_data([])
        mock_bigquery.Client.return_value.query.assert_called_with(ssm_srm_data.srm_expected_query)

@patch('api.src.rsu_ssm_srm.bigquery')
def test_query_srm_data_no_data(mock_bigquery):
    mock_bigquery.Client.return_value.query.return_value = []
    with patch.dict('api.src.rsu_ssm_srm.os.environ', {'SRM_DB_NAME': 'Fake_table'}):
        (code, data) = rsu_ssm_srm.query_srm_data([])
        assert data == []
        assert code == 200

@patch('api.src.rsu_ssm_srm.bigquery')
def test_query_srm_data_single_result(mock_bigquery):
    mock_bigquery.Client.return_value.query.return_value = [ssm_srm_data.srm_record_one]
    with patch.dict('api.src.rsu_ssm_srm.os.environ', {'SRM_DB_NAME': 'Fake_table'}):
        (code, data) = rsu_ssm_srm.query_srm_data([])
        assert data == ssm_srm_data.srm_single_result_expected
        assert code == 200

@patch('api.src.rsu_ssm_srm.bigquery')
def test_query_srm_data_multiple_result(mock_bigquery):
    mock_bigquery.Client.return_value.query.return_value = [
    ssm_srm_data.srm_record_one, 
    ssm_srm_data.srm_record_two,
    ssm_srm_data.srm_record_three
    ]
    with patch.dict('api.src.rsu_ssm_srm.os.environ', {'SRM_DB_NAME': 'Fake_table'}):
        (code, data) = rsu_ssm_srm.query_srm_data([])
        assert data == ssm_srm_data.srm_multiple_result_expected
        assert code == 200