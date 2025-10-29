from unittest.mock import patch, MagicMock
from common.snmp.ntcip1218.rsu_firmware_version import get
import subprocess


@patch("common.snmp.ntcip1218.rsu_firmware_version.subprocess.run")
@patch("common.snmp.ntcip1218.rsu_firmware_version.snmpcredential.get_authstring")
def test_get_successful_response(mock_get_authstring, mock_subprocess_run):
    # Mock the SNMP credentials and subprocess output
    mock_get_authstring.return_value = "authstring"
    mock_subprocess_run.return_value = MagicMock(
        stdout=b"NTCIP1218-v01::rsuFirmwareVersion.0 = STRING: y20.61.1-b275956\n"
    )

    rsu_ip = "192.168.1.1"
    snmp_creds = {"username": "test", "password": "test123"}

    response, status_code = get(rsu_ip, snmp_creds)

    assert status_code == 200
    assert response == {"RsuFirmwareVersion": "y20.61.1-b275956"}


@patch("common.snmp.ntcip1218.rsu_firmware_version.subprocess.run")
@patch("common.snmp.ntcip1218.rsu_firmware_version.snmpcredential.get_authstring")
def test_get_snmp_error(mock_get_authstring, mock_subprocess_run):
    # Mock the SNMP credentials and subprocess error
    mock_get_authstring.return_value = "authstring"
    mock_subprocess_run.side_effect = subprocess.CalledProcessError(
        returncode=1, cmd="snmpget", stderr=b"Error: Timeout\n"
    )

    rsu_ip = "192.168.1.1"
    snmp_creds = {"username": "test", "password": "test123"}

    with patch(
        "common.snmp.ntcip1218.rsu_firmware_version.snmperrorcheck.check_error_type"
    ) as mock_check_error_type:
        mock_check_error_type.return_value = "Timeout Error"

        response, status_code = get(rsu_ip, snmp_creds)

        assert status_code == 500
        assert response == {"RsuStatus": "Timeout Error"}
