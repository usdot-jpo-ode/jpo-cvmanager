# SSL

This folder is used as an attached volume to the NGINX service and must include the server's `.crt` and `.key` file. All `.crt` and `.key` files are not tracked through git.

The `.crt` file must be in the following multi-line format:

```.crt
-----BEGIN CERTIFICATE-----
EC_KEY_BASED_CERTIFICATE
-----END CERTIFICATE-----
-----BEGIN CERTIFICATE-----
CA_INTERMEDIATE_CERTIFICATE
-----END CERTIFICATE-----
-----BEGIN CERTIFICATE-----
CA_ROOT_CERTIFICATE
-----END CERTIFICATE-----
```

The `.key` file must be in the following multi-line format:

```.key
-----BEGIN EC PRIVATE KEY-----
EC_PRIVATE_KEY_DATA
-----END EC PRIVATE KEY-----
```
