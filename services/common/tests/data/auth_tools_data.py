rsu_query_return = [
    {"ipv4_address": "1.1.1.1"},
    {"ipv4_address": "1.1.1.2"},
    {"ipv4_address": "1.1.1.3"},
]

query_organizations = set(["Test Org 3", "Test Org"])
rsu_query_statement = (
    "SELECT rsu.ipv4_address::text AS ipv4_address "
    "FROM public.rsus rsu "
    "JOIN public.rsu_organization AS rsu_org ON rsu_org.rsu_id = rsu.rsu_id "
    "JOIN public.organizations AS org ON org.organization_id = rsu_org.organization_id "
    "WHERE org.name = ANY (ARRAY['Test Org', 'Test Org 3'])"
)

intersection_query_return = [
    {"intersection_number": "1"},
    {"intersection_number": "2"},
    {"intersection_number": "3"},
]
intersection_query_statement = (
    "SELECT intersection.intersection_number as intersection_number "
    "FROM public.intersections rsu "
    "JOIN public.intersection_organization AS intersection_org ON intersection_org.intersection_id = intersection.intersection_id "
    "JOIN public.organizations AS org ON org.organization_id = intersection_org.organization_id "
    "WHERE org.name = ANY (ARRAY['Test Org', 'Test Org 3'])"
)

user_query_return = [
    {"email": "test1@gmail.com"},
]
user_query_statement = (
    "SELECT u.email as email "
    "FROM public.users u "
    "JOIN public.user_organization AS user_org ON user_org.user_id = intersection.user_id "
    "JOIN public.organizations AS org ON org.organization_id = user_org.organization_id "
    "WHERE org.name = ANY (ARRAY['Test Org', 'Test Org 3'])"
)
