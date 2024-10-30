#!/bin/bash

mkdir -p ./ssl_cert

# generate self-signed ssl key and cert
openssl req -newkey rsa:2048 -nodes -keyout ./ssl_cert/key.pem -x509 -days 3650 -out ./ssl_cert/cert.pem 

# If you would prefer, you can use the auto-generated information:
# -subj "/C=US/ST=Colorado/L=Denver/O=My Org/OU=Developers/CN=localhost/emailAddress=test@gmail.com"