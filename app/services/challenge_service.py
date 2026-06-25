import uuid
from datetime import date, datetime, timezone
from typing import Optional
from sqlalchemy import select, func
from sqlalchemy.ext.asyncio import AsyncSession
from app.models.challenge import Challenge
from app.models.post import Post
from app.services.redis_service import set_today_challenge, get_today_challenge


async def get_today_challenge_data(db: AsyncSession, user_id: Optional[str] = None) -> dict:
    today = date.today()
    cached = await get_today_challenge(today)
    if cached:
        if user_id:
            cached["user_posted"] = await _has_user_posted(db, user_id, today)
        return cached
    result = await db.execute(
        select(Challenge).where(Challenge.challenge_date == today, Challenge.status == "active")
    )
    challenge = result.scalar_one_or_none()
    if not challenge:
        challenge = await _create_fallback_challenge(db, today)
    participants = await db.execute(
        select(func.count(Post.id)).where(Post.challenge_date == today, Post.status == "active")
    )
    participants_count = participants.scalar() or 0
    ends_at = datetime(today.year, today.month, today.day, 23, 59, 59, tzinfo=timezone.utc)
    data = {
        "id": str(challenge.id),
        "date": today.isoformat(),
        "title": challenge.title_ru,
        "description": challenge.description_ru,
        "ends_at": ends_at.isoformat(),
        "user_posted": False,
        "participants_count": participants_count,
    }
    if user_id:
        data["user_posted"] = await _has_user_posted(db, user_id, today)
    ttl = int((ends_at - datetime.now(timezone.utc)).total_seconds()) + 3600
    await set_today_challenge(today, data, ttl)
    return data


async def get_challenge_by_id(db: AsyncSession, challenge_id: uuid.UUID) -> Optional[Challenge]:
    result = await db.execute(select(Challenge).where(Challenge.id == challenge_id))
    return result.scalar_one_or_none()


async def get_challenge_by_date(db: AsyncSession, d: date) -> Optional[Challenge]:
    result = await db.execute(select(Challenge).where(Challenge.challenge_date == d))
    return result.scalar_one_or_none()


async def create_challenge(db: AsyncSession, challenge_date: date, title_ru: str, description_ru: Optional[str] = None,
                           title_en: Optional[str] = None, description_en: Optional[str] = None,
                           cover_url: Optional[str] = None, status: str = "draft",
                           created_by: Optional[uuid.UUID] = None) -> Challenge:
    existing = await get_challenge_by_date(db, challenge_date)
    if existing:
        raise ValueError(f"Challenge for {challenge_date} already exists")
    challenge = Challenge(
        id=uuid.uuid4(),
        challenge_date=challenge_date,
        title_ru=title_ru,
        description_ru=description_ru,
        title_en=title_en,
        description_en=description_en,
        cover_url=cover_url,
        status=status,
        created_by=created_by,
    )
    db.add(challenge)
    await db.commit()
    await db.refresh(challenge)
    return challenge


async def _create_fallback_challenge(db: AsyncSession, today: date) -> Challenge:
    existing = await get_challenge_by_date(db, today)
    if existing:
        return existing
    challenge = Challenge(
        id=uuid.uuid4(),
        challenge_date=today,
        title_ru="Момент дня",
        description_ru="Запечатли свой момент сегодня",
        status="active",
    )
    db.add(challenge)
    await db.commit()
    await db.refresh(challenge)
    return challenge


async def _has_user_posted(db: AsyncSession, user_id: str, d: date) -> bool:
    from app.services.redis_service import check_user_posted
    if await check_user_posted(user_id, d):
        return True
    result = await db.execute(
        select(Post).where(Post.user_id == uuid.UUID(user_id), Post.challenge_date == d, Post.status.in_(["active", "processing"]))
    )
    return result.scalar_one_or_none() is not None
