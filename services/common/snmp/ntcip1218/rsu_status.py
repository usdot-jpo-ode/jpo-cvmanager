import subprocess
import logging
import common.snmp.snmpcredential as snmpcredential


def convert_status_value(val):
    """
    Converts the SNMP NTCIP-1218 value for RSU status into an integer based on the NTCIP-1218 standard.
    """
    if val == "other(1)":
        return 1
    elif val == "okay(2)":
        return 2
    elif val == "warning(3)":
        return 3
    elif val == "critical(4)":
        return 4
    # Return the value for 'unknown' status if it doesn't match any of the above
    return 5


def get(rsu_ip, snmp_creds):
    """
    Retrieves the RSU's status using SNMP NTCIP-1218 OID.
    Returns a dictionary with the RSU status as an integer.
    Returns 5 for 'unknown' status if the command fails or the status is not recognized
    which conforms to the NTCIP-1218 standardized way to report an unknown RSU status.
    """
    snmpget_result = None

    try:
        output = ""

        # Create the SNMPGet command
        cmd = "snmpget -v 3 {auth} {rsuip} NTCIP1218-v01::rsuStatus.0".format(
            auth=snmpcredential.get_authstring(snmp_creds), rsuip=rsu_ip
        )

        # Example console output for a functional RSU
        # NTCIP1218-v01::rsuStatus.0 = INTEGER: okay(2)
        logging.info(f"Running snmpget: {cmd}")
        output = subprocess.run(cmd, shell=True, capture_output=True, check=True)
        output = output.stdout.decode("utf-8").split("\n")[:-1]
    except subprocess.CalledProcessError as e:
        output = e.stderr.decode("utf-8").split("\n")[:-1]
        logging.error(
            f"Encountered error while running snmpget NTCIP1218-v01::rsuStatus: {output[-1]}"
        )
        # Returns unknown status if the command fails
        return {"RsuStatus": 5}, 500

    if len(output) == 1:
        # Parse each line of the output to build out readable SNMP configurations
        for line in output:
            # split configuration line into a property and value
            # take the value, element 1, and split it by ": " to remove "INTEGER: "
            value = line.strip().split(" = ")[1].split(": ")[1]
            status_value = convert_status_value(value)

            snmpget_result = status_value

    return {"RsuStatus": snmpget_result}, 200
