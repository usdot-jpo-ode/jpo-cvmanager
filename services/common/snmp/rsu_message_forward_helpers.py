import common.util as util
import logging
from enum import Enum

# Delta is in years
def hex_datetime(now, delta=0):
    """
    Converts a datetime object to a hexadecimal string representation.

    Args:
        now (datetime.datetime): The current datetime object to convert.
        delta (int, optional): An integer value to add to the year component before conversion. Defaults to 0.

    Returns:
        str: A hexadecimal string representing the date and time in the format:
             YYYYMMDDHHmm, where each component is zero-padded and represented in hexadecimal.
    """
    regex = "{0:0{1}x}"
    hex_string = regex.format(now.year + delta, 4)
    hex_string += regex.format(now.month, 2)
    hex_string += regex.format(now.day, 2)
    hex_string += regex.format(now.hour, 2)
    hex_string += regex.format(now.minute, 2)
    return hex_string


# Check for which J2735 PSID matches val
# BSM  - 20
# SPaT - 8002
# TIM  - 8003
# MAP  - E0000017
# SSM  - E0000015
# SRM  - E0000016
def message_type_rsu41(val):
    # Various formats PSIDs have been observed to be returned in
    # Can depend upon vendor or even firmware version of the same vendor's RSU
    if val == '" "' or val == "00 00 00 20" or val == "00 00 00 32":
        return "BSM"
    elif val == "00 00 80 02" or val == "80 02" or val == "00 03 27 70":
        return "SPaT"
    elif val == "00 00 80 03" or val == "80 03" or val == "00 03 27 71":
        return "TIM"
    elif val == "E0 00 00 17" or val == "37 58 09 64 07":
        return "MAP"
    elif val == "E0 00 00 15" or val == "37 58 09 64 06":
        return "SSM"
    elif val == "E0 00 00 16" or val == "37 58 09 64 05":
        return "SRM"
    return "Other"


def message_type_ntcip1218(val):
    if val == "20000000":
        return "BSM"
    elif val == "80020000":
        return "SPaT"
    elif val == "80030000":
        return "TIM"
    elif val.lower() == "e0000017":
        return "MAP"
    elif val.lower() == "e0000015":
        return "SSM"
    elif val.lower() == "e0000016":
        return "SRM"
    return "Other"


# Little endian
def ip_rsu41(val):
    hex = val.split()
    ipaddr = (
        f"{str(int(hex[-4], 16))}."
        f"{str(int(hex[-3], 16))}."
        f"{str(int(hex[-2], 16))}."
        f"{str(int(hex[-1], 16))}"
    )
    return ipaddr


def ip_ntcip1218(val):
    return val.strip()


def protocol(val):
    if val == "1" or val == "tcp(1)":
        return "TCP"
    elif val == "2" or val == "udp(2)":
        return "UDP"
    return "Other"


def rssi_ntcip1218(val):
    return int(val.split(" ")[0])


def fwdon(val):
    if val == "1":
        return "On"
    return "Off"


def active(val):
    # This value represents an active state
    # Currently 1 and 4 are supported
    # 1 - active
    # 4 - create (represents active to older Commsignia models)
    # active(1) - active for NTCIP 1218 response
    if val == "1" or val == "4" or val == "active(1)":
        return "Enabled"
    return "Disabled"


def startend_rsu41(val):
    hex = val.split()
    year = str(int(hex[0] + hex[1], 16))
    month = str(int(hex[2], 16))
    month = month if len(month) == 2 else "0" + month
    day = str(int(hex[3], 16))
    day = day if len(day) == 2 else "0" + day
    hour = str(int(hex[4], 16))
    hour = hour if len(hour) == 2 else "0" + hour
    min = str(int(hex[5], 16))
    min = min if len(min) == 2 else "0" + min
    return f"{year}-{month}-{day} {hour}:{min}"


def startend_ntcip1218(val):
    date, time = val.split(",")
    # Parse out the year, month and day. Pad 0s if necessary
    year, month, day = date.split("-")
    month = "0" + month if len(month) == 1 else month
    day = "0" + day if len(day) == 1 else day
    # Parse out the hours, minute and second. Pad 0s if necessary
    hour, minute = time.split(":")[0], time.split(":")[1]
    hour = "0" + hour if len(hour) == 1 else hour
    minute = "0" + minute if len(minute) == 1 else minute
    # Return the processed datetime string
    return f"{year}-{month}-{day} {hour}:{minute}"


class MsgFwdType(Enum):
    DSRC = "rsuDsrcFwd"
    RECEIVED = "rsuReceivedMsg"
    XMIT = "rsuXmitMsgFwding"

class TableNames(Enum):
    RECEIVED = "rsuReceivedMsgTable"
    XMIT = "rsuXmitMsgFwdingTable"

def format_snmp_msgfwd_configs(config_list, rsu_ip=None):
    """
    Formats and organizes SNMP message forwarding configurations into a structured dictionary format.

    This function processes a list of configuration rows, restructures them into a dictionary
    compatible with the SNMP walk response format, and classifies the configurations based on their
    message forwarding types. It handles specific message forwarding types such as DSRC, RECEIVED,
    and XMIT, ensuring that related objects (RX and TX) are available for complete NTCIP 1218
    configuration support. Unknown message forwarding types are logged with a warning.

    :param config_list: A list of configuration dictionaries where each dictionary represents data
        for a single configuration row.
    :type config_list: list[dict]
    :param rsu_ip: Optional IPv4 address of the RSU, used for logging purposes. Defaults to None.
    :type rsu_ip: str | None
    :return: A dictionary containing organized SNMP message forwarding configurations.
    :rtype: dict
    """
    msgfwd_configs_dict = {}
    for row in config_list:
        config_row = {
            "Message Type": row["message_type"].upper(),
            "IP": row["dest_ipv4"],
            "Port": row["dest_port"],
            "Start DateTime": util.format_date_denver_iso(row["start_datetime"]),
            "End DateTime": util.format_date_denver_iso(row["end_datetime"]),
            "Config Active": active(row["active"]),
            "Full WSMP": active(row["security"]),
        }

        # Based on the value of msgfwd_type, store the configuration data to match the response object of rsufwdsnmpwalk
        msgfwd_type_value = row["msgfwd_type"]
        if msgfwd_type_value.upper() == MsgFwdType.DSRC.value.upper():
            msgfwd_configs_dict[row["snmp_index"]] = config_row
        elif msgfwd_type_value.upper() == MsgFwdType.RECEIVED.value.upper():
            msgfwd_configs_dict.setdefault(TableNames.RECEIVED.value, {})[row["snmp_index"]] = config_row
        elif msgfwd_type_value.upper() == MsgFwdType.XMIT.value.upper():
            msgfwd_configs_dict.setdefault(TableNames.XMIT.value, {})[row["snmp_index"]] = config_row
        else:
            rsu_info = f" for RSU '{rsu_ip}'" if rsu_ip else ""
            logging.warning(
                f"Encountered unknown message forwarding configuration type '{msgfwd_type_value}'{rsu_info}"
            )

    # Make sure both RX and TX objects are available if the RSU ends up having NTCIP 1218 configurations
    if TableNames.RECEIVED.value in msgfwd_configs_dict and TableNames.XMIT.value not in msgfwd_configs_dict:
        msgfwd_configs_dict[TableNames.XMIT.value] = {}
    elif TableNames.XMIT.value in msgfwd_configs_dict and TableNames.RECEIVED.value not in msgfwd_configs_dict:
        msgfwd_configs_dict[TableNames.RECEIVED.value] = {}

    return {"RsuFwdSnmpwalk": msgfwd_configs_dict}
