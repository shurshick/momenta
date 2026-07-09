from datetime import date, datetime, time, timezone
from zoneinfo import ZoneInfo, ZoneInfoNotFoundError

from app.config import settings


def app_timezone() -> ZoneInfo | timezone:
    try:
        return ZoneInfo(settings.app_timezone)
    except ZoneInfoNotFoundError:
        return timezone.utc


def app_today() -> date:
    return datetime.now(app_timezone()).date()


def end_of_app_day(d: date | None = None) -> datetime:
    target = d or app_today()
    return datetime.combine(target, time(23, 59, 59), tzinfo=app_timezone())


def seconds_until_end_of_app_day(d: date | None = None, grace_seconds: int = 3600) -> int:
    now = datetime.now(app_timezone())
    ends_at = end_of_app_day(d)
    seconds = int((ends_at - now).total_seconds()) + grace_seconds
    return max(seconds, 60)


def parse_cursor_datetime(cursor: str | None) -> datetime | None:
    if not cursor:
        return None
    value = cursor.strip()
    if not value:
        return None
    if value.endswith("Z"):
        value = value[:-1] + "+00:00"
    return datetime.fromisoformat(value)
