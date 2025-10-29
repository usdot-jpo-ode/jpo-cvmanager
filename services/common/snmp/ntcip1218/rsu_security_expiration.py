import subprocess
import logging
from datetime import datetime, timedelta, timezone
import common.snmp.snmpcredential as snmpcredential
import common.snmp.snmperrorcheck as snmperrorcheck


# Returns a human-readable string of the expiration time of the RSU security certificate
# The expiration time is granular to the hour
def get(rsu_ip, snmp_creds):
    """
    Retrieves the RSU's security certificate expiration time using SNMP NTCIP-1218 OIDs.
    Returns a dictionary with the expiration time as a string in the format "YYYY-MM-DD HH:00:00 UTC".
    """
    sec_result = None
    single_cert_duration = 0
    total_cert_duration = 0

    # Retrieve all active certificate request durations
    try:
        output = ""

        # Create the SNMPWalk command
        cmd = (
            "snmpwalk -v 3 -t 5 {auth} {rsuip} NTCIP1218-v01::rsuSecAppCertReq".format(
                auth=snmpcredential.get_authstring(snmp_creds), rsuip=rsu_ip
            )
        )

        # Example console output for a functional RSU with 2 certificates
        # NTCIP1218-v01::rsuSecAppCertReq.1 = INTEGER: 168 hour
        # NTCIP1218-v01::rsuSecAppCertReq.2 = INTEGER: 168 hour
        logging.info(f"Running snmpwalk: {cmd}")
        output = subprocess.run(cmd, shell=True, capture_output=True, check=True)
        output = output.stdout.decode("utf-8").split("\n")[:-1]
    except subprocess.CalledProcessError as e:
        output = e.stderr.decode("utf-8").split("\n")[:-1]
        logging.error(
            f"Encountered error while running snmpwalk NTCIP1218-v01::rsuSecAppCertReq: {output[-1]}"
        )
        err_message = snmperrorcheck.check_error_type(output[-1])
        return {"RsuSecurityExpiration": err_message}, 500

    if len(output) >= 1:
        # Parse each line of the output to sum the total requested certificate time
        for line in output:
            if "INTEGER" not in line:
                # Skip lines that do not contain INTEGER
                continue

            # split configuration line into a property and value
            # take the value, element 1, and split it by ": " to remove "INTEGER: "
            value = line.strip().split(" = ")[1].split(": ")[1]
            cert_duration = int(value.split(" ")[0])

            if single_cert_duration == 0:
                # If single_cert_duration is not set, set it to the current cert_duration since certificates are consistent
                single_cert_duration = cert_duration
            total_cert_duration += cert_duration

    # Retrieve the first certificate expiration time if there is one
    try:
        output = ""

        # Create the SNMPWalk command
        cmd = "snmpwalk -v 3 -t 5 {auth} {rsuip} NTCIP1218-v01::rsuSecAppCertExpiration".format(
            auth=snmpcredential.get_authstring(snmp_creds), rsuip=rsu_ip
        )

        # Example console output for a functional RSU with 2 certificates
        # A value of 255 means all certificates have expired
        # NTCIP1218-v01::rsuSecAppCertExpiration.1 = INTEGER: 119 hour
        # NTCIP1218-v01::rsuSecAppCertExpiration.2 = INTEGER: 254 hour
        logging.info(f"Running snmpwalk: {cmd}")
        output = subprocess.run(cmd, shell=True, capture_output=True, check=True)
        output = output.stdout.decode("utf-8").split("\n")[:-1]
    except subprocess.CalledProcessError as e:
        output = e.stderr.decode("utf-8").split("\n")[:-1]
        logging.error(
            f"Encountered error while running snmpwalk NTCIP1218-v01::rsuSecAppCertExpiration: {output[-1]}"
        )
        err_message = snmperrorcheck.check_error_type(output[-1])
        return {"RsuSecurityExpiration": err_message}, 500

    if len(output) >= 1:
        # Parse the first line of the output if it exists to subtract the lapsed time from the total requested time
        for line in output:
            if "INTEGER" not in line:
                total_cert_duration = 0
                break

            # split configuration line into a property and value
            # take the value, element 1, and split it by ": " to remove "INTEGER: "
            value = line.strip().split(" = ")[1].split(": ")[1]
            expiration = int(value.split(" ")[0])

            if expiration != 255:
                total_cert_duration = total_cert_duration - (
                    single_cert_duration - expiration
                )
            else:
                # If the expiration is 255, it means all certificates have expired
                # Set the total_cert_duration to 0 and break out of the loop
                total_cert_duration = 0
            break

    # Record the maximum expiration time and check if the value is 255 which means all certificates have expired
    sec_result = (
        datetime.now(timezone.utc) + timedelta(hours=total_cert_duration)
    ).strftime("%Y-%m-%d %H:00:00 %Z")

    return {"RsuSecurityExpiration": sec_result}, 200
