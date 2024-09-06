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

# test_get_rsu_list

get_rsu_list_query_string = (
    "SELECT to_jsonb(row) "
    "FROM ("
    "SELECT rd.rsu_id, rd.ipv4_address, snmp.username AS snmp_username, snmp.password AS snmp_password, sver.protocol_code AS snmp_version "
    "FROM public.rsus AS rd "
    "LEFT JOIN public.snmp_credentials AS snmp ON snmp.snmp_credential_id = rd.snmp_credential_id "
    "LEFT JOIN public.snmp_protocols AS sver ON sver.snmp_protocol_id = rd.snmp_protocol_id"
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
