import pytest
import unittest.mock as mock
from addons.images.obu_ota_server.commsginia_manifest import add_contents


def test_add_contents():
    # Mock server and firmware_list
    server = "http://localhost"
    firmware_list = [
        "/path/to/ob4-generic-ro-secureboot-yZZ.XX.YY-b012345.tar.sig",
        "/path/to/ob4-generic-ro-secureboot-yZZ.XX.YY-b012346.tar.sig",
        "/path/to/bad-firmware-file-name.tar.sig",
    ]

    # Mock os.path and os.stat
    with mock.patch("os.path.getmtime", return_value=1633022452), mock.patch(
        "os.stat", return_value=mock.Mock(st_size=1024)
    ):

        # Call the function with the mock data
        manifest = add_contents(server, firmware_list)

        # Check the manifest content
        assert manifest["totalElements"] == 2
        assert manifest["numberOfElements"] == 2
        assert len(manifest["content"]) == 2

        # Check the content of the first firmware
        assert (
            manifest["content"][0]["name"]
            == "ob4-generic-ro-secureboot-yZZ.XX.YY-b012345.tar.sig"
        )
        assert manifest["content"][0]["size"] == 1024

        # Check the content of the second firmware
        assert (
            manifest["content"][1]["name"]
            == "ob4-generic-ro-secureboot-yZZ.XX.YY-b012346.tar.sig"
        )
        assert manifest["content"][1]["size"] == 1024


def test_add_contents_max_firmwares():
    # Mock server and firmware_list with more firmwares than the size of the manifest
    server = "http://localhost"
    firmware_list = [
        f"/path/to/ob4-generic-ro-secureboot-yZZ.XX.YY-b{i}.tar.sig"
        for i in range(1, 35)
    ]

    # Mock os.path and os.stat
    with mock.patch("os.path.getmtime", return_value=1633022452), mock.patch(
        "os.stat", return_value=mock.Mock(st_size=1024)
    ):

        # Call the function with the mock data and check if it raises an AttributeError
        with pytest.raises(AttributeError):
            add_contents(server, firmware_list)
