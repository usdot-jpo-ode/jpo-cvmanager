from common import snmpcredential


def test_get_authstring():
    snmp_creds = {"username": "testuser", "password": "testpassword", "encrypt_pw": "encryptpassword"}
    expected = "-u testuser -a SHA -A testpassword -x AES -X encryptpassword -l authpriv"
    assert snmpcredential.get_authstring(snmp_creds) == expected

def test_get_authstring_no_encrypt():
    snmp_creds = {"username": "testuser", "password": "testpassword", "encrypt_pw": None}
    expected = "-u testuser -a SHA -A testpassword -x AES -X testpassword -l authpriv"
    assert snmpcredential.get_authstring(snmp_creds) == expected