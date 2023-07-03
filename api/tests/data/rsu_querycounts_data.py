import multidict
from datetime import datetime
import pytz

##################################### request_data ###########################################

request_args_good = multidict.MultiDict([
        ('message', 'BSM'), 
        ('start', '2022-05-23T12:00:00'),
        ('end', '2022-05-24T12:00:00')
        ])

request_args_bad_message = multidict.MultiDict([
        ('message', 'BMS'), 
        ('start', '2022-05-23T12:00:00'),
        ('end', '2022-05-24T12:00:00')
        ])

request_args_bad_type = multidict.MultiDict([
        ('message', 14), 
        ('start', '2022-05-23T12:00:00'),
        ('end', '2022-05-24T12:00:00')
        ])

request_params_good = multidict.MultiDict([
        ('user_info', {'organizations': [{'name': 'Test', 'role': 'user'}]}),
        ('organization', 'Test')
        ])

##################################### query_rsu_counts ###########################################

rsu_counts_query_bq = "SELECT RSU, Road, SUM(Count) as Count FROM `Fake_table` " \
                "WHERE Date >= DATETIME(\"2022-05-23T18:00:00\") " \
                "AND Date < DATETIME(\"2022-05-24T18:00:00\") " \
                "AND Type = \"BSM\" GROUP BY RSU, Road "
date = pytz.timezone('UTC').localize(datetime.strptime("2023-01-01T07:00:00", "%Y-%m-%dT%H:%M:%S"))
rsu_counts_query_mongo = {
        "timestamp": {"$gte": date, "$lt": date},
        "message_type": 'BSM',
    }

rsu_one = multidict.MultiDict([
        ('RSU', '10.11.81.24'), 
        ('Road', 'Region 1'),
        ('Count', '0')
    ])
rsu_two = multidict.MultiDict([
    ('RSU', '172.16.28.136'), 
    ('Road', 'I70'),
    ('Count', '8472')
])
rsu_three = multidict.MultiDict([
    ('RSU', '172.16.28.23'), 
    ('Road', 'C-470'),
    ('Count', '0')
])

rsu_counts_expected_single = {"10.11.81.24": {"road": "Region 1", "count": "0"}}

rsu_counts_expected_multiple = {
        "10.11.81.24": {"road": "Region 1", "count": "0"},
        "172.16.28.136": {"road": "I70", "count": "8472"},
        "172.16.28.23": {"road": "C-470", "count": "0"}
        }

rsu_counts_expected_limited_rsus = {
        "172.16.28.136": {"road": "I70", "count": "8472"},
        "172.16.28.23": {"road": "C-470", "count": "0"}
        }