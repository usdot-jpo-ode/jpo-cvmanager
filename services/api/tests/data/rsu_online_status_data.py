import multidict
from datetime import datetime
from pytz import timezone

##################################### request_data ###########################################

request_params_good = multidict.MultiDict(
    [
        ("user_info", {"organizations": [{"name": "Test", "role": "user"}]}),
        ("organization", "Test"),
    ]
)

####################################### get_ping_data ##################################

ping_return_single = [
    (
        {
            "id": 104,
            "ip": "172.16.28.233",
            "datetime": "2022-06-14T18:46:58",
            "online_status": "1",
        },
    )
]

ping_expected_single = {
    "172.16.28.233": {
        "checked_timestamps": ["2022-06-14T18:46:58"],
        "online_statuses": ["1"],
    }
}

ping_return_multiple = [
    (
        {
            "id": 104,
            "ip": "172.16.28.233",
            "datetime": "2022-06-14T18:46:58",
            "online_status": "1",
        },
    ),
    (
        {
            "id": 105,
            "ip": "172.16.28.156",
            "datetime": "2022-06-14T18:56:59",
            "online_status": "1",
        },
    ),
    (
        {
            "id": 110,
            "ip": "172.16.28.134",
            "datetime": "2022-06-14T18:57:04",
            "online_status": "1",
        },
    ),
]

ping_expected_multiple = {
    "172.16.28.233": {
        "checked_timestamps": ["2022-06-14T18:46:58"],
        "online_statuses": ["1"],
    },
    "172.16.28.156": {
        "checked_timestamps": ["2022-06-14T18:56:59"],
        "online_statuses": ["1"],
    },
    "172.16.28.134": {
        "checked_timestamps": ["2022-06-14T18:57:04"],
        "online_statuses": ["1"],
    },
}

####################################### get_last_online_data ##################################

last_online_query = (
    "SELECT ping.timestamp "
    "FROM public.ping "
    "JOIN ("
    "SELECT rsus.rsu_id, rsus.ipv4_address "
    "FROM public.rsus "
    "JOIN public.rsu_organization_name AS ron_v ON ron_v.rsu_id = rsus.rsu_id "
    f"WHERE rsus.ipv4_address = '10.0.0.1' "
    ") AS rd ON ping.rsu_id = rd.rsu_id "
    "WHERE ping.rsu_id = rd.rsu_id "
    "AND result = '1' "
    "ORDER BY ping.timestamp DESC "
    "LIMIT 1"
)

last_online_query_return = [
    [datetime.strptime("06/14/2022 07:44:09 PM", "%m/%d/%Y %I:%M:%S %p")]
]

last_online_data_expected = {
    "ip": "10.0.0.1",
    "last_online": datetime.strftime(
        datetime.strptime("06/14/2022 07:44:09 PM", "%m/%d/%Y %I:%M:%S %p").astimezone(
            timezone("America/Denver")
        ),
        "%m/%d/%Y %I:%M:%S %p",
    ),
}

last_online_no_data_expected = {"ip": "10.0.0.1", "last_online": "No Data"}

####################################### get_rsu_online_statuses ##################################

mock_ping_return_single = {
    "172.16.28.233": {
        "checked_timestamps": ["2022-06-14T18:46:58"],
        "online_statuses": ["1"],
    }
}

online_status_expected_single = {"172.16.28.233": {"current_status": "online"}}

mock_last_online_return_multiple = [
    {"ip": "172.16.28.233"},
    {"ip": "172.16.28.156"},
    {"ip": "172.16.28.134"},
]

mock_ping_return_multiple = {
    "172.16.28.233": {
        "checked_timestamps": ["2022-06-14T18:46:58"],
        "online_statuses": ["1"],
    },
    "172.16.28.156": {
        "checked_timestamps": ["2022-06-14T18:56:59"],
        "online_statuses": ["1"],
    },
    "172.16.28.134": {
        "checked_timestamps": ["2022-06-14T18:57:04"],
        "online_statuses": ["1"],
    },
}

online_status_expected_multiple = {
    "172.16.28.233": {"current_status": "online"},
    "172.16.28.156": {"current_status": "online"},
    "172.16.28.134": {"current_status": "online"},
}
