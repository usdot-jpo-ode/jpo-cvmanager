def get_authstring(snmp_creds):
    # If "encrypt_pw" isn't in the dictionary, get the value from "password"
    # This must be handled separately before checking the value of "encrypt_pw" to avoid KeyErrors
    if "encrypt_pw" not in snmp_creds:
        encrypt_pw = snmp_creds["password"]
    # If "encrypt_pw" is set to an empty string or None, get the value from "password"
    elif not snmp_creds["encrypt_pw"]:
        encrypt_pw = snmp_creds["password"]
    else:
        encrypt_pw = snmp_creds["encrypt_pw"]

    snmp_authstring = (
        "-u {user} -a SHA -A {pw} -x AES -X {encrypt_pw} -l authpriv".format(
            user=snmp_creds["username"],
            pw=snmp_creds["password"],
            encrypt_pw=encrypt_pw,
        )
    )

    return snmp_authstring
