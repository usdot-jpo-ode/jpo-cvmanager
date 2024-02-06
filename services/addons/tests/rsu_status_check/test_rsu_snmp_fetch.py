from mock import MagicMock, call, patch
from addons.images.rsu_status_check import rsu_snmp_fetch

snmp_config_data = [
    {
        "rsu_id": 1,
        "msgfwd_type": 2,
        "snmp_index": 1,
        "message_type": "BSM",
        "dest_ipv4": "5.5.5.5",
        "dest_port": 46800,
        "start_datetime": "2024-02-05 00:00",
        "end_datetime": "2034-02-05 00:00",
        "active": "1",
    },
    {
        "rsu_id": 2,
        "msgfwd_type": 3,
        "snmp_index": 1,
        "message_type": "MAP",
        "dest_ipv4": "5.5.5.5",
        "dest_port": 44920,
        "start_datetime": "2024-02-05 00:00",
        "end_datetime": "2034-02-05 00:00",
        "active": "1",
    },
]

snmp_config_data_msgfwd_type_str = [
    {
        "rsu_id": 1,
        "msgfwd_type": "rsuReceivedMsg",
        "snmp_index": 1,
        "message_type": "BSM",
        "dest_ipv4": "5.5.5.5",
        "dest_port": 46800,
        "start_datetime": "2024-02-05 00:00",
        "end_datetime": "2034-02-05 00:00",
        "active": "1",
    },
    {
        "rsu_id": 2,
        "msgfwd_type": "rsuXmitMsgFwding",
        "snmp_index": 1,
        "message_type": "MAP",
        "dest_ipv4": "5.5.5.5",
        "dest_port": 44920,
        "start_datetime": "2024-02-05 00:00",
        "end_datetime": "2034-02-05 00:00",
        "active": "1",
    },
]


@patch("addons.images.rsu_status_check.rsu_snmp_fetch.pgquery.write_db")
def test_insert_config_list(mock_write_db):
    # call
    rsu_snmp_fetch.insert_config_list(snmp_config_data)

    # check
    expected_query = (
        "INSERT INTO public.snmp_msgfwd_config("
        "rsu_id, msgfwd_type, snmp_index, message_type, dest_ipv4, dest_port, start_datetime, end_datetime, active) "
        "VALUES "
        "(1, 2, 1, 'BSM', '5.5.5.5', 46800, '2024-02-05 00:00', '2034-02-05 00:00', '1'), "
        "(2, 3, 1, 'MAP', '5.5.5.5', 44920, '2024-02-05 00:00', '2034-02-05 00:00', '1')"
    )
    mock_write_db.assert_called_with(expected_query)


@patch("addons.images.rsu_status_check.rsu_snmp_fetch.pgquery.write_db")
def test_delete_config_list(mock_write_db):
    # call
    rsu_snmp_fetch.delete_config_list(snmp_config_data)

    # check
    mock_write_db.assert_has_calls(
        [
            call(
                "DELETE FROM public.snmp_msgfwd_config WHERE rsu_id=1 AND msgfwd_type=2 AND snmp_index=1"
            ),
            call(
                "DELETE FROM public.snmp_msgfwd_config WHERE rsu_id=2 AND msgfwd_type=3 AND snmp_index=1"
            ),
        ]
    )


@patch("addons.images.rsu_status_check.rsu_snmp_fetch.pgquery.query_db")
def test_get_msgfwd_types(mock_query_db):
    mock_query_db.return_value = [
        ({"snmp_msgfwd_type_id": 1, "name": "rsuDsrcFwd"},),
        ({"snmp_msgfwd_type_id": 2, "name": "rsuReceivedMsg"},),
        ({"snmp_msgfwd_type_id": 3, "name": "rsuXmitMsgFwding"},),
    ]

    # call
    result = rsu_snmp_fetch.get_msgfwd_types()

    # check
    expected_obj = {
        "rsuDsrcFwd": 1,
        "rsuReceivedMsg": 2,
        "rsuXmitMsgFwding": 3,
    }
    assert result == expected_obj


@patch("addons.images.rsu_status_check.rsu_snmp_fetch.pgquery.query_db")
def test_get_config_list(mock_query_db):
    mock_query_db.return_value = [
        (
            {
                "rsu_id": 1,
                "msgfwd_type": "rsuReceivedMsg",
                "snmp_index": 1,
                "message_type": "BSM",
                "dest_ipv4": "5.5.5.5",
                "dest_port": 46800,
                "start_datetime": "2024-02-05T00:00:00",
                "end_datetime": "2034-02-05T00:00:00",
                "active": "1",
            },
        ),
        (
            {
                "rsu_id": 2,
                "msgfwd_type": "rsuXmitMsgFwding",
                "snmp_index": 1,
                "message_type": "MAP",
                "dest_ipv4": "5.5.5.5",
                "dest_port": 44920,
                "start_datetime": "2024-02-05T00:00:00",
                "end_datetime": "2034-02-05T00:00:00",
                "active": "1",
            },
        ),
    ]

    # call
    result = rsu_snmp_fetch.get_config_list()

    # check
    assert result == snmp_config_data_msgfwd_type_str
