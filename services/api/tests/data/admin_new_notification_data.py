import multidict

##################################### request_data ###########################################

request_params_good = multidict.MultiDict([])

request_json_good = {"email": "test@gmail.com", "email_type": "some type"}

request_json_bad = {}

##################################### test_data ###########################################

good_input = {"email": "test@gmail.com", "email_type": "some type"}

bad_input = {"email": "test@gmail.com", "email_type": "some type^@^&!"}

notification_insert_query = "INSERT into public.user_email_notification(user_id, email_type_id) VALUES ((SELECT user_id FROM public.users WHERE email='test@gmail.com'), (SELECT email_type_id FROM public.email_type WHERE email_type='some type'))"
