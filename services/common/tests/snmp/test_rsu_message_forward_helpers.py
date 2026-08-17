import datetime
import pytest
from unittest.mock import patch
from common.snmp.rsu_message_forward_helpers import (
    hex_datetime,
    message_type_rsu41,
    message_type_ntcip1218,
    ip_rsu41,
    ip_ntcip1218,
    protocol,
    rssi_ntcip1218,
    fwdon,
    active,
    startend_rsu41,
    startend_ntcip1218,
    format_snmp_msgfwd_configs,
    TableNames,
)

def test_hex_datetime():
    now = datetime.datetime(2023, 10, 27, 10, 30)
    # 2023 -> 07e7, 10 -> 0a, 27 -> 1b, 10 -> 0a, 30 -> 1e
    assert hex_datetime(now) == "07e70a1b0a1e"
    # With delta=1: 2024 -> 07e8
    assert hex_datetime(now, delta=1) == "07e80a1b0a1e"

@pytest.mark.parametrize(
    "raw_value, expected",
    [
        ('" "', "BSM"),
        ("00 00 00 20", "BSM"),
        ("00 00 80 02", "SPaT"),
        ("80 02", "SPaT"),
        ("00 00 80 03", "TIM"),
        ("E0 00 00 17", "MAP"),
        ("E0 00 00 15", "SSM"),
        ("E0 00 00 16", "SRM"),
        ("unknown", "Other"),
    ],
)
def test_message_type_rsu41(raw_value, expected):
    assert message_type_rsu41(raw_value) == expected

@pytest.mark.parametrize(
    "raw_value, expected",
    [
        ("20000000", "BSM"),
        ("80020000", "SPaT"),
        ("80030000", "TIM"),
        ("E0000017", "MAP"),
        ("e0000017", "MAP"),
        ("E0000015", "SSM"),
        ("E0000016", "SRM"),
        ("unknown", "Other"),
    ],
)
def test_message_type_ntcip1218(raw_value, expected):
    assert message_type_ntcip1218(raw_value) == expected

@pytest.mark.parametrize(
    "raw_value, expected",
    [
        ("C0 A8 01 01", "192.168.1.1"),
        # It takes the last 4 components
        ("00 00 00 00 C0 A8 01 02", "192.168.1.2"),
    ],
)
def test_ip_rsu41(raw_value, expected):
    assert ip_rsu41(raw_value) == expected

def test_ip_ntcip1218():
    assert ip_ntcip1218(" 192.168.1.1 ") == "192.168.1.1"

@pytest.mark.parametrize(
    "raw_value, expected",
    [
        ("1", "TCP"),
        ("tcp(1)", "TCP"),
        ("2", "UDP"),
        ("udp(2)", "UDP"),
        ("3", "Other"),
    ],
)
def test_protocol(raw_value, expected):
    assert protocol(raw_value) == expected

def test_rssi_ntcip1218():
    assert rssi_ntcip1218("-70 dBm") == -70

def test_fwdon():
    assert fwdon("1") == "On"
    assert fwdon("0") == "Off"

@pytest.mark.parametrize(
    "raw_value, expected",
    [
        ("1", "Enabled"),
        ("4", "Enabled"),
        ("active(1)", "Enabled"),
        ("2", "Disabled"),
    ],
)
def test_active(raw_value, expected):
    assert active(raw_value) == expected

@pytest.mark.parametrize(
    "raw_value, expected",
    [
        # 2023-10-27 10:30
        # 2023 -> 07 E7, 10 -> 0A, 27 -> 1B, 10 -> 0A, 30 -> 1E
        ("07 E7 0A 1B 0A 1E", "2023-10-27 10:30"),
        # Padding check: 2023-01-02 03:04
        ("07 E7 01 02 03 04", "2023-01-02 03:04"),
    ],
)
def test_startend_rsu41(raw_value, expected):
    assert startend_rsu41(raw_value) == expected

@pytest.mark.parametrize(
    "raw_value, expected",
    [
        ("2023-10-27,10:30", "2023-10-27 10:30"),
        # Padding check
        ("2023-1-2,3:4", "2023-01-02 03:04"),
    ],
)
def test_startend_ntcip1218(raw_value, expected):
    assert startend_ntcip1218(raw_value) == expected

@patch("common.util.format_date_denver_iso")
def test_format_snmp_msgfwd_configs_dsrc(mock_format_date):
    mock_format_date.side_effect = lambda x: x # Just return the input for simplicity
    
    config_list = [
        {
            "message_type": "bsm",
            "dest_ipv4": "192.168.1.1",
            "dest_port": "1234",
            "start_datetime": "2023-01-01T00:00:00",
            "end_datetime": "2023-12-31T23:59:59",
            "active": "1",
            "security": "1",
            "msgfwd_type": "rsuDsrcFwd",
            "snmp_index": "1"
        }
    ]
    
    expected = {
        "RsuFwdSnmpwalk": {
            "1": {
                "Message Type": "BSM",
                "IP": "192.168.1.1",
                "Port": "1234",
                "Start DateTime": "2023-01-01T00:00:00",
                "End DateTime": "2023-12-31T23:59:59",
                "Config Active": "Enabled",
                "Full WSMP": "Enabled",
            }
        }
    }
    
    result = format_snmp_msgfwd_configs(config_list)
    assert result == expected

@patch("common.util.format_date_denver_iso")
def test_format_snmp_msgfwd_configs_ntcip(mock_format_date):
    mock_format_date.side_effect = lambda x: x
    
    config_list = [
        {
            "message_type": "spat",
            "dest_ipv4": "192.168.1.2",
            "dest_port": "5678",
            "start_datetime": "2023-01-01T00:00:00",
            "end_datetime": "2023-12-31T23:59:59",
            "active": "4",
            "security": "0",
            "msgfwd_type": "rsuReceivedMsg",
            "snmp_index": "2"
        },
        {
            "message_type": "map",
            "dest_ipv4": "192.168.1.3",
            "dest_port": "9012",
            "start_datetime": "2023-01-01T00:00:00",
            "end_datetime": "2023-12-31T23:59:59",
            "active": "1",
            "security": "1",
            "msgfwd_type": "rsuXmitMsgFwding",
            "snmp_index": "3"
        }
    ]
    
    result = format_snmp_msgfwd_configs(config_list)
    
    assert TableNames.RECEIVED.value in result["RsuFwdSnmpwalk"]
    assert TableNames.XMIT.value in result["RsuFwdSnmpwalk"]
    assert result["RsuFwdSnmpwalk"][TableNames.RECEIVED.value]["2"]["Message Type"] == "SPAT"
    assert result["RsuFwdSnmpwalk"][TableNames.XMIT.value]["3"]["Message Type"] == "MAP"

def test_format_snmp_msgfwd_configs_balancing():
    # Only RECEIVED
    config_list = [{
        "message_type": "bsm", "dest_ipv4": "ip", "dest_port": "port",
        "start_datetime": "start", "end_datetime": "end",
        "active": "1", "security": "0", "msgfwd_type": "rsuReceivedMsg", "snmp_index": "1"
    }]
    with patch("common.util.format_date_denver_iso", side_effect=lambda x: x):
        result = format_snmp_msgfwd_configs(config_list)
    assert TableNames.RECEIVED.value in result["RsuFwdSnmpwalk"]
    assert TableNames.XMIT.value in result["RsuFwdSnmpwalk"]
    assert result["RsuFwdSnmpwalk"][TableNames.XMIT.value] == {}

    # Only XMIT
    config_list = [{
        "message_type": "bsm", "dest_ipv4": "ip", "dest_port": "port",
        "start_datetime": "start", "end_datetime": "end",
        "active": "1", "security": "0", "msgfwd_type": "rsuXmitMsgFwding", "snmp_index": "1"
    }]
    with patch("common.util.format_date_denver_iso", side_effect=lambda x: x):
        result = format_snmp_msgfwd_configs(config_list)
    assert TableNames.RECEIVED.value in result["RsuFwdSnmpwalk"]
    assert TableNames.XMIT.value in result["RsuFwdSnmpwalk"]
    assert result["RsuFwdSnmpwalk"][TableNames.RECEIVED.value] == {}

def test_format_snmp_msgfwd_configs_unknown_type():
    config_list = [{
        "message_type": "bsm", "dest_ipv4": "ip", "dest_port": "port",
        "start_datetime": "start", "end_datetime": "end",
        "active": "1", "security": "0", "msgfwd_type": "unknown", "snmp_index": "1"
    }]
    with patch("common.util.format_date_denver_iso", side_effect=lambda x: x):
        result = format_snmp_msgfwd_configs(config_list, rsu_ip="1.1.1.1")
    
    assert result["RsuFwdSnmpwalk"] == {}

def test_format_snmp_msgfwd_configs_case_insensitivity():
    config_list = [
        {
            "message_type": "bsm", "dest_ipv4": "192.168.1.1", "dest_port": "1234",
            "start_datetime": "start", "end_datetime": "end",
            "active": "1", "security": "1", "msgfwd_type": "RSUdsrCFWD", "snmp_index": "1"
        },
        {
            "message_type": "spat", "dest_ipv4": "192.168.1.2", "dest_port": "5678",
            "start_datetime": "start", "end_datetime": "end",
            "active": "4", "security": "0", "msgfwd_type": "RsuReceiveDMsG", "snmp_index": "2"
        },
        {
            "message_type": "map", "dest_ipv4": "192.168.1.3", "dest_port": "9012",
            "start_datetime": "start", "end_datetime": "end",
            "active": "1", "security": "1", "msgfwd_type": "rsuxmitmsgfwding", "snmp_index": "3"
        }
    ]
    with patch("common.util.format_date_denver_iso", side_effect=lambda x: x):
        result = format_snmp_msgfwd_configs(config_list)
    
    # Check DSRC
    assert "1" in result["RsuFwdSnmpwalk"]
    # Check RECEIVED
    assert TableNames.RECEIVED.value in result["RsuFwdSnmpwalk"]
    assert "2" in result["RsuFwdSnmpwalk"][TableNames.RECEIVED.value]
    # Check XMIT
    assert TableNames.XMIT.value in result["RsuFwdSnmpwalk"]
    assert "3" in result["RsuFwdSnmpwalk"][TableNames.XMIT.value]
