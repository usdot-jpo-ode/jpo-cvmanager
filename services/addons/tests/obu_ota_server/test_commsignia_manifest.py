import pytest
import unittest.mock as mock
from addons.images.obu_ota_server.commsignia_manifest import (
    FirmwareName,
    add_contents,
    is_valid_firmware_name,
    parse_firmware_name,
)


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
        assert manifest["size"] == 32
        assert manifest["pageable"]["pageSize"] == 32
        assert manifest["empty"] is False
        assert len(manifest["content"]) == 2

        # Check the content of the first firmware
        first = manifest["content"][0]
        assert first["name"] == "ob4-generic-ro-secureboot-yZZ.XX.YY-b012345.tar.sig"
        assert first["size"] == 1024
        assert first["variant"] == "ob4-generic"
        assert first["releaseVersion"] == "yZZ.XX.YY-b012345"
        assert first["type"] == "OBU"
        assert first["writableSystemPartition"] is False
        assert first["secure"] is True
        # Modification times are reported in UTC
        assert first["uploadedAt"] == "2021-09-30T17:20:52Z"
        assert (
            first["links"][0]["href"]
            == "http://localhost/firmwares/commsignia/ob4-generic-ro-secureboot-yZZ.XX.YY-b012345.tar.sig"
        )

        # Check the content of the second firmware
        assert (
            manifest["content"][1]["name"]
            == "ob4-generic-ro-secureboot-yZZ.XX.YY-b012346.tar.sig"
        )
        assert manifest["content"][1]["size"] == 1024


def test_add_contents_empty():
    manifest = add_contents("http://localhost", [])

    assert manifest["totalElements"] == 0
    assert manifest["numberOfElements"] == 0
    assert manifest["size"] == 32
    assert manifest["empty"] is True


def test_add_contents_more_than_default_page_size():
    # Catalogs larger than the historical 32-entry page are returned in a single page
    server = "http://localhost"
    firmware_list = [
        f"/path/to/ob4-generic-ro-secureboot-yZZ.XX.YY-b{i}.tar.sig"
        for i in range(1, 101)
    ]

    with mock.patch("os.path.getmtime", return_value=1633022452), mock.patch(
        "os.stat", return_value=mock.Mock(st_size=1024)
    ):
        manifest = add_contents(server, firmware_list)

    assert len(manifest["content"]) == 100
    assert manifest["totalElements"] == 100
    assert manifest["numberOfElements"] == 100
    assert manifest["size"] == 100
    assert manifest["pageable"]["pageSize"] == 100
    assert manifest["totalPages"] == 1


# Keep these cases identical to CommsigniaObuFirmwareFileNameParserTest in the
# Intersection API, which validates OBU firmware names at upload time.
@pytest.mark.parametrize(
    "file_name,expected",
    [
        ("ob4-generic-ro-secureboot-y20.48.2-b228647.tar.sig", True),
        ("ob4-generic-rw-y20.48.2-b228647.tar.sig", True),
        ("rs4-generic-ro-y20.48.2-b228647.tar.sig", True),
        ("ob4-generic-custom_variant-ro-secureboot-y20.48.2-b228647.tar.sig", True),
        ("ob4-generic-ro-secureboot-y20.48.2-b228647.tar", False),
        ("ob4-generic-ro-secureboot-y20.48.2-b228647.tar.sig.bak", False),
        ("ob4-generic-ro-secureboot-y20.48.2-b228647xtarxsig", False),
        ("../ob4-generic-ro-secureboot-y20.48.2-b228647.tar.sig", False),
        ("ob4-generic-ro-secureboot-../../etc/passwd.tar.sig", False),
        ("ob4-generic-ro-secureboot-y20/b228647.tar.sig", False),
        ("ob4-generic-ro-.tar.sig", False),
        ("ob5-generic-ro-y20.48.2-b228647.tar.sig", False),
        ("ob4-generic--ro-y20.48.2.tar.sig", False),
        ("ob4-generic-ro-" + "1" * 106 + ".tar.sig", False),
        ("ob4-generic-ro-" + "1" * 105 + ".tar.sig", True),
    ],
)
def test_is_valid_firmware_name(file_name, expected):
    assert is_valid_firmware_name(file_name) is expected


# Keep these cases identical to CommsigniaObuFirmwareFileNameParserTest in the
# Intersection API, which validates OBU firmware names at upload time.
@pytest.mark.parametrize(
    "file_name,expected",
    [
        (
            "ob4-generic-ro-secureboot-y20.48.2-b228647.tar.sig",
            FirmwareName("ob4-generic", "OBU", False, True, "y20.48.2-b228647"),
        ),
        (
            "rs4-generic-rw-y20.48.2-b228647.tar.sig",
            FirmwareName("rs4-generic", "RSU", True, False, "y20.48.2-b228647"),
        ),
        (
            "ob4-generic-custom_variant-ro-y20.48.2.tar.sig",
            FirmwareName("ob4-generic-custom_variant", "OBU", False, False, "y20.48.2"),
        ),
        # The last rw/ro marker that leaves a valid release wins, as with a greedy variant
        (
            "ob4-generic-ro-rw-y20.48.2.tar.sig",
            FirmwareName("ob4-generic-ro", "OBU", True, False, "y20.48.2"),
        ),
        (
            "ob4-generic-ro-y20-ro-.tar.sig",
            FirmwareName("ob4-generic", "OBU", False, False, "y20-ro-"),
        ),
        # secureboot without a following release is the release itself
        (
            "ob4-generic-ro-secureboot-.tar.sig",
            FirmwareName("ob4-generic", "OBU", False, False, "secureboot-"),
        ),
    ],
)
def test_parse_firmware_name(file_name, expected):
    assert parse_firmware_name(file_name) == expected


def test_parse_firmware_name_rejects_long_names_quickly():
    # Names with many markers are rejected by the length limit before parsing
    assert parse_firmware_name("ob4-generic" + "-ro" * 10000 + ".tar.sig") is None
