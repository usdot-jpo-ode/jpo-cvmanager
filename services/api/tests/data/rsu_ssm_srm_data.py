import multidict
from datetime import datetime

from pytz import timezone

start_date = datetime.strftime(
    datetime.strptime("2022-12-13T07:00:00", "%Y-%m-%dT%H:%M:%S").astimezone(
        timezone("America/Denver")
    ),
    "%Y-%m-%dT%H:%M:%S",
)
end_date = datetime.strftime(
    datetime.strptime("2022-12-14T07:00:00", "%Y-%m-%dT%H:%M:%S").astimezone(
        timezone("America/Denver")
    ),
    "%Y-%m-%dT%H:%M:%S",
)

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
                                        "role": "publicTrasport",
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
                                        "role": "publicTrasport",
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
                                        "role": "publicTrasport",
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
                "type": {"role": "publicTrasport"},
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
                "type": {"role": "publicTrasport"},
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
                "type": {"role": "publicTrasport"},
                "position": {"position": {"latitude": "102.00", "longitude": "53.00"}},
            },
        }
    },
}

ssm_single_result_expected = [
    {
        "time": datetime.strftime(
            datetime.strptime(
                "12/13/2022 12:00:00 AM", "%m/%d/%Y %I:%M:%S %p"
            ).astimezone(timezone("America/Denver")),
            "%m/%d/%Y %I:%M:%S %p",
        ),
        "ip": "127.0.0.1",
        "requestId": 13,
        "role": "publicTrasport",
        "status": "granted",
        "type": "ssmTx",
    }
]

ssm_multiple_result_expected = [
    {
        "time": datetime.strftime(
            datetime.strptime(
                "12/13/2022 12:00:00 AM", "%m/%d/%Y %I:%M:%S %p"
            ).astimezone(timezone("America/Denver")),
            "%m/%d/%Y %I:%M:%S %p",
        ),
        "ip": "127.0.0.1",
        "requestId": 13,
        "role": "publicTrasport",
        "status": "granted",
        "type": "ssmTx",
    },
    {
        "time": datetime.strftime(
            datetime.strptime(
                "12/14/2022 12:00:00 AM", "%m/%d/%Y %I:%M:%S %p"
            ).astimezone(timezone("America/Denver")),
            "%m/%d/%Y %I:%M:%S %p",
        ),
        "ip": "127.0.0.1",
        "requestId": 10,
        "role": "publicTrasport",
        "status": "granted",
        "type": "ssmTx",
    },
    {
        "time": datetime.strftime(
            datetime.strptime(
                "12/12/2022 12:00:00 AM", "%m/%d/%Y %I:%M:%S %p"
            ).astimezone(timezone("America/Denver")),
            "%m/%d/%Y %I:%M:%S %p",
        ),
        "ip": "127.0.0.1",
        "requestId": 17,
        "role": "publicTrasport",
        "status": "granted",
        "type": "ssmTx",
    },
]

srm_single_result_expected = [
    {
        "time": datetime.strftime(
            datetime.strptime(
                "12/13/2022 12:00:00 AM", "%m/%d/%Y %I:%M:%S %p"
            ).astimezone(timezone("America/Denver")),
            "%m/%d/%Y %I:%M:%S %p",
        ),
        "ip": "127.0.0.1",
        "requestId": 9,
        "role": "publicTrasport",
        "lat": "100.00",
        "long": "50.00",
        "type": "srmTx",
        "status": "N/A",
    }
]

srm_multiple_result_expected = [
    {
        "time": datetime.strftime(
            datetime.strptime(
                "12/13/2022 12:00:00 AM", "%m/%d/%Y %I:%M:%S %p"
            ).astimezone(timezone("America/Denver")),
            "%m/%d/%Y %I:%M:%S %p",
        ),
        "ip": "127.0.0.1",
        "requestId": 9,
        "role": "publicTrasport",
        "lat": "100.00",
        "long": "50.00",
        "type": "srmTx",
        "status": "N/A",
    },
    {
        "time": datetime.strftime(
            datetime.strptime(
                "12/12/2022 12:00:00 AM", "%m/%d/%Y %I:%M:%S %p"
            ).astimezone(timezone("America/Denver")),
            "%m/%d/%Y %I:%M:%S %p",
        ),
        "ip": "127.0.0.1",
        "requestId": 13,
        "role": "publicTrasport",
        "lat": "101.00",
        "long": "49.00",
        "type": "srmTx",
        "status": "N/A",
    },
    {
        "time": datetime.strftime(
            datetime.strptime(
                "12/14/2022 12:00:00 AM", "%m/%d/%Y %I:%M:%S %p"
            ).astimezone(timezone("America/Denver")),
            "%m/%d/%Y %I:%M:%S %p",
        ),
        "ip": "127.0.0.1",
        "requestId": 17,
        "role": "publicTrasport",
        "lat": "102.00",
        "long": "53.00",
        "type": "srmTx",
        "status": "N/A",
    },
]


srm_processed_one = {
    "time": datetime.strftime(
        datetime.strptime("12/13/2022 12:00:00 AM", "%m/%d/%Y %I:%M:%S %p").astimezone(
            timezone("America/Denver")
        ),
        "%m/%d/%Y %I:%M:%S %p",
    ),
    "ip": "127.0.0.1",
    "requestId": 9,
    "role": "publicTrasport",
    "lat": "100.00",
    "long": "50.00",
    "type": "srmTx",
    "status": "N/A",
}

srm_processed_two = {
    "time": datetime.strftime(
        datetime.strptime("12/12/2022 12:00:00 AM", "%m/%d/%Y %I:%M:%S %p").astimezone(
            timezone("America/Denver")
        ),
        "%m/%d/%Y %I:%M:%S %p",
    ),
    "ip": "127.0.0.1",
    "requestId": 13,
    "role": "publicTrasport",
    "lat": "101.00",
    "long": "49.00",
    "type": "srmTx",
    "status": "N/A",
}

srm_processed_three = {
    "time": datetime.strftime(
        datetime.strptime("12/14/2022 12:00:00 AM", "%m/%d/%Y %I:%M:%S %p").astimezone(
            timezone("America/Denver")
        ),
        "%m/%d/%Y %I:%M:%S %p",
    ),
    "ip": "127.0.0.1",
    "requestId": 17,
    "role": "publicTrasport",
    "lat": "102.00",
    "long": "53.00",
    "type": "srmTx",
    "status": "N/A",
}
