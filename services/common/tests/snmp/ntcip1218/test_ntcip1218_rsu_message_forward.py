import datetime
from unittest.mock import MagicMock, call, patch, Mock, create_autospec
import common.snmp.ntcip1218.rsu_message_forward as rsu_message_forward
import subprocess


# static values
rsu_ip = "192.168.0.20"
snmp_creds = {
    "username": "test_username",
    "password": "test_password",
    "encrypt_pw": None,
}
dest_ip = "192.168.0.10"
rsu_index = 1


def test_ip_to_hex():
    ip = "192.168.0.10"
    expected = "00000000000000000000FFFFc0a8000a"
    assert rsufwdsnmpset.ip_to_hex(ip) == expected


def test_hex_datetime():
    dt = datetime.datetime(2020, 1, 1, 1, 1, 1)
    expected = "07e401010101"
    assert rsufwdsnmpset.hex_datetime(dt) == expected


@patch("common.rsufwdsnmpset.subprocess.run")
def test_set_rsu_status_operate(mock_run):
    # mock
    mock_run.return_value = Mock()
    mock_run.return_value.stdout = Mock()
    mock_run.return_value.stdout.decode.return_value = "test_output"

    # call
    response = rsufwdsnmpset.set_rsu_status(rsu_ip, snmp_creds, True)

    # check
    mock_run.assert_called_once_with(
        "snmpset -v 3 -u test_username -a SHA -A test_password -x AES -X test_password -l authpriv 192.168.0.20 RSU-MIB:rsuMode.0 i 4",
        shell=True,
        capture_output=True,
        check=True,
    )
    expected_response = "success"
    assert response == expected_response


@patch("common.rsufwdsnmpset.subprocess.run")
def test_set_rsu_status_standby(mock_run):
    # mock
    mock_run.return_value = Mock()
    mock_run.return_value.stdout = Mock()
    mock_run.return_value.stdout.decode.return_value = "test_output"

    # call
    response = rsufwdsnmpset.set_rsu_status(rsu_ip, snmp_creds, False)

    # check
    mock_run.assert_called_once_with(
        "snmpset -v 3 -u test_username -a SHA -A test_password -x AES -X test_password -l authpriv 192.168.0.20 RSU-MIB:rsuMode.0 i 2",
        shell=True,
        capture_output=True,
        check=True,
    )
    expected_response = "success"
    assert response == expected_response


@patch("common.rsufwdsnmpset.subprocess.run")
def test_perform_snmp_mods(subprocess_run):
    # mock
    subprocess_run.return_value = Mock()
    subprocess_run.return_value.stdout = Mock()
    subprocess_run.return_value.stdout.decode.return_value = "test_output"

    # call
    snmp_mods = [
        {"oid": "test_oid1", "value": "test_value1"},
        {"oid": "test_oid2", "value": "test_value2"},
    ]
    response = rsufwdsnmpset.perform_snmp_mods(snmp_mods)

    # check
    subprocess_run.assert_has_calls(
        [
            call(snmp_mods[0], shell=True, capture_output=True, check=True),
            call().stdout.decode("utf-8"),
            call(snmp_mods[1], shell=True, capture_output=True, check=True),
            call().stdout.decode("utf-8"),
        ]
    )
    expected_response = None
    assert response == expected_response


@patch("common.rsufwdsnmpset.subprocess.run")
@patch("common.rsufwdsnmpset.perform_snmp_mods")
def test_config_txrxmsg_tx(mock_perform_snmp_mods, mock_subprocess_run):
    # mock
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = "test_output"

    # call
    udp_port = 1234
    psid = 5678
    tx = True
    security = 1
    result = rsufwdsnmpset.config_txrxmsg(
        rsu_ip, snmp_creds, dest_ip, udp_port, rsu_index, psid, security, tx
    )

    # check
    mock_perform_snmp_mods.assert_called_once()
    expected_result = (
        "Successfully completed the NTCIP-1218 SNMPSET configuration",
        200,
    )
    assert result == expected_result


@patch("common.rsufwdsnmpset.subprocess.run")
@patch("common.rsufwdsnmpset.perform_snmp_mods")
def test_config_txrxmsg_no_tx(mock_perform_snmp_mods, mock_subprocess_run):
    # mock
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = "test_output"

    # call
    udp_port = 1234
    psid = 5678
    tx = False
    security = 0
    result = rsufwdsnmpset.config_txrxmsg(
        rsu_ip, snmp_creds, dest_ip, udp_port, rsu_index, psid, security, tx
    )

    # check
    mock_perform_snmp_mods.assert_called_once()
    expected_result = (
        "Successfully completed the NTCIP-1218 SNMPSET configuration",
        200,
    )
    assert result == expected_result


@patch("common.rsufwdsnmpset.set_rsu_status")
def test_config_rsudsrcfwd(mock_set_rsu_status):
    manufacturer = "test_manufacturer"
    udp_port = 1234
    psid = 1

    # call function
    rsufwdsnmpset.config_rsudsrcfwd(
        rsu_ip, manufacturer, snmp_creds, dest_ip, udp_port, rsu_index, psid
    )

    # check calls
    mock_set_rsu_status.assert_has_calls(
        [
            call(rsu_ip, snmp_creds, operate=False),
            call().__ne__("success"),
            call(rsu_ip, snmp_creds, operate=True),
        ]
    )


@patch("common.rsufwdsnmpset.set_rsu_status")
@patch("common.rsufwdsnmpset.subprocess.run")
def test_config_del_rsu41(mock_subprocess_run, mock_set_rsu_status):
    # mock subprocess.run
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = "test_output"

    # mock set_rsu_status
    mock_set_rsu_status.return_value = "success"

    # prepare args
    snmp_version = "41"
    msg_type = "test_msg_type"

    # call function
    rsufwdsnmpset.config_del(rsu_ip, snmp_version, snmp_creds, msg_type, rsu_index)

    # check calls
    mock_set_rsu_status.assert_has_calls(
        [
            call(rsu_ip, snmp_creds, operate=False),
            call(rsu_ip, snmp_creds, operate=True),
        ]
    )
    mock_subprocess_run.assert_called_once()


@patch("common.rsufwdsnmpset.set_rsu_status")
@patch("common.rsufwdsnmpset.subprocess.run")
def test_config_del_rsu41_set_rsu_status_failure(
    mock_subprocess_run, mock_set_rsu_status
):
    # mock subprocess.run
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = "test_output"

    # mock set_rsu_status
    mock_set_rsu_status.return_value = "failure"

    # prepare args
    snmp_version = "41"
    msg_type = "test_msg_type"

    # call function
    result = rsufwdsnmpset.config_del(
        rsu_ip, snmp_version, snmp_creds, msg_type, rsu_index
    )

    # check calls
    mock_set_rsu_status.assert_has_calls(
        [
            call(rsu_ip, snmp_creds, operate=False),
            call(rsu_ip, snmp_creds, operate=True),
        ]
    )
    mock_subprocess_run.assert_not_called()
    assert result == ("failure", 500)


@patch("common.rsufwdsnmpset.set_rsu_status")
@patch("common.rsufwdsnmpset.subprocess.run")
def test_config_del_ntcip1218_bsm(mock_subprocess_run, mock_set_rsu_status):
    # mock
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = "test_output"
    mock_set_rsu_status.return_value = "success"

    # call
    snmp_version = "1218"
    msg_type = "bsm"
    result = rsufwdsnmpset.config_del(
        rsu_ip, snmp_version, snmp_creds, msg_type, rsu_index
    )

    # check
    mock_set_rsu_status.assert_not_called()
    mock_subprocess_run.assert_called_once_with(
        "snmpset -v 3 -u test_username -a SHA -A test_password -x AES -X test_password -l authpriv 192.168.0.20 NTCIP1218-v01:rsuReceivedMsgStatus.1 i 6 ",
        shell=True,
        capture_output=True,
        check=True,
    )
    assert result == ("Successfully deleted the NTCIP 1218 SNMPSET configuration", 200)


@patch("common.rsufwdsnmpset.set_rsu_status")
@patch("common.rsufwdsnmpset.subprocess.run")
def test_config_del_ntcip1218_spat(mock_subprocess_run, mock_set_rsu_status):
    # mock
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = "test_output"
    mock_set_rsu_status.return_value = "success"

    # call
    snmp_version = "1218"
    msg_type = "spat"
    result = rsufwdsnmpset.config_del(
        rsu_ip, snmp_version, snmp_creds, msg_type, rsu_index
    )

    # check
    mock_set_rsu_status.assert_not_called()
    mock_subprocess_run.assert_called_once_with(
        "snmpset -v 3 -u test_username -a SHA -A test_password -x AES -X test_password -l authpriv 192.168.0.20 NTCIP1218-v01:rsuXmitMsgFwdingStatus.1 i 6 ",
        shell=True,
        capture_output=True,
        check=True,
    )
    assert result == ("Successfully deleted the NTCIP 1218 SNMPSET configuration", 200)


@patch("common.rsufwdsnmpset.set_rsu_status")
@patch("common.rsufwdsnmpset.subprocess.run")
def test_config_del_ntcip1218_map(mock_subprocess_run, mock_set_rsu_status):
    # mock
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = "test_output"
    mock_set_rsu_status.return_value = "success"

    # call
    snmp_version = "1218"
    msg_type = "map"
    result = rsufwdsnmpset.config_del(
        rsu_ip, snmp_version, snmp_creds, msg_type, rsu_index
    )

    # check
    mock_set_rsu_status.assert_not_called()
    mock_subprocess_run.assert_called_once_with(
        "snmpset -v 3 -u test_username -a SHA -A test_password -x AES -X test_password -l authpriv 192.168.0.20 NTCIP1218-v01:rsuXmitMsgFwdingStatus.1 i 6 ",
        shell=True,
        capture_output=True,
        check=True,
    )
    assert result == ("Successfully deleted the NTCIP 1218 SNMPSET configuration", 200)


@patch("common.rsufwdsnmpset.set_rsu_status")
@patch("common.rsufwdsnmpset.subprocess.run")
def test_config_del_ntcip1218_ssm(mock_subprocess_run, mock_set_rsu_status):
    # mock
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = "test_output"
    mock_set_rsu_status.return_value = "success"

    # call
    snmp_version = "1218"
    msg_type = "ssm"
    result = rsufwdsnmpset.config_del(
        rsu_ip, snmp_version, snmp_creds, msg_type, rsu_index
    )

    # check
    mock_set_rsu_status.assert_not_called()
    mock_subprocess_run.assert_called_once_with(
        "snmpset -v 3 -u test_username -a SHA -A test_password -x AES -X test_password -l authpriv 192.168.0.20 NTCIP1218-v01:rsuXmitMsgFwdingStatus.1 i 6 ",
        shell=True,
        capture_output=True,
        check=True,
    )
    assert result == ("Successfully deleted the NTCIP 1218 SNMPSET configuration", 200)


@patch("common.rsufwdsnmpset.set_rsu_status")
@patch("common.rsufwdsnmpset.subprocess.run")
def test_config_del_ntcip1218_srm(mock_subprocess_run, mock_set_rsu_status):
    # mock
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = "test_output"
    mock_set_rsu_status.return_value = "success"

    # call
    snmp_version = "1218"
    msg_type = "srm"
    result = rsufwdsnmpset.config_del(
        rsu_ip, snmp_version, snmp_creds, msg_type, rsu_index
    )

    # check
    mock_set_rsu_status.assert_not_called()
    mock_subprocess_run.assert_called_once_with(
        "snmpset -v 3 -u test_username -a SHA -A test_password -x AES -X test_password -l authpriv 192.168.0.20 NTCIP1218-v01:rsuReceivedMsgStatus.1 i 6 ",
        shell=True,
        capture_output=True,
        check=True,
    )
    assert result == ("Successfully deleted the NTCIP 1218 SNMPSET configuration", 200)


@patch("common.rsufwdsnmpset.set_rsu_status")
@patch("common.rsufwdsnmpset.subprocess.run")
def test_config_del_ntcip1218_tim(mock_subprocess_run, mock_set_rsu_status):
    # mock
    mock_subprocess_run.return_value = Mock()
    mock_subprocess_run.return_value.stdout = Mock()
    mock_subprocess_run.return_value.stdout.decode.return_value = "test_output"
    mock_set_rsu_status.return_value = "success"

    # call
    snmp_version = "1218"
    msg_type = "tim"
    result = rsufwdsnmpset.config_del(
        rsu_ip, snmp_version, snmp_creds, msg_type, rsu_index
    )

    # check
    mock_set_rsu_status.assert_not_called()
    mock_subprocess_run.assert_called_once_with(
        "snmpset -v 3 -u test_username -a SHA -A test_password -x AES -X test_password -l authpriv 192.168.0.20 NTCIP1218-v01:rsuXmitMsgFwdingStatus.1 i 6 ",
        shell=True,
        capture_output=True,
        check=True,
    )
    assert result == ("Successfully deleted the NTCIP 1218 SNMPSET configuration", 200)


@patch("common.rsufwdsnmpset.set_rsu_status")
def test_config_del_unsupported_snmp_version(mock_set_rsu_status):
    # prepare args
    snmp_version = "test_version"
    msg_type = "test_msg_type"

    # call function
    result = rsufwdsnmpset.config_del(
        rsu_ip, snmp_version, snmp_creds, msg_type, rsu_index
    )

    # check result
    expected_response = (
        "Supported SNMP protocol versions are currently only RSU 4.1 and NTCIP 1218"
    )
    expected_code = 501
    assert result == (expected_response, expected_code)

    # check calls
    mock_set_rsu_status.assert_not_called()


@patch("common.rsufwdsnmpset.config_rsudsrcfwd")
@patch("common.rsufwdsnmpset.config_txrxmsg")
def test_config_init_commsignia_bsm(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd.return_value = "success"
    snmp_version = "41"
    manufacturer = "Commsignia"
    msg_type = "BSM"
    security = 1
    result = rsufwdsnmpset.config_init(
        rsu_ip,
        manufacturer,
        snmp_version,
        snmp_creds,
        dest_ip,
        msg_type,
        rsu_index,
        security,
    )
    expected_result = "success"
    assert result == expected_result
    mock_config_msgfwd.assert_called_once_with(
        rsu_ip, manufacturer, snmp_creds, dest_ip, "46800", rsu_index, "20"
    )
    mock_config_msgfwd_yunex.assert_not_called()


@patch("common.rsufwdsnmpset.config_rsudsrcfwd")
@patch("common.rsufwdsnmpset.config_txrxmsg")
def test_config_init_commsignia_spat(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd.return_value = "success"
    snmp_version = "41"
    manufacturer = "Commsignia"
    msg_type = "SPaT"
    security = 0
    result = rsufwdsnmpset.config_init(
        rsu_ip,
        manufacturer,
        snmp_version,
        snmp_creds,
        dest_ip,
        msg_type,
        rsu_index,
        security,
    )
    expected_result = "success"
    assert result == expected_result
    mock_config_msgfwd.assert_called_once_with(
        rsu_ip, manufacturer, snmp_creds, dest_ip, "44910", rsu_index, "8002"
    )
    mock_config_msgfwd_yunex.assert_not_called()


@patch("common.rsufwdsnmpset.config_rsudsrcfwd")
@patch("common.rsufwdsnmpset.config_txrxmsg")
def test_config_init_commsignia_map(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd.return_value = "success"
    snmp_version = "41"
    manufacturer = "Commsignia"
    msg_type = "MAP"
    security = 1
    result = rsufwdsnmpset.config_init(
        rsu_ip,
        manufacturer,
        snmp_version,
        snmp_creds,
        dest_ip,
        msg_type,
        rsu_index,
        security,
    )
    expected_result = "success"
    assert result == expected_result
    mock_config_msgfwd.assert_called_once_with(
        rsu_ip,
        manufacturer,
        snmp_creds,
        dest_ip,
        "44920",
        rsu_index,
        "E0000017",
        raw=True,
    )
    mock_config_msgfwd_yunex.assert_not_called()


@patch("common.rsufwdsnmpset.config_rsudsrcfwd")
@patch("common.rsufwdsnmpset.config_txrxmsg")
def test_config_init_commsignia_ssm(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd.return_value = "success"
    snmp_version = "41"
    manufacturer = "Commsignia"
    msg_type = "SSM"
    security = 0
    result = rsufwdsnmpset.config_init(
        rsu_ip,
        manufacturer,
        snmp_version,
        snmp_creds,
        dest_ip,
        msg_type,
        rsu_index,
        security,
    )
    expected_result = "success"
    assert result == expected_result
    mock_config_msgfwd.assert_called_once_with(
        rsu_ip,
        manufacturer,
        snmp_creds,
        dest_ip,
        "44900",
        rsu_index,
        "E0000015",
        raw=True,
    )
    mock_config_msgfwd_yunex.assert_not_called()


@patch("common.rsufwdsnmpset.config_rsudsrcfwd")
@patch("common.rsufwdsnmpset.config_txrxmsg")
def test_config_init_commsignia_srm(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd.return_value = "success"
    snmp_version = "41"
    manufacturer = "Commsignia"
    msg_type = "SRM"
    security = 1
    result = rsufwdsnmpset.config_init(
        rsu_ip,
        manufacturer,
        snmp_version,
        snmp_creds,
        dest_ip,
        msg_type,
        rsu_index,
        security,
    )
    expected_result = "success"
    assert result == expected_result
    mock_config_msgfwd.assert_called_once_with(
        rsu_ip,
        manufacturer,
        snmp_creds,
        dest_ip,
        "44930",
        rsu_index,
        "E0000016",
        raw=True,
    )
    mock_config_msgfwd_yunex.assert_not_called()


@patch("common.rsufwdsnmpset.config_rsudsrcfwd")
@patch("common.rsufwdsnmpset.config_txrxmsg")
def test_config_init_commsignia_tim(mock_config_msgfwd_yunex, mock_config_msgfwd):
    mock_config_msgfwd.return_value = "success"
    snmp_version = "41"
    manufacturer = "Commsignia"
    msg_type = "TIM"
    security = 1
    result = rsufwdsnmpset.config_init(
        rsu_ip,
        manufacturer,
        snmp_version,
        snmp_creds,
        dest_ip,
        msg_type,
        rsu_index,
        security,
    )
    expected_result = "success"
    assert result == expected_result
    mock_config_msgfwd.assert_called_once_with(
        rsu_ip,
        manufacturer,
        snmp_creds,
        dest_ip,
        "47900",
        rsu_index,
        "8003",
        raw=True,
    )
    mock_config_msgfwd_yunex.assert_not_called()


@patch("common.rsufwdsnmpset.config_rsudsrcfwd")
@patch("common.rsufwdsnmpset.config_txrxmsg")
def test_config_init_unsupported_msg_type_rsu41(
    mock_config_msfwd_yunex, mock_config_msgfwd
):
    snmp_version = "41"
    manufacturer = "test"
    msg_type = "test_msg_type"
    security = 0
    result = rsufwdsnmpset.config_init(
        rsu_ip,
        manufacturer,
        snmp_version,
        snmp_creds,
        dest_ip,
        msg_type,
        rsu_index,
        security,
    )
    expected_result = (
        "Supported message type is currently only BSM, SPaT, MAP, SSM, SRM and TIM",
        501,
    )
    assert result == expected_result
    mock_config_msgfwd.assert_not_called()
    mock_config_msfwd_yunex.assert_not_called()


@patch("common.rsufwdsnmpset.config_rsudsrcfwd")
@patch("common.rsufwdsnmpset.config_txrxmsg")
def test_config_init_unsupported_msg_type_ntcip1218(
    mock_config_msfwd_yunex, mock_config_msgfwd
):
    snmp_version = "1218"
    manufacturer = "test"
    msg_type = "test_msg_type"
    security = 1
    result = rsufwdsnmpset.config_init(
        rsu_ip,
        manufacturer,
        snmp_version,
        snmp_creds,
        dest_ip,
        msg_type,
        rsu_index,
        security,
    )
    expected_result = (
        "Supported message type is currently only BSM, SPaT, MAP, SSM, SRM and TIM",
        501,
    )
    assert result == expected_result
    mock_config_msgfwd.assert_not_called()
    mock_config_msfwd_yunex.assert_not_called()


@patch("common.rsufwdsnmpset.config_rsudsrcfwd")
@patch("common.rsufwdsnmpset.config_txrxmsg")
def test_config_init_unsupported_snmp_version(
    mock_config_msfwd_yunex, mock_config_msgfwd
):
    snmp_version = "test_snmp_version"
    manufacturer = "test_manufacturer"
    msg_type = "test_msg_type"
    security = 0
    result = rsufwdsnmpset.config_init(
        rsu_ip,
        manufacturer,
        snmp_version,
        snmp_creds,
        dest_ip,
        msg_type,
        rsu_index,
        security,
    )
    expected_result = (
        "Supported SNMP protocol versions are currently only RSU 4.1 and NTCIP 1218",
        501,
    )
    assert result == expected_result
    mock_config_msgfwd.assert_not_called()
    mock_config_msfwd_yunex.assert_not_called()


@patch("common.rsufwdsnmpset.SnmpsetSchema.validate")
@patch("common.rsufwdsnmpset.config_init")
def test_post(mock_config_init, mock_validate):
    # mock validate
    mock_validate.return_value = None

    # mock config_init
    mock_config_init.return_value = {"status": "success"}, 200

    # prepare request
    request = {
        "args": {
            "msg_type": "init",
            "dest_ip": "192.168.0.20",
            "rsu_index": 1,
            "security": 1,
        },
        "rsu_ip": "192.168.0.20",
        "manufacturer": "test_manufacturer",
        "snmp_version": "test_snmp_version",
        "snmp_creds": {"username": "test_username", "password": "test_password"},
    }

    # call function
    result = rsufwdsnmpset.post(request)

    # check calls
    mock_validate.assert_called_once()
    mock_config_init.assert_called_once_with(
        rsu_ip=request["rsu_ip"],
        manufacturer=request["manufacturer"],
        snmp_version=request["snmp_version"],
        snmp_creds=request["snmp_creds"],
        dest_ip=request["args"]["dest_ip"],
        msg_type=request["args"]["msg_type"],
        index=request["args"]["rsu_index"],
        security=request["args"]["security"],
    )

    # check result
    expected_code = 200
    expected_response = {"status": "success"}
    expected_result = {"RsuFwdSnmpset": expected_response}, expected_code
    assert result == expected_result


@patch("common.rsufwdsnmpset.SnmpsetSchema.validate")
@patch("common.rsufwdsnmpset.config_init")
def test_post_error(mock_config_init, mock_validate):
    # mock validate
    mock_validate.return_value = "error"

    # prepare request
    request = {
        "args": {
            "msg_type": "init",
            "rsu_index": 1,
            "security": 0,
        },
        "rsu_ip": "192.168.0.20",
        "manufacturer": "test_manufacturer",
        "snmp_version": "test_snmp_version",
        "snmp_creds": {"username": "test_username", "password": "test_password"},
    }

    # call function
    result = rsufwdsnmpset.post(request)

    # check calls
    mock_validate.assert_called_once_with(request["args"])
    mock_config_init.assert_not_called()

    # check result
    assert result == (
        "The provided args does not match required values: "
        + mock_validate.return_value,
        400,
    )


@patch("common.rsufwdsnmpset.SnmpsetDeleteSchema.validate")
@patch("common.rsufwdsnmpset.config_del")
def test_delete(mock_config_del, mock_validate):
    # mock validate
    mock_validate.return_value = None

    # mock config_del
    mock_config_del.return_value = {"status": "success"}, 200

    # prepare request
    request = {
        "args": {
            "msg_type": "delete",
            "rsu_index": 1,
        },
        "rsu_ip": "192.168.0.20",
        "manufacturer": "test_manufacturer",
        "snmp_version": "test_snmp_version",
        "snmp_creds": {"username": "test_username", "password": "test_password"},
    }

    # call function
    result = rsufwdsnmpset.delete(request)

    # check calls
    mock_validate.assert_called_once()
    mock_config_del.assert_called_once_with(
        rsu_ip=request["rsu_ip"],
        snmp_version=request["snmp_version"],
        snmp_creds=request["snmp_creds"],
        msg_type=request["args"]["msg_type"],
        rsu_index=request["args"]["rsu_index"],
    )

    # check result
    expected_code = 200
    expected_response = {"status": "success"}
    expected_result = {"RsuFwdSnmpset": expected_response}, expected_code
    assert result == expected_result


@patch("common.rsufwdsnmpset.SnmpsetDeleteSchema.validate")
@patch("common.rsufwdsnmpset.config_del")
def test_delete_error(mock_config_del, mock_validate):
    # mock validate
    mock_validate.return_value = "error"

    # prepare request
    request = {
        "args": {
            "msg_type": "delete",
            "rsu_index": 1,
        },
        "rsu_ip": "192.168.0.20",
        "manufacturer": "test_manufacturer",
        "snmp_creds": {"username": "test_username", "password": "test_password"},
    }

    # call function
    result = rsufwdsnmpset.delete(request)

    # check calls
    mock_validate.assert_called_once()
    mock_config_del.assert_not_called()

    # check result
    expected_result = (
        "The provided args does not match required values: "
        + mock_validate.return_value,
        400,
    )
    assert result == expected_result


@patch(
    "common.snmp.ntcip1218.rsu_message_forward.snmpcredential.get_authstring",
    return_value="auth_string",
)
@patch(
    "common.snmp.ntcip1218.rsu_message_forward.snmperrorcheck.check_error_type",
    return_value="error message",
)
@patch(
    "common.snmp.ntcip1218.rsu_message_forward.perform_snmp_mods",
    side_effect=raise_called_process_error,
)
def test_ntcip1218_rsu_message_forward_set_exception(
    mock_perform_snmp_mods, mock_check_error_type, mock_get_authstring
):
    # Setup
    rsu_ip = "192.168.1.1"
    snmp_creds = {"username": "username", "password": "password"}
    dest_ip = "192.168.1.2"
    udp_port = "44920"
    rsu_index = 1
    psid = "20"
    tx = True
    security = 1

    # Call the function
    response, code = rsu_message_forward.set(
        rsu_ip, snmp_creds, dest_ip, udp_port, rsu_index, psid, security, tx
    )
    mock_check_error_type.assert_called_once_with("any")
    # Assert the function result
    assert code == 500
    assert response == "error message"


@patch("common.snmp.ntcip1218.rsu_message_forward.subprocess.run")
@patch(
    "common.snmp.ntcip1218.rsu_message_forward.snmperrorcheck.check_error_type",
    return_value="test error",
)
def test_ntcip1218_rsu_message_forward_delete_exception(
    mock_check_error_type, mock_run
):
    # Setup
    rsu_ip = "192.168.1.1"
    snmp_creds = {"username": "username", "password": "password", "encrypt_pw": None}
    msg_type = (
        "bsm"  # This can be any of the following: ['bsm', 'spat', 'map', 'ssm', 'srm']
    )
    rsu_index = 1

    # mock subprocess.run to raise CalledProcessError
    mock_error = Mock()
    mock_error.stderr.decode.return_value = "any\n"
    mock_run.return_value = ["hello"]
    mock_run.side_effect = subprocess.CalledProcessError(
        1, cmd=["any"], stderr=b"\n error line"
    )

    # Call the function
    response, code = rsu_message_forward.delete(rsu_ip, snmp_creds, msg_type, rsu_index)

    # Assert the function result
    expected_response = "test error"
    expected_code = 500
    assert code == expected_code
    assert response == expected_response
