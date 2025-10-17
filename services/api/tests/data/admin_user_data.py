import multidict

##################################### request data ###########################################

request_environ = multidict.MultiDict([])

request_args_good = {"user_email": "test@gmail.com"}

request_args_bad = {"user_email": 5}

request_json_good = {
    "orig_email": "test@gmail.com",
    "email": "test@gmail.com",
    "first_name": "test",
    "last_name": "test",
    "super_user": True,
    "organizations_to_add": [{"name": "Test Org3", "role": "admin"}],
    "organizations_to_modify": [{"name": "Test Org2", "role": "user"}],
    "organizations_to_remove": [{"name": "Test Org1", "role": "user"}],
}

request_json_bad = {
    "orig_email": "test@gmail.com",
    "email": "test@gmail.com",
    "first_name": "test",
    "last_name": "test",
    "super_user": True,
    "organizations_to_add": [{"name": "Test Org3", "role": "admin"}],
    "organizations_to_remove": [{"name": "Test Org1", "role": "user"}],
}

request_json_unsafe_input = {
    "orig_email": "test@gmail.com",
    "email": "test@gmail.com",
    "first_name": "test",
    "last_name": "tes--t",
    "super_user": True,
    "organizations_to_add": [{"name": "Test Org3#@", "role": "adm#!in"}],
    "organizations_to_modify": [{"name": "Test O%@!rg2", "role": "user"}],
    "organizations_to_remove": [{"name": "Test O!##rg1", "role": "!#user"}],
}

##################################### function data ###########################################

get_user_data_return = [
    (
        {
            "email": "test@gmail.com",
            "first_name": "test",
            "last_name": "test",
            "super_user": "1",
            "name": "test org",
            "role": "admin",
        },
    ),
]

get_user_data_expected = [
    {
        "email": "test@gmail.com",
        "first_name": "test",
        "last_name": "test",
        "super_user": True,
        "organizations": [{"name": "test org", "role": "admin"}],
    }
]

expected_get_user_query = (
    "SELECT to_jsonb(row) "
    "FROM ("
    "SELECT u.email, u.first_name, u.last_name, u.super_user, org.name, roles.name AS role "
    "FROM public.users u "
    "LEFT JOIN public.user_organization AS uo ON uo.user_id = u.user_id "
    "LEFT JOIN public.organizations AS org ON org.organization_id = uo.organization_id "
    "LEFT JOIN public.roles ON roles.role_id = uo.role_id"
    ") as row"
)

expected_get_user_query_one = (
    "SELECT to_jsonb(row) "
    "FROM ("
    "SELECT u.email, u.first_name, u.last_name, u.super_user, org.name, roles.name AS role "
    "FROM public.users u "
    "LEFT JOIN public.user_organization AS uo ON uo.user_id = u.user_id "
    "LEFT JOIN public.organizations AS org ON org.organization_id = uo.organization_id "
    "LEFT JOIN public.roles ON roles.role_id = uo.role_id"
    " WHERE u.email = :user_email"
    ") as row"
)
expected_get_user_query_one_params = {"user_email": "test@gmail.com"}

modify_user_sql = (
    "UPDATE public.users SET "
    "email=:email, "
    "first_name=:first_name, "
    "last_name=:last_name, "
    "super_user=:super_user "
    "WHERE email=:orig_email"
)
modify_user_params = {
    "email": "test@gmail.com",
    "first_name": "test",
    "last_name": "test",
    "super_user": "1",
    "orig_email": "test@gmail.com",
}

add_org_sql = (
    "INSERT INTO public.user_organization(user_id, organization_id, role_id) VALUES"
    " ("
    "(SELECT user_id FROM public.users WHERE email = :email), "
    "(SELECT organization_id FROM public.organizations WHERE name = :org_name_0), "
    "(SELECT role_id FROM public.roles WHERE name = :org_role_0)"
    ")"
)
add_org_params = {
    "email": "test@gmail.com",
    "org_name_0": "Test Org3",
    "org_role_0": "admin",
}

modify_org_sql = (
    "UPDATE public.user_organization "
    "SET role_id = (SELECT role_id FROM public.roles WHERE name = :role) "
    "WHERE user_id = (SELECT user_id FROM public.users WHERE email = :email) "
    "AND organization_id = (SELECT organization_id FROM public.organizations WHERE name = :org_name)"
)
modify_org_params = {
    "email": "test@gmail.com",
    "org_name": "Test Org2",
    "role": "user",
}

remove_org_sql = (
    "DELETE FROM public.user_organization WHERE "
    "user_id = (SELECT user_id FROM public.users WHERE email = :email) "
    "AND organization_id IN (SELECT organization_id FROM public.organizations WHERE name IN (:org_name_0))"
)
remove_org_params = {
    "email": "test@gmail.com",
    "org_name_0": "Test Org1",
}

delete_user_calls = [
    "DELETE FROM public.user_organization WHERE user_id = (SELECT user_id FROM public.users WHERE email = :email)",
    "DELETE FROM public.users WHERE email = :email",
]
