import datetime
from src import util


def test_format_date_utc():
    dt = datetime.datetime(2020, 1, 1, 1, 1, 1)
    datetimeString = dt.strftime("%Y-%m-%dT%H:%M:%S")
    assert util.format_date_utc(datetimeString) == "2020-01-01T08:01:01"


def test_format_date_utc_failure():
    d = False
    assert util.format_date_utc(d) == None
    d = None
    assert util.format_date_utc(d) == None


def test_format_date_denver():
    dt = datetime.datetime(2020, 1, 1, 1, 1, 1)
    datetimeString = dt.strftime("%Y-%m-%dT%H:%M:%S")
    assert util.format_date_denver(datetimeString) == "01/01/2020 01:01:01 AM"


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
