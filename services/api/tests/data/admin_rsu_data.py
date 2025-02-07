import multidict

##################################### request data ###########################################

request_environ = multidict.MultiDict([])

request_args_rsu_good = {"rsu_ip": "10.0.0.1"}
request_args_all_good = {"rsu_ip": "all"}
request_args_str_bad = {"rsu_ip": 5}
request_args_ipv4_bad = {"rsu_ip": "test"}

request_json_good = {
    "orig_ip": "10.0.0.1",
    "ip": "10.0.0.1",
    "geo_position": {"longitude": -100.0, "latitude": 38.0},
    "milepost": 900.1,
    "primary_route": "Test Route",
    "serial_number": "test",
    "model": "manufacturer model",
    "scms_id": "test",
    "ssh_credential_group": "test",
    "snmp_credential_group": "test",
    "snmp_version_group": "test",
    "organizations_to_add": ["Test Org2"],
    "organizations_to_remove": ["Test Org1"],
}

request_json_bad = {
    "orig_ip": "10.0.0.1",
    "ip": "10.0.0.1",
    "geo_position": {"longitude": "test", "latitude": 38.0},
    "milepost": 900.1,
    "primary_route": "Test Route",
    "serial_number": "test",
    "model": "manufacturer model",
    "ssh_credential_group": "test",
    "snmp_credential_group": "test",
    "snmp_version_group": "test",
    "organizations_to_add": ["Test Org"],
    "organizations_to_remove": [],
}


##################################### function data ###########################################

get_rsu_data_return = [
    (
        {
            "ipv4_address": "10.11.81.12",
            "latitude": 45,
            "longitude": 45,
            "milepost": 45,
            "primary_route": "test route",
            "serial_number": "test",
            "model": "test",
            "iss_scms_id": "test",
            "ssh_credential": "ssh test",
            "snmp_credential": "snmp test",
            "snmp_version": "snmp test",
            "org_name": "test org",
        },
    ),
]

expected_get_rsu_all = [
    {
        "ip": "10.11.81.12",
        "geo_position": {"latitude": 45, "longitude": 45},
        "milepost": 45,
        "primary_route": "test route",
        "serial_number": "test",
        "model": "test",
        "scms_id": "test",
        "ssh_credential_group": "ssh test",
        "snmp_credential_group": "snmp test",
        "snmp_version_group": "snmp test",
        "organizations": ["test org"],
    }
]

expected_get_rsu_query_all = (
    "SELECT to_jsonb(row) "
    "FROM ("
    "SELECT ipv4_address, ST_X(geography::geometry) AS longitude, ST_Y(geography::geometry) AS latitude, "
    "milepost, primary_route, serial_number, iss_scms_id, concat(man.name, ' ',rm.name) AS model, "
    "rsu_cred.nickname AS ssh_credential, snmp_cred.nickname AS snmp_credential, snmp_ver.nickname AS snmp_version, org.name AS org_name "
    "FROM public.rsus "
    "JOIN public.rsu_models AS rm ON rm.rsu_model_id = rsus.model "
    "JOIN public.manufacturers AS man ON man.manufacturer_id = rm.manufacturer "
    "JOIN public.rsu_credentials AS rsu_cred ON rsu_cred.credential_id = rsus.credential_id "
    "JOIN public.snmp_credentials AS snmp_cred ON snmp_cred.snmp_credential_id = rsus.snmp_credential_id "
    "JOIN public.snmp_protocols AS snmp_ver ON snmp_ver.snmp_protocol_id = rsus.snmp_protocol_id "
    "JOIN public.rsu_organization AS ro ON ro.rsu_id = rsus.rsu_id  "
    "JOIN public.organizations AS org ON org.organization_id = ro.organization_id"
    ") as row"
)

expected_get_rsu_query_one = (
    "SELECT to_jsonb(row) "
    "FROM ("
    "SELECT ipv4_address, ST_X(geography::geometry) AS longitude, ST_Y(geography::geometry) AS latitude, "
    "milepost, primary_route, serial_number, iss_scms_id, concat(man.name, ' ',rm.name) AS model, "
    "rsu_cred.nickname AS ssh_credential, snmp_cred.nickname AS snmp_credential, snmp_ver.nickname AS snmp_version, org.name AS org_name "
    "FROM public.rsus "
    "JOIN public.rsu_models AS rm ON rm.rsu_model_id = rsus.model "
    "JOIN public.manufacturers AS man ON man.manufacturer_id = rm.manufacturer "
    "JOIN public.rsu_credentials AS rsu_cred ON rsu_cred.credential_id = rsus.credential_id "
    "JOIN public.snmp_credentials AS snmp_cred ON snmp_cred.snmp_credential_id = rsus.snmp_credential_id "
    "JOIN public.snmp_protocols AS snmp_ver ON snmp_ver.snmp_protocol_id = rsus.snmp_protocol_id "
    "JOIN public.rsu_organization AS ro ON ro.rsu_id = rsus.rsu_id  "
    "JOIN public.organizations AS org ON org.organization_id = ro.organization_id"
    " WHERE ipv4_address = '10.11.81.12'"
    ") as row"
)

modify_rsu_sql = (
    "UPDATE public.rsus SET "
    f"geography=ST_GeomFromText('POINT(-100.0 38.0)'), "
    f"milepost=900.1, "
    f"ipv4_address='10.0.0.1', "
    f"serial_number='test', "
    f"primary_route='Test Route', "
    f"model=(SELECT rsu_model_id FROM public.rsu_models WHERE name = 'model'), "
    f"credential_id=(SELECT credential_id FROM public.rsu_credentials WHERE nickname = 'test'), "
    f"snmp_credential_id=(SELECT snmp_credential_id FROM public.snmp_credentials WHERE nickname = 'test'), "
    f"snmp_protocol_id=(SELECT snmp_protocol_id FROM public.snmp_protocols WHERE nickname = 'test'), "
    f"iss_scms_id='test' "
    f"WHERE ipv4_address='10.0.0.1'"
)

add_org_sql = (
    "INSERT INTO public.rsu_organization(rsu_id, organization_id) VALUES"
    " ("
    "(SELECT rsu_id FROM public.rsus WHERE ipv4_address = '10.0.0.1'), "
    "(SELECT organization_id FROM public.organizations WHERE name = 'Test Org2')"
    ")"
)

remove_org_sql = (
    "DELETE FROM public.rsu_organization WHERE "
    "rsu_id=(SELECT rsu_id FROM public.rsus WHERE ipv4_address = '10.0.0.1') "
    "AND organization_id=(SELECT organization_id FROM public.organizations WHERE name = 'Test Org1')"
)

delete_rsu_calls = [
    "DELETE FROM public.rsu_organization WHERE rsu_id=(SELECT rsu_id FROM public.rsus WHERE ipv4_address = '10.11.81.12')",
    "DELETE FROM public.ping WHERE rsu_id=(SELECT rsu_id FROM public.rsus WHERE ipv4_address = '10.11.81.12')",
    "DELETE FROM public.scms_health WHERE rsu_id=(SELECT rsu_id FROM public.rsus WHERE ipv4_address = '10.11.81.12')",
    "DELETE FROM public.snmp_msgfwd_config WHERE rsu_id=(SELECT rsu_id FROM public.rsus WHERE ipv4_address = '10.11.81.12')",
    "DELETE FROM public.rsus WHERE ipv4_address = '10.11.81.12'",
]
