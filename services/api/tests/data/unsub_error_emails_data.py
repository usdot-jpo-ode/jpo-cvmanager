import multidict

##################################### request_data ###########################################

request_params_good = multidict.MultiDict([])

##################################### test_data ###########################################
get_subscribed_users_query = "SELECT email FROM public.users WHERE receive_error_emails = '1'"
get_subscribed_users_query_resp = [
    {'email': 'test@gmail.com'},
    {'email': 'test2@gmail.com'},
]

get_unsubscribe_user_query = "SELECT receive_error_emails FROM public.users WHERE email = 'test@gmail.com'"
get_unsubscribe_user_remove_query = "UPDATE public.users SET receive_error_emails='0' WHERE email = 'test@gmail.com'"
