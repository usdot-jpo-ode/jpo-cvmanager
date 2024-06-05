import multidict
import datetime

##################################### request_data ###########################################

request_args_good = multidict.MultiDict(
    [
        ("geometry", "-13 14"),
        ("start", "2022-05-23T12:00:00"),
        ("end", "2022-05-24T12:00:00"),
    ]
)

request_args_bad_message = multidict.MultiDict(
    [("start", "2022-05-23T12:00:00"), ("end", "2022-05-24T12:00:00")]
)

request_params_good = multidict.MultiDict(
    [
        ("user_info", {"organizations": [{"name": "Test", "role": "user"}]}),
        ("organization", "Test"),
    ]
)

##################################### query_geo_data ###########################################

point_list = [10.000, -10.000]

mongo_geo_data_response = [
    {
        "_id": "bson_id",
        "type": "Feature",
        "properties": {"id": "8.8.8.8", "timestamp": datetime.datetime.utcnow()},
        "geometry": {"type": "Point", "coordinates": point_list},
    }
]

processed_geo_message_data = [
    {
        "type": "Feature",
        "properties": {
            "id": "8.8.8.8",
            "time": datetime.datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%Sz"),
        },
        "geometry": {"type": "Point", "coordinates": point_list},
    }
]

bq_geo_data_response = [
    {
        "Ip": "8.8.8.8",
        "long": point_list[0],
        "lat": point_list[1],
        "time": datetime.datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%SZ"),
    },
]

rsu_geo_query = (
    "SELECT DISTINCT geo.metadata.originIp as Ip, geo.payload.data.coreData.position.longitude as long, "
    "geo.payload.data.coreData.position.latitude as lat, geo.metadata.odeReceivedAt as time "
    "FROM `Fake_table` WHERE TIMESTAMP(geo.metadata.odeReceivedAt) "
    '>= TIMESTAMP("2022-05-23T18:00:00") AND TIMESTAMP(geo.metadata.odeReceivedAt) <= TIMESTAMP("2022-05-24T18:00:00") '
    "AND ST_CONTAINS(ST_GEOGFROM('POLYGON((-105.63907347720362 39.785390458673525,-105.64302767662384 39.73371501339022))"
    "'), ST_GEOGPOINT(geo.payload.data.coreData.position.longitude, geo.payload.data.coreData.position.latitude))"
)

record_one = multidict.MultiDict(
    [
        ("Ip", "10.11.81.24"),
        ("lat", 39),
        ("long", -105),
        ("time", "2022-5-23T19:06:22.785568Z"),
    ]
)
record_two = multidict.MultiDict(
    [
        ("Ip", "172.16.28.136"),
        ("lat", 39.5),
        ("long", -105.55),
        ("time", "2022-5-23T19:12:28.506411Z"),
    ]
)
record_three = multidict.MultiDict(
    [
        ("Ip", "172.16.28.23"),
        ("lat", 40),
        ("long", -106),
        ("time", "2022-5-23T20:12:31.506417Z"),
    ]
)

geo_data_expected_single = [
    {
        "type": "Feature",
        "geometry": {"type": "Point", "coordinates": [-105, 39]},
        "properties": {"id": "10.11.81.24", "time": "2022-05-23T13:06:22.785568-06:00"},
    }
]

geo_data_expected_multiple = [
    {
        "type": "Feature",
        "geometry": {"type": "Point", "coordinates": [-105, 39]},
        "properties": {"id": "10.11.81.24", "time": "2022-05-23T13:06:22.785568-06:00"},
    },
    {
        "type": "Feature",
        "geometry": {"type": "Point", "coordinates": [-105.55, 39.5]},
        "properties": {
            "id": "172.16.28.136",
            "time": "2022-05-23T13:12:28.506411-06:00",
        },
    },
    {
        "type": "Feature",
        "geometry": {"type": "Point", "coordinates": [-106, 40]},
        "properties": {
            "id": "172.16.28.23",
            "time": "2022-05-23T14:12:31.506417-06:00",
        },
    },
]
