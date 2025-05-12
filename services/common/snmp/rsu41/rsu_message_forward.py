import subprocess
import logging
import common.snmp.snmpcredential as snmpcredential
import common.snmp.snmperrorcheck as snmperrorcheck
import common.util as util
import common.snmp.rsu_message_forward_helpers as rsu_message_forward_helpers
from datetime import datetime

# SNMP property to string name and processing function
# Supports SNMP RSU 4.1 Spec
prop_namevalue = {
    "iso.0.15628.4.1.7.1.2": (
        "Message Type",
        rsu_message_forward_helpers.message_type_rsu41,
    ),
    "iso.0.15628.4.1.7.1.3": ("IP", rsu_message_forward_helpers.ip_rsu41),
    "iso.0.15628.4.1.7.1.4": ("Port", int),
    "iso.0.15628.4.1.7.1.5": ("Protocol", rsu_message_forward_helpers.protocol),
    "iso.0.15628.4.1.7.1.6": ("RSSI", int),
    "iso.0.15628.4.1.7.1.7": ("Frequency", int),
    "iso.0.15628.4.1.7.1.8": (
        "Start DateTime",
        rsu_message_forward_helpers.startend_rsu41,
    ),
    "iso.0.15628.4.1.7.1.9": (
        "End DateTime",
        rsu_message_forward_helpers.startend_rsu41,
    ),
    "iso.0.15628.4.1.7.1.10": ("Forwarding", rsu_message_forward_helpers.fwdon),
    "iso.0.15628.4.1.7.1.11": ("Config Active", rsu_message_forward_helpers.active),
}


def ip_to_hex(ip):
    hex_dest_ip = ""
    for octet in ip.split("."):
        if len(hex(int(octet))[2:]) == 1:
            hex_dest_ip += "0"
        hex_dest_ip += hex(int(octet))[2:]
    return "00000000000000000000FFFF" + hex_dest_ip


# delta is in years
def hex_datetime(now, delta=0):
    # Regex to convert int to hex and to ensure a leading 0 if less than specified length
    regex = "{0:0{1}x}"
    hex = regex.format(now.year + delta, 4)
    hex += regex.format(now.month, 2)
    hex += regex.format(now.day, 2)
    hex += regex.format(now.hour, 2)
    hex += regex.format(now.minute, 2)
    return hex


def set_rsu_status(rsu_ip, snmp_creds, operate):
    try:
        if operate:
            logging.info(f"Changing RSU status to operate..")
            output = subprocess.run(
                f"snmpset -v 3 -t 5 {snmpcredential.get_authstring(snmp_creds)} {rsu_ip} RSU-MIB:rsuMode.0 i 4",
                shell=True,
                capture_output=True,
                check=True,
            )
            output = output.stdout.decode("utf-8").split("\n")[:-1]
        else:
            logging.info(f"Changing RSU status to standby..")
            output = subprocess.run(
                f"snmpset -v 3 -t 5 {snmpcredential.get_authstring(snmp_creds)} {rsu_ip} RSU-MIB:rsuMode.0 i 2",
                shell=True,
                capture_output=True,
                check=True,
            )
            output = output.stdout.decode("utf-8").split("\n")[:-1]
        logging.info(f"RSU status change output: {output}")
        return "success"
    except subprocess.CalledProcessError as e:
        output = e.stderr.decode("utf-8").split("\n")[:-1]
        logging.error(f"Encountered error while changing RSU status: {output[-1]}")
        err_message = snmperrorcheck.check_error_type(output[-1])
        return err_message


def perform_snmp_mods(snmp_mods):
    for snmp_mod in snmp_mods:
        # Perform configuration
        logging.info(f'Running SNMPSET "{snmp_mod}"')
        output = subprocess.run(snmp_mod, shell=True, capture_output=True, check=True)
        output = output.stdout.decode("utf-8").split("\n")[:-1]
        logging.info(f"SNMPSET output: {output}")


def get(rsu_ip, snmp_creds):
    # Create the SNMPWalk command based on the road
    cmd = "snmpwalk -v 3 -t 5 {auth} {rsuip} 1.0.15628.4.1.7".format(
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


# Configures message forwarding over SNMP based on the RSU v4.1 specification
def set(
    rsu_ip, manufacturer, snmp_creds, dest_ip, udp_port, rsu_index, psid, raw=False
):
    try:
        # Put RSU in standby
        rsu_mod_result = set_rsu_status(rsu_ip, snmp_creds, operate=False)
        if rsu_mod_result != "success":
            return rsu_mod_result, 500

        # Create a hex version of destIP using the specified endian type
        hex_dest_ip = ip_to_hex(dest_ip)

        logging.info("Running SNMP config on {}".format(rsu_ip))

        snmp_mods = []
        authstring = snmpcredential.get_authstring(snmp_creds)
        # Raw is for running the SNMP commands without the RSU 4.1 spec
        if not raw:
            snmp_mod = "snmpset -v 3 -t 5 {auth} {rsuip} ".format(
                auth=authstring, rsuip=rsu_ip
            )
            snmp_mod += "RSU-MIB:rsuDsrcFwdStatus.{index} i 4 ".format(index=rsu_index)
            snmp_mod += "RSU-MIB:rsuDsrcFwdPsid.{index} x {msgpsid} ".format(
                index=rsu_index, msgpsid=psid
            )
            snmp_mod += "RSU-MIB:rsuDsrcFwdDestIpAddr.{index} x {destip} ".format(
                index=rsu_index, destip=hex_dest_ip
            )
            snmp_mod += "RSU-MIB:rsuDsrcFwdDestPort.{index} i {port} ".format(
                index=rsu_index, port=udp_port
            )
            snmp_mod += "RSU-MIB:rsuDsrcFwdProtocol.{index} i 2 ".format(
                index=rsu_index
            )
            snmp_mod += "RSU-MIB:rsuDsrcFwdRssi.{index} i -100 ".format(index=rsu_index)
            snmp_mod += "RSU-MIB:rsuDsrcFwdMsgInterval.{index} i 1 ".format(
                index=rsu_index
            )
            # Start datetime, hex of the current time (timezone based on the manufacturer)
            if manufacturer == "Commsignia":
                # Configured timezone
                now = util.utc2tz(datetime.now())
            else:
                # UTC
                now = datetime.now()
            snmp_mod += "RSU-MIB:rsuDsrcFwdDeliveryStart.{index} x {dt} ".format(
                index=rsu_index, dt=hex_datetime(now)
            )
            # Stop datetime, hex of the current time + 10 years in the future
            snmp_mod += "RSU-MIB:rsuDsrcFwdDeliveryStop.{index} x {dt} ".format(
                index=rsu_index, dt=hex_datetime(now, 10)
            )
            snmp_mod += "RSU-MIB:rsuDsrcFwdEnable.{index} i 1".format(index=rsu_index)
            snmp_mods.append(snmp_mod)
        else:
            # Commands must be run individually to be run without the RSU 4.1 spec
            # This must be done when configuring MAP, SSM and SRM because their PSIDs are not compatible with the RSU 4.1 spec MIB
            snmp_mods.append(
                "snmpset -v 3 -t 5 {auth} {rsuip} 1.0.15628.4.1.7.1.2.{index} x {msgpsid}".format(
                    auth=authstring, rsuip=rsu_ip, index=rsu_index, msgpsid=psid
                )
            )
            snmp_mods.append(
                "snmpset -v 3 -t 5 {auth} {rsuip} 1.0.15628.4.1.7.1.3.{index} x {destip}".format(
                    auth=authstring, rsuip=rsu_ip, index=rsu_index, destip=hex_dest_ip
                )
            )
            snmp_mods.append(
                "snmpset -v 3 -t 5 {auth} {rsuip} 1.0.15628.4.1.7.1.4.{index} i {port}".format(
                    auth=authstring, rsuip=rsu_ip, index=rsu_index, port=udp_port
                )
            )
            snmp_mods.append(
                "snmpset -v 3 -t 5 {auth} {rsuip} 1.0.15628.4.1.7.1.5.{index} i 2".format(
                    auth=authstring, rsuip=rsu_ip, index=rsu_index
                )
            )
            snmp_mods.append(
                "snmpset -v 3 -t 5 {auth} {rsuip} 1.0.15628.4.1.7.1.6.{index} i -100".format(
                    auth=authstring, rsuip=rsu_ip, index=rsu_index
                )
            )
            snmp_mods.append(
                "snmpset -v 3 -t 5 {auth} {rsuip} 1.0.15628.4.1.7.1.7.{index} i 1".format(
                    auth=authstring, rsuip=rsu_ip, index=rsu_index
                )
            )
            # Start datetime, hex of the current time (timezone based on the manufacturer)
            if manufacturer == "Commsignia":
                # Configured timezone
                now = util.utc2tz(datetime.now())
            else:
                # UTC
                now = datetime.now()
            snmp_mods.append(
                "snmpset -v 3 -t 5 {auth} {rsuip} 1.0.15628.4.1.7.1.8.{index} x {dt}".format(
                    auth=authstring, rsuip=rsu_ip, index=rsu_index, dt=hex_datetime(now)
                )
            )
            # Stop datetime, hex of the current time + 10 years in the future
            snmp_mods.append(
                "snmpset -v 3 -t 5 {auth} {rsuip} 1.0.15628.4.1.7.1.9.{index} x {dt}".format(
                    auth=authstring,
                    rsuip=rsu_ip,
                    index=rsu_index,
                    dt=hex_datetime(now, 10),
                )
            )
            snmp_mods.append(
                "snmpset -v 3 -t 5 {auth} {rsuip} 1.0.15628.4.1.7.1.10.{index} i 1".format(
                    auth=authstring, rsuip=rsu_ip, index=rsu_index
                )
            )
            snmp_mods.append(
                "snmpset -v 3 -t 5 {auth} {rsuip} 1.0.15628.4.1.7.1.11.{index} i 4".format(
                    auth=authstring, rsuip=rsu_ip, index=rsu_index
                )
            )

        perform_snmp_mods(snmp_mods)
        response = "Successfully completed the rsuDsrcFwd SNMPSET configuration"
        code = 200
    except subprocess.CalledProcessError as e:
        output = e.stderr.decode("utf-8").split("\n")[:-1]
        logging.error(f"Encountered error while modifying RSU SNMP: {output[-1]}")
        response = snmperrorcheck.check_error_type(output[-1])
        code = 500
    finally:
        # Put RSU in run mode, this doesn't need to be captured
        # If the previous commands work, this should work
        # If the previous commands fail, this will probably fail
        # and we want to preserve the previous failure as the return message
        set_rsu_status(rsu_ip, snmp_creds, operate=True)

    return response, code


def delete(rsu_ip, snmp_creds, rsu_index):
    try:
        # Put RSU in standby
        rsu_mod_result = set_rsu_status(rsu_ip, snmp_creds, operate=False)
        if rsu_mod_result != "success":
            return rsu_mod_result, 500

        snmp_mods = "snmpset -v 3 -t 5 {auth} {rsuip} ".format(
            auth=snmpcredential.get_authstring(snmp_creds), rsuip=rsu_ip
        )
        snmp_mods += "RSU-MIB:rsuDsrcFwdStatus.{index} i 6 ".format(index=rsu_index)

        # Perform configurations
        logging.info(f'Running SNMPSET deletion "{snmp_mods}"')
        output = subprocess.run(snmp_mods, shell=True, capture_output=True, check=True)
        output = output.stdout.decode("utf-8").split("\n")[:-1]
        logging.info(f"SNMPSET output: {output}")

        response = "Successfully deleted the RSU 4.1 SNMPSET configuration"
        code = 200
    except subprocess.CalledProcessError as e:
        output = e.stderr.decode("utf-8").split("\n")[:-1]
        logging.error(
            f"Encountered error while deleting the RSU 4.1 SNMP config: {output[-1]}"
        )
        response = snmperrorcheck.check_error_type(output[-1])
        code = 500
    finally:
        # Put RSU in run mode, this doesn't need to be captured
        # If the previous commands work, this should work
        # If the previous commands fail, this will probably fail
        # and we want to preserve the previous failure as the return message
        set_rsu_status(rsu_ip, snmp_creds, operate=True)

    return response, code
