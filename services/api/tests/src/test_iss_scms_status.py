from unittest.mock import patch, MagicMock
import api.src.iss_scms_status as iss_scms_status
import api.tests.data.iss_scms_status_data as iss_scms_status_data

###################################### Testing Requests ##########################################

def test_request_options():
    info = iss_scms_status.IssScmsStatus()
    (body, code, headers) = info.options()
    assert body == ''
    assert code == 204
    assert headers['Access-Control-Allow-Methods'] == 'GET'

@patch('api.src.iss_scms_status.get_iss_scms_status')
def test_entry_get(mock_get_iss_scms_status):
    req = MagicMock()
    req.environ = iss_scms_status_data.request_params_good
    mock_get_iss_scms_status.return_value = {}
    with patch("api.src.iss_scms_status.request", req):
        status = iss_scms_status.IssScmsStatus()
        (body, code, headers) = status.get()

        mock_get_iss_scms_status.assert_called_once()
        assert code == 200
        assert headers['Access-Control-Allow-Origin'] == "test.com"
        assert body == {}

###################################### Testing Functions ##########################################


@patch('api.src.iss_scms_status.pgquery')
def test_get_iss_status_no_data(mock_pgquery):
    mock_pgquery.query_db.return_value = {}
    expected_rsu_data = {}
    expected_query = "SELECT jsonb_build_object('ip', rd.ipv4_address, 'health', scms_health_data.health, 'expiration', scms_health_data.expiration) " \
        "FROM public.rsus AS rd " \
        "JOIN public.rsu_organization_name AS ron_v ON ron_v.rsu_id = rd.rsu_id " \
        "LEFT JOIN (" \
            "SELECT a.rsu_id, a.health, a.timestamp, a.expiration " \
            "FROM (" \
                "SELECT sh.rsu_id, sh.health, sh.timestamp, sh.expiration, ROW_NUMBER() OVER (PARTITION BY sh.rsu_id order by sh.timestamp DESC) AS row_id " \
                "FROM public.scms_health AS sh" \
            ") AS a " \
            "WHERE a.row_id <= 1 ORDER BY rsu_id" \
        ") AS scms_health_data ON rd.rsu_id = scms_health_data.rsu_id " \
        f"WHERE ron_v.name = 'Test' " \
        "ORDER BY rd.ipv4_address"
    actual_result = iss_scms_status.get_iss_scms_status('Test')
    mock_pgquery.query_db.assert_called_with(expected_query)

    assert actual_result == expected_rsu_data

@patch('api.src.iss_scms_status.pgquery')
def test_get_iss_status_single_result(mock_pgquery):
    mock_pgquery.query_db.return_value = iss_scms_status_data.return_value_single_result
    actual_result = iss_scms_status.get_iss_scms_status('Test')
    mock_pgquery.query_db.assert_called_once()

    assert actual_result == iss_scms_status_data.expected_rsu_data_single_result

@patch('api.src.iss_scms_status.pgquery')
def test_get_iss_status_single_null_result(mock_pgquery):
    mock_pgquery.query_db.return_value = iss_scms_status_data.return_value_single_null_result
    actual_result = iss_scms_status.get_iss_scms_status('Test')
    mock_pgquery.query_db.assert_called_once()

    assert actual_result == iss_scms_status_data.expected_rsu_data_single_null_result

@patch('api.src.iss_scms_status.pgquery')
def test_get_iss_status_multiple_result(mock_pgquery):
    mock_pgquery.query_db.return_value = iss_scms_status_data.return_value_multiple_result
    actual_result = iss_scms_status.get_iss_scms_status('Test')
    mock_pgquery.query_db.assert_called_once()

    assert actual_result == iss_scms_status_data.expected_rsu_data_multiple_result

# test that get_iss_scms_status is calling pgquery.query_db with expected arguments
@patch('common.pgquery.db_config', new={'pool_size': 5, 'max_overflow': 2, 'pool_timeout': 30, 'pool_recycle': 1800})
@patch('common.pgquery.db', new=None)
@patch('common.pgquery.query_db')
def test_get_iss_scms_status_query(mock_query_db):
    # mock return values for function dependencies
    mock_query_db.return_value = iss_scms_status_data.return_value_single_result

    result = iss_scms_status.get_iss_scms_status('Test')
    assert(result == iss_scms_status_data.expected_rsu_data_single_result)
    iss_scms_status.pgquery.query_db.assert_called_once_with(iss_scms_status_data.expectedQuery)