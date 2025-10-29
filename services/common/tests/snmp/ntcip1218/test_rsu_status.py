import pytest
import subprocess
from unittest.mock import patch, MagicMock
from common.snmp.ntcip1218.rsu_status import get, convert_status_value


@pytest.mark.parametrize(
    "input_value, expected_output",
    [
        ("other(1)", 1),
        ("okay(2)", 2),
        ("warning(3)", 3),
        ("critical(4)", 4),
        ("unknown(5)", 5),
    ],
)
def test_convert_status_value(input_value, expected_output):
    assert convert_status_value(input_value) == expected_output


@patch("common.snmp.ntcip1218.rsu_status.subprocess.run")
@patch("common.snmp.ntcip1218.rsu_status.snmpcredential.get_authstring")
def test_get_success(mock_get_authstring, mock_subprocess_run):
    mock_get_authstring.return_value = "authString"
    mock_subprocess_run.return_value = MagicMock(
        stdout=b"NTCIP1218-v01::rsuStatus.0 = INTEGER: okay(2)\n"
    )

    rsu_ip = "192.168.1.1"
    snmp_creds = {"username": "test", "password": "test123"}

    response, status_code = get(rsu_ip, snmp_creds)

    assert status_code == 200
    assert response == {"RsuStatus": 2}
    mock_get_authstring.assert_called_once_with(snmp_creds)
    mock_subprocess_run.assert_called_once()


@patch("common.snmp.ntcip1218.rsu_status.subprocess.run")
@patch("common.snmp.ntcip1218.rsu_status.snmpcredential.get_authstring")
def test_get_snmp_error(mock_get_authstring, mock_subprocess_run):
    mock_get_authstring.return_value = "authString"
    mock_subprocess_run.side_effect = subprocess.CalledProcessError(
        returncode=1, cmd="snmpget", stderr=b"Error: Timeout\n"
    )

    rsu_ip = "192.168.1.1"
    snmp_creds = {"username": "test", "password": "test123"}

    response, status_code = get(rsu_ip, snmp_creds)

    assert status_code == 500
    assert response["RsuStatus"] == 5
    mock_get_authstring.assert_called_once_with(snmp_creds)
    mock_subprocess_run.assert_called_once()
