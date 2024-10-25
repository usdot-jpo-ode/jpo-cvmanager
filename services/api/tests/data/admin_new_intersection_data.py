import multidict

##################################### request_data ###########################################

request_params_good = multidict.MultiDict([])

request_json_good = {
    "intersection_id": 1,
    "ref_pt": {"longitude": -100.0, "latitude": 38.0},
    "origin_ip": "10.0.0.1",
    "intersection_name": "test intersection",
    "organizations": ["Test Org"],
    "rsus": ["10.0.0.1"],
}

request_json_bad = {
    "intersection_id": "1",
    "ref_pt": {"longitude": -100.0},
    "origin_ip": "10.0.0.1",
    "intersection_name": "test intersection",
    "organizations": ["Test Org"],
    "rsus": ["10.0.0.1"],
}

bad_input_check_safe_input = {
    "intersection_id": 1,
    "ref_pt": {"longitude": -100.0},
    "origin_ip": "10.0.0.1",
    "intersection_name": "test intersection--",
    "organizations": ["Test Org"],
    "rsus": ["10.0.0.1"],
}
