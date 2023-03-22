from src import snmpcredential

def test_get_authstring():
    snmp_creds = {"username": "testuser", "password": "testpassword"}
    expected = '-u testuser -a SHA -A testpassword -x AES -X testpassword -l authpriv'
    assert snmpcredential.get_authstring(snmp_creds) == expected