from unittest.mock import call, patch, MagicMock
from collections import deque
from addons.images.firmware_manager.upgrade_scheduler import upgrade_scheduler
import addons.tests.firmware_manager.upgrade_scheduler.test_upgrade_scheduler_values as fmv
import pytest


@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.active_upgrades",
    {},
)
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.pgquery.query_db"
)
def test_get_rsu_upgrade_data_all(mock_querydb):
    mock_querydb.return_value = [
        ({"ipv4_address": "8.8.8.8"}, ""),
        ({"ipv4_address": "9.9.9.9"}, ""),
    ]

    result = upgrade_scheduler.get_rsu_upgrade_data()

    mock_querydb.assert_called_with(fmv.all_rsus_query)
    assert result == [{"ipv4_address": "8.8.8.8"}, {"ipv4_address": "9.9.9.9"}]


@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.active_upgrades",
    {},
)
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.pgquery.query_db"
)
def test_get_rsu_upgrade_data_one(mock_querydb):
    mock_querydb.return_value = [(fmv.rsu_info, "")]

    result = upgrade_scheduler.get_rsu_upgrade_data(rsu_ip="8.8.8.8")

    expected_result = [fmv.rsu_info]
    mock_querydb.assert_called_with(fmv.one_rsu_query)
    assert result == expected_result


# start_tasks_from_queue tests


@patch.dict("os.environ", {"UPGRADE_RUNNER_ENDPOINT": "http://test-endpoint"})
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.active_upgrades",
    {},
)
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.upgrade_queue",
    deque(["8.8.8.8"]),
)
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.upgrade_queue_info",
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
@patch("addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.logging")
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.requests.post",
    side_effect=Exception("Exception during request"),
)
def test_start_tasks_from_queue_post_exception(mock_post, mock_logging):
    upgrade_scheduler.start_tasks_from_queue()

    # Assert firmware upgrade process was started with expected arguments
    mock_post.assert_called_with(
        "http://test-endpoint/run_firmware_upgrade",
        json={
            "ipv4_address": "8.8.8.8",
            "manufacturer": "Commsignia",
            "model": "ITS-RS4-M",
            "ssh_username": "user",
            "ssh_password": "psw",
            "target_firmware_id": 2,
            "target_firmware_version": "y20.39.0",
            "install_package": "install_package.tar",
        },
    )

    # Assert logging
    mock_logging.info.assert_not_called()
    mock_logging.error.assert_called_with(
        f"Encountered error of type {Exception} while starting automatic upgrade process for 8.8.8.8: Exception during request"
    )


@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.active_upgrades",
    {},
)
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.upgrade_queue",
    deque(["8.8.8.8"]),
)
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.upgrade_queue_info",
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
@patch("addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.logging")
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.requests.post"
)
def test_start_tasks_from_queue_no_env_var(mock_post, mock_logging):
    upgrade_scheduler.start_tasks_from_queue()

    # Assert logging
    mock_logging.info.assert_not_called()
    mock_logging.error.assert_called_with(
        f"Encountered error of type {Exception} while starting automatic upgrade process for 8.8.8.8: The UPGRADE_RUNNER_ENDPOINT environment variable is undefined!"
    )


@patch.dict("os.environ", {"UPGRADE_RUNNER_ENDPOINT": "http://test-endpoint"})
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.active_upgrades",
    {},
)
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.upgrade_queue",
    deque(["8.8.8.8"]),
)
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.upgrade_queue_info",
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
@patch("addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.logging")
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.requests.post"
)
def test_start_tasks_from_queue_post_success(mock_post, mock_logging):
    mock_post_response = MagicMock()
    mock_post_response.status_code = 201
    mock_post.return_value = mock_post_response

    upgrade_scheduler.start_tasks_from_queue()

    # Assert firmware upgrade process was started with expected arguments
    mock_post.assert_called_with(
        "http://test-endpoint/run_firmware_upgrade",
        json={
            "manufacturer": "Commsignia",
            "model": "ITS-RS4-M",
            "ssh_username": "user",
            "ssh_password": "psw",
            "target_firmware_id": 2,
            "target_firmware_version": "y20.39.0",
            "install_package": "install_package.tar",
        },
    )

    mock_logging.info.assert_called_with(
        f"Firmware upgrade runner successfully requested to begin the upgrade for 8.8.8.8"
    )
    mock_logging.error.assert_not_called()


@patch.dict("os.environ", {"UPGRADE_RUNNER_ENDPOINT": "http://test-endpoint"})
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.active_upgrades",
    {},
)
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.upgrade_queue",
    deque(["8.8.8.8"]),
)
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.upgrade_queue_info",
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
@patch("addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.logging")
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.requests.post"
)
def test_start_tasks_from_queue_post_fail(mock_post, mock_logging):
    mock_post_response = MagicMock()
    mock_post_response.status_code = 500
    mock_post.return_value = mock_post_response

    upgrade_scheduler.start_tasks_from_queue()

    # Assert firmware upgrade process was started with expected arguments
    mock_post.assert_called_with(
        "http://test-endpoint/run_firmware_upgrade",
        json={
            "manufacturer": "Commsignia",
            "model": "ITS-RS4-M",
            "ssh_username": "user",
            "ssh_password": "psw",
            "target_firmware_id": 2,
            "target_firmware_version": "y20.39.0",
            "install_package": "install_package.tar",
        },
    )

    mock_logging.info.assert_not_called()
    mock_logging.error.assert_called_with(
        f"Firmware upgrade runner request failed for 8.8.8.8, check Upgrade Runner logs for details"
    )


# init_firmware_upgrade tests


@patch("addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.logging")
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.active_upgrades",
    {},
)
def test_init_firmware_upgrade_missing_rsu_ip(mock_logging):
    mock_flask_request = MagicMock()
    mock_flask_request.get_json.return_value = {}
    mock_flask_jsonify = MagicMock()
    with patch(
        "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.request",
        mock_flask_request,
    ):
        with patch(
            "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.jsonify",
            mock_flask_jsonify,
        ):
            message, code = upgrade_scheduler.init_firmware_upgrade()

            mock_flask_jsonify.assert_called_with(
                {"error": "Missing 'rsu_ip' parameter"}
            )
            assert code == 400

            mock_logging.info.assert_not_called()
            mock_logging.error.assert_not_called()


@patch("addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.logging")
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.active_upgrades",
    {"8.8.8.8": {}},
)
def test_init_firmware_upgrade_already_running(mock_logging):
    mock_flask_request = MagicMock()
    mock_flask_request.get_json.return_value = {"rsu_ip": "8.8.8.8"}
    mock_flask_jsonify = MagicMock()
    with patch(
        "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.request",
        mock_flask_request,
    ):
        with patch(
            "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.jsonify",
            mock_flask_jsonify,
        ):
            message, code = upgrade_scheduler.init_firmware_upgrade()

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


@patch("addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.logging")
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.active_upgrades",
    {},
)
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.get_rsu_upgrade_data",
    MagicMock(return_value=[]),
)
def test_init_firmware_upgrade_no_eligible_upgrade(mock_logging):
    mock_flask_request = MagicMock()
    mock_flask_request.get_json.return_value = {"rsu_ip": "8.8.8.8"}
    mock_flask_jsonify = MagicMock()
    with patch(
        "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.request",
        mock_flask_request,
    ):
        with patch(
            "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.jsonify",
            mock_flask_jsonify,
        ):
            message, code = upgrade_scheduler.init_firmware_upgrade()

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


@patch("addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.logging")
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.active_upgrades",
    {},
)
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.get_rsu_upgrade_data",
    MagicMock(return_value=[fmv.rsu_info]),
)
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.start_tasks_from_queue"
)
def test_init_firmware_upgrade_success(mock_stfq, mock_logging):
    mock_flask_request = MagicMock()
    mock_flask_request.get_json.return_value = {"rsu_ip": "8.8.8.8"}
    mock_flask_jsonify = MagicMock()
    with patch(
        "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.request",
        mock_flask_request,
    ):
        with patch(
            "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.jsonify",
            mock_flask_jsonify,
        ):
            message, code = upgrade_scheduler.init_firmware_upgrade()

            # Assert start_tasks_from_queue is called
            mock_stfq.assert_called_with()

            # Assert the process reference is successfully tracked in the upgrade_queue
            assert upgrade_scheduler.upgrade_queue[0] == "8.8.8.8"

            # Assert REST response is as expected from a successful run
            mock_flask_jsonify.assert_called_with(
                {"message": f"Firmware upgrade started successfully for '8.8.8.8'"}
            )
            assert code == 201

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

    upgrade_scheduler.upgrade_queue = deque([])


# firmware_upgrade_completed tests


@patch("addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.logging")
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.active_upgrades",
    {},
)
def test_firmware_upgrade_completed_missing_rsu_ip(mock_logging):
    mock_flask_request = MagicMock()
    mock_flask_request.get_json.return_value = {}
    mock_flask_jsonify = MagicMock()
    with patch(
        "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.request",
        mock_flask_request,
    ):
        with patch(
            "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.jsonify",
            mock_flask_jsonify,
        ):
            message, code = upgrade_scheduler.firmware_upgrade_completed()

            mock_flask_jsonify.assert_called_with(
                {"error": "Missing 'rsu_ip' parameter"}
            )
            assert code == 400

            # Assert logging
            mock_logging.info.assert_not_called()
            mock_logging.error.assert_not_called()


@patch("addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.logging")
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.active_upgrades",
    {},
)
def test_firmware_upgrade_completed_unknown_process(mock_logging):
    mock_flask_request = MagicMock()
    mock_flask_request.get_json.return_value = {
        "rsu_ip": "8.8.8.8",
        "status": "success",
    }
    mock_flask_jsonify = MagicMock()
    with patch(
        "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.request",
        mock_flask_request,
    ):
        with patch(
            "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.jsonify",
            mock_flask_jsonify,
        ):
            message, code = upgrade_scheduler.firmware_upgrade_completed()

            mock_flask_jsonify.assert_called_with(
                {
                    "error": "Specified device is not actively being upgraded or was already completed"
                }
            )
            assert code == 400

            # Assert logging
            mock_logging.info.assert_not_called()
            mock_logging.error.assert_not_called()


@patch("addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.logging")
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.active_upgrades",
    {"8.8.8.8": fmv.upgrade_info},
)
def test_firmware_upgrade_completed_missing_status(mock_logging):
    mock_flask_request = MagicMock()
    mock_flask_request.get_json.return_value = {"rsu_ip": "8.8.8.8"}
    mock_flask_jsonify = MagicMock()
    with patch(
        "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.request",
        mock_flask_request,
    ):
        with patch(
            "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.jsonify",
            mock_flask_jsonify,
        ):
            message, code = upgrade_scheduler.firmware_upgrade_completed()

            mock_flask_jsonify.assert_called_with(
                {"error": "Missing 'status' parameter"}
            )
            assert code == 400

            # Assert logging
            mock_logging.info.assert_not_called()
            mock_logging.error.assert_not_called()


@patch("addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.logging")
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.active_upgrades",
    {"8.8.8.8": fmv.upgrade_info},
)
def test_firmware_upgrade_completed_illegal_status(mock_logging):
    mock_flask_request = MagicMock()
    mock_flask_request.get_json.return_value = {"rsu_ip": "8.8.8.8", "status": "frog"}
    mock_flask_jsonify = MagicMock()
    with patch(
        "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.request",
        mock_flask_request,
    ):
        with patch(
            "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.jsonify",
            mock_flask_jsonify,
        ):
            message, code = upgrade_scheduler.firmware_upgrade_completed()

            mock_flask_jsonify.assert_called_with(
                {
                    "error": "Wrong value for 'status' parameter - must be either 'success' or 'fail'"
                }
            )
            assert code == 400

            # Assert logging
            mock_logging.info.assert_not_called()
            mock_logging.error.assert_not_called()


@patch("addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.logging")
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.active_upgrades",
    {"8.8.8.8": fmv.upgrade_info},
)
def test_firmware_upgrade_completed_fail_status(mock_logging):
    mock_flask_request = MagicMock()
    mock_flask_request.get_json.return_value = {"rsu_ip": "8.8.8.8", "status": "fail"}
    mock_flask_jsonify = MagicMock()
    with patch(
        "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.request",
        mock_flask_request,
    ):
        with patch(
            "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.jsonify",
            mock_flask_jsonify,
        ):
            message, code = upgrade_scheduler.firmware_upgrade_completed()

            assert "8.8.8.8" not in upgrade_scheduler.active_upgrades
            mock_flask_jsonify.assert_called_with(
                {"message": "Firmware upgrade successfully marked as complete"}
            )
            assert code == 204

            # Assert logging
            mock_logging.info.assert_called_with(
                "Marking firmware upgrade as complete for '8.8.8.8'"
            )
            mock_logging.error.assert_not_called()


@patch("addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.logging")
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.active_upgrades",
    {"8.8.8.8": fmv.upgrade_info},
)
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.pgquery.write_db"
)
def test_firmware_upgrade_completed_success_status(mock_writedb, mock_logging):
    mock_flask_request = MagicMock()
    mock_flask_request.get_json.return_value = {
        "rsu_ip": "8.8.8.8",
        "status": "success",
    }
    mock_flask_jsonify = MagicMock()
    with patch(
        "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.request",
        mock_flask_request,
    ):
        with patch(
            "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.jsonify",
            mock_flask_jsonify,
        ):
            message, code = upgrade_scheduler.firmware_upgrade_completed()

            mock_writedb.assert_called_with(
                "UPDATE public.rsus SET firmware_version=2 WHERE ipv4_address='8.8.8.8'"
            )
            assert "8.8.8.8" not in upgrade_scheduler.active_upgrades
            mock_flask_jsonify.assert_called_with(
                {"message": "Firmware upgrade successfully marked as complete"}
            )
            assert code == 204

            # Assert logging
            mock_logging.info.assert_called_with(
                "Marking firmware upgrade as complete for '8.8.8.8'"
            )
            mock_logging.error.assert_not_called()


@patch("addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.logging")
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.active_upgrades",
    {"8.8.8.8": fmv.upgrade_info},
)
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.pgquery.write_db",
    side_effect=Exception("Failure to query PostgreSQL"),
)
def test_firmware_upgrade_completed_success_status_exception(
    mock_writedb, mock_logging
):
    mock_flask_request = MagicMock()
    mock_flask_request.get_json.return_value = {
        "rsu_ip": "8.8.8.8",
        "status": "success",
    }
    mock_flask_jsonify = MagicMock()
    with patch(
        "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.request",
        mock_flask_request,
    ):
        with patch(
            "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.jsonify",
            mock_flask_jsonify,
        ):
            message, code = upgrade_scheduler.firmware_upgrade_completed()

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


@patch("addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.logging")
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.active_upgrades",
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
        "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.request",
        mock_flask_request,
    ):
        with patch(
            "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.jsonify",
            mock_flask_jsonify,
        ):
            message, code = upgrade_scheduler.list_active_upgrades()

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


@patch.dict("os.environ", {"UPGRADE_RUNNER_ENDPOINT": "http://test-endpoint"})
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.active_upgrades",
    {},
)
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.get_rsu_upgrade_data",
    MagicMock(return_value=fmv.single_rsu_info),
)
@patch("addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.logging")
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.requests.post",
    side_effect=Exception("Exception during request"),
)
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.get_upgrade_limit"
)
def test_check_for_upgrades_exception(mock_upgrade_limit, mock_post, mock_logging):
    mock_upgrade_limit.return_value = 5
    upgrade_scheduler.check_for_upgrades()

    # Assert firmware upgrade process was started with expected arguments
    mock_post.assert_called_with(
        "http://test-endpoint/run_firmware_upgrade",
        json={
            "ipv4_address": "9.9.9.9",
            "manufacturer": "Commsignia",
            "model": "ITS-RS4-M",
            "ssh_username": "user",
            "ssh_password": "psw",
            "target_firmware_id": 2,
            "target_firmware_version": "y20.39.0",
            "install_package": "install_package.tar",
        },
    )

    # Assert the process reference is successfully tracked in the active_upgrades dictionary
    assert "9.9.9.9" not in upgrade_scheduler.active_upgrades
    mock_logging.info.assert_has_calls(
        [
            call("Checking PostgreSQL DB for RSUs with new target firmware"),
            call("Adding '9.9.9.9' to the firmware manager upgrade queue"),
            call("Firmware upgrade successfully started for '9.9.9.9'"),
        ]
    )
    mock_logging.error.assert_called_with(
        f"Encountered error of type {Exception} while starting automatic upgrade process for 9.9.9.9: Exception during request"
    )


@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.active_upgrades",
    {},
)
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.get_rsu_upgrade_data",
    MagicMock(return_value=fmv.multi_rsu_info),
)
@patch("addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.logging")
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.start_tasks_from_queue"
)
@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.get_upgrade_limit"
)
def test_check_for_upgrades(mock_upgrade_limit, mock_stfq, mock_logging):
    mock_upgrade_limit.return_value = 5
    upgrade_scheduler.check_for_upgrades()

    # Assert firmware upgrade process was started with expected arguments
    mock_stfq.assert_called_once_with()

    # Assert the process reference is successfully tracked in the active_upgrades dictionary
    assert upgrade_scheduler.upgrade_queue[1] == "9.9.9.9"
    mock_logging.info.assert_has_calls(
        [
            call("Checking PostgreSQL DB for RSUs with new target firmware"),
            call("Adding '8.8.8.8' to the firmware manager upgrade queue"),
            call("Firmware upgrade successfully started for '8.8.8.8'"),
            call("Adding '9.9.9.9' to the firmware manager upgrade queue"),
            call("Firmware upgrade successfully started for '9.9.9.9'"),
        ]
    )
    mock_logging.info.assert_called_with(
        "Firmware upgrade successfully started for '9.9.9.9'"
    )


# Other tests


@patch("addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.serve")
def test_serve_rest_api(mock_serve):
    upgrade_scheduler.serve_rest_api()
    mock_serve.assert_called_with(upgrade_scheduler.app, host="0.0.0.0", port=8080)


@patch(
    "addons.images.firmware_manager.upgrade_scheduler.upgrade_scheduler.BackgroundScheduler"
)
def test_init_background_task(mock_bgscheduler):
    mock_bgscheduler_obj = mock_bgscheduler.return_value

    upgrade_scheduler.init_background_task()

    mock_bgscheduler.assert_called_with({"apscheduler.timezone": "UTC"})
    mock_bgscheduler_obj.add_job.assert_called_with(
        upgrade_scheduler.check_for_upgrades, "cron", minute="0"
    )
    mock_bgscheduler_obj.start.assert_called_with()


def test_get_upgrade_limit_no_env():
    limit = upgrade_scheduler.get_upgrade_limit()
    assert limit == 1


@patch.dict("os.environ", {"ACTIVE_UPGRADE_LIMIT": "5"})
def test_get_upgrade_limit_with_env():
    limit = upgrade_scheduler.get_upgrade_limit()
    assert limit == 5


@patch.dict("os.environ", {"ACTIVE_UPGRADE_LIMIT": "bad_value"})
def test_get_upgrade_limit_with_bad_env():
    with pytest.raises(
        ValueError,
        match="The environment variable 'ACTIVE_UPGRADE_LIMIT' must be an integer.",
    ):
        upgrade_scheduler.get_upgrade_limit()
