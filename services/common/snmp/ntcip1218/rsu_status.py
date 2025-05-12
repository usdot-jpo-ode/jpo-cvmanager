import subprocess
import logging
import common.snmp.snmpcredential as snmpcredential


def convert_status_value(val):
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
