import subprocess
import logging
import common.snmp.snmpcredential as snmpcredential
import common.snmp.snmperrorcheck as snmperrorcheck
import common.snmp.rsu_message_forwarding_helpers as rsu_message_forwarding_helpers

# SNMP property to string name and processing function
# Supports SNMP RSU 4.1 Spec
prop_namevalue = {
    "iso.0.15628.4.1.7.1.2": (
        "Message Type",
        rsu_message_forwarding_helpers.message_type_rsu41,
    ),
    "iso.0.15628.4.1.7.1.3": ("IP", rsu_message_forwarding_helpers.ip_rsu41),
    "iso.0.15628.4.1.7.1.4": ("Port", int),
    "iso.0.15628.4.1.7.1.5": ("Protocol", rsu_message_forwarding_helpers.protocol),
    "iso.0.15628.4.1.7.1.6": ("RSSI", int),
    "iso.0.15628.4.1.7.1.7": ("Frequency", int),
    "iso.0.15628.4.1.7.1.8": (
        "Start DateTime",
        rsu_message_forwarding_helpers.startend_rsu41,
    ),
    "iso.0.15628.4.1.7.1.9": (
        "End DateTime",
        rsu_message_forwarding_helpers.startend_rsu41,
    ),
    "iso.0.15628.4.1.7.1.10": ("Forwarding", rsu_message_forwarding_helpers.fwdon),
    "iso.0.15628.4.1.7.1.11": ("Config Active", rsu_message_forwarding_helpers.active),
}


def get(rsu_ip, snmp_creds):
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
