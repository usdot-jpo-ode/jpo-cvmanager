import multidict

##################################### request_data ###########################################

request_params_good = multidict.MultiDict(
    [
        ("user_info", {"organizations": [{"name": "Test", "role": "user"}]}),
        ("organization", "Test"),
    ]
)

###################################### Single Result ##########################################
return_value_single_result = [
    ({"ip": "10.0.0.1", "health": "1", "expiration": "2022-11-02T00:00:00.00Z"},)
]

return_value_single_null_result = [
    ({"ip": "10.0.0.1", "health": None, "expiration": None},)
]

expected_rsu_data_single_result = {
    "10.0.0.1": {"health": "1", "expiration": "11/01/2022 06:00:00 PM"}
}

expected_rsu_data_single_null_result = {"10.0.0.1": None}

return_value_multiple_result = [
    ({"ip": "10.0.0.1", "health": "1", "expiration": "2022-11-02T00:00:00.00Z"},),
    ({"ip": "10.0.0.2", "health": "0", "expiration": "2022-11-03T00:00:00.00Z"},),
]

expected_rsu_data_multiple_result = {
    "10.0.0.1": {"expiration": "11/01/2022 06:00:00 PM", "health": "1"},
    "10.0.0.2": {"expiration": "11/02/2022 06:00:00 PM", "health": "0"},
}

expectedQuery = (
    "SELECT jsonb_build_object('ip', rd.ipv4_address, 'health', scms_health_data.health, 'expiration', scms_health_data.expiration) "
    "FROM public.rsus AS rd "
    "JOIN public.rsu_organization_name AS ron_v ON ron_v.rsu_id = rd.rsu_id "
    "LEFT JOIN ("
    "SELECT a.rsu_id, a.health, a.timestamp, a.expiration "
    "FROM ("
    "SELECT sh.rsu_id, sh.health, sh.timestamp, sh.expiration, ROW_NUMBER() OVER (PARTITION BY sh.rsu_id order by sh.timestamp DESC) AS row_id "
    "FROM public.scms_health AS sh"
    ") AS a "
    "WHERE a.row_id <= 1 ORDER BY rsu_id"
    ") AS scms_health_data ON rd.rsu_id = scms_health_data.rsu_id "
    "WHERE ron_v.name = :org_name "
    "ORDER BY rd.ipv4_address"
)
