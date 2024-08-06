from dateutil.parser import parse
import pytz
import os
import logging


# expects datetime string
def format_date_utc(d, type="string"):
    if not d:
        return None
    tmp = parse(d)
    utc_tz = tmp.astimezone(pytz.UTC)

    if type.upper() == "STRING":
        return utc_tz.strftime("%Y-%m-%dT%H:%M:%S")
    elif type.upper() == "DATETIME":
        return utc_tz
    else:
        return None


# expects datetime string
def format_date_denver(d):
    if not d:
        return None
    tmp = parse(d)
    denver_tz = tmp.astimezone(pytz.timezone(os.getenv("TIMEZONE", "America/Denver")))
    return denver_tz.strftime("%m/%d/%Y %I:%M:%S %p")


# expects datetime string
def format_date_denver_iso(d):
    if not d:
        return None
    tmp = parse(d)
    denver_tz = tmp.astimezone(pytz.timezone(os.getenv("TIMEZONE", "America/Denver")))
    return denver_tz.isoformat()


# expects datetime, utilizes environment variable to custom timezone
def utc2tz(d):
    if not d:
        return None
    tz_d = d.astimezone(pytz.timezone(os.getenv("TIMEZONE", "America/Denver")))
    return tz_d


def validate_file_type(file_name, extension=".tar"):
    """Validate the file type of the file to be downloaded.

    Args:
        file_name (str): The name of the file to be downloaded.
        extension (str): The file extension to validate against.
    """
    if not file_name.endswith(extension):
        logging.error(
            f'Unsupported file type for storage object {file_name}. Only "{extension}" files are supported.'
        )
        return False
    return True
