def get_authstring(snmp_creds):
  snmp_authstring = '-u {user} -a SHA -A {pw} -x AES -X {pw} -l authpriv'.format(user=snmp_creds["username"], pw=snmp_creds["password"])
  return snmp_authstring