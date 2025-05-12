import pytest
from unittest.mock import patch, MagicMock
from api.src.rsu_snmpset import (
    fetch_index,
    perform_snmp_operation,
    execute_rsufwdsnmpset,
)


@pytest.fixture
def mock_rsu_info():
    return {
        "rsu_id": 1,
        "snmp_username": "test_user",
        "snmp_password": "test_pass",
        "snmp_encrypt_pw": "test_encrypt",
        "snmp_version": "1218",
    }


@pytest.fixture
def mock_args():
    return {
        "msg_type": "bsm",
        "dest_ip": "192.168.1.1",
        "security": "test_security",
    }


def test_fetch_index_add1(mock_rsu_info):
    with patch("api.src.rsu_snmpset.nticp1218_rsu_mf.get") as mock_get:
        mock_get.return_value = (
            {
                "RsuFwdSnmpwalk": {
                    "rsuReceivedMsgTable": {"1": {}, "2": {}, "3": {}},
                }
            },
            200,
        )
        index = fetch_index("add", "192.168.1.1", mock_rsu_info, "bsm")
        assert index == 4


def test_fetch_index_add2(mock_rsu_info):
    with patch("api.src.rsu_snmpset.nticp1218_rsu_mf.get") as mock_get:
        mock_get.return_value = (
            {
                "RsuFwdSnmpwalk": {
                    "rsuXmitMsgFwdingTable": {"1": {}, "2": {}},
                }
            },
            200,
        )
        index = fetch_index("add", "192.168.1.1", mock_rsu_info, "spat")
        assert index == 3


def test_fetch_index_del(mock_rsu_info):
    with patch("api.src.rsu_snmpset.nticp1218_rsu_mf.get") as mock_get:
        mock_get.return_value = (
            {
                "RsuFwdSnmpwalk": {
                    "rsuReceivedMsgTable": {
                        "1": {"Message Type": "BSM", "IP": "192.168.1.1"}
                    },
                    "rsuXmitMsgFwdingTable": {},
                }
            },
            200,
        )
        index = fetch_index("del", "192.168.1.1", mock_rsu_info, "bsm")
        assert index == 1


def test_fetch_index_invalid_snmp_version(mock_rsu_info):
    mock_rsu_info["snmp_version"] = "unsupported"
    index = fetch_index("add", "192.168.1.1", mock_rsu_info, "bsm")
    assert index == -1


def test_perform_snmp_operation_set(mock_rsu_info, mock_args):
    with patch("api.src.rsu_snmpset.nticp1218_rsu_mf.set") as mock_set:
        mock_set.return_value = ({"result": "success"}, 200)
        data, code = perform_snmp_operation(
            "rsufwdsnmpset-add", "192.168.1.1", mock_rsu_info, {}, mock_args, 1
        )
        assert code == 200
        assert data == {"result": "success"}


def test_perform_snmp_operation_delete(mock_rsu_info, mock_args):
    with patch("api.src.rsu_snmpset.nticp1218_rsu_mf.delete") as mock_delete:
        mock_delete.return_value = ({"result": "deleted"}, 200)
        data, code = perform_snmp_operation(
            "rsufwdsnmpset-del", "192.168.1.1", mock_rsu_info, {}, mock_args, 1
        )
        assert code == 200
        assert data == {"result": "deleted"}


def test_execute_rsufwdsnmpset(mock_rsu_info, mock_args):
    with patch(
        "api.src.rsu_snmpset.rsu_commands.fetch_rsu_info"
    ) as mock_fetch_rsu_info, patch(
        "api.src.rsu_snmpset.fetch_index"
    ) as mock_fetch_index, patch(
        "api.src.rsu_snmpset.perform_snmp_operation"
    ) as mock_perform_snmp_operation, patch(
        "api.src.rsu_snmpset.update_rsu_snmp_pg.get_snmp_msgfwd_configs"
    ) as mock_get_configs, patch(
        "api.src.rsu_snmpset.update_rsu_snmp_pg.update_postgresql"
    ) as mock_update_pg:

        mock_fetch_rsu_info.return_value = mock_rsu_info
        mock_fetch_index.return_value = 1
        mock_perform_snmp_operation.return_value = ({"result": "success"}, 200)
        mock_get_configs.return_value = []
        mock_update_pg.return_value = None

        rsu_list = ["192.168.1.1"]
        result = execute_rsufwdsnmpset(
            "rsufwdsnmpset-add", "test_org", rsu_list, mock_args
        )
        assert result["192.168.1.1"]["code"] == 200
        assert result["192.168.1.1"]["data"] == {"result": "success"}
