import multidict

##################################### request_data ###########################################

request_params_good = multidict.MultiDict([])

request_json_good = {
  "email": "jdoe@example.com",
  "first_name": "John",
  "last_name": "Doe",
  "super_user": True,
  "receive_error_emails": True,
  "organizations": [
    {"name": "Test Org", "role": "operator"}
  ]
}

request_json_bad = {
  "email": "jdoe@example.com",
  "first_name": "John",
  "last_name": "Doe",
  "super_user": True,
  "receive_error_emails": True,
  "organizations": ["Test Org"]
}

##################################### test_data ###########################################

good_input = {
  "email": "jdoe@example.com",
  "first_name": "John",
  "last_name": "Doe",
  "super_user": True,
  "receive_error_emails": True,
  "organizations": [
    {"name": "Test Org", "role": "operator"}
  ]
}

bad_input = {
  "email": "j--doe@exa@mple.com",
  "first_name": "John--",
  "last_name": "Doe@#",
  "super_user": True,
  "receive_error_emails": True,
  "organizations": [
    {"name": "Test Org##", "role": "operator"}
  ]
}

user_insert_query = "INSERT INTO public.users(email, first_name, last_name, super_user, receive_error_emails) " \
  "VALUES ('jdoe@example.com', 'John', 'Doe', '1', '1')"

user_org_insert_query = "INSERT INTO public.user_organization(user_id, organization_id, role_id) VALUES" \
  " (" \
    "(SELECT user_id FROM public.users WHERE email = 'jdoe@example.com'), " \
    "(SELECT organization_id FROM public.organizations WHERE name = 'Test Org'), " \
    "(SELECT role_id FROM public.roles WHERE name = 'operator')" \
  ")"