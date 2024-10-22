import multidict

##################################### request data ###########################################

request_environ = multidict.MultiDict([])

request_args_intersection_good = {"intersection_id": "1123"}
request_args_all_good = {"intersection_id": "all"}
request_args_str_bad = {"intersection_id": 5}

request_json_good = {
    "intersection_id": "1121",
    "orig_intersection_id": "1123",
    "ref_pt": {"longitude": 40.1, "latitude": 41.1},
    "bbox": {
        "longitude1": 42.1,
        "latitude1": 43.1,
        "longitude2": 44.1,
        "latitude2": 45.1,
    },
    "intersection_name": "test intersection",
    "origin_ip": "10.0.0.1",
    "organizations_to_add": ["Test Org1", "Test Org2"],
    "organizations_to_remove": ["Test Org3", "Test Org4"],
    "rsus_to_add": ["1.1.1.1", "1.1.1.2"],
    "rsus_to_remove": ["1.1.1.3", "1.1.1.4"],
}

request_json_bad = {
    "intersection_id": "1123",
    "orig_intersection_id": "1123",
    "ref_pt": {"longitude": "test", "latitude": 41.1},
    "bbox": {
        "longitude1": 42.1,
        "latitude1": 43.1,
        "longitude2": 44.1,
        "latitude2": 45.1,
    },
    "intersection_name": "test intersection",
    "origin_ip": "10.0.0.1",
    "organizations_to_add": ["Test Org2"],
    "organizations_to_remove": ["Test Org1"],
    "rsus_to_add": ["1.1.1.1"],
    "rsus_to_remove": ["1.1.1.2"],
}


##################################### function data ###########################################

get_intersection_data_return = [
    (
        {
            "intersection_number": "1123",
            "ref_pt_latitude": 40.1,
            "ref_pt_longitude": 41.1,
            "bbox_latitude_1": 42.1,
            "bbox_longitude_1": 43.1,
            "bbox_latitude_2": 44.1,
            "bbox_longitude_2": 45.1,
            "intersection_name": "Test intersection",
            "origin_ip": "10.0.0.1",
            "org_name": "test org",
            "rsu_ip": "1.1.1.1",
        },
    ),
]

expected_get_intersection_all = [
    {
        "intersection_id": "1123",
        "ref_pt": {
            "latitude": 40.1,
            "longitude": 41.1,
        },
        "bbox": {
            "latitude1": 42.1,
            "longitude1": 43.1,
            "latitude2": 44.1,
            "longitude2": 45.1,
        },
        "intersection_name": "Test intersection",
        "origin_ip": "10.0.0.1",
        "organizations": ["test org"],
        "rsus": ["1.1.1.1"],
    }
]

expected_get_intersection_query_all = (
    "SELECT to_jsonb(row) "
    "FROM ("
    "SELECT intersection_number, ST_X(ref_pt::geometry) AS ref_pt_longitude, ST_Y(ref_pt::geometry) AS ref_pt_latitude, "
    "ST_XMin(bbox::geometry) AS bbox_longitude_1, ST_YMin(bbox::geometry) AS bbox_latitude_1, "
    "ST_XMax(bbox::geometry) AS bbox_longitude_2, ST_YMax(bbox::geometry) AS bbox_latitude_2, "
    "intersection_name, origin_ip, "
    "org.name AS org_name, rsu.ipv4_address AS rsu_ip  "
    "FROM public.intersections "
    "JOIN public.intersection_organization AS ro ON ro.intersection_id = intersections.intersection_id  "
    "JOIN public.organizations AS org ON org.organization_id = ro.organization_id  "
    "LEFT JOIN public.rsu_intersection AS rsu_intersection ON ro.intersection_id = intersections.intersection_id  "
    "LEFT JOIN public.rsus AS rsu ON rsu.rsu_id = rsu_intersection.rsu_id"
    ") as row"
)

expected_get_intersection_query_one = (
    "SELECT to_jsonb(row) "
    "FROM ("
    "SELECT intersection_number, ST_X(ref_pt::geometry) AS ref_pt_longitude, ST_Y(ref_pt::geometry) AS ref_pt_latitude, "
    "ST_XMin(bbox::geometry) AS bbox_longitude_1, ST_YMin(bbox::geometry) AS bbox_latitude_1, "
    "ST_XMax(bbox::geometry) AS bbox_longitude_2, ST_YMax(bbox::geometry) AS bbox_latitude_2, "
    "intersection_name, origin_ip, "
    "org.name AS org_name, rsu.ipv4_address AS rsu_ip  "
    "FROM public.intersections "
    "JOIN public.intersection_organization AS ro ON ro.intersection_id = intersections.intersection_id  "
    "JOIN public.organizations AS org ON org.organization_id = ro.organization_id  "
    "LEFT JOIN public.rsu_intersection AS rsu_intersection ON ro.intersection_id = intersections.intersection_id  "
    "LEFT JOIN public.rsus AS rsu ON rsu.rsu_id = rsu_intersection.rsu_id"
    " WHERE intersection_number = '1123'"
    ") as row"
)

modify_intersection_sql = (
    "UPDATE public.intersections SET "
    "intersection_number='1121', "
    "ref_pt=ST_GeomFromText('POINT(40.1 41.1)')"
    ", bbox=ST_MakeEnvelope(42.1,43.1,44.1,45.1)"
    ", intersection_name='test intersection'"
    ", origin_ip='10.0.0.1' "
    "WHERE intersection_number='1123'"
)

add_org_sql = (
    "INSERT INTO public.intersection_organization(intersection_id, organization_id) VALUES"
    " ("
    "(SELECT intersection_id FROM public.intersections WHERE intersection_number = '1121'), "
    "(SELECT organization_id FROM public.organizations WHERE name = 'Test Org1')"
    "),"
    " ("
    "(SELECT intersection_id FROM public.intersections WHERE intersection_number = '1121'), "
    "(SELECT organization_id FROM public.organizations WHERE name = 'Test Org2')"
    ")"
)

remove_org_sql_3 = (
    "DELETE FROM public.intersection_organization WHERE "
    "intersection_id=(SELECT intersection_id FROM public.intersections WHERE intersection_number = '1121') "
    "AND organization_id=(SELECT organization_id FROM public.organizations WHERE name = 'Test Org3')"
)

remove_org_sql_4 = (
    "DELETE FROM public.intersection_organization WHERE "
    "intersection_id=(SELECT intersection_id FROM public.intersections WHERE intersection_number = '1121') "
    "AND organization_id=(SELECT organization_id FROM public.organizations WHERE name = 'Test Org4')"
)

add_rsu_sql = (
    "INSERT INTO public.rsu_intersection(rsu_id, intersection_id) VALUES"
    " ("
    "(SELECT rsu_id FROM public.rsus WHERE ipv4_address = '1.1.1.1'), "
    "(SELECT intersection_id FROM public.intersections WHERE intersection_number = '1121')"
    "),"
    " ("
    "(SELECT rsu_id FROM public.rsus WHERE ipv4_address = '1.1.1.2'), "
    "(SELECT intersection_id FROM public.intersections WHERE intersection_number = '1121')"
    ")"
)

remove_rsu_sql_3 = (
    "DELETE FROM public.rsu_intersection WHERE "
    "intersection_id=(SELECT intersection_id FROM public.intersections WHERE intersection_number = '1121') "
    "AND rsu_id=(SELECT rsu_id FROM public.rsus WHERE ipv4_address = '1.1.1.3')"
)

remove_rsu_sql_4 = (
    "DELETE FROM public.rsu_intersection WHERE "
    "intersection_id=(SELECT intersection_id FROM public.intersections WHERE intersection_number = '1121') "
    "AND rsu_id=(SELECT rsu_id FROM public.rsus WHERE ipv4_address = '1.1.1.4')"
)

delete_intersection_calls = [
    "DELETE FROM public.intersection_organization WHERE intersection_id=(SELECT intersection_id FROM public.intersections WHERE intersection_number = '1111')",
    "DELETE FROM public.rsu_intersection WHERE intersection_id=(SELECT intersection_id FROM public.intersections WHERE intersection_number = '1111')",
    "DELETE FROM public.intersections WHERE intersection_number = '1111'",
]
