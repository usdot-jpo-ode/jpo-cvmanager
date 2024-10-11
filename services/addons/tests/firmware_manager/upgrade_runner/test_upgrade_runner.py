from unittest.mock import call, patch, MagicMock
from subprocess import DEVNULL
from addons.images.firmware_manager.upgrade_runner import upgrade_runner
from werkzeug.exceptions import BadRequest
import addons.tests.firmware_manager.upgrade_runner.test_upgrade_runner_values as fmv

# start_upgrade_task tests


@patch("addons.images.firmware_manager.upgrade_runner.upgrade_runner.Popen")
def test_start_upgrade_task_success(mock_popen):
    with upgrade_runner.app.app_context():
        try:
            response = upgrade_runner.start_upgrade_task(fmv.request_body_good)

            # Assert
            expected_json_str = (
                '\'{"ipv4_address": "8.8.8.8", "manufacturer": "Commsignia", "model": "ITS-RS4-M", '
                '"ssh_username": "user", "ssh_password": "psw", "target_firmware_id": 2, "target_firmware_version": "y20.39.0", '
                '"install_package": "install_package.tar"}\''
            )
            mock_popen.assert_called_with(
                ["python3", f"/home/commsignia_upgrader.py", expected_json_str],
                stdout=DEVNULL,
            )
            assert response[1] == 201
        except Exception as e:
            assert False


@patch(
    "addons.images.firmware_manager.upgrade_runner.upgrade_runner.Popen",
    side_effect=Exception("Process failed to start"),
)
def test_start_upgrade_task_fail(mock_popen):
    with upgrade_runner.app.app_context():
        try:
            upgrade_runner.start_upgrade_task(fmv.request_body_good)
            assert False
        except Exception as e:
            # Assert
            expected_json_str = (
                '\'{"ipv4_address": "8.8.8.8", "manufacturer": "Commsignia", "model": "ITS-RS4-M", '
                '"ssh_username": "user", "ssh_password": "psw", "target_firmware_id": 2, "target_firmware_version": "y20.39.0", '
                '"install_package": "install_package.tar"}\''
            )
            mock_popen.assert_called_with(
                ["python3", f"/home/commsignia_upgrader.py", expected_json_str],
                stdout=DEVNULL,
            )


# run_firmware_upgrade tests


@patch(
    "addons.images.firmware_manager.upgrade_runner.upgrade_runner.start_upgrade_task",
    MagicMock(),
)
@patch("addons.images.firmware_manager.upgrade_runner.upgrade_runner.logging")
def test_run_firmware_upgrade_missing_rsu_ip(mock_logging):
    mock_flask_request = MagicMock()
    mock_flask_request.get_json.return_value = fmv.request_body_bad

    with upgrade_runner.app.app_context():
        with patch(
            "addons.images.firmware_manager.upgrade_runner.upgrade_runner.request",
            mock_flask_request,
        ):
            try:
                upgrade_runner.run_firmware_upgrade()
                assert False
            except BadRequest as e:
                mock_logging.error.assert_called_with(
                    "{'ipv4_address': ['Missing data for required field.']}"
                )


@patch(
    "addons.images.firmware_manager.upgrade_runner.upgrade_runner.start_upgrade_task"
)
@patch("addons.images.firmware_manager.upgrade_runner.upgrade_runner.logging")
def test_run_firmware_upgrade_success(mock_logging, mock_start_upgrade_task):
    mock_flask_request = MagicMock()
    mock_flask_request.get_json.return_value = fmv.request_body_good

    with upgrade_runner.app.app_context():
        with patch(
            "addons.images.firmware_manager.upgrade_runner.upgrade_runner.request",
            mock_flask_request,
        ):
            try:
                upgrade_runner.run_firmware_upgrade()
                mock_logging.error.assert_not_called()
                mock_start_upgrade_task.assert_called_with(fmv.request_body_good)
            except BadRequest as e:
                assert False


# Other tests


@patch("addons.images.firmware_manager.upgrade_runner.upgrade_runner.serve")
def test_serve_rest_api(mock_serve):
    upgrade_runner.serve_rest_api()
    mock_serve.assert_called_with(upgrade_runner.app, host="0.0.0.0", port=8080)
