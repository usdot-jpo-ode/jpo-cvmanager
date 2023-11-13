import multidict

##################################### request_data ###########################################

request_params_good = multidict.MultiDict([
        ('user_info', {'organizations': [{'name': 'Test', 'role': 'user'}]}),
        ('organization', 'Test')
        ])

###################################### Single Result ##########################################
return_value_single_result = [
    ({
        "id": 228,
        "type": "Feature",
        "geometry": {
                "type": "Point",
                "coordinates": [
                    -104.891699,
                    39.563912
                ]
        },
        "properties": {
            "Model": 1,
            "Milepost": 24.86,
            "Geography": "0101000020E61000000FB8AE9811395AC036E9B6442EC84340",
            "OSVersion": 2,
            "RsuDataId": 228,
            "Ipv4Address": "172.16.28.49",
            "CredentialId": 2,
            "PrimaryRoute": "C-470",
            "SerialNumber": "PEM00055",
            "FirmwareVersion": 1
        }
    },)
]

expected_rsu_data_single_result = {
    'rsuList': [
        {
            'id': 228,
            'type': 'Feature',
            'geometry': {
                'type': 'Point',
                'coordinates': [
                    -104.891699,
                    39.563912
                ]
            },
            'properties': {
                'Model': 1,
                'Milepost': 24.86,
                'Geography': '0101000020E61000000FB8AE9811395AC036E9B6442EC84340',
                'OSVersion': 2,
                'RsuDataId': 228,
                'Ipv4Address': '172.16.28.49',
                'CredentialId': 2,
                'PrimaryRoute': 'C-470',
                'SerialNumber': 'PEM00055',
                'FirmwareVersion': 1
            }
        }
    ]
}
###################################### Multiple Results ##########################################
return_value_multiple_results = [
    ({
        'id': 228,
        'type': 'Feature',
        'geometry': {
                'type': 'Point',
                'coordinates': [
                    -104.891699,
                    39.563912
                ]
        },
        'properties': {
            'Model': 1,
            'Milepost': 24.86,
            'Geography': '0101000020E61000000FB8AE9811395AC036E9B6442EC84340',
            'OSVersion': 2,
            'RsuDataId': 228,
            'Ipv4Address': '172.16.28.49',
            'CredentialId': 2,
            'PrimaryRoute': 'C-470',
            'SerialNumber': 'PEM00055',
            'FirmwareVersion': 1
        }
    },),
    ({
        'id': 229,
        'type': 'Feature',
        'geometry': {
                'type': 'Point',
                'coordinates': [
                    -104.882612,
                    39.56041
                ]
        },
        'properties': {
            'Model': 1,
            'Milepost': 25.4,
            'Geography': '0101000020E6100000A8C30AB77C385AC0F92CCF83BBC74340',
            'OSVersion': 2,
            'RsuDataId': 229,
            'Ipv4Address': '172.16.28.50',
            'CredentialId': 2,
            'PrimaryRoute': 'C-470',
            'SerialNumber': 'PEM00060',
            'FirmwareVersion': 1
        }
    },),
    ({
        'id': 230,
        'type': 'Feature',
        'geometry': {
                'type': 'Point',
            'coordinates': [
                        -104.877269,
                        39.555865
            ]
        },
        'properties': {
            'Model': 1,
            'Milepost': 25.84,
            'Geography': '0101000020E6100000DB32E02C25385AC0DAFE959526C74340',
            'OSVersion': 2,
            'RsuDataId': 230,
            'Ipv4Address': '172.16.28.51',
            'CredentialId': 2,
            'PrimaryRoute': 'C-470',
            'SerialNumber': 'PEM00084',
            'FirmwareVersion': 1
        }
    },)
]

expected_rsu_data_multiple_results = {
    'rsuList': [
        {
            'id': 228,
            'type': 'Feature',
            'geometry': {
                'type': 'Point',
                'coordinates': [
                    -104.891699,
                    39.563912
                ]
            },
            'properties': {
                'Model': 1,
                'Milepost': 24.86,
                'Geography': '0101000020E61000000FB8AE9811395AC036E9B6442EC84340',
                'OSVersion': 2,
                'RsuDataId': 228,
                'Ipv4Address': '172.16.28.49',
                'CredentialId': 2,
                'PrimaryRoute': 'C-470',
                'SerialNumber': 'PEM00055',
                'FirmwareVersion': 1
            }
        },
        {
            'id': 229,
            'type': 'Feature',
            'geometry': {
                'type': 'Point',
                'coordinates': [
                    -104.882612,
                    39.56041
                ]
            },
            'properties': {
                'Model': 1,
                'Milepost': 25.4,
                'Geography': '0101000020E6100000A8C30AB77C385AC0F92CCF83BBC74340',
                'OSVersion': 2,
                'RsuDataId': 229,
                'Ipv4Address': '172.16.28.50',
                'CredentialId': 2,
                'PrimaryRoute': 'C-470',
                'SerialNumber': 'PEM00060',
                'FirmwareVersion': 1
            }
        },
        {
            'id': 230,
            'type': 'Feature',
            'geometry': {
                'type': 'Point',
                'coordinates': [
                    -104.877269,
                    39.555865
                ]
            },
            'properties': {
                'Model': 1,
                'Milepost': 25.84,
                'Geography': '0101000020E6100000DB32E02C25385AC0DAFE959526C74340',
                'OSVersion': 2,
                'RsuDataId': 230,
                'Ipv4Address': '172.16.28.51',
                'CredentialId': 2,
                'PrimaryRoute': 'C-470',
                'SerialNumber': 'PEM00084',
                'FirmwareVersion': 1
            }
        }
    ]
}
