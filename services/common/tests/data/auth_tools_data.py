query_organizations = ["Test Org", "Test Org 3"]
rsu_set_for_org_query_statement: tuple[str, dict] = (
    (
        "SELECT rsu.ipv4_address::text AS ipv4_address "
        "FROM public.rsus rsu "
        "JOIN public.rsu_organization AS rsu_org ON rsu_org.rsu_id = rsu.rsu_id "
        "JOIN public.organizations AS org ON org.organization_id = rsu_org.organization_id "
        "WHERE org.name IN (:item_0, :item_1)"
    ),
    {"item_0": "Test Org", "item_1": "Test Org 3"},
)

rsu_query_return = [
    {"ipv4_address": "1.1.1.1"},
    {"ipv4_address": "1.1.1.2"},
    {"ipv4_address": "1.1.1.3"},
]
rsu_query_statement: tuple[str, dict] = (
    (
        "SELECT rsu.ipv4_address::text AS ipv4_address "
        "FROM public.rsus rsu "
        "JOIN public.rsu_organization AS rsu_org ON rsu_org.rsu_id = rsu.rsu_id "
        "JOIN public.organizations AS org ON org.organization_id = rsu_org.organization_id "
        "WHERE org.name IN (:item_0) AND rsu.ipv4_address = :rsu_ip"
    ),
    {"rsu_ip": "1.1.1.1", "item_0": "a"},
)

intersection_query_return = [
    {"intersection_number": "1"},
    {"intersection_number": "2"},
    {"intersection_number": "3"},
]
intersection_query_statement: tuple[str, dict] = (
    (
        "SELECT intersection.intersection_number as intersection_number "
        "FROM public.intersections intersection "
        "JOIN public.intersection_organization AS intersection_org ON intersection_org.intersection_id = intersection.intersection_id "
        "JOIN public.organizations AS org ON org.organization_id = intersection_org.organization_id "
        "WHERE org.name IN (:item_0) AND intersection.intersection_number = :intersection_id"
    ),
    {"intersection_id": "1", "item_0": "a"},
)

user_query_return = [
    {"email": "test1@gmail.com"},
]
user_query_statement: tuple[str, dict] = (
    (
        "SELECT u.email as email "
        "FROM public.users u "
        "JOIN public.user_organization AS user_org ON user_org.user_id = u.user_id "
        "JOIN public.organizations AS org ON org.organization_id = user_org.organization_id "
        "WHERE org.name IN (:item_0) AND u.email = :user_email"
    ),
    {"user_email": "test1@gmail.com", "item_0": "a"},
)
