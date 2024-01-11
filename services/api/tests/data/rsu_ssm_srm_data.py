import multidict
from datetime import datetime

ssm_expected_query = (
    "SELECT rtdh_timestamp as time, "
    f"ssm.metadata.originIp as ip, ssm.payload.data.status.signalStatus[ordinal(1)].sigStatus.signalStatusPackage[ordinal(1)].requester.request, "
    f"ssm.payload.data.status.signalStatus[ordinal(1)].sigStatus.signalStatusPackage[ordinal(1)].requester.typeData.role, "
    f"ssm.payload.data.status.signalStatus[ordinal(1)].sigStatus.signalStatusPackage[ordinal(1)].status, "
    f'ssm.metadata.recordType as type FROM `Fake_table` WHERE TIMESTAMP(rtdh_timestamp) >= "2022-12-13T07:00:00" '
    f'AND TIMESTAMP(rtdh_timestamp) <= "2022-12-14T07:00:00" ORDER BY rtdh_timestamp ASC'
)

srm_expected_query = (
    "SELECT rtdh_timestamp as time, srm.metadata.originIp as ip, "
    f"srm.payload.data.requests.signalRequestPackage[ordinal(1)].request.requestID as request, "
    f"srm.payload.data.requestor.type.role, srm.payload.data.requestor.position.position.latitude as lat, "
    f"srm.payload.data.requestor.position.position.longitude as long, srm.metadata.recordType as type "
    f'FROM `Fake_table` WHERE TIMESTAMP(rtdh_timestamp) >= "2022-12-13T07:00:00" AND '
    f'TIMESTAMP(rtdh_timestamp) <= "2022-12-14T07:00:00" ORDER BY rtdh_timestamp ASC'
)

ssm_record_one = multidict.MultiDict(
    [
        ("time", datetime.strptime("2022/12/13 00:00:00", "%Y/%m/%d %H:%M:%S")),
        ("ip", "127.0.0.1"),
        ("request", 13),
        ("role", "publicTrasport"),
        ("status", "granted"),
        ("type", "ssmTx"),
    ]
)
ssm_record_two = multidict.MultiDict(
    [
        ("time", datetime.strptime("2022/12/14 00:00:00", "%Y/%m/%d %H:%M:%S")),
        ("ip", "127.0.0.1"),
        ("request", 10),
        ("role", "publicTrasport"),
        ("status", "granted"),
        ("type", "ssmTx"),
    ]
)
ssm_record_three = multidict.MultiDict(
    [
        ("time", datetime.strptime("2022/12/12 00:00:00", "%Y/%m/%d %H:%M:%S")),
        ("ip", "127.0.0.1"),
        ("request", 17),
        ("role", "publicTrasport"),
        ("status", "granted"),
        ("type", "ssmTx"),
    ]
)
srm_record_one = multidict.MultiDict(
    [
        ("time", datetime.strptime("2022/12/13 00:00:00", "%Y/%m/%d %H:%M:%S")),
        ("ip", "127.0.0.1"),
        ("request", 9),
        ("role", "publicTrasport"),
        ("lat", "100.00"),
        ("long", "50.00"),
        ("status", "N/A"),
        ("type", "srmTx"),
    ]
)
srm_record_two = multidict.MultiDict(
    [
        ("time", datetime.strptime("2022/12/12 00:00:00", "%Y/%m/%d %H:%M:%S")),
        ("ip", "127.0.0.1"),
        ("request", 13),
        ("role", "publicTrasport"),
        ("lat", "101.00"),
        ("long", "49.00"),
        ("status", "N/A"),
        ("type", "srmTx"),
    ]
)
srm_record_three = multidict.MultiDict(
    [
        ("time", datetime.strptime("2022/12/14 00:00:00", "%Y/%m/%d %H:%M:%S")),
        ("ip", "127.0.0.1"),
        ("request", 17),
        ("role", "publicTrasport"),
        ("lat", "102.00"),
        ("long", "53.00"),
        ("status", "N/A"),
        ("type", "srmTx"),
    ]
)

ssm_single_result_expected = [
    {
        "time": "12/13/2022 12:00:00 AM",
        "ip": "127.0.0.1",
        "requestId": 13,
        "role": "publicTrasport",
        "status": "granted",
        "type": "ssmTx",
    }
]

ssm_multiple_result_expected = [
    {
        "time": "12/13/2022 12:00:00 AM",
        "ip": "127.0.0.1",
        "requestId": 13,
        "role": "publicTrasport",
        "status": "granted",
        "type": "ssmTx",
    },
    {
        "time": "12/14/2022 12:00:00 AM",
        "ip": "127.0.0.1",
        "requestId": 10,
        "role": "publicTrasport",
        "status": "granted",
        "type": "ssmTx",
    },
    {
        "time": "12/12/2022 12:00:00 AM",
        "ip": "127.0.0.1",
        "requestId": 17,
        "role": "publicTrasport",
        "status": "granted",
        "type": "ssmTx",
    },
]

srm_single_result_expected = [
    {
        "time": "12/13/2022 12:00:00 AM",
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
        "time": "12/13/2022 12:00:00 AM",
        "ip": "127.0.0.1",
        "requestId": 9,
        "role": "publicTrasport",
        "lat": "100.00",
        "long": "50.00",
        "type": "srmTx",
        "status": "N/A",
    },
    {
        "time": "12/12/2022 12:00:00 AM",
        "ip": "127.0.0.1",
        "requestId": 13,
        "role": "publicTrasport",
        "lat": "101.00",
        "long": "49.00",
        "type": "srmTx",
        "status": "N/A",
    },
    {
        "time": "12/14/2022 12:00:00 AM",
        "ip": "127.0.0.1",
        "requestId": 17,
        "role": "publicTrasport",
        "lat": "102.00",
        "long": "53.00",
        "type": "srmTx",
        "status": "N/A",
    },
]
