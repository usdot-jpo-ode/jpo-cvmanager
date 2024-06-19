from common.pgquery import query_db

def get_email_list(msg_type):
    email_list = []
    email_query = (
        "SELECT to_jsonb(row) FROM (SELECT email FROM public.users WHERE user_id IN "
        "(SELECT user_id FROM public.user_email_notification WHERE email_type_id = "
        f"(SELECT email_type_id FROM email_type WHERE email_type = '{msg_type}'))) as row"
    )

    data = query_db(email_query)
    for row in data:
        row = dict(row[0])
        email_list.append(row["email"])

    return email_list