import multidict

##################################### request_data ###########################################

request_args_good = multidict.MultiDict([
        ('geometry', [[-105.18530732421814,39.727751014682724],[-105.42357331542925,39.75572678980467],[-105.4228866699211,39.65644066619139],[-105.12213593749924,39.680218966965384],[-105.18530732421814,39.727751014682724]])
        ])

request_args_bad_message = multidict.MultiDict([
        ('geometry', [[5.1], 10.444])
        ])

request_args_bad_type = multidict.MultiDict([
        ('geometry', 'bad type')
        ])

request_params_good = multidict.MultiDict([
        ('user_info', {'organizations': [{'name': 'Test', 'role': 'user'}]}),
        ('organization', 'Test')
        ])

##################################### query_rsu_counts ###########################################

rsu_counts_query = "SELECT RSU, Road, SUM(Count) as Count FROM `Fake_table` " \
                "WHERE Date >= DATETIME(\"2022-05-23T18:00:00\") " \
                "AND Date < DATETIME(\"2022-05-24T18:00:00\") " \
                "AND Type = \"BSM\" GROUP BY RSU, Road "

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