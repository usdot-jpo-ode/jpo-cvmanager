import multidict

##################################### request_data ###########################################

request_params_good = multidict.MultiDict([])

request_json_good = {
  "ip": "10.0.0.1",
  "geo_position": {
    "longitude": -100.0,
    "latitude": 38.0
  },
  "milepost": 900.1,
  "primary_route": "Test Route",
  "serial_number": "test",
  "model": "Commsignia",
  "scms_id": "",
  "ssh_credential_group": "test",
  "snmp_credential_group": "test",
  "snmp_version_group": "test",
  "organizations": ["Test Org"]
}

request_json_bad = {
  "ip": "10.0.0.1",
  "geo_position": {
    "longitude": -100.0
  },
  "milepost": 900.1,
  "primary_route": "Test Route",
  "serial_number": "test",
  "model": "Commsignia",
  "scms_id": "",
  "ssh_credential_group": "test",
  "snmp_credential_group": "test",
  "snmp_version_group": "test",
  "organizations": ["Test Org"]
}

##################################### test_data ###########################################

good_input = {
  'primary_route': 'test route',
  'model': 'test test',
  'serial_number': 'test',
  'scms_id': 'test',
  'ssh_credential_group': 'test',
  'snmp_credential_group': 'test',
  "snmp_version_group": "test"
}

bad_input = {
  'primary_route': 'test route--',
  'model': 'test test@#',
  'serial_number': 'test',
  'scms_id': 'test',
  'ssh_credential_group': 'test*&&',
  'snmp_credential_group': 'test',
  "snmp_version_group": "test"
}

mock_post_body_commsignia = {
  "ip": "10.0.0.1",
  "geo_position": {
    "latitude": 39.896450,
    "longitude": -104.984451
  },
  "milepost": 900.52,
  "primary_route": "Test Route",
  "serial_number": "test",
  "model": "Commsignia RSU",
  "scms_id": "",
  "ssh_credential_group": "test",
  "snmp_credential_group": "test",
  "snmp_version_group": "test",
  "organizations": ['test']
}

mock_post_body_yunex = {
  "ip": "10.0.0.1",
  "geo_position": {
    "latitude": 39.896450,
    "longitude": -104.984451
  },
  "milepost": 900.52,
  "primary_route": "Test Route",
  "serial_number": "test",
  "model": "Yunex RSU",
  "scms_id": "custom",
  "ssh_credential_group": "test",
  "snmp_credential_group": "test",
  "snmp_version_group": "test",
  "organizations": ['test']
}

mock_post_body_yunex_no_scms = {
  "ip": "10.0.0.1",
  "geo_position": {
    "latitude": 39.896450,
    "longitude": -104.984451
  },
  "milepost": 900.52,
  "primary_route": "Test Route",
  "serial_number": "test",
  "model": "Yunex RSU",
  "scms_id": "",
  "ssh_credential_group": "test",
  "snmp_credential_group": "test",
  "snmp_version_group": "test",
  "organizations": ['test']
}

rsu_query_commsignia = "INSERT INTO public.rsus(geography, milepost, ipv4_address, serial_number, primary_route, model, credential_id, snmp_credential_id, snmp_version_id, iss_scms_id) " \
  "VALUES (" \
  "ST_GeomFromText('POINT(-104.984451 39.89645)'), " \
  "900.52, " \
  "'10.0.0.1', " \
  "'test', " \
  "'Test Route', " \
  "(SELECT rsu_model_id FROM public.rsu_models WHERE name = 'RSU'), " \
  "(SELECT credential_id FROM public.rsu_credentials WHERE nickname = 'test'), " \
  "(SELECT snmp_credential_id FROM public.snmp_credentials WHERE nickname = 'test'), " \
  "(SELECT snmp_version_id FROM public.snmp_versions WHERE nickname = 'test'), " \
  "'test'" \
  ")"

rsu_query_yunex = "INSERT INTO public.rsus(geography, milepost, ipv4_address, serial_number, primary_route, model, credential_id, snmp_credential_id, snmp_version_id, iss_scms_id) " \
  "VALUES (" \
  "ST_GeomFromText('POINT(-104.984451 39.89645)'), " \
  "900.52, " \
  "'10.0.0.1', " \
  "'test', " \
  "'Test Route', " \
  "(SELECT rsu_model_id FROM public.rsu_models WHERE name = 'RSU'), " \
  "(SELECT credential_id FROM public.rsu_credentials WHERE nickname = 'test'), " \
  "(SELECT snmp_credential_id FROM public.snmp_credentials WHERE nickname = 'test'), " \
  "(SELECT snmp_version_id FROM public.snmp_versions WHERE nickname = 'test'), " \
  "'custom'" \
  ")"

rsu_org_query= "INSERT INTO public.rsu_organization(rsu_id, organization_id) VALUES" \
  " (" \
  "(SELECT rsu_id FROM public.rsus WHERE ipv4_address = '10.0.0.1'), " \
  "(SELECT organization_id FROM public.organizations WHERE name = 'test')" \
  ")"