import subprocess
import logging
import common.snmp.snmpcredential as snmpcredential
import common.snmp.snmperrorcheck as snmperrorcheck
import common.util as util
import common.snmp.rsu_message_forward_helpers as rsu_message_forward_helpers
from datetime import datetime


# NTCIP-1218 standard SNMP OIDs property to string name and processing function
prop_namevalue = {
    # These values are based off the NTCIP 1218 rsuReceivedMsgTable table
    "NTCIP1218-v01::rsuReceivedMsgPsid": (
        "Message Type",
        rsu_message_forward_helpers.message_type_ntcip1218,
    ),
    "NTCIP1218-v01::rsuReceivedMsgDestIpAddr": (
        "IP",
        rsu_message_forward_helpers.ip_ntcip1218,
    ),
    "NTCIP1218-v01::rsuReceivedMsgDestPort": ("Port", int),
    "NTCIP1218-v01::rsuReceivedMsgProtocol": (
        "Protocol",
        rsu_message_forward_helpers.protocol,
    ),
    "NTCIP1218-v01::rsuReceivedMsgRssi": (
        "RSSI",
        rsu_message_forward_helpers.rssi_ntcip1218,
    ),
    "NTCIP1218-v01::rsuReceivedMsgInterval": ("Frequency", int),
    "NTCIP1218-v01::rsuReceivedMsgDeliveryStart": (
        "Start DateTime",
        rsu_message_forward_helpers.startend_ntcip1218,
    ),
    "NTCIP1218-v01::rsuReceivedMsgDeliveryStop": (
        "End DateTime",
        rsu_message_forward_helpers.startend_ntcip1218,
    ),
    "NTCIP1218-v01::rsuReceivedMsgStatus": (
        "Config Active",
        rsu_message_forward_helpers.active,
    ),
    "NTCIP1218-v01::rsuReceivedMsgSecure": (
        "Full WSMP",
        rsu_message_forward_helpers.active,
    ),
    "NTCIP1218-v01::rsuReceivedMsgAuthMsgInterval": (
        "Security Filter",
        rsu_message_forward_helpers.active,
    ),
    # -----
    # These values are based off the NTCIP 1218 rsuXmitMsgFwdingTable table
    "NTCIP1218-v01::rsuXmitMsgFwdingPsid": (
        "Message Type",
        rsu_message_forward_helpers.message_type_ntcip1218,
    ),
    "NTCIP1218-v01::rsuXmitMsgFwdingDestIpAddr": (
        "IP",
        rsu_message_forward_helpers.ip_ntcip1218,
    ),
    "NTCIP1218-v01::rsuXmitMsgFwdingDestPort": ("Port", int),
    "NTCIP1218-v01::rsuXmitMsgFwdingProtocol": (
        "Protocol",
        rsu_message_forward_helpers.protocol,
    ),
    "NTCIP1218-v01::rsuXmitMsgFwdingDeliveryStart": (
        "Start DateTime",
        rsu_message_forward_helpers.startend_ntcip1218,
    ),
    "NTCIP1218-v01::rsuXmitMsgFwdingDeliveryStop": (
        "End DateTime",
        rsu_message_forward_helpers.startend_ntcip1218,
    ),
    "NTCIP1218-v01::rsuXmitMsgFwdingSecure": (
        "Full WSMP",
        rsu_message_forward_helpers.active,
    ),
    "NTCIP1218-v01::rsuXmitMsgFwdingStatus": (
        "Config Active",
        rsu_message_forward_helpers.active,
    ),
}


# delta is in years
def hex_datetime(now, delta=0):
    # Regex to convert int to hex and to ensure a leading 0 if less than specified length
    regex = "{0:0{1}x}"
    hex_string = regex.format(now.year + delta, 4)
    hex_string += regex.format(now.month, 2)
    hex_string += regex.format(now.day, 2)
    hex_string += regex.format(now.hour, 2)
    hex_string += regex.format(now.minute, 2)
    return hex_string


def perform_snmp_mods(snmp_mods):
    for snmp_mod in snmp_mods:
        # Perform configuration
        logging.info(f'Running SNMPSET "{snmp_mod}"')
        output = subprocess.run(snmp_mod, shell=True, capture_output=True, check=True)
        output = output.stdout.decode("utf-8").split("\n")[:-1]
        logging.info(f"SNMPSET output: {output}")


def get(rsu_ip, snmp_creds):
    snmpwalk_results = {"rsuReceivedMsgTable": {}, "rsuXmitMsgFwdingTable": {}}
    # Start with rsuReceivedMsgTable
    output = ""
    try:
        # Create the SNMPWalk command based on the road
        cmd = "snmpwalk -v 3 -t 5 {auth} {rsuip} NTCIP1218-v01:rsuReceivedMsgTable".format(
            auth=snmpcredential.get_authstring(snmp_creds), rsuip=rsu_ip
        )

        # Example console output of a single configuration for rsuReceivedMsgTable
        # NTCIP1218-v01::rsuReceivedMsgPsid.1 = STRING: 20000000    # BSM
        # NTCIP1218-v01::rsuReceivedMsgDestIpAddr.1 = STRING: 10.235.1.135
        # NTCIP1218-v01::rsuReceivedMsgDestPort.1 = INTEGER: 46800
        # NTCIP1218-v01::rsuReceivedMsgProtocol.1 = INTEGER: udp(2)
        # NTCIP1218-v01::rsuReceivedMsgRssi.1 = INTEGER: -100 dBm
        # NTCIP1218-v01::rsuReceivedMsgInterval.1 = INTEGER: 1
        # NTCIP1218-v01::rsuReceivedMsgDeliveryStart.1 = STRING: 2024-1-30,2:57:0.0
        # NTCIP1218-v01::rsuReceivedMsgDeliveryStop.1 = STRING: 2034-1-30,2:57:0.0
        # NTCIP1218-v01::rsuReceivedMsgStatus.1 = INTEGER: active(1)
        # NTCIP1218-v01::rsuReceivedMsgSecure.1 = INTEGER: 0    # 0 - Only forward unsigned payload. 1 - Forward entire signed WSMP message.
        # NTCIP1218-v01::rsuReceivedMsgAuthMsgInterval.1 = INTEGER: 0    # 0 means off. Only 0 is supported.
        logging.info(f"Running snmpwalk: {cmd}")
        output = subprocess.run(cmd, shell=True, capture_output=True, check=True)
        output = output.stdout.decode("utf-8").split("\n")[:-1]
    except subprocess.CalledProcessError as e:
        output = e.stderr.decode("utf-8").split("\n")[:-1]
        logging.error(f"Encountered error while running snmpwalk: {output[-1]}")
        err_message = snmperrorcheck.check_error_type(output[-1])
        return {"RsuFwdSnmpwalk": err_message}, 500

    # Placeholder for possible other failed scenarios
    # A proper rsuReceivedMsgTable configuration will be exactly 11 lines of output.
    # Any RSU with an output of less than 11 can be assumed to be an RSU with
    # no rsuReceivedMsgTable configurations.
    if len(output) >= 11:
        snmp_config = {}

        # Parse each line of the output to build out readable SNMP configurations
        for line in output:
            # split configuration line into a property and value
            prop, value = line.strip().split(" = ")
            # grab the property name and index
            prop_name, prop_index = prop.split(".")

            # If the index value already exists in the dict, ensure to add the new configuration value to it to build out a full SNMP configuration
            config = snmp_config[prop_index] if prop_index in snmp_config else {}
            # Assign the processed value of the the property to the readable property value and store the info based on the index value
            # The value is processed based on the type of property it is
            # The readable property name is based on the property
            config[prop_namevalue[prop_name][0]] = prop_namevalue[prop_name][1](
                value.split(": ")[1]
            )
            snmp_config[prop_index] = config

        snmpwalk_results["rsuReceivedMsgTable"] = snmp_config

    # Second, check rsuXmitMsgFwdingTable
    output = ""
    try:
        # Create the SNMPWalk command based on the road
        cmd = "snmpwalk -v 3 -t 5 {auth} {rsuip} NTCIP1218-v01:rsuXmitMsgFwdingTable".format(
            auth=snmpcredential.get_authstring(snmp_creds), rsuip=rsu_ip
        )

        # Example console output of a single configuration for rsuXmitMsgFwdingTable
        # NTCIP1218-v01::rsuXmitMsgFwdingPsid.1 = STRING: e0000017    #MAP
        # NTCIP1218-v01::rsuXmitMsgFwdingDestIpAddr.1 = STRING: 10.235.1.135
        # NTCIP1218-v01::rsuXmitMsgFwdingDestPort.1 = INTEGER: 44920
        # NTCIP1218-v01::rsuXmitMsgFwdingProtocol.1 = INTEGER: udp(2)
        # NTCIP1218-v01::rsuXmitMsgFwdingDeliveryStart.1 = STRING: 2024-2-1,3:36:0.0
        # NTCIP1218-v01::rsuXmitMsgFwdingDeliveryStop.1 = STRING: 2034-2-1,3:36:0.0
        # NTCIP1218-v01::rsuXmitMsgFwdingSecure.1 = INTEGER: 0    # 0 - Only forward unsigned payload. 1 - Forward entire signed WSMP message.
        # NTCIP1218-v01::rsuXmitMsgFwdingStatus.1 = INTEGER: active(1)
        logging.info(f"Running snmpwalk: {cmd}")
        output = subprocess.run(cmd, shell=True, capture_output=True, check=True)
        output = output.stdout.decode("utf-8").split("\n")[:-1]
    except subprocess.CalledProcessError as e:
        output = e.stderr.decode("utf-8").split("\n")[:-1]
        logging.error(f"Encountered error while running snmpwalk: {output[-1]}")
        err_message = snmperrorcheck.check_error_type(output[-1])
        return {"RsuFwdSnmpwalk": err_message}, 500

    # Placeholder for possible other failed scenarios
    # A proper rsuXmitMsgFwdingTable configuration will be exactly 8 lines of output.
    # Any RSU with an output of less than 8 can be assumed to be an RSU with
    # no rsuXmitMsgFwdingTable configurations, or that some form error occurred in
    # reading an RSU's SNMP configuration data. In either scenario, simply returning an
    # empty response will suffice for the first implementation.
    if len(output) >= 8:
        snmp_config = {}

        # Parse each line of the output to build out readable SNMP configurations
        for line in output:
            ## split configuration line into a property and value
            prop, value = line.strip().split(" = ")
            # grab the property name and index
            prop_name, prop_index = prop.split(".")

            # If the index value already exists in the dict, ensure to add the new configuration value to it to build out a full SNMP configuration
            config = snmp_config[prop_index] if prop_index in snmp_config else {}
            # Assign the processed value of the the property to the readable property value and store the info based on the index value
            # The value is processed based on the type of property it is
            # The readable property name is based on the property
            config[prop_namevalue[prop_name][0]] = prop_namevalue[prop_name][1](
                value.split(": ")[1]
            )
            snmp_config[prop_index] = config

        snmpwalk_results["rsuXmitMsgFwdingTable"] = snmp_config

    return {"RsuFwdSnmpwalk": snmpwalk_results}, 200


def set(rsu_ip, snmp_creds, dest_ip, udp_port, rsu_index, psid, security, tx):
    try:
        logging.info("Running SNMP config on NTCIP-1218 RSU {}".format(dest_ip))

        snmp_mods = []
        authstring = snmpcredential.get_authstring(snmp_creds)

        # Only forward TX messages. Uses rsuXmitMsgFwdingTable table indexes
        # rsuXmitMsgFwdingPsid - hex : PSID
        # rsuXmitMsgFwdingDestIpAddr - string : Destination  IP (IPv4)
        # rsuXmitMsgFwdingDestPort - int : port
        # rsuXmitMsgFwdingProtocol - int : protocol (1: tcp, 2: udp)
        # rsuXmitMsgFwdingDeliveryStart - hex : start datetime
        # rsuXmitMsgFwdingDeliveryStop - hex : end datetime
        # rsuXmitMsgFwdingSecure - int : WSMP full message (0: only payload, 1: full message)
        # rsuXmitMsgFwdingStatus - int : SNMP row value (4: create, 6: delete)
        if tx:
            snmp_mod = "snmpset -v 3 -t 5 {auth} {rsuip} ".format(
                auth=authstring, rsuip=rsu_ip
            )
            snmp_mod += (
                "NTCIP1218-v01:rsuXmitMsgFwdingPsid.{index} x {msgpsid} ".format(
                    index=rsu_index, msgpsid=psid
                )
            )
            snmp_mod += (
                "NTCIP1218-v01:rsuXmitMsgFwdingDestIpAddr.{index} s {destip} ".format(
                    index=rsu_index, destip=dest_ip
                )
            )
            snmp_mod += (
                "NTCIP1218-v01:rsuXmitMsgFwdingDestPort.{index} i {port} ".format(
                    index=rsu_index, port=udp_port
                )
            )
            snmp_mod += "NTCIP1218-v01:rsuXmitMsgFwdingProtocol.{index} i 2 ".format(
                index=rsu_index
            )

            # NTCIP-1218 expects a hex value of 16 length for rsuXmitMsgFwdingTable
            now = util.utc2tz(datetime.now())
            start_hex = hex_datetime(now) + "0000"
            end_hex = hex_datetime(now, 10) + "0000"

            snmp_mod += (
                "NTCIP1218-v01:rsuXmitMsgFwdingDeliveryStart.{index} x {dt} ".format(
                    index=rsu_index, dt=start_hex
                )
            )
            snmp_mod += (
                "NTCIP1218-v01:rsuXmitMsgFwdingDeliveryStop.{index} x {dt} ".format(
                    index=rsu_index, dt=end_hex
                )
            )
            snmp_mod += "NTCIP1218-v01:rsuXmitMsgFwdingSecure.{index} i {sec} ".format(
                index=rsu_index, sec=security
            )
            snmp_mod += "NTCIP1218-v01:rsuXmitMsgFwdingStatus.{index} i 4".format(
                index=rsu_index
            )

            snmp_mods.append(snmp_mod)
        # Only forward RX messages. Uses rsuReceivedMsgTable table indexes
        # rsuReceivedMsgPsid - hex : PSID
        # rsuReceivedMsgDestIpAddr - string : Destination  IP (IPv4)
        # rsuReceivedMsgDestPort - int : port
        # rsuReceivedMsgProtocol - int : protocol (1: tcp, 2: udp)
        # rsuReceivedMsgRssi - int - rssi (-100 is recommended)
        # rsuReceivedMsgInterval - int : message forward rate (forward every nth message)
        # rsuReceivedMsgDeliveryStart - hex : start datetime
        # rsuReceivedMsgDeliveryStop - hex : end datetime
        # rsuReceivedMsgStatus - int : SNMP row value (4: create, 6: delete)
        # rsuReceivedMsgSecure - int : WSMP full message (0: only payload, 1: full message)
        # rsuReceivedMsgAuthMsgInterval - int : Do not turn on. (0: off, 1: on)
        else:
            snmp_mod = "snmpset -v 3 -t 5 {auth} {rsuip} ".format(
                auth=authstring, rsuip=rsu_ip
            )
            snmp_mod += "NTCIP1218-v01:rsuReceivedMsgPsid.{index} x {msgpsid} ".format(
                index=rsu_index, msgpsid=psid
            )
            snmp_mod += (
                "NTCIP1218-v01:rsuReceivedMsgDestIpAddr.{index} s {destip} ".format(
                    index=rsu_index, destip=dest_ip
                )
            )
            snmp_mod += "NTCIP1218-v01:rsuReceivedMsgDestPort.{index} i {port} ".format(
                index=rsu_index, port=udp_port
            )
            snmp_mod += "NTCIP1218-v01:rsuReceivedMsgProtocol.{index} i 2 ".format(
                index=rsu_index
            )
            snmp_mod += "NTCIP1218-v01:rsuReceivedMsgRssi.{index} i -100 ".format(
                index=rsu_index
            )
            snmp_mod += "NTCIP1218-v01:rsuReceivedMsgInterval.{index} i 1 ".format(
                index=rsu_index
            )

            # NTCIP-1218 expects a hex value of 16 length for rsuReceivedMsgTable
            now = util.utc2tz(datetime.now())
            start_hex = hex_datetime(now) + "0000"
            end_hex = hex_datetime(now, 10) + "0000"

            snmp_mod += (
                "NTCIP1218-v01:rsuReceivedMsgDeliveryStart.{index} x {dt} ".format(
                    index=rsu_index, dt=start_hex
                )
            )
            snmp_mod += (
                "NTCIP1218-v01:rsuReceivedMsgDeliveryStop.{index} x {dt} ".format(
                    index=rsu_index, dt=end_hex
                )
            )
            snmp_mod += "NTCIP1218-v01:rsuReceivedMsgStatus.{index} i 4 ".format(
                index=rsu_index
            )
            snmp_mod += "NTCIP1218-v01:rsuReceivedMsgSecure.{index} i {sec} ".format(
                index=rsu_index, sec=security
            )
            snmp_mod += (
                "NTCIP1218-v01:rsuReceivedMsgAuthMsgInterval.{index} i 0".format(
                    index=rsu_index
                )
            )

            snmp_mods.append(snmp_mod)

        perform_snmp_mods(snmp_mods)
        response = "Successfully completed the NTCIP-1218 SNMPSET configuration"
        code = 200
    except subprocess.CalledProcessError as e:
        output = e.stderr.decode("utf-8").split("\n")[:-1]
        logging.error(
            f"Encountered error while modifying NTCIP-1218 RSU SNMP: {output[-1]}"
        )
        response = snmperrorcheck.check_error_type(output[-1])
        code = 500

    return response, code


def delete(rsu_ip, snmp_creds, msg_type, rsu_index):
    try:
        snmp_mods = "snmpset -v 3 -t 5 {auth} {rsuip} ".format(
            auth=snmpcredential.get_authstring(snmp_creds), rsuip=rsu_ip
        )
        msgtype_oid_mapping = {
            "bsm": "rsuReceivedMsgStatus",
            "spat": "rsuXmitMsgFwdingStatus",
            "map": "rsuXmitMsgFwdingStatus",
            "ssm": "rsuXmitMsgFwdingStatus",
            "srm": "rsuReceivedMsgStatus",
            "tim": "rsuXmitMsgFwdingStatus",
        }

        snmp_mods += "NTCIP1218-v01:{oid}.{index} i 6 ".format(
            oid=msgtype_oid_mapping[msg_type.lower()], index=rsu_index
        )

        # Perform configurations
        logging.info(f'Running SNMPSET deletion "{snmp_mods}"')
        output = subprocess.run(snmp_mods, shell=True, capture_output=True, check=True)
        output = output.stdout.decode("utf-8").split("\n")[:-1]
        logging.info(f"SNMPSET output: {output}")

        response = "Successfully deleted the NTCIP 1218 SNMPSET configuration"
        code = 200
    except subprocess.CalledProcessError as e:
        logging.error("Output: %s", e.stderr.decode("utf-8").split("\n"))
        output = e.stderr.decode("utf-8").split("\n")[:-1]

        logging.error(
            f"Encountered error while deleting NTCIP 1218 SNMP config: {output[-1]}"
        )
        response = snmperrorcheck.check_error_type(output[-1])
        code = 500

    return response, code
