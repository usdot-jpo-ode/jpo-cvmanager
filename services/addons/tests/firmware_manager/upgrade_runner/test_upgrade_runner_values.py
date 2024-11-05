request_body_good = {
    "ipv4_address": "8.8.8.8",
    "manufacturer": "Commsignia",
    "model": "ITS-RS4-M",
    "ssh_username": "user",
    "ssh_password": "psw",
    "target_firmware_id": 2,
    "target_firmware_version": "y20.39.0",
    "install_package": "install_package.tar",
}

request_body_bad = {
    "manufacturer": "Commsignia",
    "model": "ITS-RS4-M",
    "ssh_username": "user",
    "ssh_password": "psw",
    "target_firmware_id": 2,
    "target_firmware_version": "y20.39.0",
    "install_package": "install_package.tar",
}
