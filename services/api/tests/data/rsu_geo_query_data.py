import multidict

##################################### request_data ###########################################

request_args_good = multidict.MultiDict(
    [
        (
            "geometry",
            [
                [-105.18530732421814, 39.727751014682724],
                [-105.42357331542925, 39.75572678980467],
                [-105.4228866699211, 39.65644066619139],
                [-105.12213593749924, 39.680218966965384],
                [-105.18530732421814, 39.727751014682724],
            ],
        )
    ]
)

request_args_bad_message = multidict.MultiDict([("geometry", [[5.1], 10.444])])

request_args_bad_type = multidict.MultiDict([("geometry", "bad type")])

request_params_good = multidict.MultiDict(
    [
        ("user_info", {"organizations": [{"name": "Test", "role": "user"}]}),
        ("organization", "Test"),
    ]
)

##################################### query_org_rsus ###########################################

rsu_org_query = (
    "SELECT ipv4_address from public.rsus as rd "
    "JOIN public.rsu_organization_name AS ron_v ON ron_v.rsu_id = rd.rsu_id "
    f"WHERE ron_v.name = 'Test'"
)

point_list = [
    [-105.34460908203145, 39.724583197251334],
    [-105.34666901855489, 39.670180083300174],
    [-105.25122529296911, 39.679162192647944],
    [-105.2539718750002, 39.72088725644132],
    [-105.34460908203145, 39.724583197251334],
]

point_list_vendor = [
    [-105.34460908203145, 39.724583197251334],
    [-105.34666901855489, 39.670180083300174],
    [-105.25122529296911, 39.679162192647944],
    [-105.2539718750002, 39.72088725644132],
    [-105.34460908203145, 39.724583197251334],
]

rsu_devices_query = "SELECT to_jsonb(row) FROM (SELECT ipv4_address as ip, ST_X(geography::geometry) AS long, ST_Y(geography::geometry) AS lat FROM rsus WHERE ipv4_address = ANY('{10.11.81.12}'::inet[]) AND ST_Contains(ST_SetSRID(ST_GeomFromText('POLYGON((-105.34460908203145 39.724583197251334,-105.34666901855489 39.670180083300174,-105.25122529296911 39.679162192647944,-105.2539718750002 39.72088725644132,-105.34460908203145 39.724583197251334))'), 4326), rsus.geography::geometry)) as row"

rsu_devices_query_vendor = "SELECT to_jsonb(row) FROM (SELECT ipv4_address as ip, ST_X(geography::geometry) AS long, ST_Y(geography::geometry) AS lat FROM rsus WHERE ipv4_address = ANY('{10.11.81.12}'::inet[])  AND ipv4_address IN (SELECT rd.ipv4_address FROM public.rsus as rd JOIN public.rsu_models as rm ON rm.rsu_model_id = rd.model JOIN public.manufacturers as man on man.manufacturer_id = rm.manufacturer WHERE man.name = 'Test') AND ST_Contains(ST_SetSRID(ST_GeomFromText('POLYGON((-105.34460908203145 39.724583197251334,-105.34666901855489 39.670180083300174,-105.25122529296911 39.679162192647944,-105.2539718750002 39.72088725644132,-105.34460908203145 39.724583197251334))'), 4326), rsus.geography::geometry)) as row"