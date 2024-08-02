from unittest.mock import patch, MagicMock
from subprocess import DEVNULL
from collections import deque
import test_firmware_manager_values as fmv

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
    mock_logging.error.assert_called_with(
        f"Encountered error of type {Exception} while starting automatic upgrade process for 8.8.8.8: Process failed to start"
    )


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
def test_start_tasks_from_queue_popen_success(mock_popen):
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


# init_firmware_upgrade tests


@patch("addons.images.firmware_manager.firmware_manager.active_upgrades", {})
def test_init_firmware_upgrade_missing_rsu_ip():
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


@patch(
    "addons.images.firmware_manager.firmware_manager.active_upgrades", {"8.8.8.8": {}}
)
def test_init_firmware_upgrade_already_running():
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


@patch("addons.images.firmware_manager.firmware_manager.active_upgrades", {})
@patch(
    "addons.images.firmware_manager.firmware_manager.get_rsu_upgrade_data",
    MagicMock(return_value=[]),
)
def test_init_firmware_upgrade_no_eligible_upgrade():
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
                    "error": f"Firmware upgrade failed to start for '8.8.8.8': the target firmware is already installed or is an invalid upgrade from the current firmware"
                }
            )
            assert code == 500


@patch("addons.images.firmware_manager.firmware_manager.active_upgrades", {})
@patch(
    "addons.images.firmware_manager.firmware_manager.get_rsu_upgrade_data",
    MagicMock(return_value=[fmv.rsu_info]),
)
@patch("addons.images.firmware_manager.firmware_manager.start_tasks_from_queue")
def test_init_firmware_upgrade_success(mock_stfq):
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

    firmware_manager.upgrade_queue = deque([])


# firmware_upgrade_completed tests


@patch("addons.images.firmware_manager.firmware_manager.active_upgrades", {})
def test_firmware_upgrade_completed_missing_rsu_ip():
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


@patch("addons.images.firmware_manager.firmware_manager.active_upgrades", {})
def test_firmware_upgrade_completed_unknown_process():
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


@patch(
    "addons.images.firmware_manager.firmware_manager.active_upgrades",
    {"8.8.8.8": fmv.upgrade_info},
)
def test_firmware_upgrade_completed_missing_status():
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


@patch(
    "addons.images.firmware_manager.firmware_manager.active_upgrades",
    {"8.8.8.8": fmv.upgrade_info},
)
def test_firmware_upgrade_completed_illegal_status():
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


@patch(
    "addons.images.firmware_manager.firmware_manager.active_upgrades",
    {"8.8.8.8": fmv.upgrade_info},
)
def test_firmware_upgrade_completed_fail_status():
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


@patch(
    "addons.images.firmware_manager.firmware_manager.active_upgrades",
    {"8.8.8.8": fmv.upgrade_info},
)
@patch("addons.images.firmware_manager.firmware_manager.pgquery.write_db")
def test_firmware_upgrade_completed_success_status(mock_writedb):
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


@patch(
    "addons.images.firmware_manager.firmware_manager.active_upgrades",
    {"8.8.8.8": fmv.upgrade_info},
)
@patch(
    "addons.images.firmware_manager.firmware_manager.pgquery.write_db",
    side_effect=Exception("Failure to query PostgreSQL"),
)
def test_firmware_upgrade_completed_success_status_exception(mock_writedb):
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


# list_active_upgrades tests


@patch(
    "addons.images.firmware_manager.firmware_manager.active_upgrades",
    {"8.8.8.8": fmv.upgrade_info},
)
def test_list_active_upgrades():
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


# check_for_upgrades tests


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
def test_check_for_upgrades_exception(mock_popen, mock_logging):
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
    mock_logging.error.assert_called_with(
        f"Encountered error of type {Exception} while starting automatic upgrade process for 9.9.9.9: Process failed to start"
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
def test_check_for_upgrades(mock_stfq, mock_logging):
    firmware_manager.check_for_upgrades()

    # Assert firmware upgrade process was started with expected arguments
    mock_stfq.assert_called_once_with()

    # Assert the process reference is successfully tracked in the active_upgrades dictionary
    assert firmware_manager.upgrade_queue[1] == "9.9.9.9"
    mock_logging.info.assert_called_with(
        "Firmware upgrade successfully started for '9.9.9.9'"
    )


# Other tests


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
