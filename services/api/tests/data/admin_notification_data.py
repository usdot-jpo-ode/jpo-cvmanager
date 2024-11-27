from api.tests.data import auth_data

##################################### request data ###########################################

request_environ_specified_org_1 = auth_data.get_request_environ()
request_environ_specified_org_1.organization = "Test Org"
request_environ_specified_org_1.role = "admin"

request_args_good = {"user_email": "test@gmail.com"}

request_args_delete_good = {"email": "test@gmail.com", "email_type": "old"}

request_args_bad = {"user_email": 14}

request_json_good = {
    "email": "test@gmail.com",
    "old_email_type": "old",
    "new_email_type": "new",
}

request_json_bad = {
    "email": "test@gmail.com",
    "new_email_type": "new",
}

request_json_unsafe_input = {
    "email": "test@gmail.com!",
    "email_type": "type",
}

##################################### function data ###########################################

# get_notification_data

get_notification_data_pgdb_return = [
    (
        {
            "email": "test@gmail.com",
            "first_name": "first",
            "last_name": "last",
            "email_type": "test type",
        },
    ),
]

get_notification_data_result = [
    {
        "email": "test@gmail.com",
        "first_name": "first",
        "last_name": "last",
        "email_type": "test type",
    }
]

get_notification_data_sql = (
    "SELECT to_jsonb(row) FROM (SELECT u.email, u.first_name, u.last_name, "
    "e.email_type FROM public.user_email_notification JOIN public.users AS u "
    "ON u.user_id = user_email_notification.user_id JOIN public.email_type "
    "AS e ON e.email_type_id = user_email_notification.email_type_id WHERE"
    " user_email_notification.user_id IN (SELECT user_id FROM public.users"
    " WHERE email = 'test@gmail.com')) as row"
)

# modify_notification

modify_notification_sql = (
    "UPDATE public.user_email_notification SET email_type_id = "
    "(SELECT email_type_id FROM public.email_type WHERE email_type = 'new') "
    "WHERE user_id = (SELECT user_id FROM public.users WHERE email = "
    "'test@gmail.com')  AND email_type_id = (SELECT email_type_id "
    "FROM public.email_type WHERE email_type = 'old')"
)

# delete_notification

delete_notification_call = "DELETE FROM public.user_email_notification WHERE user_id IN (SELECT user_id FROM public.users WHERE email = 'test@gmail.com') AND email_type_id IN (SELECT email_type_id FROM public.email_type WHERE email_type = 'test type')"
