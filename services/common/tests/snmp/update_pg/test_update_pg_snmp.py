import pytest
from unittest.mock import patch
from common.snmp.update_pg.update_pg_snmp import UpdatePostgresSnmpAbstractClass


class TestUpdatePostgresSnmpAbstractClass(UpdatePostgresSnmpAbstractClass):
    def update_postgresql(self, rsu_snmp_configs_obj, subset=False):
        pass

    def get_snmp_configs(self, rsu_list):
        pass


@pytest.fixture
def mock_pgquery():
    with patch("common.pgquery.query_db") as mock_query_db:
        yield mock_query_db


def test_get_rsu_list(mock_pgquery):
    mock_pgquery.return_value = [
        [
            {
                "rsu_id": 1,
                "ipv4_address": "192.168.1.1",
                "snmp_username": "user1",
                "snmp_password": "pass1",
                "snmp_encrypt_pw": "enc1",
                "snmp_version": "v2c",
            }
        ],
        [
            {
                "rsu_id": 2,
                "ipv4_address": "192.168.1.2",
                "snmp_username": "user2",
                "snmp_password": "pass2",
                "snmp_encrypt_pw": "enc2",
                "snmp_version": "v3",
            }
        ],
    ]

    updater = TestUpdatePostgresSnmpAbstractClass()
    rsu_list = updater.get_rsu_list()

    assert len(rsu_list) == 2
    assert rsu_list[0]["rsu_id"] == 1
    assert rsu_list[0]["ipv4_address"] == "192.168.1.1"
    assert rsu_list[1]["snmp_version"] == "v3"


def test_insert_config_list():
    updater = TestUpdatePostgresSnmpAbstractClass()
    result = updater.insert_config_list([{"rsu_id": 1, "config": "test_config"}])
    assert result is None


def test_delete_config_list():
    updater = TestUpdatePostgresSnmpAbstractClass()
    result = updater.delete_config_list([{"rsu_id": 1, "config": "test_config"}])
    assert result is None


def test_get_config_list():
    updater = TestUpdatePostgresSnmpAbstractClass()
    result = updater.get_config_list({"rsu_id": 1})
    assert result is None
