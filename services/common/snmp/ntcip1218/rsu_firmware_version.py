import subprocess
import logging
import common.snmp.snmpcredential as snmpcredential
import common.snmp.snmperrorcheck as snmperrorcheck


def get(rsu_ip, snmp_creds):
    snmpget_result = None

    try:
        output = ""

        # Create the SNMPGet command
        cmd = "snmpget -v 3 -t 5 {auth} {rsuip} NTCIP1218-v01::rsuFirmwareVersion.0".format(
            auth=snmpcredential.get_authstring(snmp_creds), rsuip=rsu_ip
        )

        # Example console output for a functional RSU
        # NTCIP1218-v01::rsuFirmwareVersion.0 = STRING: y20.61.1-b275956
        logging.info(f"Running snmpget: {cmd}")
        output = subprocess.run(cmd, shell=True, capture_output=True, check=True)
        output = output.stdout.decode("utf-8").split("\n")[:-1]
    except subprocess.CalledProcessError as e:
        output = e.stderr.decode("utf-8").split("\n")[:-1]
        logging.error(
            f"Encountered error while running snmpget NTCIP1218-v01::rsuFirmwareVersion: {output[-1]}"
        )
        err_message = snmperrorcheck.check_error_type(output[-1])
        return {"RsuStatus": err_message}, 500

    if len(output) == 1:
        # Parse each line of the output to build out readable SNMP configurations
        for line in output:
            # split configuration line into a property and value
            # take the value, element 1, and split it by ": " to remove "STRING: "
            value = line.strip().split(" = ")[1].split(": ")[1]

            snmpget_result = value

    return {"RsuFirmwareVersion": snmpget_result}, 200
