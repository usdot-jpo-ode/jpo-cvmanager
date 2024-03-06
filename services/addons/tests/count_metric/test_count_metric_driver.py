from os import environ
from addons.images.count_metric import driver
from mock import MagicMock
from unittest.mock import patch


@patch("addons.images.count_metric.driver.pgquery.query_db")
def test_get_rsu_list(mock_query_db):
    # mock
    mock_query_db.return_value = [
        (
            {
                "ipv4_address": "192.168.0.10",
                "primary_route": "I-80",
            },
        ),
    ]

    # run
    result = driver.get_rsu_list()

    expected_result = [{"ipv4_address": "192.168.0.10", "primary_route": "I-80"}]
    mock_query_db.assert_called_once()
    assert result == expected_result


@patch("addons.images.count_metric.driver.get_rsu_list")
def test_populateRsuDict_success(mock_get_rsu_list):
    # prepare
    mock_get_rsu_list.return_value = [
        {"ipv4_address": "192.168.0.10", "primary_route": "I-80"}
    ]

    # call
    driver.populateRsuDict()

    # check that  rsu_location_dict is correct
    rsu_location_dict = driver.rsu_location_dict
    expected_rsu_location_dict = {"192.168.0.10": "I-80"}
    assert rsu_location_dict == expected_rsu_location_dict

    # check that rsu_count_dict is correct
    rsu_count_dict = driver.rsu_count_dict
    expected_rsu_count_dict = {"I-80": {"192.168.0.10": 0}, "Unknown": {}}
    assert rsu_count_dict == expected_rsu_count_dict


@patch("addons.images.count_metric.driver.get_rsu_list")
def test_populateRsuDict_empty_object(mock_get_rsu_list):
    # prepare
    mock_get_rsu_list.return_value = []

    driver.rsu_location_dict = {}
    driver.rsu_count_dict = {}

    driver.populateRsuDict()

    assert driver.rsu_location_dict == {}
    assert driver.rsu_count_dict == {"Unknown": {}}


@patch("addons.images.count_metric.driver.rsu_location_dict", {})
@patch("addons.images.count_metric.driver.rsu_count_dict", {})
@patch("addons.images.count_metric.driver.populateRsuDict", MagicMock())
@patch("addons.images.count_metric.driver.KafkaMessageCounter")
def test_run_counter_success(mock_KafkaMessageCounter):
    # prepare
    mock_KafkaMessageCounter.return_value = MagicMock()
    mock_KafkaMessageCounter.return_value.run = MagicMock()
    environ["MESSAGE_TYPES"] = "bsm"

    # call
    driver.run_counter()

    # check
    driver.populateRsuDict.assert_called_once()
    driver.KafkaMessageCounter.assert_called()


def test_run_counter_message_types_not_set():
    # prepare
    environ["MESSAGE_TYPES"] = ""
    driver.rsu_location_dict = {}
    driver.rsu_count_dict = {}
    driver.logging = MagicMock()
    driver.logging.error = MagicMock()
    driver.exit = MagicMock()
    driver.exit.side_effect = SystemExit

    # call
    try:
        driver.run_counter()
    except SystemExit:
        pass

    # check
    driver.logging.error.assert_called_once_with(
        "MESSAGE_TYPES environment variable not set! Exiting."
    )
    driver.exit.assert_called_once_with(
        "MESSAGE_TYPES environment variable not set! Exiting."
    )
