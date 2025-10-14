from unittest.mock import MagicMock, patch

import pytest
from api.src import rsu_upgrade
from werkzeug.exceptions import Conflict


@patch("api.src.rsu_upgrade.pgquery.query_db")
def test_check_for_upgrade_true(mock_query_db):
    # mock query_db
    mock_query_db.return_value = [
        (
            {
                "eligible_upgrade_id": 2,
                "eligible_upgrade_name": "new_fw_name",
                "eligible_upgrade_version": "1.0.0",
            },
        ),
    ]

    # call function
    rsu_ip = "192.168.0.10"
    actual_response = rsu_upgrade.check_for_upgrade(rsu_ip)

    # assert query_db was called with correct query
    expected_query = (
        "SELECT to_jsonb(row) "
        "FROM ("
        "SELECT fur.to_id AS eligible_upgrade_id, fi2.name AS eligible_upgrade_name, fi2.version AS eligible_upgrade_version "
        "FROM public.rsus AS rd "
        "JOIN public.firmware_upgrade_rules fur ON fur.from_id = rd.firmware_version "
        "JOIN public.firmware_images fi2 ON fi2.firmware_id = fur.to_id "
        "WHERE rd.ipv4_address = :rsu_ip"
        ") as row"
    )

    expected_response = {
        "upgrade_available": True,
        "upgrade_id": 2,
        "upgrade_name": "new_fw_name",
        "upgrade_version": "1.0.0",
    }

    mock_query_db.assert_called_with(expected_query, params={"rsu_ip": "192.168.0.10"})
    assert actual_response == expected_response


@patch("api.src.rsu_upgrade.pgquery.query_db")
def test_check_for_upgrade_false(mock_query_db):
    mock_query_db.return_value = []

    # call function
    rsu_ip = "192.168.0.10"
    actual_response = rsu_upgrade.check_for_upgrade(rsu_ip)

    # Assert expected query and response
    expected_query = (
        "SELECT to_jsonb(row) "
        "FROM ("
        "SELECT fur.to_id AS eligible_upgrade_id, fi2.name AS eligible_upgrade_name, fi2.version AS eligible_upgrade_version "
        "FROM public.rsus AS rd "
        "JOIN public.firmware_upgrade_rules fur ON fur.from_id = rd.firmware_version "
        "JOIN public.firmware_images fi2 ON fi2.firmware_id = fur.to_id "
        "WHERE rd.ipv4_address = :rsu_ip"
        ") as row"
    )

    expected_response = {
        "upgrade_available": False,
        "upgrade_id": -1,
        "upgrade_name": "",
        "upgrade_version": "",
    }

    mock_query_db.assert_called_with(expected_query, params={"rsu_ip": "192.168.0.10"})
    assert actual_response == expected_response


@patch("api.src.api_environment.FIRMWARE_MANAGER_ENDPOINT", "http://1.1.1.1:8080")
@patch("api.src.rsu_upgrade.requests.post")
@patch("api.src.rsu_upgrade.pgquery.write_db")
@patch(
    "api.src.rsu_upgrade.check_for_upgrade",
    return_value={
        "upgrade_available": True,
        "upgrade_id": 2,
        "upgrade_name": "new_fw_name",
        "upgrade_version": "1.0.0",
    },
)
def test_mark_rsu_for_upgrade_eligible(
    mock_check_for_upgrade, mock_write_db, mock_requests_post
):
    mock_response = MagicMock()
    mock_requests_post.return_value = mock_response
    mock_response.text = '{"message":"test successful"}'
    mock_response.status_code = 200

    # call function
    rsu_ip = "192.168.0.10"
    actual_message, actual_status_code = rsu_upgrade.mark_rsu_for_upgrade(rsu_ip)

    # Make assertions for each step of the function
    mock_check_for_upgrade.assert_called_with("192.168.0.10")

    expected_query = "UPDATE public.rsus SET target_firmware_version = 2 WHERE ipv4_address = '192.168.0.10'"
    mock_write_db.assert_called_with(expected_query)

    mock_requests_post.assert_called_with(
        "http://1.1.1.1:8080/init_firmware_upgrade", json={"rsu_ip": "192.168.0.10"}
    )

    expected_message, expected_status_code = {"message": "test successful"}, 200
    assert actual_message == expected_message
    assert actual_status_code == expected_status_code


@patch("api.src.api_environment.FIRMWARE_MANAGER_ENDPOINT", "http://1.1.1.1:8080")
@patch("api.src.rsu_upgrade.requests.post")
@patch("api.src.rsu_upgrade.pgquery.write_db")
@patch(
    "api.src.rsu_upgrade.check_for_upgrade",
    return_value={
        "upgrade_available": True,
        "upgrade_id": 2,
        "upgrade_name": "new_fw_name",
        "upgrade_version": "1.0.0",
    },
)
def test_mark_rsu_for_upgrade_eligible_but_rejected(
    mock_check_for_upgrade, mock_write_db, mock_requests_post
):
    mock_response = MagicMock()
    mock_requests_post.return_value = mock_response
    mock_response.text = '{"error":"Firmware upgrade request denied due to an upgrade already occurring for the target RSU"}'
    mock_response.status_code = 500

    # call function
    rsu_ip = "192.168.0.10"
    actual_message, actual_status_code = rsu_upgrade.mark_rsu_for_upgrade(rsu_ip)

    # Make assertions for each step of the function
    mock_check_for_upgrade.assert_called_with("192.168.0.10")

    expected_query = "UPDATE public.rsus SET target_firmware_version = 2 WHERE ipv4_address = '192.168.0.10'"
    mock_write_db.assert_called_with(expected_query)

    mock_requests_post.assert_called_with(
        "http://1.1.1.1:8080/init_firmware_upgrade", json={"rsu_ip": "192.168.0.10"}
    )

    expected_message, expected_status_code = {
        "error": "Firmware upgrade request denied due to an upgrade already occurring for the target RSU"
    }, 500
    assert actual_message == expected_message
    assert actual_status_code == expected_status_code


@patch("api.src.api_environment.FIRMWARE_MANAGER_ENDPOINT", "http://1.1.1.1:8080")
@patch("api.src.rsu_upgrade.requests.post")
@patch("api.src.rsu_upgrade.pgquery.write_db")
@patch(
    "api.src.rsu_upgrade.check_for_upgrade",
    return_value={
        "upgrade_available": False,
        "upgrade_id": -1,
        "upgrade_name": "",
        "upgrade_version": "",
    },
)
def test_mark_rsu_for_upgrade_ineligible(
    mock_check_for_upgrade, mock_write_db, mock_requests_post
):
    # call function
    rsu_ip = "192.168.0.10"

    expected_message = f"409 Conflict: Requested RSU '{rsu_ip}' is already up to date with the latest firmware"
    with pytest.raises(Conflict) as exc_info:
        rsu_upgrade.mark_rsu_for_upgrade(rsu_ip)

    assert str(exc_info.value) == expected_message

    # Make assertions for each step of the function
    mock_check_for_upgrade.assert_called_with("192.168.0.10")
    mock_write_db.assert_not_called()
    mock_requests_post.assert_not_called()
