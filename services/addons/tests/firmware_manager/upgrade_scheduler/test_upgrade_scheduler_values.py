from unittest.mock import MagicMock

all_rsus_query = (
    "SELECT to_jsonb(row) "
    "FROM ("
    "SELECT ipv4_address, man.name AS manufacturer, rm.name AS model, rc.username AS ssh_username, rc.password AS ssh_password, "
    "fi.firmware_id AS target_firmware_id, fi.version AS target_firmware_version, fi.install_package AS install_package "
    "FROM public.rsus rd "
    "JOIN public.rsu_models rm ON rm.rsu_model_id = rd.model "
    "JOIN public.manufacturers man ON man.manufacturer_id = rm.manufacturer "
    "JOIN public.rsu_credentials rc ON rc.credential_id = rd.credential_id "
    "JOIN public.firmware_upgrade_rules fur ON fur.from_id = rd.firmware_version "
    "JOIN public.firmware_images fi ON fi.firmware_id = rd.target_firmware_version "
    "WHERE firmware_version != target_firmware_version AND target_firmware_version = fur.to_id"
    ") as row"
)

one_rsu_query = (
    "SELECT to_jsonb(row) "
    "FROM ("
    "SELECT ipv4_address, man.name AS manufacturer, rm.name AS model, rc.username AS ssh_username, rc.password AS ssh_password, "
    "fi.firmware_id AS target_firmware_id, fi.version AS target_firmware_version, fi.install_package AS install_package "
    "FROM public.rsus rd "
    "JOIN public.rsu_models rm ON rm.rsu_model_id = rd.model "
    "JOIN public.manufacturers man ON man.manufacturer_id = rm.manufacturer "
    "JOIN public.rsu_credentials rc ON rc.credential_id = rd.credential_id "
    "JOIN public.firmware_upgrade_rules fur ON fur.from_id = rd.firmware_version "
    "JOIN public.firmware_images fi ON fi.firmware_id = rd.target_firmware_version "
    "WHERE firmware_version != target_firmware_version AND target_firmware_version = fur.to_id"
    " AND ipv4_address = '8.8.8.8'"
    ") as row"
)

rsu_info = {
    "ipv4_address": "8.8.8.8",
    "manufacturer": "Commsignia",
    "model": "ITS-RS4-M",
    "ssh_username": "user",
    "ssh_password": "psw",
    "target_firmware_id": 2,
    "target_firmware_version": "y20.39.0",
    "install_package": "install_package.tar",
}

multi_rsu_info = [
    {
        "ipv4_address": "8.8.8.8",
        "manufacturer": "Commsignia",
        "model": "ITS-RS4-M",
        "ssh_username": "user",
        "ssh_password": "psw",
        "target_firmware_id": 2,
        "target_firmware_version": "y20.39.0",
        "install_package": "install_package.tar",
    },
    {
        "ipv4_address": "9.9.9.9",
        "manufacturer": "Commsignia",
        "model": "ITS-RS4-M",
        "ssh_username": "user",
        "ssh_password": "psw",
        "target_firmware_id": 2,
        "target_firmware_version": "y20.39.0",
        "install_package": "install_package.tar",
    },
]

single_rsu_info = [
    {
        "ipv4_address": "9.9.9.9",
        "manufacturer": "Commsignia",
        "model": "ITS-RS4-M",
        "ssh_username": "user",
        "ssh_password": "psw",
        "target_firmware_id": 2,
        "target_firmware_version": "y20.39.0",
        "install_package": "install_package.tar",
    },
]

upgrade_info = {
    "process": MagicMock(),
    "manufacturer": "Commsignia",
    "model": "ITS-RS4-M",
    "ssh_username": "user",
    "ssh_password": "psw",
    "target_firmware_id": 2,
    "target_firmware_version": "y20.39.0",
    "install_package": "install_package.tar",
}
