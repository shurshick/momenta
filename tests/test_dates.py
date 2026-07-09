from datetime import date, datetime
from zoneinfo import ZoneInfo

from app.utils.dates import parse_cursor_datetime, seconds_until_end_of_app_day


def test_parse_cursor_datetime_accepts_z_suffix():
    parsed = parse_cursor_datetime("2026-07-09T10:00:00Z")

    assert parsed == datetime(2026, 7, 9, 10, 0, tzinfo=ZoneInfo("UTC"))


def test_seconds_until_end_of_app_day_is_positive():
    ttl = seconds_until_end_of_app_day(date.today())

    assert ttl > 0
