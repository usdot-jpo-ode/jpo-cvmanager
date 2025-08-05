import subprocess
import logging
import common.snmp.snmpcredential as snmpcredential
import common.snmp.snmperrorcheck as snmperrorcheck


def convert_location_value(val):
    """
    Converts the SNMP NTCIP-1218 value for latitude or longitude into a float in degrees.
    """
    numerical = val.split(" ")[0]
    # rsuLocationLat = 900000001 represents unknown
    if numerical == "900000001":
        return None
    # rsuLocationLon = 1800000001 represents unknown
    elif numerical == "1800000001":
        return None
    else:
        # Convert the value to a float and divide by 10^7 to get the latitude/longitude in degrees
        try:
            return float(numerical) / 10000000.0
        except ValueError:
            logging.error(f"Invalid value for latitude/longitude: {val}")
    return None


def get(rsu_ip, snmp_creds):
    """
    Retrieves the RSU's latitude and longitude using SNMP NTCIP-1218 OIDs.
    Returns a dictionary with latitude and longitude in degrees.
    """
    snmpget_result = {"latitude": None, "longitude": None}

    # Collect RSU latitude
    try:
        output = ""

        # Create the SNMPGet command
        cmd = "snmpget -v 3 -t 5 {auth} {rsuip} NTCIP1218-v01::rsuGnssLat.0".format(
            auth=snmpcredential.get_authstring(snmp_creds), rsuip=rsu_ip
        )

        # Example console output for a functional RSU
        # NTCIP1218-v01::rsuGnssLat.0 = INTEGER: 405672318 tenth of a microdegree
        logging.info(f"Running snmpget: {cmd}")
        output = subprocess.run(cmd, shell=True, capture_output=True, check=True)
        output = output.stdout.decode("utf-8").split("\n")[:-1]
    except subprocess.CalledProcessError as e:
        output = e.stderr.decode("utf-8").split("\n")[:-1]
        logging.error(
            f"Encountered error while running snmpget NTCIP1218-v01::rsuGnssLat: {output[-1]}"
        )
        err_message = snmperrorcheck.check_error_type(output[-1])
        return {"RsuLocation": err_message}, 500

    if len(output) == 1:
        # Parse each line of the output to build out readable SNMP configurations
        for line in output:
            # split configuration line into a property and value
            # take the value, element 1, and split it by ": " to remove "INTEGER: "
            value = line.strip().split(" = ")[1].split(": ")[1]
            latitude = convert_location_value(value)

            snmpget_result["latitude"] = latitude

    # Collect RSU longitude
    try:
        output = ""

        # Create the SNMPGet command
        cmd = "snmpget -v 3 -t 5 {auth} {rsuip} NTCIP1218-v01::rsuGnssLon.0".format(
            auth=snmpcredential.get_authstring(snmp_creds), rsuip=rsu_ip
        )

        # Example console output for a functional RSU
        # NTCIP1218-v01::rsuGnssLon.0 = INTEGER: -1050342786 tenth of a microdegree
        logging.info(f"Running snmpget: {cmd}")
        output = subprocess.run(cmd, shell=True, capture_output=True, check=True)
        output = output.stdout.decode("utf-8").split("\n")[:-1]
    except subprocess.CalledProcessError as e:
        output = e.stderr.decode("utf-8").split("\n")[:-1]
        logging.error(
            f"Encountered error while running snmpget NTCIP1218-v01::rsuGnssLon: {output[-1]}"
        )
        err_message = snmperrorcheck.check_error_type(output[-1])
        return {"RsuLocation": err_message}, 500

    if len(output) == 1:
        # Parse each line of the output to build out readable SNMP configurations
        for line in output:
            # split configuration line into a property and value
            # take the value, element 1, and split it by ": " to remove "INTEGER: "
            value = line.strip().split(" = ")[1].split(": ")[1]
            longitude = convert_location_value(value)

            snmpget_result["longitude"] = longitude

    return {"RsuLocation": snmpget_result}, 200
