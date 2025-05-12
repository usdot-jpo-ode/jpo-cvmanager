import subprocess
from unittest.mock import patch, MagicMock
from common.snmp.ntcip1218.rsu_location import convert_location_value, get


def test_convert_location_value_valid():
    assert convert_location_value("405672318 tenth of a microdegree") == 40.5672318


def test_convert_location_value_unknown_latitude():
    assert convert_location_value("900000001 tenth of a microdegree") is None


def test_convert_location_value_unknown_longitude():
    assert convert_location_value("1800000001 tenth of a microdegree") is None


def test_convert_location_value_invalid():
    with patch("common.snmp.ntcip1218.rsu_location.logging.error") as mock_log:
        assert convert_location_value("invalid_value") is None
        mock_log.assert_called_once_with(
            "Invalid value for latitude/longitude: invalid_value"
        )


@patch("common.snmp.ntcip1218.rsu_location.subprocess.run")
@patch("common.snmp.ntcip1218.rsu_location.snmpcredential.get_authstring")
def test_get_success(mock_get_authstring, mock_subprocess_run):
    mock_get_authstring.return_value = "auth_string"
    mock_subprocess_run.side_effect = [
        MagicMock(stdout=b"NTCIP1218-v01::rsuGnssLat.0 = INTEGER: 405672318\n"),
        MagicMock(stdout=b"NTCIP1218-v01::rsuGnssLon.0 = INTEGER: -1050342786\n"),
    ]

    result, status_code = get("192.168.1.1", "snmp_creds")
    assert status_code == 200
    assert result == {
        "RsuLocation": {"latitude": 40.5672318, "longitude": -105.0342786}
    }


@patch("common.snmp.ntcip1218.rsu_location.subprocess.run")
@patch("common.snmp.ntcip1218.rsu_location.snmpcredential.get_authstring")
def test_get_latitude_error(mock_get_authstring, mock_subprocess_run):
    mock_get_authstring.return_value = "auth_string"
    mock_subprocess_run.side_effect = [
        subprocess.CalledProcessError(
            returncode=1, cmd="cmd", stderr=b"Error: SNMP timeout\n"
        ),
    ]

    with patch(
        "common.snmp.ntcip1218.rsu_location.snmperrorcheck.check_error_type"
    ) as mock_check_error:
        mock_check_error.return_value = "SNMP timeout"
        result, status_code = get("192.168.1.1", "snmp_creds")
        assert status_code == 500
        assert result == {"RsuLocation": "SNMP timeout"}


@patch("common.snmp.ntcip1218.rsu_location.subprocess.run")
@patch("common.snmp.ntcip1218.rsu_location.snmpcredential.get_authstring")
def test_get_longitude_error(mock_get_authstring, mock_subprocess_run):
    mock_get_authstring.return_value = "auth_string"
    mock_subprocess_run.side_effect = [
        MagicMock(stdout=b"NTCIP1218-v01::rsuGnssLat.0 = INTEGER: 405672318\n"),
        subprocess.CalledProcessError(
            returncode=1, cmd="cmd", stderr=b"Error: SNMP timeout\n"
        ),
    ]

    with patch(
        "common.snmp.ntcip1218.rsu_location.snmperrorcheck.check_error_type"
    ) as mock_check_error:
        mock_check_error.return_value = "SNMP timeout"
        result, status_code = get("192.168.1.1", "snmp_creds")
        assert status_code == 500
        assert result == {"RsuLocation": "SNMP timeout"}
