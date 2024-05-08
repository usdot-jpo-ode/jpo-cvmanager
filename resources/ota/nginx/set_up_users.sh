#!/usr/bin/env bash

function generate_user() {
    htpasswd -c -b /var/www/.htpasswd "${OTA_USERNAME}" "${OTA_PASSWORD}"
}

function main() {
    mkdir -p /var/www/
    chmod 755 /var/www/
    touch /var/www/.htpasswd
    generate_user
}

if [[ -z "${OTA_USERNAME}" ]]; then
    echo "Error: USERNAME environment variable is not set."
    exit 1
fi

if [[ -z "${OTA_PASSWORD}" ]]; then
    echo "Error: PASSWORD environment variable is not set."
    exit 1
fi

main