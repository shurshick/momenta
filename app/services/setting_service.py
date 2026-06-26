from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from app.models.setting import Setting

_cache: dict[str, str] = {}


async def get_setting(db: AsyncSession, key: str, default: str | None = None) -> str | None:
    if key in _cache:
        return _cache[key]
    result = await db.execute(select(Setting).where(Setting.key == key))
    row = result.scalar_one_or_none()
    if row:
        _cache[key] = row.value
        return row.value
    return default


async def set_setting(db: AsyncSession, key: str, value: str) -> None:
    result = await db.execute(select(Setting).where(Setting.key == key))
    row = result.scalar_one_or_none()
    if row:
        row.value = value
    else:
        row = Setting(key=key, value=value)
        db.add(row)
    await db.commit()
    _cache[key] = value


async def get_all_settings(db: AsyncSession) -> dict[str, str]:
    result = await db.execute(select(Setting))
    rows = result.scalars().all()
    return {row.key: row.value for row in rows}


def invalidate_cache(key: str | None = None) -> None:
    if key:
        _cache.pop(key, None)
    else:
        _cache.clear()
