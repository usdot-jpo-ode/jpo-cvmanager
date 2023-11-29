from ipaddress import ip_address
from unittest.mock import patch, MagicMock
import api.src.rsu_map_info as rsu_map_info
import multidict
import werkzeug
import pytest


##################################### Testing Requests ###########################################

def test_options_request():
    mapInfo = rsu_map_info.RsuMapInfo()
    (body, code, headers)= mapInfo.options()
    assert body == ''
    assert code == 204
    assert headers['Access-Control-Allow-Methods'] == 'GET'

@patch('api.src.rsu_map_info.get_map_data')
def test_get_request(mock_get_map_data):
    req = MagicMock()
    req.args = multidict.MultiDict([
        ('ip_address', '192.168.11.18')
        ])
    mapInfo = rsu_map_info.RsuMapInfo()
    mock_get_map_data.return_value = (200, {"Some Data"})    
    with patch("api.src.rsu_map_info.request", req):
        (data, code, headers)= mapInfo.get()
        assert code == 200
        assert headers['Access-Control-Allow-Origin'] == "test.com"
        assert headers['Content-Type'] == "application/json"
        assert data == {"Some Data"}
        mock_get_map_data.assert_called_once()

##################################### Testing Schema ###########################################

def test_get_schema_validate():
    req = MagicMock()
    req.args = multidict.MultiDict([
        ('ip_address', 192)
        ])
    mapInfo = rsu_map_info.RsuMapInfo()
    with patch("api.src.rsu_map_info.request", req):
        with pytest.raises(werkzeug.exceptions.BadRequest):
            mapInfo.get()

##################################### Testing get_map_data ###########################################

@patch('api.src.rsu_map_info.pgquery')
def test_get_map_data_query(mock_pgquery):
    mock_pgquery.query_db.return_value = {}
    organization = 'test'
    ip_address = '192.168.11.22'
    expected_query = "SELECT mi.geojson, mi.date " \
          "FROM public.map_info AS mi " \
          "JOIN public.rsus AS rd ON rd.ipv4_address = mi.ipv4_address " \
          "JOIN public.rsu_organization_name AS ron_v ON ron_v.rsu_id = rd.rsu_id " \
          f"WHERE ron_v.name = '{organization}' AND mi.ipv4_address = '{ip_address}'"
    rsu_map_info.get_map_data(ip_address, organization)  
    mock_pgquery.query_db.assert_called_with(expected_query)
    mock_pgquery.query_db.assert_called_once()

@patch('api.src.rsu_map_info.pgquery')
def test_get_map_data_no_data(mock_pgquery):
    mock_pgquery.query_db.return_value = {}
    organization = 'test'
    (code, actual_result) = rsu_map_info.get_map_data('192.168.11.22', organization)
    assert code == 200
    assert actual_result == "No Data"

@patch('api.src.rsu_map_info.pgquery')
@patch('api.src.rsu_map_info.format_date_denver')
def test_get_map_data_return_data(mock_format_date_denver, mock_pgquery):
    mock_pgquery.query_db.return_value = [["some return data", "some return date"]]
    mock_format_date_denver.return_value = "some return date"
    organization = 'test'
    (code, actual_result) = rsu_map_info.get_map_data('192.168.11.22', organization)
    assert code == 200
    assert actual_result == {"geojson": "some return data", "date": "some return date"}

def test_get_map_data_exception():
    with patch('api.src.rsu_map_info.pgquery.query_db', side_effect=Exception('testing error handling')):
        organization = 'test'
        (code, result) = rsu_map_info.get_map_data('192.168.11.22', organization)
        assert code == 400
        assert result == "Error selecting GeoJSON data for 192.168.11.22"