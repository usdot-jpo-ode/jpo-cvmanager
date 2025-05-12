import pytest
from unittest.mock import patch
from datetime import datetime, timezone
from common.snmp.update_pg.update_rsu_health import UpdatePostgresRsuHealth


@pytest.fixture
def rsu_health_instance():
    return UpdatePostgresRsuHealth()


def test_insert_config_list(rsu_health_instance):
    with patch("common.pgquery.write_db") as mock_write_db:
        snmp_config_list = [
            {"timestamp": "2023-01-01 12:00", "health": 1, "rsu_id": 101},
            {"timestamp": "2023-01-01 13:00", "health": 0, "rsu_id": 102},
        ]
        rsu_health_instance.insert_config_list(snmp_config_list)
        expected_query = (
            "INSERT INTO public.rsu_health(timestamp, health, rsu_id) VALUES "
            "('2023-01-01 12:00', 1, 101),"
            " ('2023-01-01 13:00', 0, 102)"
        )
        mock_write_db.assert_called_once_with(expected_query)


def test_update_postgresql(rsu_health_instance):
    with patch.object(rsu_health_instance, "insert_config_list") as mock_insert:
        rsu_snmp_configs_obj = {
            101: {
                "timestamp": datetime(2023, 1, 1, 12, 0, tzinfo=timezone.utc),
                "health": 1,
            },
            102: {
                "timestamp": datetime(2023, 1, 1, 13, 0, tzinfo=timezone.utc),
                "health": 0,
            },
        }
        rsu_health_instance.update_postgresql(rsu_snmp_configs_obj)
        expected_snmp_config_list = [
            {"timestamp": "2023-01-01 12:00", "health": 1, "rsu_id": 101},
            {"timestamp": "2023-01-01 13:00", "health": 0, "rsu_id": 102},
        ]
        mock_insert.assert_called_once_with(expected_snmp_config_list)


def test_process_rsu_success(rsu_health_instance):
    rsu = {
        "snmp_username": "user",
        "snmp_password": "pass",
        "snmp_encrypt_pw": "encrypt",
        "snmp_version": "1218",
        "ipv4_address": "192.168.1.1",
        "rsu_id": 101,
    }
    response_mock = {"RsuStatus": 1}
    with patch(
        "common.snmp.ntcip1218.rsu_status.get", return_value=(response_mock, 200)
    ):
        rsu_id, config = rsu_health_instance.process_rsu(rsu)
        assert rsu_id == 101
        assert config["health"] == 1
        assert isinstance(config["timestamp"], datetime)


def test_process_rsu_unsupported_version(rsu_health_instance):
    rsu = {
        "snmp_username": "user",
        "snmp_password": "pass",
        "snmp_encrypt_pw": "encrypt",
        "snmp_version": "1234",
        "ipv4_address": "192.168.1.1",
        "rsu_id": 101,
    }
    with patch("logging.info") as mock_logging:
        rsu_id, config = rsu_health_instance.process_rsu(rsu)
        assert rsu_id == 101
        assert config == 5
        mock_logging.assert_called_with(
            "Unsupported SNMP version for collecting security data for 101"
        )


def test_get_snmp_configs():
    rsu_list = [
        {
            "snmp_username": "user1",
            "snmp_password": "pass1",
            "snmp_encrypt_pw": "encrypt1",
            "snmp_version": "1218",
            "ipv4_address": "192.168.1.1",
            "rsu_id": 101,
        },
        {
            "snmp_username": "user2",
            "snmp_password": "pass2",
            "snmp_encrypt_pw": "encrypt2",
            "snmp_version": "1218",
            "ipv4_address": "192.168.1.2",
            "rsu_id": 102,
        },
    ]

    mock_pool_results = [
        (
            101,
            {
                "timestamp": datetime(2023, 1, 1, 12, 0, tzinfo=timezone.utc),
                "health": 1,
            },
        ),
        (
            102,
            {
                "timestamp": datetime(2023, 1, 1, 13, 0, tzinfo=timezone.utc),
                "health": 0,
            },
        ),
    ]

    with patch("common.snmp.update_pg.update_rsu_health.Pool") as mock_pool:
        mock_pool.return_value.__enter__.return_value.map.return_value = (
            mock_pool_results
        )
        rsu_health = UpdatePostgresRsuHealth()
        config_obj = rsu_health.get_snmp_configs(rsu_list)
        assert config_obj == {
            101: {
                "timestamp": datetime(2023, 1, 1, 12, 0, tzinfo=timezone.utc),
                "health": 1,
            },
            102: {
                "timestamp": datetime(2023, 1, 1, 13, 0, tzinfo=timezone.utc),
                "health": 0,
            },
        }
