##################################### request_data ###########################################

request_json_good = {"geometry": [[30, 10], [10, 30], [40, 40]]}
request_json_bad = {"geometry": "bad_data"}

##################################### function_data ###########################################

coordinate_list = [[30, 10], [10, 30], [40, 40]]
expected_bq_query = """
        SELECT 
            sas.segment_id, 
            sev.total_hard_brake_count, 
            sev.shape_geom  
        FROM 
            `test_segment_agg_stats_table` AS sas 
        INNER JOIN 
            `test_segment_event_stats_table` AS sev ON (sas.segment_id = sev.segment_id)
        WHERE ST_WITHIN(
            sev.shape_geom, 
            ST_GEOGFROMTEXT('POLYGON((30 10, 10 30, 40 40))')
        ) OR ST_INTERSECTS(
            sev.shape_geom, 
            ST_GEOGFROMTEXT('POLYGON((30 10, 10 30, 40 40))'))
        ORDER BY segment_id DESC
        """

query_return_data = {
    "segment_id": [1, 2, 3],
    "total_hard_brake_count": [5, 3, 8],
    "shape_geom": [
        "LINESTRING (30 10, 10 30, 40 40)",
        "LINESTRING (30 10, 10 30, 40 40)",
        "LINESTRING (30 10, 10 30, 40 40)",
    ],
}
feature_list = [
    {
        "type": "Feature",
        "geometry": {
            "type": "LineString",
            "coordinates": [(30, 10), (10, 30), (40, 40)],
        },
        "properties": {
            "segment_id": 1,
            "total_hard_brake_count": 5,
        },
    },
    {
        "type": "Feature",
        "geometry": {
            "type": "LineString",
            "coordinates": [(30, 10), (10, 30), (40, 40)],
        },
        "properties": {
            "segment_id": 2,
            "total_hard_brake_count": 3,
        },
    },
    {
        "type": "Feature",
        "geometry": {
            "type": "LineString",
            "coordinates": [(30, 10), (10, 30), (40, 40)],
        },
        "properties": {
            "segment_id": 3,
            "total_hard_brake_count": 8,
        },
    },
]
