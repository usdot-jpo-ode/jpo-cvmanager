import pytest
import subprocess
from unittest.mock import patch, MagicMock
from datetime import datetime, timezone, timedelta
from common.snmp.ntcip1218.rsu_security_expiration import get


@pytest.fixture
def mock_snmp_creds():
    return MagicMock()


@pytest.fixture
def mock_snmpcredential():
    with patch("common.snmp.ntcip1218.rsu_security_expiration.snmpcredential") as mock:
        yield mock


@pytest.fixture
def mock_subprocess():
    with patch("common.snmp.ntcip1218.rsu_security_expiration.subprocess") as mock:
        yield mock


def test_get_successful(mock_snmp_creds, mock_snmpcredential, mock_subprocess):
    mock_snmpcredential.get_authstring.return_value = "authString"
    mock_subprocess.run.side_effect = [
        MagicMock(stdout=b"NTCIP1218-v01::rsuSecAppCertReq.1 = INTEGER: 168 hour\n"),
        MagicMock(
            stdout=b"NTCIP1218-v01::rsuSecAppCertExpiration.1 = INTEGER: 119 hour\n"
        ),
    ]

    result, status_code = get("192.168.1.1", mock_snmp_creds)

    assert status_code == 200
    expected_time = (datetime.now(timezone.utc) + timedelta(hours=119)).strftime(
        "%Y-%m-%d %H:00:00 %Z"
    )
    assert result["RsuSecurityExpiration"] == expected_time


def test_get_all_certificates_expired(
    mock_snmp_creds, mock_snmpcredential, mock_subprocess
):
    mock_snmpcredential.get_authstring.return_value = "authString"
    mock_subprocess.run.side_effect = [
        MagicMock(stdout=b"NTCIP1218-v01::rsuSecAppCertReq.1 = INTEGER: 168 hour\n"),
        MagicMock(
            stdout=b"NTCIP1218-v01::rsuSecAppCertExpiration.1 = INTEGER: 255 hour\n"
        ),
    ]

    result, status_code = get("192.168.1.1", mock_snmp_creds)

    assert status_code == 200
    expected_time = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:00:00 %Z")
    assert result["RsuSecurityExpiration"] == expected_time


def test_get_no_certificates(mock_snmp_creds, mock_snmpcredential, mock_subprocess):
    mock_snmpcredential.get_authstring.return_value = "authString"
    mock_subprocess.run.side_effect = [
        MagicMock(stdout=b""),
        MagicMock(stdout=b""),
    ]

    result, status_code = get("192.168.1.1", mock_snmp_creds)

    assert status_code == 200
    expected_time = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:00:00 %Z")
    assert result["RsuSecurityExpiration"] == expected_time
