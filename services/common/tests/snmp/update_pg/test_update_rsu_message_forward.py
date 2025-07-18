from mock import MagicMock, call, patch
from common.snmp.update_pg.update_rsu_message_forward import (
    UpdatePostgresRsuMessageForward,
)
from common.tests.snmp.update_pg.data import test_update_rsu_message_forward_data


@patch("common.snmp.update_pg.update_rsu_message_forward.pgquery.write_db")
def test_insert_config_list(mock_write_db):
    # call
    update_pg_rsu_message_forward = UpdatePostgresRsuMessageForward()
    update_pg_rsu_message_forward.insert_config_list(
        test_update_rsu_message_forward_data.snmp_config_data
    )

    # check
    expected_query = (
        "INSERT INTO public.snmp_msgfwd_config("
        "rsu_id, msgfwd_type, snmp_index, message_type, dest_ipv4, dest_port, start_datetime, end_datetime, active, security) "
        "VALUES "
        "(1, 2, 1, 'BSM', '5.5.5.5', 46800, '2024-02-05 00:00', '2034-02-05 00:00', '1', '1'), "
        "(2, 3, 1, 'MAP', '5.5.5.5', 44920, '2024-02-05 00:00', '2034-02-05 00:00', '1', '1')"
    )
    mock_write_db.assert_called_with(expected_query)


@patch("common.snmp.update_pg.update_rsu_message_forward.pgquery.write_db")
def test_delete_config_list(mock_write_db):
    # call
    update_pg_rsu_message_forward = UpdatePostgresRsuMessageForward()
    update_pg_rsu_message_forward.delete_config_list(
        test_update_rsu_message_forward_data.snmp_config_data
    )

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


@patch("common.snmp.update_pg.update_rsu_message_forward.pgquery.query_db")
def test_get_msgfwd_types(mock_query_db):
    mock_query_db.return_value = [
        ({"snmp_msgfwd_type_id": 1, "name": "rsuDsrcFwd"},),
        ({"snmp_msgfwd_type_id": 2, "name": "rsuReceivedMsg"},),
        ({"snmp_msgfwd_type_id": 3, "name": "rsuXmitMsgFwding"},),
    ]

    # call
    update_pg_rsu_message_forward = UpdatePostgresRsuMessageForward()
    result = update_pg_rsu_message_forward.get_msgfwd_types()

    # check
    assert result == test_update_rsu_message_forward_data.msgfwd_types


@patch("common.snmp.update_pg.update_rsu_message_forward.pgquery.query_db")
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
                "security": "1",
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
                "security": "1",
            },
        ),
    ]

    # call
    update_pg_rsu_message_forward = UpdatePostgresRsuMessageForward()
    result = update_pg_rsu_message_forward.get_config_list()

    # check
    assert (
        result == test_update_rsu_message_forward_data.snmp_config_data_msgfwd_type_str
    )


@patch(
    "common.snmp.update_pg.update_rsu_message_forward.UpdatePostgresRsuMessageForward.get_config_list"
)
@patch(
    "common.snmp.update_pg.update_rsu_message_forward.UpdatePostgresRsuMessageForward.get_msgfwd_types"
)
@patch(
    "common.snmp.update_pg.update_rsu_message_forward.UpdatePostgresRsuMessageForward.delete_config_list"
)
@patch(
    "common.snmp.update_pg.update_rsu_message_forward.UpdatePostgresRsuMessageForward.insert_config_list"
)
def test_update_postgresql_add(
    mock_insert_config_list,
    mock_delete_config_list,
    mock_get_msgfwd_types,
    mock_get_config_list,
):
    mock_get_config_list.return_value = []
    mock_get_msgfwd_types.return_value = (
        test_update_rsu_message_forward_data.msgfwd_types
    )

    # call
    update_pg_rsu_message_forward = UpdatePostgresRsuMessageForward()
    update_pg_rsu_message_forward.update_postgresql(
        test_update_rsu_message_forward_data.sample_rsu_snmp_configs_obj_1
    )

    # check
    assert mock_delete_config_list.call_count == 0
    mock_insert_config_list.assert_called_with(
        test_update_rsu_message_forward_data.snmp_config_data
    )


@patch(
    "common.snmp.update_pg.update_rsu_message_forward.UpdatePostgresRsuMessageForward.get_config_list"
)
@patch(
    "common.snmp.update_pg.update_rsu_message_forward.UpdatePostgresRsuMessageForward.get_msgfwd_types"
)
@patch(
    "common.snmp.update_pg.update_rsu_message_forward.UpdatePostgresRsuMessageForward.delete_config_list"
)
@patch(
    "common.snmp.update_pg.update_rsu_message_forward.UpdatePostgresRsuMessageForward.insert_config_list"
)
def test_update_postgresql_delete(
    mock_insert_config_list,
    mock_delete_config_list,
    mock_get_msgfwd_types,
    mock_get_config_list,
):
    mock_get_config_list.return_value = (
        test_update_rsu_message_forward_data.snmp_config_data_msgfwd_type_str
    )
    mock_get_msgfwd_types.return_value = (
        test_update_rsu_message_forward_data.msgfwd_types
    )

    # call
    update_pg_rsu_message_forward = UpdatePostgresRsuMessageForward()
    update_pg_rsu_message_forward.update_postgresql(
        test_update_rsu_message_forward_data.sample_rsu_snmp_configs_obj_2
    )

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
                "security": "1",
            }
        ]
    )
    assert mock_insert_config_list.call_count == 0


@patch(
    "common.snmp.update_pg.update_rsu_message_forward.UpdatePostgresRsuMessageForward.get_config_list"
)
@patch(
    "common.snmp.update_pg.update_rsu_message_forward.UpdatePostgresRsuMessageForward.get_msgfwd_types"
)
@patch(
    "common.snmp.update_pg.update_rsu_message_forward.UpdatePostgresRsuMessageForward.delete_config_list"
)
@patch(
    "common.snmp.update_pg.update_rsu_message_forward.UpdatePostgresRsuMessageForward.insert_config_list"
)
def test_update_postgresql_nothing(
    mock_insert_config_list,
    mock_delete_config_list,
    mock_get_msgfwd_types,
    mock_get_config_list,
):
    mock_get_config_list.return_value = (
        test_update_rsu_message_forward_data.snmp_config_data_msgfwd_type_str_2
    )
    mock_get_msgfwd_types.return_value = (
        test_update_rsu_message_forward_data.msgfwd_types
    )

    # call
    update_pg_rsu_message_forward = UpdatePostgresRsuMessageForward()
    update_pg_rsu_message_forward.update_postgresql(
        test_update_rsu_message_forward_data.sample_rsu_snmp_configs_obj_3
    )

    # check
    assert mock_delete_config_list.call_count == 0
    assert mock_insert_config_list.call_count == 0


@patch("common.snmp.update_pg.update_rsu_message_forward.ntcip1218_rsumf.get")
@patch("common.snmp.update_pg.update_rsu_message_forward.rsu41_rsumf.get")
def test_get_snmp_msgfwd_configs(mock_rsu41_rsumf_get, mock_ntcip1218_rsumf_get):
    mock_ntcip1218_rsumf_get.side_effect = (
        test_update_rsu_message_forward_data.side_effect_ntcip1218_return_values
    )
    mock_rsu41_rsumf_get.side_effect = (
        test_update_rsu_message_forward_data.side_effect_rsu41_return_values
    )

    # call
    update_pg_rsu_message_forward = UpdatePostgresRsuMessageForward()
    result = update_pg_rsu_message_forward.get_snmp_configs(
        test_update_rsu_message_forward_data.rsu_list
    )

    # verify
    assert result == test_update_rsu_message_forward_data.get_snmp_configs_expected
