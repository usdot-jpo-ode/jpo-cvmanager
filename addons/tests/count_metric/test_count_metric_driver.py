from os import environ
from images.count_metric import driver
from mock import MagicMock

def test_gen_auth_success():
    # prepare
    driver.id_token = MagicMock()
    driver.id_token.fetch_id_token = MagicMock(return_value='token')
    driver.google.auth.transport.requests = MagicMock()

    # call
    result = driver.gen_auth('endpoint')
    
    # check that fetch_id_token is called with the correct arguments
    driver.id_token.fetch_id_token.assert_called_with(driver.google.auth.transport.requests.Request(), 'endpoint')

    # check that the result is correct
    assert(result == 'token')

def test_populateRsuDict_success():
    # prepare
    rsuJson = {
        "rsuList": [
            {
                "rsuId": "rsu1",
                "properties": {
                    "ipv4_address": "192.168.0.10",
                    "primary_route": "I-80"
                }
            },
            {
                "rsuId": "rsu2",
                "properties": {
                    "ipv4_address": "192.168.0.11",
                    "primary_route": "I-80"
                }
            },
            {
                "rsuId": "rsu3",
                "properties": {
                    "ipv4_address": "192.168.0.12",
                    "primary_route": "I-80"
                }
            }
        ],

    }

    # call
    driver.populateRsuDict(rsuJson)

    # check that  rsu_location_dict is correct
    rsu_location_dict = driver.rsu_location_dict
    expected_rsu_location_dict = {
        "192.168.0.10": "I-80",
        "192.168.0.11": "I-80",
        "192.168.0.12": "I-80"
    }
    assert(rsu_location_dict == expected_rsu_location_dict)

    # check that rsu_count_dict is correct
    rsu_count_dict = driver.rsu_count_dict
    expected_rsu_count_dict = {
        "I-80": {
            "192.168.0.10": 0,
            "192.168.0.11": 0,
            "192.168.0.12": 0
        },
        "Unknown": {}
    }
    assert(rsu_count_dict == expected_rsu_count_dict)

def test_populateRsuDict_empty_object():
    # prepare
    rsuJson = {}

    # call
    keyErrorRaised = False
    try:
        driver.populateRsuDict(rsuJson)
    except KeyError:
        keyErrorRaised = True

    # check
    assert(keyErrorRaised)

def test_populateRsuDict_empty_rsuList_array():
    # prepare
    rsuJson = {
        "rsuList": []
    }
    driver.rsu_location_dict = {}
    driver.rsu_count_dict = {}

    # call
    driver.populateRsuDict(rsuJson)

    # check
    assert(driver.rsu_location_dict == {})
    assert(driver.rsu_count_dict == {'Unknown': {}})

def test_populateRsuDict_improperly_formatted_rsuList_array():
    # prepare
    rsuJson = {
        "rsuList": [
            {
                "rsuId": "rsu1",
                "properties": {
                    "ipv4_address": "192.168.0.1",
                    "primary_route": "I-80"
                }
            },
            {
                "rsuId": "rsu2",
                "properties": {
                    
                }
            }
        ]
    }
    driver.rsu_location_dict = {}
    driver.rsu_count_dict = {}

    # call
    keyErrorRaised = False
    try:
        driver.populateRsuDict(rsuJson)
    except KeyError:
        keyErrorRaised = True

    # check
    assert(keyErrorRaised)

def test_run_success():
    # prepare
    driver.rsu_location_dict = {}
    driver.rsu_count_dict = {}
    driver.gen_auth = MagicMock(return_value='token')
    driver.populateRsuDict = MagicMock()
    driver.requests = MagicMock()
    driver.requests.get = MagicMock(return_value=MagicMock(status_code=200, json=MagicMock(return_value='rsuJson')))
    driver.KafkaMessageCounter = MagicMock()
    driver.KafkaMessageCounter.return_value = MagicMock()
    driver.KafkaMessageCounter.return_value.run = MagicMock()
    environ['MESSAGE_TYPES'] = 'bsm'
    environ['RSU_INFO_ENDPOINT'] = 'myendpoint'

    # call
    driver.run()

    # check
    expected_endpoint = environ['RSU_INFO_ENDPOINT']
    driver.gen_auth.assert_called_with(expected_endpoint)
    driver.populateRsuDict.assert_called_with('rsuJson')
    driver.requests.get.assert_called()
    driver.KafkaMessageCounter.assert_called()

def test_run_message_types_not_set():
    # prepare
    environ.clear()
    driver.rsu_location_dict = {}
    driver.rsu_count_dict = {}
    driver.logging = MagicMock()
    driver.logging.error = MagicMock()
    driver.exit = MagicMock()
    driver.exit.side_effect = SystemExit

    # call
    try:
        driver.run()
    except SystemExit:
        pass

    # check
    driver.logging.error.assert_called_once_with("MESSAGE_TYPES environment variable not set! Exiting.")
    driver.exit.assert_called_once_with("MESSAGE_TYPES environment variable not set! Exiting.")

def test_run_rsu_info_endpoint_not_set():
    # prepare
    environ.clear()
    driver.rsu_location_dict = {}
    driver.rsu_count_dict = {}
    driver.logging = MagicMock()
    driver.logging.error = MagicMock()
    driver.exit = MagicMock()
    driver.exit.side_effect = SystemExit
    environ['MESSAGE_TYPES'] = 'bsm'

    # call
    try:
        driver.run()
    except SystemExit:
        pass

    # check
    driver.logging.error.assert_called_once_with("RSU_INFO_ENDPOINT environment variable not set! Exiting.")
    driver.exit.assert_called_once_with("RSU_INFO_ENDPOINT environment variable not set! Exiting.")

def test_run_rsu_info_endpoint_not_reachable():
    # prepare
    driver.rsu_location_dict = {}
    driver.rsu_count_dict = {}
    driver.gen_auth = MagicMock(return_value='token')
    driver.populateRsuDict = MagicMock()
    driver.requests = MagicMock()
    driver.requests.get = MagicMock(return_value=MagicMock(status_code=404))
    environ['MESSAGE_TYPES'] = 'bsm'
    environ['RSU_INFO_ENDPOINT'] = 'myendpoint'
    driver.exit = MagicMock()
    driver.exit.side_effect = SystemExit

    # call
    try:
        driver.run()
    except SystemExit:
        pass

    # check
    expected_endpoint = environ['RSU_INFO_ENDPOINT']
    driver.gen_auth.assert_called_with(expected_endpoint)
    driver.requests.get.assert_called()
    driver.exit.assert_called_once_with()
    driver.populateRsuDict.assert_not_called()

def test_run_rsu_info_unobtainable():
    # prepare
    driver.rsu_location_dict = {}
    driver.rsu_count_dict = {}
    driver.gen_auth = MagicMock(return_value='token')
    driver.populateRsuDict = MagicMock()
    driver.requests = MagicMock()
    driver.requests.get = MagicMock(side_effect=Exception('myexception'))
    environ['MESSAGE_TYPES'] = 'bsm'
    environ['RSU_INFO_ENDPOINT'] = 'myendpoint'
    driver.exit = MagicMock()
    driver.exit.side_effect = SystemExit
    driver.logging = MagicMock()
    driver.logging.error = MagicMock()

    # call
    try:
        driver.run()
    except SystemExit:
        pass

    # check
    expected_endpoint = environ['RSU_INFO_ENDPOINT']
    driver.gen_auth.assert_called_with(expected_endpoint)
    driver.requests.get.assert_called()
    driver.exit.assert_called_once_with()
    driver.populateRsuDict.assert_not_called()
    driver.logging.error.assert_called_once_with("RSU info could not be obtained: myexception")