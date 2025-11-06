import pytest
from unittest.mock import patch
from datetime import datetime, timezone
from common.snmp.update_pg.update_rsu_security import UpdatePostgresRsuSecurity


@pytest.fixture
def rsu_security_instance():
    return UpdatePostgresRsuSecurity()


def test_insert_config_list_empty(rsu_security_instance):
    with patch("common.pgquery.write_db") as mock_write_db:
        snmp_config_list = []
        rsu_security_instance.insert_config_list(snmp_config_list)
        mock_write_db.assert_not_called()


def test_update_postgresql_empty(rsu_security_instance):
    with patch.object(rsu_security_instance, "insert_config_list") as mock_insert:
        rsu_snmp_configs_obj = {}
        rsu_security_instance.update_postgresql(rsu_snmp_configs_obj)
        mock_insert.assert_not_called()


def test_process_rsu_snmp_failure(rsu_security_instance):
    rsu = {
        "snmp_username": "user",
        "snmp_password": "pass",
        "snmp_encrypt_pw": "encrypt",
        "snmp_version": "1218",
        "ipv4_address": "192.168.1.1",
        "rsu_id": 101,
    }
    with patch(
        "common.snmp.ntcip1218.rsu_security_expiration.get", return_value=({}, 500)
    ):
        rsu_id, config = rsu_security_instance.process_rsu(rsu)
        assert rsu_id == 101
        assert config is None


def test_process_rsu_expired_certificate(rsu_security_instance):
    rsu = {
        "snmp_username": "user",
        "snmp_password": "pass",
        "snmp_encrypt_pw": "encrypt",
        "snmp_version": "1218",
        "ipv4_address": "192.168.1.1",
        "rsu_id": 101,
    }
    response_mock = {"RsuSecurityExpiration": "2022-01-01 12:00:00 UTC"}
    with patch(
        "common.snmp.ntcip1218.rsu_security_expiration.get",
        return_value=(response_mock, 200),
    ):
        rsu_id, config = rsu_security_instance.process_rsu(rsu)
        assert rsu_id == 101
        assert config["health"] == 0
        assert config["expiration"] == datetime(2022, 1, 1, 12, 0, tzinfo=timezone.utc)


def test_get_snmp_configs_empty_list(rsu_security_instance):
    rsu_list = []
    with patch("common.snmp.update_pg.update_rsu_security.Pool") as mock_pool:
        rsu_security_instance.get_snmp_configs(rsu_list)
        mock_pool.assert_called_once()
