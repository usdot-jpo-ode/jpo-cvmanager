from unittest.mock import patch, MagicMock
import pytest
import src.rsu_geo_query as rsu_geo_query
import tests.data.rsu_geo_query_data as rsu_geo_query_data


##################################### Testing Requests ###########################################

def test_options_request():
    counts = rsu_geo_query.RsuGeoQuery()
    (body, code, headers)= counts.options()
    assert body == ''
    assert code == 204
    assert headers['Access-Control-Allow-Methods'] == 'POST'

@patch('src.rsu_geo_query.query_org_rsus')
@patch('src.rsu_geo_query.query_rsu_devices')
def test_post_request(mock_query, mock_rsus):
    req = MagicMock()
    req.args = rsu_geo_query_data.request_args_good
    req.environ = rsu_geo_query_data.request_params_good
    counts = rsu_geo_query.RsuGeoQuery()
    mock_rsus.return_value = ['10.0.0.1', '10.0.0.2', '10.0.0.3'], 200
    mock_query.return_value = ['10.0.0.1'], 200
    with patch("src.rsu_geo_query.request", req):
        (data, code, headers)= counts.post()
        assert code == 200
        assert headers['Access-Control-Allow-Origin'] == "*"
        assert headers['Content-Type'] == "application/json"
        assert data == ['10.0.0.1']
        
################################### Testing Data Validation #########################################

def test_post_request_invalid_message():
    req = MagicMock()
    req.args = rsu_geo_query_data.request_args_bad_message
    counts = rsu_geo_query.RsuGeoQuery()
    with patch("src.rsu_geo_query.request", req):
        (data, code, headers)= counts.post()
        assert code == 400
        assert headers['Access-Control-Allow-Origin'] == "*"
        assert data == "Invalid Message Type.\nValid message types: SSM, BSM, SPAT, SRM, MAP"

# def test_schema_validate_bad_data():
#     req = MagicMock()
#     req.args = querycounts_data.request_args_bad_type
#     counts = rsu_querycounts.RsuQueryCounts()
#     with patch("src.rsu_querycounts.request", req):
#         with pytest.raises(Exception):
#             assert counts.get()