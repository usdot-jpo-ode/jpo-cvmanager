from mock import MagicMock, call, patch
from addons.images.rsu_status_check import rsu_snmp_fetch
from addons.tests.rsu_status_check.data import rsu_snmp_fetch_data


@patch("addons.images.rsu_status_check.rsu_snmp_fetch.pgquery.write_db")
def test_insert_config_list(mock_write_db):
    # call
    rsu_snmp_fetch.insert_config_list(rsu_snmp_fetch_data.snmp_config_data)

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
    rsu_snmp_fetch.delete_config_list(rsu_snmp_fetch_data.snmp_config_data)

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
    assert result == rsu_snmp_fetch_data.msgfwd_types


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
    assert result == rsu_snmp_fetch_data.snmp_config_data_msgfwd_type_str


@patch("addons.images.rsu_status_check.rsu_snmp_fetch.get_config_list")
@patch("addons.images.rsu_status_check.rsu_snmp_fetch.get_msgfwd_types")
@patch("addons.images.rsu_status_check.rsu_snmp_fetch.delete_config_list")
@patch("addons.images.rsu_status_check.rsu_snmp_fetch.insert_config_list")
def test_update_postgresql_add(
    mock_insert_config_list,
    mock_delete_config_list,
    mock_get_msgfwd_types,
    mock_get_config_list,
):
    mock_get_config_list.return_value = []
    mock_get_msgfwd_types.return_value = rsu_snmp_fetch_data.msgfwd_types

    # call
    rsu_snmp_fetch.update_postgresql(rsu_snmp_fetch_data.sample_rsu_snmp_configs_obj_1)

    # check
    assert mock_delete_config_list.call_count == 0
    mock_insert_config_list.assert_called_with(rsu_snmp_fetch_data.snmp_config_data)


@patch("addons.images.rsu_status_check.rsu_snmp_fetch.get_config_list")
@patch("addons.images.rsu_status_check.rsu_snmp_fetch.get_msgfwd_types")
@patch("addons.images.rsu_status_check.rsu_snmp_fetch.delete_config_list")
@patch("addons.images.rsu_status_check.rsu_snmp_fetch.insert_config_list")
def test_update_postgresql_delete(
    mock_insert_config_list,
    mock_delete_config_list,
    mock_get_msgfwd_types,
    mock_get_config_list,
):
    mock_get_config_list.return_value = (
        rsu_snmp_fetch_data.snmp_config_data_msgfwd_type_str
    )
    mock_get_msgfwd_types.return_value = rsu_snmp_fetch_data.msgfwd_types

    # call
    rsu_snmp_fetch.update_postgresql(rsu_snmp_fetch_data.sample_rsu_snmp_configs_obj_2)

    # check
    mock_delete_config_list.assert_called_with(
        [
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
            }
        ]
    )
    assert mock_insert_config_list.call_count == 0


@patch("addons.images.rsu_status_check.rsu_snmp_fetch.get_config_list")
@patch("addons.images.rsu_status_check.rsu_snmp_fetch.get_msgfwd_types")
@patch("addons.images.rsu_status_check.rsu_snmp_fetch.delete_config_list")
@patch("addons.images.rsu_status_check.rsu_snmp_fetch.insert_config_list")
def test_update_postgresql_nothing(
    mock_insert_config_list,
    mock_delete_config_list,
    mock_get_msgfwd_types,
    mock_get_config_list,
):
    mock_get_config_list.return_value = (
        rsu_snmp_fetch_data.snmp_config_data_msgfwd_type_str_2
    )
    mock_get_msgfwd_types.return_value = rsu_snmp_fetch_data.msgfwd_types

    # call
    rsu_snmp_fetch.update_postgresql(rsu_snmp_fetch_data.sample_rsu_snmp_configs_obj_3)

    # check
    assert mock_delete_config_list.call_count == 0
    assert mock_insert_config_list.call_count == 0


@patch("addons.images.rsu_status_check.rsu_snmp_fetch.rsufwdsnmpwalk.get")
def test_get_snmp_configs(mock_rsufwdsnmpwalk_get):
    mock_rsufwdsnmpwalk_get.side_effect = rsu_snmp_fetch_data.side_effect_return_values

    # call
    result = rsu_snmp_fetch.get_snmp_configs(rsu_snmp_fetch_data.rsu_list)

    # verify
    assert result == rsu_snmp_fetch_data.get_snmp_configs_expected


@patch("addons.images.rsu_status_check.rsu_snmp_fetch.pgquery.query_db")
def test_get_rsu_list(mock_query_db):
    mock_query_db.return_value = rsu_snmp_fetch_data.query_rsu_list

    # call
    result = rsu_snmp_fetch.get_rsu_list()

    # verify
    mock_query_db.assert_called_with(rsu_snmp_fetch_data.get_rsu_list_query_string)
    assert result == rsu_snmp_fetch_data.rsu_list
