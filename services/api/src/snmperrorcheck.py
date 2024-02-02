def check_error_type(output):
    if "Authentication failure (incorrect password, community or key)" in output:
        return "Authentication failure. RSU SNMP credentials are unexpected."
    elif "Reason: notWritable (That object does not support modification)" in output:
        return "RSU SNMP is in notWritable mode. RSU needs troubleshooting."
    elif "Timeout" in output:
        return "RSU SNMP timed out. RSU needs troubleshooting."
    else:
        return output
