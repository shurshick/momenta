import uuid
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
    try:
        return datetime.fromisoformat(value)
    except ValueError:
        return None


def parse_post_cursor(cursor: str | None) -> tuple[datetime, uuid.UUID | None] | None:
    if not cursor:
        return None
    value = cursor.strip()
    if not value:
        return None
    if "|" not in value:
        parsed_at = parse_cursor_datetime(value)
        return (parsed_at, None) if parsed_at else None
    created_at_text, post_id = value.rsplit("|", 1)
    parsed_at = parse_cursor_datetime(created_at_text)
    if not parsed_at:
        return None
    try:
        return parsed_at, uuid.UUID(post_id)
    except ValueError:
        return None
