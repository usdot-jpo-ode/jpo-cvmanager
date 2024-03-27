from unittest.mock import Mock, patch
from common import snmpwalk_helpers


def test_message_type_rsu41():
    # test BSM PSIDs
    val = '" "'
    assert snmpwalk_helpers.message_type_rsu41(val) == "BSM"
    val = "00 00 00 20"
    assert snmpwalk_helpers.message_type_rsu41(val) == "BSM"

    # test SPAT PSIDs
    val = "00 00 80 02"
    assert snmpwalk_helpers.message_type_rsu41(val) == "SPaT"
    val = "80 02"
    assert snmpwalk_helpers.message_type_rsu41(val) == "SPaT"

    # test TIM PSID
    val = "00 00 80 03"
    assert snmpwalk_helpers.message_type_rsu41(val) == "TIM"

    # test MAP PSID
    val = "E0 00 00 17"
    assert snmpwalk_helpers.message_type_rsu41(val) == "MAP"

    # test SSM PSID
    val = "E0 00 00 15"
    assert snmpwalk_helpers.message_type_rsu41(val) == "SSM"

    # test SRM PSID
    val = "E0 00 00 16"
    assert snmpwalk_helpers.message_type_rsu41(val) == "SRM"

    # test other PSID
    val = "00 00 00 00"
    assert snmpwalk_helpers.message_type_rsu41(val) == "Other"


def test_message_type_ntcip1218():
    # test BSM PSID
    val = "20000000"
    assert snmpwalk_helpers.message_type_ntcip1218(val) == "BSM"

    # test SPAT PSID
    val = "80020000"
    assert snmpwalk_helpers.message_type_ntcip1218(val) == "SPaT"

    # test TIM PSID
    val = "80030000"
    assert snmpwalk_helpers.message_type_ntcip1218(val) == "TIM"

    # test MAP PSID
    val = "e0000017"
    assert snmpwalk_helpers.message_type_ntcip1218(val) == "MAP"

    # test SSM PSID
    val = "e0000015"
    assert snmpwalk_helpers.message_type_ntcip1218(val) == "SSM"

    # test SRM PSID
    val = "e0000016"
    assert snmpwalk_helpers.message_type_ntcip1218(val) == "SRM"

    # test other PSID
    val = "00000000"
    assert snmpwalk_helpers.message_type_ntcip1218(val) == "Other"


def test_ip():
    val = "c0 a8 00 0a"
    assert snmpwalk_helpers.ip_rsu41(val) == "192.168.0.10"


def test_ip_ntcip1218():
    val = "10.0.0.1"
    assert snmpwalk_helpers.ip_ntcip1218(val) == "10.0.0.1"


def test_protocol():
    val = "1"
    assert snmpwalk_helpers.protocol(val) == "TCP"
    val = "2"
    assert snmpwalk_helpers.protocol(val) == "UDP"
    val = "14"
    assert snmpwalk_helpers.protocol(val) == "Other"


def test_fwdon():
    val = "1"
    assert snmpwalk_helpers.fwdon(val) == "On"
    val = "0"
    assert snmpwalk_helpers.fwdon(val) == "Off"


def test_active():
    val = "1"
    assert snmpwalk_helpers.active(val) == "Enabled"
    val = "4"
    assert snmpwalk_helpers.active(val) == "Enabled"
    val = "17"
    assert snmpwalk_helpers.active(val) == "Disabled"


def test_startend_rsu41():
    # prepare hex input
    hex_input = "05 06 07 08 09 10"

    # call function
    output = snmpwalk_helpers.startend_rsu41(hex_input)

    # verify
    expected_output = "1286-07-08 09:16"
    assert output == expected_output


def test_startend_ntcip1218():
    # prepare hex input
    str_input = "2024-02-05,9:5:31.45"

    # call function
    output = snmpwalk_helpers.startend_ntcip1218(str_input)

    # verify
    expected_output = "2024-02-05 09:05"
    assert output == expected_output
