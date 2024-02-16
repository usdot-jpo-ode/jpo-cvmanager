def get_authstring(snmp_creds):
    encrypt_pw = snmp_creds["encrypt_pw"] if snmp_creds["encrypt_pw"] != None else snmp_creds["password"]
    snmp_authstring = "-u {user} -a SHA -A {pw} -x AES -X {encrypt_pw} -l authpriv".format(
        user=snmp_creds["username"], pw=snmp_creds["password"], encrypt_pw=encrypt_pw
    )
    return snmp_authstring
