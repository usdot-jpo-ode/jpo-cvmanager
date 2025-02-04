ssm_record_one = {
    "recordGeneratedAt": "2022-12-13T07:00:00.000+00:00",
    "metadata": {"originIp": "127.0.0.1", "recordType": "ssmTx"},
    "payload": {
        "data": {
            "status": {
                "signalStatus": [
                    {
                        "sigStatus": {
                            "signalStatusPackage": [
                                {
                                    "requester": {
                                        "request": 13,
                                        "role": "publicTransport",
                                    },
                                    "status": "granted",
                                }
                            ]
                        }
                    }
                ]
            }
        }
    },
}

ssm_record_two = {
    "recordGeneratedAt": "2022-12-14T07:00:00.000+00:00",
    "metadata": {"originIp": "127.0.0.1", "recordType": "ssmTx"},
    "payload": {
        "data": {
            "status": {
                "signalStatus": [
                    {
                        "sigStatus": {
                            "signalStatusPackage": [
                                {
                                    "requester": {
                                        "request": 10,
                                        "role": "publicTransport",
                                    },
                                    "status": "granted",
                                }
                            ]
                        }
                    }
                ]
            }
        }
    },
}

ssm_record_three = {
    "recordGeneratedAt": "2022-12-12T07:00:00.000+00:00",
    "metadata": {"originIp": "127.0.0.1", "recordType": "ssmTx"},
    "payload": {
        "data": {
            "status": {
                "signalStatus": [
                    {
                        "sigStatus": {
                            "signalStatusPackage": [
                                {
                                    "requester": {
                                        "request": 17,
                                        "role": "publicTransport",
                                    },
                                    "status": "granted",
                                }
                            ]
                        }
                    }
                ]
            }
        }
    },
}


srm_record_one = {
    "recordGeneratedAt": "2022-12-13T07:00:00.000+00:00",
    "metadata": {"originIp": "127.0.0.1", "recordType": "srmTx"},
    "payload": {
        "data": {
            "requests": {"signalRequestPackage": [{"request": {"requestID": 9}}]},
            "requestor": {
                "type": {"role": "publicTransport"},
                "position": {"position": {"latitude": "100.00", "longitude": "50.00"}},
            },
        }
    },
}
srm_record_two = {
    "recordGeneratedAt": "2022-12-12T07:00:00.000+00:00",
    "metadata": {"originIp": "127.0.0.1", "recordType": "srmTx"},
    "payload": {
        "data": {
            "requests": {"signalRequestPackage": [{"request": {"requestID": 13}}]},
            "requestor": {
                "type": {"role": "publicTransport"},
                "position": {"position": {"latitude": "101.00", "longitude": "49.00"}},
            },
        }
    },
}
srm_record_three = {
    "recordGeneratedAt": "2022-12-14T07:00:00.000+00:00",
    "metadata": {"originIp": "127.0.0.1", "recordType": "srmTx"},
    "payload": {
        "data": {
            "requests": {"signalRequestPackage": [{"request": {"requestID": 17}}]},
            "requestor": {
                "type": {"role": "publicTransport"},
                "position": {"position": {"latitude": "102.00", "longitude": "53.00"}},
            },
        }
    },
}

ssm_single_result_expected = [
    {
        "time": "12/13/2022 12:00:00 AM",
        "ip": "127.0.0.1",
        "requestId": 13,
        "role": "publicTransport",
        "status": "granted",
        "type": "ssmTx",
    }
]

ssm_multiple_result_expected = [
    {
        "time": "12/13/2022 12:00:00 AM",
        "ip": "127.0.0.1",
        "requestId": 13,
        "role": "publicTransport",
        "status": "granted",
        "type": "ssmTx",
    },
    {
        "time": "12/14/2022 12:00:00 AM",
        "ip": "127.0.0.1",
        "requestId": 10,
        "role": "publicTransport",
        "status": "granted",
        "type": "ssmTx",
    },
    {
        "time": "12/12/2022 12:00:00 AM",
        "ip": "127.0.0.1",
        "requestId": 17,
        "role": "publicTransport",
        "status": "granted",
        "type": "ssmTx",
    },
]

srm_single_result_expected = [
    {
        "time": "12/13/2022 12:00:00 AM",
        "ip": "127.0.0.1",
        "requestId": 9,
        "role": "publicTransport",
        "lat": "100.00",
        "long": "50.00",
        "type": "srmTx",
        "status": "N/A",
    }
]

srm_multiple_result_expected = [
    {
        "time": "12/13/2022 12:00:00 AM",
        "ip": "127.0.0.1",
        "requestId": 9,
        "role": "publicTransport",
        "lat": "100.00",
        "long": "50.00",
        "type": "srmTx",
        "status": "N/A",
    },
    {
        "time": "12/12/2022 12:00:00 AM",
        "ip": "127.0.0.1",
        "requestId": 13,
        "role": "publicTransport",
        "lat": "101.00",
        "long": "49.00",
        "type": "srmTx",
        "status": "N/A",
    },
    {
        "time": "12/14/2022 12:00:00 AM",
        "ip": "127.0.0.1",
        "requestId": 17,
        "role": "publicTransport",
        "lat": "102.00",
        "long": "53.00",
        "type": "srmTx",
        "status": "N/A",
    },
]


srm_processed_one = {
    "time": "12/13/2022 12:00:00 AM",
    "ip": "127.0.0.1",
    "requestId": 9,
    "role": "publicTransport",
    "lat": "100.00",
    "long": "50.00",
    "type": "srmTx",
    "status": "N/A",
}

srm_processed_two = {
    "time": "12/12/2022 12:00:00 AM",
    "ip": "127.0.0.1",
    "requestId": 13,
    "role": "publicTransport",
    "lat": "101.00",
    "long": "49.00",
    "type": "srmTx",
    "status": "N/A",
}

srm_processed_three = {
    "time": "12/14/2022 12:00:00 AM",
    "ip": "127.0.0.1",
    "requestId": 17,
    "role": "publicTransport",
    "lat": "102.00",
    "long": "53.00",
    "type": "srmTx",
    "status": "N/A",
}
