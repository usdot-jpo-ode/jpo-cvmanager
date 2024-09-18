import multidict

##################################### request_data ###########################################

request_params_good = multidict.MultiDict([])

request_json_good = {"name": "new org", "email": "new@email.com"}

request_json_bad = {}

##################################### test_data ###########################################

good_input = {"name": "new org", "email": "new email"}

bad_input = {"name": "new--^@^&! org", "email": "bad@email@com"}

org_insert_query = (
    "INSERT INTO public.organizations(name, email) "
    "VALUES ('new org', 'new@email.com')"
)
