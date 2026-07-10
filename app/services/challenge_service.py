import logging
import random
import uuid
from datetime import date, datetime
from typing import Optional

from sqlalchemy import desc, func, select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.challenge import Challenge
from app.models.post import Post
from app.services.challenge_templates import (
    AUTO_CHALLENGE_TEMPLATES,
    FALLBACK_CHALLENGE,
    ChallengeTemplate,
)
from app.utils.dates import app_today, end_of_app_day

logger = logging.getLogger(__name__)


def current_app_date() -> date:
    return app_today()


async def get_today_challenge_data(db: AsyncSession, user_id: Optional[str] = None) -> dict:
    today = current_app_date()
    challenge = await get_or_create_today_challenge(db, today)

    participants = await db.execute(
        select(func.count(func.distinct(Post.user_id))).where(
            Post.challenge_date == today,
            Post.status == "active",
        )
    )
    participants_count = participants.scalar() or 0
    user_posted = await _has_user_posted(db, user_id, today) if user_id else False
    ends_at = end_of_app_day(today)

    return _challenge_to_today_payload(
        challenge=challenge,
        today=today,
        participants_count=participants_count,
        user_posted=user_posted,
        ends_at=ends_at,
    )


async def get_or_create_today_challenge(
    db: AsyncSession, challenge_date: date | None = None
) -> Challenge:
    today = challenge_date or current_app_date()
    existing = await get_active_challenge_by_date(db, today)
    if existing:
        return existing

    challenge = await generate_auto_challenge_for_date(db, today)
    db.add(challenge)
    try:
        await db.commit()
        await db.refresh(challenge)
        logger.info("Auto challenge created for %s: %s", today.isoformat(), challenge.title_ru)
        return challenge
    except IntegrityError:
        await db.rollback()
        raced = await get_active_challenge_by_date(db, today) or await get_challenge_by_date(
            db, today
        )
        if raced:
            return raced
        raise


async def generate_auto_challenge_for_date(db: AsyncSession, challenge_date: date) -> Challenge:
    template = await _select_template(db, challenge_date)
    return Challenge(
        id=uuid.uuid4(),
        challenge_date=challenge_date,
        title_ru=template.title,
        description_ru=template.description,
        prompt_ru=template.prompt,
        status="active",
        source="auto",
    )


async def get_challenge_by_id(db: AsyncSession, challenge_id: uuid.UUID) -> Optional[Challenge]:
    result = await db.execute(select(Challenge).where(Challenge.id == challenge_id))
    return result.scalar_one_or_none()


async def get_active_challenge_by_date(db: AsyncSession, d: date) -> Optional[Challenge]:
    result = await db.execute(
        select(Challenge).where(Challenge.challenge_date == d, Challenge.status == "active")
    )
    return result.scalar_one_or_none()


async def get_challenge_by_date(db: AsyncSession, d: date) -> Optional[Challenge]:
    result = await db.execute(select(Challenge).where(Challenge.challenge_date == d))
    return result.scalar_one_or_none()


async def create_challenge(
    db: AsyncSession,
    challenge_date: date,
    title_ru: str,
    description_ru: Optional[str] = None,
    title_en: Optional[str] = None,
    description_en: Optional[str] = None,
    cover_url: Optional[str] = None,
    status: str = "draft",
    created_by: Optional[uuid.UUID] = None,
) -> Challenge:
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
        source="manual",
        created_by=created_by,
    )
    db.add(challenge)
    await db.commit()
    await db.refresh(challenge)
    return challenge


async def _select_template(db: AsyncSession, challenge_date: date) -> ChallengeTemplate:
    if not AUTO_CHALLENGE_TEMPLATES:
        return FALLBACK_CHALLENGE

    result = await db.execute(
        select(Challenge.title_ru)
        .where(
            Challenge.source == "auto",
            Challenge.challenge_date < challenge_date,
        )
        .order_by(desc(Challenge.challenge_date))
        .limit(7)
    )
    recent_titles = {row[0] for row in result.all()}
    candidates = [
        template for template in AUTO_CHALLENGE_TEMPLATES if template.title not in recent_titles
    ]
    return random.choice(candidates or AUTO_CHALLENGE_TEMPLATES)


def _challenge_to_today_payload(
    challenge: Challenge,
    today: date,
    participants_count: int,
    user_posted: bool,
    ends_at: datetime,
) -> dict:
    return {
        "id": str(challenge.id),
        "date": today.isoformat(),
        "challenge_date": today.isoformat(),
        "title": challenge.title_ru,
        "description": challenge.description_ru,
        "prompt": challenge.prompt_ru,
        "source": challenge.source,
        "ends_at": ends_at.isoformat(),
        "user_posted": user_posted,
        "participants_count": participants_count,
    }


async def _has_user_posted(db: AsyncSession, user_id: str, d: date) -> bool:
    result = await db.execute(
        select(Post.id)
        .where(
            Post.user_id == uuid.UUID(user_id),
            Post.challenge_date == d,
            Post.status.in_(["active", "processing", "uploading"]),
        )
        .limit(1)
    )
    return result.scalar_one_or_none() is not None
