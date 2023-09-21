import multidict
import datetime

##################################### request_data ###########################################

request_params_good = multidict.MultiDict(
    [("user_info", {"organizations": [{"name": "Test", "role": "user"}]}), ("organization", "Test")]
)

###################################### Sample Data ##########################################

point_list = [10.000, -10.000]

mongo_bsm_data_response = [
    {
        "_id": "bson_id",
        "type": "Feature",
        "properties": {"id": "8.8.8.8", "timestamp": datetime.datetime.utcnow()},
        "geometry": {"type": "Point", "coordinates": point_list},
    }
]

processed_bsm_message_data = [
    {
        "type": "Feature",
        "properties": {"id": "8.8.8.8", "time": datetime.datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%S")},
        "geometry": {"type": "Point", "coordinates": point_list},
    }
]

bq_bsm_data_response = [
    {
        "Ip": "8.8.8.8",
        "long": point_list[0],
        "lat": point_list[1],
        "time": datetime.datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%SZ"),
    },
]
