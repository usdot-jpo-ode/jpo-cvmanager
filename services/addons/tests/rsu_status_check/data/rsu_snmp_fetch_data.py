# General data

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

msgfwd_types = {
    "rsuDsrcFwd": 1,
    "rsuReceivedMsg": 2,
    "rsuXmitMsgFwding": 3,
}

# test_update_postgresql

sample_rsu_snmp_configs_obj_1 = {
    1: [
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
    ],
    2: [
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
    ],
    3: "Unable to retrieve latest SNMP config",
}

sample_rsu_snmp_configs_obj_2 = {
    1: [],
    2: "Unable to retrieve latest SNMP config",
}

sample_rsu_snmp_configs_obj_3 = {
    1: [
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
    ],
}

snmp_config_data_msgfwd_type_str_2 = [
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
]

# test_get_snmp_configs

rsu_list = [
    {
        "rsu_id": 1,
        "ipv4_address": "5.5.5.5",
        "snmp_version": "1218",
        "snmp_username": "username",
        "snmp_password": "password",
    },
    {
        "rsu_id": 2,
        "ipv4_address": "6.6.6.6",
        "snmp_version": "1218",
        "snmp_username": "username",
        "snmp_password": "password",
    },
    {
        "rsu_id": 3,
        "ipv4_address": "7.7.7.7",
        "snmp_version": "41",
        "snmp_username": "username",
        "snmp_password": "password",
    },
    {
        "rsu_id": 4,
        "ipv4_address": "8.8.8.8",
        "snmp_version": "41",
        "snmp_username": "username",
        "snmp_password": "password",
    },
]

side_effect_return_values = [
    (
        {
            "RsuFwdSnmpwalk": {
                "rsuReceivedMsgTable": {
                    1: {
                        "Message Type": "BSM",
                        "IP": "10.0.0.5",
                        "Port": 46800,
                        "Start DateTime": "2024-02-05 00:00",
                        "End DateTime": "2034-02-05 00:00",
                        "Config Active": "Enabled",
                    }
                },
                "rsuXmitMsgFwdingTable": {},
            }
        },
        200,
    ),
    (
        {
            "RsuFwdSnmpwalk": {
                "rsuReceivedMsgTable": {
                    1: {
                        "Message Type": "BSM",
                        "IP": "10.0.0.8",
                        "Port": 46800,
                        "Start DateTime": "2024-02-05 00:00",
                        "End DateTime": "2034-02-05 00:00",
                        "Config Active": "Enabled",
                    }
                },
                "rsuXmitMsgFwdingTable": {
                    1: {
                        "Message Type": "MAP",
                        "IP": "10.0.0.8",
                        "Port": 44920,
                        "Start DateTime": "2024-02-05 00:00",
                        "End DateTime": "2034-02-05 00:00",
                        "Config Active": "Enabled",
                    }
                },
            }
        },
        200,
    ),
    (
        {
            "RsuFwdSnmpwalk": {
                1: {
                    "Message Type": "BSM",
                    "IP": "10.0.0.8",
                    "Port": 46800,
                    "Start DateTime": "2024-02-05 00:00",
                    "End DateTime": "2034-02-05 00:00",
                    "Config Active": "Enabled",
                }
            }
        },
        200,
    ),
    (
        {
            "RsuFwdSnmpwalk": "Authentication failure (incorrect password, community or key)"
        },
        500,
    ),
]

get_snmp_configs_expected = {
    1: [
        {
            "rsu_id": 1,
            "msgfwd_type": "rsuReceivedMsg",
            "snmp_index": 1,
            "message_type": "BSM",
            "dest_ipv4": "10.0.0.5",
            "dest_port": 46800,
            "start_datetime": "2024-02-05 00:00",
            "end_datetime": "2034-02-05 00:00",
            "active": "1",
        },
    ],
    2: [
        {
            "rsu_id": 2,
            "msgfwd_type": "rsuReceivedMsg",
            "snmp_index": 1,
            "message_type": "BSM",
            "dest_ipv4": "10.0.0.8",
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
            "dest_ipv4": "10.0.0.8",
            "dest_port": 44920,
            "start_datetime": "2024-02-05 00:00",
            "end_datetime": "2034-02-05 00:00",
            "active": "1",
        },
    ],
    3: [
        {
            "rsu_id": 3,
            "msgfwd_type": "rsuDsrcFwd",
            "snmp_index": 1,
            "message_type": "BSM",
            "dest_ipv4": "10.0.0.8",
            "dest_port": 46800,
            "start_datetime": "2024-02-05 00:00",
            "end_datetime": "2034-02-05 00:00",
            "active": "1",
        },
    ],
    4: "Unable to retrieve latest SNMP config",
}

# test_get_rsu_list

get_rsu_list_query_string = (
    "SELECT to_jsonb(row) "
    "FROM ("
    "SELECT rd.rsu_id, rd.ipv4_address, snmp.username AS snmp_username, snmp.password AS snmp_password, sver.version_code AS snmp_version "
    "FROM public.rsus AS rd "
    "LEFT JOIN public.snmp_credentials AS snmp ON snmp.snmp_credential_id = rd.snmp_credential_id "
    "LEFT JOIN public.snmp_versions AS sver ON sver.snmp_version_id = rd.snmp_version_id"
    ") as row"
)

query_rsu_list = [
    (
        {
            "rsu_id": 1,
            "ipv4_address": "5.5.5.5",
            "snmp_version": "1218",
            "snmp_username": "username",
            "snmp_password": "password",
        },
    ),
    (
        {
            "rsu_id": 2,
            "ipv4_address": "6.6.6.6",
            "snmp_version": "1218",
            "snmp_username": "username",
            "snmp_password": "password",
        },
    ),
    (
        {
            "rsu_id": 3,
            "ipv4_address": "7.7.7.7",
            "snmp_version": "41",
            "snmp_username": "username",
            "snmp_password": "password",
        },
    ),
    (
        {
            "rsu_id": 4,
            "ipv4_address": "8.8.8.8",
            "snmp_version": "41",
            "snmp_username": "username",
            "snmp_password": "password",
        },
    ),
]
