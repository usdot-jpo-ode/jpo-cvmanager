from common.pgquery import query_db


def build_user_email_list(msg_type, org_name):
    # Build the user email query based on the availability of an organization name
    email_query = (
        "SELECT to_jsonb(row) FROM ("
        "SELECT email FROM public.users "
        "WHERE user_id IN ("
        "SELECT user_id FROM public.user_email_notification "
        "WHERE email_type_id = ("
        f"SELECT email_type_id FROM public.email_type WHERE email_type = '{msg_type}'"
        "))"
    )
    if org_name:
        email_query += (
            " AND user_id IN ("
            "SELECT user_id FROM public.user_organization "
            "WHERE organization_id = ("
            f"SELECT organization_id FROM public.organizations WHERE name = '{org_name}'"
            "))"
        )
    email_query += ") as row"

    data = query_db(email_query)

    email_list = []
    for row in data:
        row = dict(row[0])
        email_list.append(row["email"])
    return email_list


def build_org_email_list(org_name):
    email_query = (
        "SELECT to_jsonb(row) FROM ("
        f"SELECT email FROM public.organizations WHERE name = '{org_name}'"
        ") as row"
    )

    data = query_db(email_query)

    email_list = []
    for row in data:
        row = dict(row[0])
        email = row["email"]
        if email:
            if email.strip() != "":
                email_list.append(row["email"])
    return email_list


def get_email_list(msg_type, org_name=None):
    email_list = []

    # Verify presence of organization email if org_name is provided
    if org_name:
        email_list = build_org_email_list(org_name)

    # Generate the email list based off of user email preferences if no organization email is configured
    if len(email_list) == 0:
        email_list = build_user_email_list(msg_type, org_name)

    return email_list
