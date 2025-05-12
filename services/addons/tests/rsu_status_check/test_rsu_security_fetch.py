from unittest.mock import patch, MagicMock
from addons.images.rsu_status_check.rsu_security_fetch import main


@patch("os.environ", {"RSU_SECURITY_FETCH": "False"})
@patch("addons.images.rsu_status_check.rsu_security_fetch.UpdatePostgresRsuSecurity")
@patch("addons.images.rsu_status_check.rsu_security_fetch.logging")
def test_main_service_disabled(mock_logging, mock_update_pg):
    main()
    mock_logging.info.assert_called_once_with(
        "The rsu-security-fetch service is disabled and will not run"
    )
    mock_update_pg.assert_not_called()


@patch("os.environ", {"RSU_SECURITY_FETCH": "True"})
@patch("addons.images.rsu_status_check.rsu_security_fetch.UpdatePostgresRsuSecurity")
@patch("addons.images.rsu_status_check.rsu_security_fetch.logging")
def test_main_service_enabled(mock_logging, mock_update_pg):
    mock_instance = MagicMock()
    mock_update_pg.return_value = mock_instance
    mock_instance.get_rsu_list.return_value = ["rsu1", "rsu2"]
    mock_instance.get_snmp_configs.return_value = {"rsu1": "config1", "rsu2": "config2"}

    main()

    mock_update_pg.assert_called_once()
    mock_instance.get_rsu_list.assert_called_once()
    mock_instance.get_snmp_configs.assert_called_once_with(["rsu1", "rsu2"])
    mock_instance.update_postgresql.assert_called_once_with(
        {"rsu1": "config1", "rsu2": "config2"}
    )
