import multidict
from datetime import datetime
import pytz

##################################### request_data ###########################################

request_environ = multidict.MultiDict(
    [
        ("user_info", {"organizations": [{"name": "Test", "role": "user"}]}),
        ("organization", "Test"),
    ]
)

request_args_good = multidict.MultiDict(
    [
        ("rsu_ip", "10.0.0.80"),
    ]
)

request_args_bad_message = multidict.MultiDict(
    [
        ("rsu_ip", "bad rsu ip"),
    ]
)

##################################### query_msgfwd_configs ###########################################

rsu_msgfwd_query = (
    "SELECT to_jsonb(row) "
    "FROM ("
    "SELECT smt.name msgfwd_type, snmp_index, message_type, dest_ipv4, dest_port, start_datetime, end_datetime, active, security "
    "FROM public.snmp_msgfwd_config smc "
    "JOIN public.snmp_msgfwd_type smt ON smc.msgfwd_type = smt.snmp_msgfwd_type_id "
    "JOIN ("
    "SELECT rd.rsu_id, rd.ipv4_address "
    "FROM public.rsus rd "
    "JOIN public.rsu_organization_name AS ron_v ON ron_v.rsu_id = rd.rsu_id "
    f"WHERE ron_v.name = 'Test'"
    ") rdo ON smc.rsu_id = rdo.rsu_id "
    f"WHERE rdo.ipv4_address = '10.0.0.80' "
    "ORDER BY smt.name, snmp_index ASC"
    ") as row"
)

return_value_rsuDsrcFwd = [
    (
        {
            "msgfwd_type": "rsuDsrcFwd",
            "snmp_index": 1,
            "message_type": "BSM",
            "dest_ipv4": "10.0.0.80",
            "dest_port": 46800,
            "start_datetime": "2024/04/01T00:00:00-06:00",
            "end_datetime": "2034/04/01T00:00:00-06:00",
            "active": "1",
            "security": "0",
        },
    ),
    (
        {
            "msgfwd_type": "rsuDsrcFwd",
            "snmp_index": 2,
            "message_type": "BSM",
            "dest_ipv4": "10.0.0.81",
            "dest_port": 46800,
            "start_datetime": "2024/04/01T00:00:00-06:00",
            "end_datetime": "2034/04/01T00:00:00-06:00",
            "active": "1",
            "security": "1",
        },
    ),
]

result_rsuDsrcFwd = {
    "RsuFwdSnmpwalk": {
        1: {
            "Message Type": "BSM",
            "IP": "10.0.0.80",
            "Port": 46800,
            "Start DateTime": "2024-04-01T00:00:00-06:00",
            "End DateTime": "2034-04-01T00:00:00-06:00",
            "Config Active": "Enabled",
            "Full WSMP": "Disabled",
        },
        2: {
            "Message Type": "BSM",
            "IP": "10.0.0.81",
            "Port": 46800,
            "Start DateTime": "2024-04-01T00:00:00-06:00",
            "End DateTime": "2034-04-01T00:00:00-06:00",
            "Config Active": "Enabled",
            "Full WSMP": "Enabled",
        },
    }
}


return_value_rxtxfwd = [
    (
        {
            "msgfwd_type": "rsuReceivedMsg",
            "snmp_index": 1,
            "message_type": "BSM",
            "dest_ipv4": "10.0.0.80",
            "dest_port": 46800,
            "start_datetime": "2024/04/01T00:00:00-06:00",
            "end_datetime": "2034/04/01T00:00:00-06:00",
            "active": "1",
            "security": "1",
        },
    ),
    (
        {
            "msgfwd_type": "rsuXmitMsgFwding",
            "snmp_index": 1,
            "message_type": "MAP",
            "dest_ipv4": "10.0.0.80",
            "dest_port": 44920,
            "start_datetime": "2024/04/01T00:00:00-06:00",
            "end_datetime": "2034/04/01T00:00:00-06:00",
            "active": "1",
            "security": "1",
        },
    ),
]

result_rxtxfwd = {
    "RsuFwdSnmpwalk": {
        "rsuReceivedMsgTable": {
            1: {
                "Message Type": "BSM",
                "IP": "10.0.0.80",
                "Port": 46800,
                "Start DateTime": "2024-04-01T00:00:00-06:00",
                "End DateTime": "2034-04-01T00:00:00-06:00",
                "Config Active": "Enabled",
                "Full WSMP": "Enabled",
            }
        },
        "rsuXmitMsgFwdingTable": {
            1: {
                "Message Type": "MAP",
                "IP": "10.0.0.80",
                "Port": 44920,
                "Start DateTime": "2024-04-01T00:00:00-06:00",
                "End DateTime": "2034-04-01T00:00:00-06:00",
                "Config Active": "Enabled",
                "Full WSMP": "Enabled",
            }
        },
    }
}
