from unittest.mock import call, patch, MagicMock
from subprocess import DEVNULL
from collections import deque
import test_firmware_manager_values as fmv
import pytest
from addons.images.firmware_manager import firmware_manager


@patch("addons.images.firmware_manager.firmware_manager.active_upgrades", {})
@patch("addons.images.firmware_manager.firmware_manager.pgquery.query_db")
def test_get_rsu_upgrade_data_all(mock_querydb):
    mock_querydb.return_value = [
        ({"ipv4_address": "8.8.8.8"}, ""),
        ({"ipv4_address": "9.9.9.9"}, ""),
    ]

    result = firmware_manager.get_rsu_upgrade_data()

    mock_querydb.assert_called_with(fmv.all_rsus_query)
    assert result == [{"ipv4_address": "8.8.8.8"}, {"ipv4_address": "9.9.9.9"}]


@patch("addons.images.firmware_manager.firmware_manager.active_upgrades", {})
@patch("addons.images.firmware_manager.firmware_manager.pgquery.query_db")
def test_get_rsu_upgrade_data_one(mock_querydb):
    mock_querydb.return_value = [(fmv.rsu_info, "")]

    result = firmware_manager.get_rsu_upgrade_data(rsu_ip="8.8.8.8")

    expected_result = [fmv.rsu_info]
    mock_querydb.assert_called_with(fmv.one_rsu_query)
    assert result == expected_result


# start_tasks_from_queue tests


@patch("addons.images.firmware_manager.firmware_manager.active_upgrades", {})
@patch(
    "addons.images.firmware_manager.firmware_manager.upgrade_queue", deque(["8.8.8.8"])
)
@patch(
    "addons.images.firmware_manager.firmware_manager.upgrade_queue_info",
    {
        "8.8.8.8": {
            "ipv4_address": "8.8.8.8",
            "manufacturer": "Commsignia",
            "model": "ITS-RS4-M",
            "ssh_username": "user",
            "ssh_password": "psw",
            "target_firmware_id": 2,
            "target_firmware_version": "y20.39.0",
            "install_package": "install_package.tar",
        }
    },
)
@patch("addons.images.firmware_manager.firmware_manager.logging")
@patch(
    "addons.images.firmware_manager.firmware_manager.Popen",
    side_effect=Exception("Process failed to start"),
)
def test_start_tasks_from_queue_popen_fail(mock_popen, mock_logging):
    firmware_manager.start_tasks_from_queue()

    # Assert firmware upgrade process was started with expected arguments
    expected_json_str = (
        '\'{"ipv4_address": "8.8.8.8", "manufacturer": "Commsignia", "model": "ITS-RS4-M", '
        '"ssh_username": "user", "ssh_password": "psw", "target_firmware_id": 2, "target_firmware_version": "y20.39.0", '
        '"install_package": "install_package.tar"}\''
    )
    mock_popen.assert_called_with(
        ["python3", f"/home/commsignia_upgrader.py", expected_json_str],
        stdout=DEVNULL,
    )

    # Assert logging
    mock_logging.info.assert_not_called()
    mock_logging.error.assert_called_with(
        f"Encountered error of type {Exception} while starting automatic upgrade process for 8.8.8.8: Process failed to start"
    )


@patch("addons.images.firmware_manager.firmware_manager.logging")
@patch("addons.images.firmware_manager.firmware_manager.active_upgrades", {})
@patch(
    "addons.images.firmware_manager.firmware_manager.upgrade_queue", deque(["8.8.8.8"])
)
@patch(
    "addons.images.firmware_manager.firmware_manager.upgrade_queue_info",
    {
        "8.8.8.8": {
            "ipv4_address": "8.8.8.8",
            "manufacturer": "Commsignia",
            "model": "ITS-RS4-M",
            "ssh_username": "user",
            "ssh_password": "psw",
            "target_firmware_id": 2,
            "target_firmware_version": "y20.39.0",
            "install_package": "install_package.tar",
        }
    },
)
@patch("addons.images.firmware_manager.firmware_manager.Popen")
def test_start_tasks_from_queue_popen_success(mock_popen, mock_logging):
    mock_popen_obj = mock_popen.return_value

    firmware_manager.start_tasks_from_queue()

    # Assert firmware upgrade process was started with expected arguments
    expected_json_str = (
        '\'{"ipv4_address": "8.8.8.8", "manufacturer": "Commsignia", "model": "ITS-RS4-M", '
        '"ssh_username": "user", "ssh_password": "psw", "target_firmware_id": 2, "target_firmware_version": "y20.39.0", '
        '"install_package": "install_package.tar"}\''
    )
    mock_popen.assert_called_with(
        ["python3", f"/home/commsignia_upgrader.py", expected_json_str],
        stdout=DEVNULL,
    )
    # Assert the process reference is successfully tracked in the active_upgrades dictionary
    assert firmware_manager.active_upgrades["8.8.8.8"]["process"] == mock_popen_obj

    mock_logging.info.assert_not_called()
    mock_logging.error.assert_not_called()


# init_firmware_upgrade tests


@patch("addons.images.firmware_manager.firmware_manager.logging")
@patch("addons.images.firmware_manager.firmware_manager.active_upgrades", {})
def test_init_firmware_upgrade_missing_rsu_ip(mock_logging):
    mock_flask_request = MagicMock()
    mock_flask_request.get_json.return_value = {}
    mock_flask_jsonify = MagicMock()
    with patch(
        "addons.images.firmware_manager.firmware_manager.request", mock_flask_request
    ):
        with patch(
            "addons.images.firmware_manager.firmware_manager.jsonify",
            mock_flask_jsonify,
        ):
            message, code = firmware_manager.init_firmware_upgrade()

            mock_flask_jsonify.assert_called_with(
                {"error": "Missing 'rsu_ip' parameter"}
            )
            assert code == 400

            mock_logging.info.assert_not_called()
            mock_logging.error.assert_not_called()


@patch("addons.images.firmware_manager.firmware_manager.logging")
@patch(
    "addons.images.firmware_manager.firmware_manager.active_upgrades", {"8.8.8.8": {}}
)
def test_init_firmware_upgrade_already_running(mock_logging):
    mock_flask_request = MagicMock()
    mock_flask_request.get_json.return_value = {"rsu_ip": "8.8.8.8"}
    mock_flask_jsonify = MagicMock()
    with patch(
        "addons.images.firmware_manager.firmware_manager.request", mock_flask_request
    ):
        with patch(
            "addons.images.firmware_manager.firmware_manager.jsonify",
            mock_flask_jsonify,
        ):
            message, code = firmware_manager.init_firmware_upgrade()

            mock_flask_jsonify.assert_called_with(
                {
                    "error": f"Firmware upgrade failed to start for '8.8.8.8': an upgrade is already underway or queued for the target device"
                }
            )
            assert code == 500

            # Assert logging
            mock_logging.info.assert_called_with(
                "Checking if existing upgrade is running or queued for '8.8.8.8'"
            )
            mock_logging.error.assert_not_called()


@patch(
    "addons.images.firmware_manager.firmware_manager.was_latest_ping_successful_for_rsu"
)
@patch("addons.images.firmware_manager.firmware_manager.logging")
@patch("addons.images.firmware_manager.firmware_manager.active_upgrades", {})
@patch(
    "addons.images.firmware_manager.firmware_manager.get_rsu_upgrade_data",
    MagicMock(return_value=[]),
)
def test_init_firmware_upgrade_no_eligible_upgrade(
    mock_logging, mock_was_latest_ping_successful_for_rsu
):
    mock_flask_request = MagicMock()
    mock_flask_request.get_json.return_value = {"rsu_ip": "8.8.8.8"}
    mock_flask_jsonify = MagicMock()
    mock_was_latest_ping_successful_for_rsu.return_value = True
    with patch(
        "addons.images.firmware_manager.firmware_manager.request", mock_flask_request
    ):
        with patch(
            "addons.images.firmware_manager.firmware_manager.jsonify",
            mock_flask_jsonify,
        ):
            message, code = firmware_manager.init_firmware_upgrade()

            mock_flask_jsonify.assert_called_with(
                {
                    "error": f"Firmware upgrade failed to start for '8.8.8.8': the target firmware is already installed or is an invalid upgrade from the current firmware"
                }
            )
            assert code == 500

            # Assert logging
            mock_logging.info.assert_has_calls(
                [
                    call(
                        "Checking if existing upgrade is running or queued for '8.8.8.8'"
                    ),
                    call("Querying RSU data for '8.8.8.8'"),
                ]
            )
            mock_logging.error.assert_not_called()


@patch(
    "addons.images.firmware_manager.firmware_manager.was_latest_ping_successful_for_rsu"
)
@patch("addons.images.firmware_manager.firmware_manager.logging")
@patch("addons.images.firmware_manager.firmware_manager.active_upgrades", {})
@patch(
    "addons.images.firmware_manager.firmware_manager.get_rsu_upgrade_data",
    MagicMock(return_value=[]),
)
def test_init_firmware_upgrade_rsu_not_reachable(
    mock_logging, mock_was_latest_ping_successful_for_rsu
):
    mock_flask_request = MagicMock()
    mock_flask_request.get_json.return_value = {"rsu_ip": "8.8.8.8"}
    mock_flask_jsonify = MagicMock()
    mock_was_latest_ping_successful_for_rsu.return_value = False
    with patch(
        "addons.images.firmware_manager.firmware_manager.request", mock_flask_request
    ):
        with patch(
            "addons.images.firmware_manager.firmware_manager.jsonify",
            mock_flask_jsonify,
        ):
            message, code = firmware_manager.init_firmware_upgrade()

            mock_flask_jsonify.assert_called_with(
                {
                    "error": f"Firmware upgrade failed to start for '8.8.8.8': device is unreachable"
                }
            )
            assert code == 500

            # Assert logging
            mock_logging.info.assert_has_calls(
                [
                    call(
                        "Checking if existing upgrade is running or queued for '8.8.8.8'"
                    )
                ]
            )
            mock_logging.error.assert_not_called()


@patch(
    "addons.images.firmware_manager.firmware_manager.was_latest_ping_successful_for_rsu"
)
@patch("addons.images.firmware_manager.firmware_manager.logging")
@patch("addons.images.firmware_manager.firmware_manager.active_upgrades", {})
@patch(
    "addons.images.firmware_manager.firmware_manager.get_rsu_upgrade_data",
    MagicMock(return_value=[fmv.rsu_info]),
)
@patch("addons.images.firmware_manager.firmware_manager.start_tasks_from_queue")
def test_init_firmware_upgrade_success(
    mock_stfq, mock_logging, mock_was_latest_ping_successful_for_rsu
):
    mock_was_latest_ping_successful_for_rsu.return_value = True
    mock_flask_request = MagicMock()
    mock_flask_request.get_json.return_value = {"rsu_ip": "8.8.8.8"}
    mock_flask_jsonify = MagicMock()
    with patch(
        "addons.images.firmware_manager.firmware_manager.request", mock_flask_request
    ):
        with patch(
            "addons.images.firmware_manager.firmware_manager.jsonify",
            mock_flask_jsonify,
        ):
            message, code = firmware_manager.init_firmware_upgrade()

            # Assert start_tasks_from_queue is called
            mock_stfq.assert_called_with()

            # Assert the process reference is successfully tracked in the upgrade_queue
            assert firmware_manager.upgrade_queue[0] == "8.8.8.8"

            # Assert REST response is as expected from a successful run
            mock_flask_jsonify.assert_called_with(
                {"message": f"Firmware upgrade started successfully for '8.8.8.8'"}
            )
            assert code == 201

            mock_was_latest_ping_successful_for_rsu.assert_called_with("8.8.8.8")

            # Assert logging
            mock_logging.info.assert_has_calls(
                [
                    call(
                        "Checking if existing upgrade is running or queued for '8.8.8.8'"
                    ),
                    call("Querying RSU data for '8.8.8.8'"),
                    call("Adding '8.8.8.8' to the firmware manager upgrade queue"),
                ]
            )
            mock_logging.error.assert_not_called()

    firmware_manager.upgrade_queue = deque([])


# firmware_upgrade_completed tests


@patch("addons.images.firmware_manager.firmware_manager.logging")
@patch("addons.images.firmware_manager.firmware_manager.active_upgrades", {})
def test_firmware_upgrade_completed_missing_rsu_ip(mock_logging):
    mock_flask_request = MagicMock()
    mock_flask_request.get_json.return_value = {}
    mock_flask_jsonify = MagicMock()
    with patch(
        "addons.images.firmware_manager.firmware_manager.request", mock_flask_request
    ):
        with patch(
            "addons.images.firmware_manager.firmware_manager.jsonify",
            mock_flask_jsonify,
        ):
            message, code = firmware_manager.firmware_upgrade_completed()

            mock_flask_jsonify.assert_called_with(
                {"error": "Missing 'rsu_ip' parameter"}
            )
            assert code == 400

            # Assert logging
            mock_logging.info.assert_not_called()
            mock_logging.error.assert_not_called()


@patch("addons.images.firmware_manager.firmware_manager.logging")
@patch("addons.images.firmware_manager.firmware_manager.active_upgrades", {})
def test_firmware_upgrade_completed_unknown_process(mock_logging):
    mock_flask_request = MagicMock()
    mock_flask_request.get_json.return_value = {
        "rsu_ip": "8.8.8.8",
        "status": "success",
    }
    mock_flask_jsonify = MagicMock()
    with patch(
        "addons.images.firmware_manager.firmware_manager.request", mock_flask_request
    ):
        with patch(
            "addons.images.firmware_manager.firmware_manager.jsonify",
            mock_flask_jsonify,
        ):
            message, code = firmware_manager.firmware_upgrade_completed()

            mock_flask_jsonify.assert_called_with(
                {
                    "error": "Specified device is not actively being upgraded or was already completed"
                }
            )
            assert code == 400

            # Assert logging
            mock_logging.info.assert_not_called()
            mock_logging.error.assert_not_called()


@patch("addons.images.firmware_manager.firmware_manager.logging")
@patch(
    "addons.images.firmware_manager.firmware_manager.active_upgrades",
    {"8.8.8.8": fmv.upgrade_info},
)
def test_firmware_upgrade_completed_missing_status(mock_logging):
    mock_flask_request = MagicMock()
    mock_flask_request.get_json.return_value = {"rsu_ip": "8.8.8.8"}
    mock_flask_jsonify = MagicMock()
    with patch(
        "addons.images.firmware_manager.firmware_manager.request", mock_flask_request
    ):
        with patch(
            "addons.images.firmware_manager.firmware_manager.jsonify",
            mock_flask_jsonify,
        ):
            message, code = firmware_manager.firmware_upgrade_completed()

            mock_flask_jsonify.assert_called_with(
                {"error": "Missing 'status' parameter"}
            )
            assert code == 400

            # Assert logging
            mock_logging.info.assert_not_called()
            mock_logging.error.assert_not_called()


@patch("addons.images.firmware_manager.firmware_manager.logging")
@patch(
    "addons.images.firmware_manager.firmware_manager.active_upgrades",
    {"8.8.8.8": fmv.upgrade_info},
)
def test_firmware_upgrade_completed_illegal_status(mock_logging):
    mock_flask_request = MagicMock()
    mock_flask_request.get_json.return_value = {"rsu_ip": "8.8.8.8", "status": "frog"}
    mock_flask_jsonify = MagicMock()
    with patch(
        "addons.images.firmware_manager.firmware_manager.request", mock_flask_request
    ):
        with patch(
            "addons.images.firmware_manager.firmware_manager.jsonify",
            mock_flask_jsonify,
        ):
            message, code = firmware_manager.firmware_upgrade_completed()

            mock_flask_jsonify.assert_called_with(
                {
                    "error": "Wrong value for 'status' parameter - must be either 'success' or 'fail'"
                }
            )
            assert code == 400

            # Assert logging
            mock_logging.info.assert_not_called()
            mock_logging.error.assert_not_called()


@patch(
    "addons.images.firmware_manager.firmware_manager.reset_consecutive_failure_count_for_rsu"
)
@patch(
    "addons.images.firmware_manager.firmware_manager.increment_consecutive_failure_count_for_rsu"
)
@patch(
    "addons.images.firmware_manager.firmware_manager.is_rsu_at_max_retries_limit",
    return_value=False,
)
@patch("addons.images.firmware_manager.firmware_manager.logging")
@patch(
    "addons.images.firmware_manager.firmware_manager.active_upgrades",
    {"8.8.8.8": fmv.upgrade_info},
)
def test_firmware_upgrade_completed_fail_status(
    mock_logging,
    mock_is_rsu_at_max_retries_limit,
    mock_increment_consecutive_failure_count_for_rsu,
    mock_reset_consecutive_failure_count_for_rsu,
):
    mock_flask_request = MagicMock()
    mock_flask_request.get_json.return_value = {"rsu_ip": "8.8.8.8", "status": "fail"}
    mock_flask_jsonify = MagicMock()
    with patch(
        "addons.images.firmware_manager.firmware_manager.request", mock_flask_request
    ):
        with patch(
            "addons.images.firmware_manager.firmware_manager.jsonify",
            mock_flask_jsonify,
        ):
            message, code = firmware_manager.firmware_upgrade_completed()

            assert "8.8.8.8" not in firmware_manager.active_upgrades
            mock_flask_jsonify.assert_called_with(
                {"message": "Firmware upgrade successfully marked as complete"}
            )
            assert code == 204

            mock_is_rsu_at_max_retries_limit.asset_called_with("8.8.8.8")
            mock_increment_consecutive_failure_count_for_rsu.assert_called_once()
            mock_reset_consecutive_failure_count_for_rsu.assert_not_called()

            # Assert logging
            mock_logging.info.assert_called_with(
                "Marking firmware upgrade as complete for '8.8.8.8'"
            )
            mock_logging.error.assert_not_called()


@patch(
    "addons.images.firmware_manager.firmware_manager.reset_consecutive_failure_count_for_rsu"
)
@patch("addons.images.firmware_manager.firmware_manager.pgquery.write_db")
@patch(
    "addons.images.firmware_manager.firmware_manager.increment_consecutive_failure_count_for_rsu"
)
@patch(
    "addons.images.firmware_manager.firmware_manager.is_rsu_at_max_retries_limit",
    return_value=True,
)
@patch("addons.images.firmware_manager.firmware_manager.logging")
@patch(
    "addons.images.firmware_manager.firmware_manager.active_upgrades",
    {"8.8.8.8": fmv.upgrade_info},
)
def test_firmware_upgrade_completed_fail_status_reached_max_retries(
    mock_logging,
    mock_is_rsu_at_max_retries_limit,
    mock_increment_consecutive_failure_count_for_rsu,
    mock_writedb,
    mock_reset_consecutive_failure_count_for_rsu,
):
    mock_flask_request = MagicMock()
    mock_flask_request.get_json.return_value = {"rsu_ip": "8.8.8.8", "status": "fail"}
    mock_flask_jsonify = MagicMock()
    with patch(
        "addons.images.firmware_manager.firmware_manager.request", mock_flask_request
    ):
        with patch(
            "addons.images.firmware_manager.firmware_manager.jsonify",
            mock_flask_jsonify,
        ):
            message, code = firmware_manager.firmware_upgrade_completed()

            assert "8.8.8.8" not in firmware_manager.active_upgrades
            mock_flask_jsonify.assert_called_with(
                {"message": "Firmware upgrade successfully marked as complete"}
            )
            assert code == 204

            mock_increment_consecutive_failure_count_for_rsu.assert_called_once_with(
                "8.8.8.8"
            )
            mock_is_rsu_at_max_retries_limit.assert_called_with("8.8.8.8")
            mock_writedb.assert_has_calls(
                [
                    call(
                        "UPDATE public.rsus SET target_firmware_version=firmware_version WHERE ipv4_address='8.8.8.8'"
                    ),
                    call(
                        "insert into max_retry_limit_reached_instances (rsu_id, reached_at, target_firmware_version) values ((select rsu_id from rsus where ipv4_address='8.8.8.8'), now(), (select firmware_id from firmware_images where name='y20.39.0'))"
                    ),
                ]
            )

            mock_reset_consecutive_failure_count_for_rsu.assert_called_once_with(
                "8.8.8.8"
            )

            # Assert logging
            mock_logging.info.assert_called_with(
                "Marking firmware upgrade as complete for '8.8.8.8'"
            )
            mock_logging.error.assert_called_with(
                "RSU 8.8.8.8 has reached the maximum number of upgrade retries. Setting target_firmware_version to firmware_version and resetting consecutive failures count."
            )


@patch(
    "addons.images.firmware_manager.firmware_manager.reset_consecutive_failure_count_for_rsu"
)
@patch("addons.images.firmware_manager.firmware_manager.logging")
@patch(
    "addons.images.firmware_manager.firmware_manager.active_upgrades",
    {"8.8.8.8": fmv.upgrade_info},
)
@patch("addons.images.firmware_manager.firmware_manager.pgquery.write_db")
def test_firmware_upgrade_completed_success_status(
    mock_writedb, mock_logging, mock_reset_consecutive_failure_count_for_rsu
):
    mock_flask_request = MagicMock()
    mock_flask_request.get_json.return_value = {
        "rsu_ip": "8.8.8.8",
        "status": "success",
    }
    mock_flask_jsonify = MagicMock()
    with patch(
        "addons.images.firmware_manager.firmware_manager.request", mock_flask_request
    ):
        with patch(
            "addons.images.firmware_manager.firmware_manager.jsonify",
            mock_flask_jsonify,
        ):
            message, code = firmware_manager.firmware_upgrade_completed()

            mock_writedb.assert_called_with(
                "UPDATE public.rsus SET firmware_version=2 WHERE ipv4_address='8.8.8.8'"
            )
            assert "8.8.8.8" not in firmware_manager.active_upgrades
            mock_flask_jsonify.assert_called_with(
                {"message": "Firmware upgrade successfully marked as complete"}
            )
            assert code == 204

            mock_reset_consecutive_failure_count_for_rsu.assert_called_with("8.8.8.8")

            # Assert logging
            mock_logging.info.assert_called_with(
                "Marking firmware upgrade as complete for '8.8.8.8'"
            )
            mock_logging.error.assert_not_called()


@patch(
    "addons.images.firmware_manager.firmware_manager.reset_consecutive_failure_count_for_rsu"
)
@patch("addons.images.firmware_manager.firmware_manager.logging")
@patch(
    "addons.images.firmware_manager.firmware_manager.active_upgrades",
    {"8.8.8.8": fmv.upgrade_info},
)
@patch(
    "addons.images.firmware_manager.firmware_manager.pgquery.write_db",
    side_effect=Exception("Failure to query PostgreSQL"),
)
def test_firmware_upgrade_completed_success_status_exception(
    mock_writedb, mock_logging, mock_reset_consecutive_failure_count_for_rsu
):
    mock_flask_request = MagicMock()
    mock_flask_request.get_json.return_value = {
        "rsu_ip": "8.8.8.8",
        "status": "success",
    }
    mock_flask_jsonify = MagicMock()
    with patch(
        "addons.images.firmware_manager.firmware_manager.request", mock_flask_request
    ):
        with patch(
            "addons.images.firmware_manager.firmware_manager.jsonify",
            mock_flask_jsonify,
        ):
            message, code = firmware_manager.firmware_upgrade_completed()

            mock_writedb.assert_called_with(
                "UPDATE public.rsus SET firmware_version=2 WHERE ipv4_address='8.8.8.8'"
            )
            mock_flask_jsonify.assert_called_with(
                {
                    "error": "Unexpected error occurred while querying the PostgreSQL database - firmware upgrade not marked as complete"
                }
            )
            assert code == 500

            # Assert logging
            mock_logging.info.assert_not_called()
            mock_logging.error.assert_called_with(
                "Encountered error of type <class 'Exception'> while querying the PostgreSQL database: Failure to query PostgreSQL"
            )


# list_active_upgrades tests


@patch("addons.images.firmware_manager.firmware_manager.logging")
@patch(
    "addons.images.firmware_manager.firmware_manager.active_upgrades",
    {"8.8.8.8": fmv.upgrade_info},
)
def test_list_active_upgrades(mock_logging):
    mock_flask_request = MagicMock()
    mock_flask_request.get_json.return_value = {
        "rsu_ip": "8.8.8.8",
        "status": "success",
    }
    mock_flask_jsonify = MagicMock()
    with patch(
        "addons.images.firmware_manager.firmware_manager.request", mock_flask_request
    ):
        with patch(
            "addons.images.firmware_manager.firmware_manager.jsonify",
            mock_flask_jsonify,
        ):
            message, code = firmware_manager.list_active_upgrades()

            expected_active_upgrades = {
                "8.8.8.8": {
                    "manufacturer": "Commsignia",
                    "model": "ITS-RS4-M",
                    "target_firmware_id": 2,
                    "target_firmware_version": "y20.39.0",
                    "install_package": "install_package.tar",
                }
            }
            mock_flask_jsonify.assert_called_with(
                {"active_upgrades": expected_active_upgrades, "upgrade_queue": []}
            )
            assert code == 200

            # Assert logging
            mock_logging.info.assert_not_called()
            mock_logging.error.assert_not_called()


# check_for_upgrades tests


@patch(
    "addons.images.firmware_manager.firmware_manager.was_latest_ping_successful_for_rsu"
)
@patch(
    "addons.images.firmware_manager.firmware_manager.active_upgrades",
    {},
)
@patch(
    "addons.images.firmware_manager.firmware_manager.get_rsu_upgrade_data",
    MagicMock(return_value=fmv.single_rsu_info),
)
@patch("addons.images.firmware_manager.firmware_manager.logging")
@patch(
    "addons.images.firmware_manager.firmware_manager.Popen",
    side_effect=Exception("Process failed to start"),
)
@patch("addons.images.firmware_manager.firmware_manager.get_upgrade_limit")
def test_check_for_upgrades_exception(
    mock_upgrade_limit,
    mock_popen,
    mock_logging,
    mock_was_latest_ping_successful_for_rsu,
):
    mock_upgrade_limit.return_value = 5
    mock_was_latest_ping_successful_for_rsu.return_value = True
    firmware_manager.check_for_upgrades()

    # Assert firmware upgrade process was started with expected arguments
    expected_json_str = (
        '\'{"ipv4_address": "9.9.9.9", "manufacturer": "Commsignia", "model": "ITS-RS4-M", '
        '"ssh_username": "user", "ssh_password": "psw", "target_firmware_id": 2, "target_firmware_version": "y20.39.0", '
        '"install_package": "install_package.tar"}\''
    )
    mock_popen.assert_called_once_with(
        ["python3", f"/home/commsignia_upgrader.py", expected_json_str], stdout=DEVNULL
    )

    # Assert the process reference is successfully tracked in the active_upgrades dictionary
    assert "9.9.9.9" not in firmware_manager.active_upgrades
    mock_logging.info.assert_has_calls(
        [
            call("Checking PostgreSQL DB for RSUs with new target firmware"),
            call("Adding '9.9.9.9' to the firmware manager upgrade queue"),
            call("Firmware upgrade successfully started for '9.9.9.9'"),
        ]
    )
    mock_logging.error.assert_called_with(
        f"Encountered error of type {Exception} while starting automatic upgrade process for 9.9.9.9: Process failed to start"
    )


@patch(
    "addons.images.firmware_manager.firmware_manager.was_latest_ping_successful_for_rsu"
)
@patch(
    "addons.images.firmware_manager.firmware_manager.active_upgrades",
    {},
)
@patch(
    "addons.images.firmware_manager.firmware_manager.get_rsu_upgrade_data",
    MagicMock(return_value=fmv.multi_rsu_info),
)
@patch("addons.images.firmware_manager.firmware_manager.logging")
@patch("addons.images.firmware_manager.firmware_manager.start_tasks_from_queue")
@patch("addons.images.firmware_manager.firmware_manager.get_upgrade_limit")
def test_check_for_upgrades_SUCCESS(
    mock_upgrade_limit, mock_stfq, mock_logging, mock_was_latest_ping_successful_for_rsu
):
    mock_upgrade_limit.return_value = 5
    mock_was_latest_ping_successful_for_rsu.return_value = True
    firmware_manager.check_for_upgrades()

    # Assert firmware upgrade process was started with expected arguments
    mock_stfq.assert_called_once_with()

    # Assert the process reference is successfully tracked in the active_upgrades dictionary
    assert firmware_manager.upgrade_queue[1] == "9.9.9.9"
    mock_logging.info.assert_has_calls(
        [
            call("Checking PostgreSQL DB for RSUs with new target firmware"),
            call("Adding '8.8.8.8' to the firmware manager upgrade queue"),
            call("Firmware upgrade successfully started for '8.8.8.8'"),
            call("Adding '9.9.9.9' to the firmware manager upgrade queue"),
            call("Firmware upgrade successfully started for '9.9.9.9'"),
        ]
    )


# Other tests


@patch("addons.images.firmware_manager.firmware_manager.pgquery.query_db")
def test_was_latest_ping_successful_for_rsu(mock_query_db):
    # prepare
    rsu_ip = "8.8.8.8"
    expected_query = "select result from ping where rsu_id=(select rsu_id from rsus where ipv4_address='8.8.8.8') order by timestamp desc limit 1"
    mock_query_db.return_value = [(True,)]

    # execute
    result = firmware_manager.was_latest_ping_successful_for_rsu(rsu_ip)

    # verify
    assert result == True
    mock_query_db.assert_called_with(expected_query)


@patch("addons.images.firmware_manager.firmware_manager.pgquery.query_db")
def test_was_latest_ping_successful_for_rsu_NO_RESULTS(mock_query_db):
    # prepare
    rsu_ip = "8.8.8.8"
    expected_query = "select result from ping where rsu_id=(select rsu_id from rsus where ipv4_address='8.8.8.8') order by timestamp desc limit 1"
    mock_query_db.return_value = []

    # execute
    result = firmware_manager.was_latest_ping_successful_for_rsu(rsu_ip)

    # verify
    assert result == False
    mock_query_db.assert_called_with(expected_query)


@patch("addons.images.firmware_manager.firmware_manager.pgquery.write_db")
def test_increment_consecutive_failure_count_for_rsu(mock_write_db):
    # prepare
    rsu_ip = "8.8.8.8"
    expected_query = f"insert into consecutive_firmware_upgrade_failures (rsu_id, consecutive_failures) values ((select rsu_id from rsus where ipv4_address='{rsu_ip}'), 1) on conflict (rsu_id) do update set consecutive_failures=consecutive_firmware_upgrade_failures.consecutive_failures+1"

    # execute
    firmware_manager.increment_consecutive_failure_count_for_rsu(rsu_ip)

    # verify
    mock_write_db.assert_called_with(expected_query)


@patch("addons.images.firmware_manager.firmware_manager.pgquery.write_db")
def test_reset_consecutive_failure_count_for_rsu(mock_write_db):
    # prepare
    rsu_ip = "8.8.8.8"
    expected_query = f"insert into consecutive_firmware_upgrade_failures (rsu_id, consecutive_failures) values ((select rsu_id from rsus where ipv4_address='{rsu_ip}'), 0) on conflict (rsu_id) do update set consecutive_failures=0"

    # execute
    firmware_manager.reset_consecutive_failure_count_for_rsu(rsu_ip)

    # verify
    mock_write_db.assert_called_with(expected_query)


@patch.dict("os.environ", {"FW_UPGRADE_MAX_RETRY_LIMIT": "3"})
@patch("addons.images.firmware_manager.firmware_manager.pgquery.query_db")
def test_is_rsu_at_max_retries_limit_TRUE(mock_query_db):
    # prepare
    rsu_ip = "8.8.8.8"
    expected_query = "select consecutive_failures from consecutive_firmware_upgrade_failures where rsu_id=(select rsu_id from rsus where ipv4_address='8.8.8.8')"
    mock_query_db.return_value = [(3,)]

    # execute
    result = firmware_manager.is_rsu_at_max_retries_limit(rsu_ip)

    # verify
    assert result == True
    mock_query_db.assert_called_with(expected_query)


@patch.dict("os.environ", {"FW_UPGRADE_MAX_RETRY_LIMIT": "3"})
@patch("addons.images.firmware_manager.firmware_manager.pgquery.query_db")
def test_is_rsu_at_max_retries_limit_FALSE(mock_query_db):
    # prepare
    rsu_ip = "8.8.8.8"
    expected_query = "select consecutive_failures from consecutive_firmware_upgrade_failures where rsu_id=(select rsu_id from rsus where ipv4_address='8.8.8.8')"
    mock_query_db.return_value = [(2,)]

    # execute
    result = firmware_manager.is_rsu_at_max_retries_limit(rsu_ip)

    # verify
    assert result == False
    mock_query_db.assert_called_with(expected_query)


@patch.dict("os.environ", {"FW_UPGRADE_MAX_RETRY_LIMIT": "3"})
@patch("addons.images.firmware_manager.firmware_manager.pgquery.query_db")
def test_is_rsu_at_max_retries_limit_NO_RESULTS(mock_query_db):
    # prepare
    rsu_ip = "8.8.8.8"
    expected_query = "select consecutive_failures from consecutive_firmware_upgrade_failures where rsu_id=(select rsu_id from rsus where ipv4_address='8.8.8.8')"
    mock_query_db.return_value = []

    # execute
    result = firmware_manager.is_rsu_at_max_retries_limit(rsu_ip)

    # verify
    assert result == False
    mock_query_db.assert_called_with(expected_query)


@patch("addons.images.firmware_manager.firmware_manager.pgquery.write_db")
def test_log_max_retries_reached_incident_for_rsu_to_postgres(mock_write_db):
    # prepare
    rsu_ip = "8.8.8.8"
    expected_query = "insert into max_retry_limit_reached_instances (rsu_id, reached_at, target_firmware_version) values ((select rsu_id from rsus where ipv4_address='8.8.8.8'), now(), (select firmware_id from firmware_images where name='y20.39.0'))"

    # execute
    firmware_manager.log_max_retries_reached_incident_for_rsu_to_postgres(
        rsu_ip, "y20.39.0"
    )

    # verify
    mock_write_db.assert_called_with(expected_query)


@patch("addons.images.firmware_manager.firmware_manager.serve")
def test_serve_rest_api(mock_serve):
    firmware_manager.serve_rest_api()
    mock_serve.assert_called_with(firmware_manager.app, host="0.0.0.0", port=8080)


@patch("addons.images.firmware_manager.firmware_manager.BackgroundScheduler")
def test_init_background_task(mock_bgscheduler):
    mock_bgscheduler_obj = mock_bgscheduler.return_value

    firmware_manager.init_background_task()

    mock_bgscheduler.assert_called_with({"apscheduler.timezone": "UTC"})
    mock_bgscheduler_obj.add_job.assert_called_with(
        firmware_manager.check_for_upgrades, "cron", minute="0"
    )
    mock_bgscheduler_obj.start.assert_called_with()


def test_get_upgrade_limit_no_env():
    limit = firmware_manager.get_upgrade_limit()
    assert limit == 1


@patch.dict("os.environ", {"ACTIVE_UPGRADE_LIMIT": "5"})
def test_get_upgrade_limit_with_env():
    limit = firmware_manager.get_upgrade_limit()
    assert limit == 5


@patch.dict("os.environ", {"ACTIVE_UPGRADE_LIMIT": "bad_value"})
def test_get_upgrade_limit_with_bad_env():
    with pytest.raises(
        ValueError,
        match="The environment variable 'ACTIVE_UPGRADE_LIMIT' must be an integer.",
    ):
        firmware_manager.get_upgrade_limit()
