from common import snmperrorcheck


def test_check_error_type():
    output = "Authentication failure (incorrect password, community or key)"
    expected = "Authentication failure. RSU SNMP credentials are unexpected."
    assert snmperrorcheck.check_error_type(output) == expected

    output = "Reason: notWritable (That object does not support modification)"
    expected = "RSU SNMP is in notWritable mode. RSU needs troubleshooting."
    assert snmperrorcheck.check_error_type(output) == expected

    output = "Timeout"
    expected = "RSU SNMP timed out. RSU needs troubleshooting."
    assert snmperrorcheck.check_error_type(output) == expected

    output = "Other error"
    expected = "Other error"
    assert snmperrorcheck.check_error_type(output) == expected
