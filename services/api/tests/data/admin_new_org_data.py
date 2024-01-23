import multidict

##################################### request_data ###########################################

request_params_good = multidict.MultiDict([])

request_json_good = {"name": "new org"}

request_json_bad = {}

##################################### test_data ###########################################

good_input = {"name": "new org"}

bad_input = {"name": "new--^@^&! org"}

org_insert_query = "INSERT INTO public.organizations(name) " "VALUES ('new org')"
