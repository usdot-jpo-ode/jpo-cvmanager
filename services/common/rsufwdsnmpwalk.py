import subprocess
import logging
import common.snmpcredential as snmpcredential
import common.snmperrorcheck as snmperrorcheck
import common.snmpwalk_helpers as snmpwalk_helpers


# SNMP property to string name and processing function
# Supports SNMP RSU 4.1 Spec and NTCIP 1218 SNMP tables
prop_namevalue = {
    # These values are based off the RSU 4.1 Spec
    "iso.0.15628.4.1.7.1.2": ("Message Type", snmpwalk_helpers.message_type_rsu41),
    "iso.0.15628.4.1.7.1.3": ("IP", snmpwalk_helpers.ip_rsu41),
    "iso.0.15628.4.1.7.1.4": ("Port", int),
    "iso.0.15628.4.1.7.1.5": ("Protocol", snmpwalk_helpers.protocol),
    "iso.0.15628.4.1.7.1.6": ("RSSI", int),
    "iso.0.15628.4.1.7.1.7": ("Frequency", int),
    "iso.0.15628.4.1.7.1.8": ("Start DateTime", snmpwalk_helpers.startend_rsu41),
    "iso.0.15628.4.1.7.1.9": ("End DateTime", snmpwalk_helpers.startend_rsu41),
    "iso.0.15628.4.1.7.1.10": ("Forwarding", snmpwalk_helpers.fwdon),
    "iso.0.15628.4.1.7.1.11": ("Config Active", snmpwalk_helpers.active),
    # -----
    # These values are based off the NTCIP 1218 rsuReceivedMsgTable table
    "NTCIP1218-v01::rsuReceivedMsgPsid": (
        "Message Type",
        snmpwalk_helpers.message_type_ntcip1218,
    ),
    "NTCIP1218-v01::rsuReceivedMsgDestIpAddr": ("IP", snmpwalk_helpers.ip_ntcip1218),
    "NTCIP1218-v01::rsuReceivedMsgDestPort": ("Port", int),
    "NTCIP1218-v01::rsuReceivedMsgProtocol": ("Protocol", snmpwalk_helpers.protocol),
    "NTCIP1218-v01::rsuReceivedMsgRssi": ("RSSI", snmpwalk_helpers.rssi_ntcip1218),
    "NTCIP1218-v01::rsuReceivedMsgInterval": ("Frequency", int),
    "NTCIP1218-v01::rsuReceivedMsgDeliveryStart": (
        "Start DateTime",
        snmpwalk_helpers.startend_ntcip1218,
    ),
    "NTCIP1218-v01::rsuReceivedMsgDeliveryStop": (
        "End DateTime",
        snmpwalk_helpers.startend_ntcip1218,
    ),
    "NTCIP1218-v01::rsuReceivedMsgStatus": ("Config Active", snmpwalk_helpers.active),
    "NTCIP1218-v01::rsuReceivedMsgSecure": ("Full WSMP", snmpwalk_helpers.active),
    "NTCIP1218-v01::rsuReceivedMsgAuthMsgInterval": (
        "Security Filter",
        snmpwalk_helpers.active,
    ),
    # -----
    # These values are based off the NTCIP 1218 rsuXmitMsgFwdingTable table
    "NTCIP1218-v01::rsuXmitMsgFwdingPsid": (
        "Message Type",
        snmpwalk_helpers.message_type_ntcip1218,
    ),
    "NTCIP1218-v01::rsuXmitMsgFwdingDestIpAddr": ("IP", snmpwalk_helpers.ip_ntcip1218),
    "NTCIP1218-v01::rsuXmitMsgFwdingDestPort": ("Port", int),
    "NTCIP1218-v01::rsuXmitMsgFwdingProtocol": ("Protocol", snmpwalk_helpers.protocol),
    "NTCIP1218-v01::rsuXmitMsgFwdingDeliveryStart": (
        "Start DateTime",
        snmpwalk_helpers.startend_ntcip1218,
    ),
    "NTCIP1218-v01::rsuXmitMsgFwdingDeliveryStop": (
        "End DateTime",
        snmpwalk_helpers.startend_ntcip1218,
    ),
    "NTCIP1218-v01::rsuXmitMsgFwdingSecure": ("Full WSMP", snmpwalk_helpers.active),
    "NTCIP1218-v01::rsuXmitMsgFwdingStatus": ("Config Active", snmpwalk_helpers.active),
}


def snmpwalk_rsudsrcfwd(snmp_creds, rsu_ip):
    # Create the SNMPWalk command based on the road
    cmd = "snmpwalk -v 3 {auth} {rsuip} 1.0.15628.4.1.7".format(
        auth=snmpcredential.get_authstring(snmp_creds), rsuip=rsu_ip
    )
    output = ""
    try:
        # Example console output of a single configuration for message forwarding
        # iso.0.15628.4.1.7.1.2.1 = STRING: " "    #BSM
        # iso.0.15628.4.1.7.1.3.1 = Hex-STRING: 00 00 00 00 00 00 00 00 00 00 FF FF 0A 01 01 03    #10.1.1.3
        # iso.0.15628.4.1.7.1.4.1 = INTEGER: 46800    #port
        # iso.0.15628.4.1.7.1.5.1 = INTEGER: 2    #UDP
        # iso.0.15628.4.1.7.1.6.1 = INTEGER: -100    #rssi
        # iso.0.15628.4.1.7.1.7.1 = INTEGER: 1    #Forward every message
        # iso.0.15628.4.1.7.1.8.1 = Hex-STRING: 07 B2 0C 1F 11 00    # 1970-12-31 17:00
        # iso.0.15628.4.1.7.1.9.1 = Hex-STRING: 07 F4 0C 1F 11 00    # 2036-12-31 17:00
        # iso.0.15628.4.1.7.1.10.1 = INTEGER: 1    # turn this configuration on
        # iso.0.15628.4.1.7.1.11.1 = INTEGER: 1    # activate this index
        logging.info(f"Running snmpwalk: {cmd}")
        output = subprocess.run(cmd, shell=True, capture_output=True, check=True)
        output = output.stdout.decode("utf-8").split("\n")[:-1]
    except subprocess.CalledProcessError as e:
        output = e.stderr.decode("utf-8").split("\n")[:-1]
        logging.error(f"Encountered error while running snmpwalk: {output[-1]}")
        err_message = snmperrorcheck.check_error_type(output[-1])
        return {"RsuFwdSnmpwalk": err_message}, 500

    # Placeholder for possible other failed scenarios
    # A proper message forwarding configuration will be exactly 10 lines of output.
    # Any RSU with an output of less than 10 can be assumed to be an RSU with
    # no message forwarding configurations, or that some form error occurred in
    # reading an RSU's SNMP configuration data. In either scenario, simply returning an
    # empty response will suffice for the first implementation.
    if len(output) < 10:
        return {"RsuFwdSnmpwalk": {}}, 200

    snmp_config = {}

    # Parse each line of the output to build out readable SNMP configurations
    for line in output:
        # split configuration line into a property and value
        prop, raw_value = line.strip().split(" = ")
        # grab the configuration substring value for the property id while removing the index value
        prop_substr = prop[: -(len(prop.split(".")[-1]) + 1)]
        # grab the index value for the config
        key = prop.split(".")[-1]

        # If the index value already exists in the dict, ensure to add the new configuration value to it to build out a full SNMP configuration
        config = snmp_config[key] if key in snmp_config else {}
        # Assign the processed value of the the property to the readable property value and store the info based on the index value
        # The value is processed based on the type of property it is
        # The readable property name is based on the property
        config[prop_namevalue[prop_substr][0]] = prop_namevalue[prop_substr][1](
            raw_value.split(": ")[1]
        )
        snmp_config[key] = config

    return {"RsuFwdSnmpwalk": snmp_config}, 200


def snmpwalk_txrxmsg(snmp_creds, rsu_ip):
    snmpwalk_results = {"rsuReceivedMsgTable": {}, "rsuXmitMsgFwdingTable": {}}
    # Start with rsuReceivedMsgTable
    output = ""
    try:
        # Create the SNMPWalk command based on the road
        cmd = "snmpwalk -v 3 {auth} {rsuip} NTCIP1218-v01:rsuReceivedMsgTable".format(
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
        cmd = "snmpwalk -v 3 {auth} {rsuip} NTCIP1218-v01:rsuXmitMsgFwdingTable".format(
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


def get(request):
    logging.info(f"Running command, GET rsuFwdSnmpwalk")

    if request["snmp_version"] == "41":
        return snmpwalk_rsudsrcfwd(request["snmp_creds"], request["rsu_ip"])
    elif request["snmp_version"] == "1218":
        return snmpwalk_txrxmsg(request["snmp_creds"], request["rsu_ip"])
    else:
        return "Supported SNMP versions are currently only RSU 4.1 and NTCIP 1218", 501
