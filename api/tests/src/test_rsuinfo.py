from unittest.mock import patch, MagicMock
import src.rsuinfo as rsuinfo
import tests.data.rsu_info_data as rsu_info_data

###################################### Testing Requests ##########################################


def test_request_options():
    info = rsuinfo.RsuInfo()
    (body, code, headers) = info.options()
    assert body == ''
    assert code == 204
    assert headers['Access-Control-Allow-Methods'] == 'GET'


@patch('src.rsuinfo.get_rsu_data')
def test_entry_get(mock_get_rsu_data):
    req = MagicMock()
    req.environ = rsu_info_data.request_params_good
    mock_get_rsu_data.return_value = {"rsuList": []}
    with patch("src.rsuinfo.request", req):
        info = rsuinfo.RsuInfo()
        (body, code, headers) = info.get()

        mock_get_rsu_data.assert_called_once()
        assert code == 200
        assert headers['Access-Control-Allow-Origin'] == "*"
        assert body == {"rsuList": []}

###################################### Testing Functions ##########################################


@patch('src.rsuinfo.pgquery')
def test_get_rsu_data_no_data(mock_pgquery):
    mock_pgquery.query_db.return_value = {}
    expected_rsu_data = {"rsuList": []}
    expected_query = "SELECT jsonb_build_object('type', 'Feature', 'id', row.rsu_id, 'geometry', ST_AsGeoJSON(row.geography)::jsonb, 'properties', to_jsonb(row)) " \
                    "FROM (" \
                        "SELECT rd.rsu_id, rd.geography, rd.milepost, rd.ipv4_address, rd.serial_number, rd.primary_route, rm.name AS model_name, man.name AS manufacturer_name " \
                        "FROM public.rsus AS rd " \
                        "JOIN public.rsu_organization_name AS ron_v ON ron_v.rsu_id = rd.rsu_id " \
                        "JOIN public.rsu_models AS rm ON rm.rsu_model_id = rd.model " \
                        "JOIN public.manufacturers AS man ON man.manufacturer_id = rm.manufacturer " \
                        "WHERE ron_v.name = 'Test'" \
                    ") AS row"
    actual_result = rsuinfo.get_rsu_data('Test')
    mock_pgquery.query_db.assert_called_with(expected_query)

    assert actual_result == expected_rsu_data


@patch('src.rsuinfo.pgquery')
def test_get_rsu_data_single_result(mock_pgquery):
    mock_pgquery.query_db.return_value = rsu_info_data.return_value_single_result
    actual_result = rsuinfo.get_rsu_data('Test')
    mock_pgquery.query_db.assert_called_once()

    assert actual_result == rsu_info_data.expected_rsu_data_single_result


@patch('src.rsuinfo.pgquery')
def test_get_rsu_data_multiple_result(mock_pgquery):
    mock_pgquery.query_db.return_value = rsu_info_data.return_value_multiple_results
    actual_result = rsuinfo.get_rsu_data('Test')
    mock_pgquery.query_db.assert_called_once()

    assert actual_result == rsu_info_data.expected_rsu_data_multiple_results


# test that get_rsu_data is calling pgquery.query_db with expected arguments
@patch('src.pgquery.db_config', new={'pool_size': 5, 'max_overflow': 2, 'pool_timeout': 30, 'pool_recycle': 1800})
@patch('src.pgquery.db', new=None)
def test_get_rsu_data():
    # mock return values for function dependencies
    rsuinfo.pgquery.query_db = MagicMock(
        return_value = [[{'name': 'Alice'}]]
    )

    # call function
    organization = 'test'
    result = rsuinfo.get_rsu_data(organization)

    # check return value
    expectedResult = {"rsuList": [{'name': 'Alice'}]}
    assert(result == expectedResult)

    # check that pgquery.query_db was called with expected arguments
    expectedQuery = "SELECT jsonb_build_object('type', 'Feature', 'id', row.rsu_id, 'geometry', ST_AsGeoJSON(row.geography)::jsonb, 'properties', to_jsonb(row)) FROM (SELECT rd.rsu_id, rd.geography, rd.milepost, rd.ipv4_address, rd.serial_number, rd.primary_route, rm.name AS model_name, man.name AS manufacturer_name FROM public.rsus AS rd JOIN public.rsu_organization_name AS ron_v ON ron_v.rsu_id = rd.rsu_id JOIN public.rsu_models AS rm ON rm.rsu_model_id = rd.model JOIN public.manufacturers AS man ON man.manufacturer_id = rm.manufacturer WHERE ron_v.name = 'test') AS row"
    rsuinfo.pgquery.query_db.assert_called_once_with(expectedQuery)

# TODO: add more tests here