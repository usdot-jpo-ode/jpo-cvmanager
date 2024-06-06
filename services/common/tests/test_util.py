import datetime

import pytz
from common import util


def test_format_date_utc():
    dt = datetime.datetime(2020, 1, 1, 1, 1, 1)
    datetimeString = dt.strftime("%Y-%m-%dT%H:%M:%S")
    expected_dt = datetime.datetime.strftime(
        dt.astimezone(pytz.UTC), "%Y-%m-%dT%I:%M:%S"
    )
    assert util.format_date_utc(datetimeString) == expected_dt


def test_format_date_utc_failure():
    d = False
    assert util.format_date_utc(d) == None
    d = None
    assert util.format_date_utc(d) == None


def test_format_date_denver():
    dt = datetime.datetime(2020, 1, 1, 1, 1, 1)
    expected_dt = datetime.datetime.strftime(
        dt.astimezone(pytz.timezone("America/Denver")), "%m/%d/%Y %I:%M:%S %p"
    )
    datetimeString = dt.strftime("%Y-%m-%dT%H:%M:%S")
    assert util.format_date_denver(datetimeString) == expected_dt


def test_format_date_denver_failure():
    d = False
    assert util.format_date_denver(d) == None
    d = None
    assert util.format_date_denver(d) == None


def test_utc2tz():
    dt = datetime.datetime(2020, 1, 1, 1, 1, 1)
    expected_tz_d = dt.astimezone(datetime.timezone(datetime.timedelta(hours=-7)))
    assert util.utc2tz(dt) == expected_tz_d


def test_utc2tz_failure():
    d = False
    assert util.utc2tz(d) == None
    d = None
    assert util.utc2tz(d) == None


def test_validation_file_type():
    file_name = "test.tar"
    file_extension = ".tar"
    validation = util.validate_file_type(file_name, file_extension)
    assert validation == True


def test_validation_file_type_no_extension():
    file_name = "test.tar"
    validation = util.validate_file_type(file_name)
    assert validation == True


def test_validation_file_type_failure():
    file_name = "test.blob"
    file_extension = ".tar"
    validation = util.validate_file_type(file_name, file_extension)
    assert validation == False
