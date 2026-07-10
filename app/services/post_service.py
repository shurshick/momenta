import logging
import uuid
from datetime import date, datetime, timedelta, timezone
from typing import Optional

from sqlalchemy import desc, func, select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.post import Post
from app.models.reaction import Reaction
from app.services.counter_service import CounterService
from app.services.redis_service import (  # noqa: F401
    add_to_feed,
    increment_counter,
    mark_user_posted,
)
from app.services.setting_service import get_setting
from app.utils.dates import parse_cursor_datetime

logger = logging.getLogger(__name__)

DEFAULT_DELETE_WINDOW_MINUTES = 60


async def assert_can_create_post(
    db: AsyncSession,
    user_id: uuid.UUID,
    challenge_date: date,
) -> None:
    limit_str = await get_setting(db, "daily_post_limit", "1")
    try:
        daily_limit = int(limit_str)
    except (ValueError, TypeError):
        daily_limit = 1

    if daily_limit <= 0:
        logger.info("Daily limit disabled for user=%s", user_id)
        return

    today_count = (
        await db.execute(
            select(func.count(Post.id)).where(
                Post.user_id == user_id,
                Post.challenge_date == challenge_date,
                Post.status.in_(["active", "processing", "uploading"]),
            )
        )
    ).scalar() or 0

    logger.info(
        "Daily limit check: user=%s date=%s count=%s limit=%s",
        user_id,
        challenge_date,
        today_count,
        daily_limit,
    )
    if today_count >= daily_limit:
        raise ValueError(f"Лимит {daily_limit} моментов в день исчерпан")


async def create_post(
    db: AsyncSession,
    user_id: uuid.UUID,
    challenge_id: uuid.UUID,
    challenge_date: date,
    media_type: str,
    original_url: str,
    preview_url: Optional[str] = None,
    thumb_url: Optional[str] = None,
    caption: Optional[str] = None,
    country: Optional[str] = None,
    city: Optional[str] = None,
    post_id: uuid.UUID | None = None,
) -> Post:
    await assert_can_create_post(db, user_id, challenge_date)

    post = Post(
        id=post_id or uuid.uuid4(),
        user_id=user_id,
        challenge_id=challenge_id,
        challenge_date=challenge_date,
        media_type=media_type,
        original_url=original_url,
        preview_url=preview_url,
        thumb_url=thumb_url,
        caption=caption,
        country=country,
        city=city,
        status="processing",
    )
    db.add(post)
    try:
        await db.commit()
    except IntegrityError:
        await db.rollback()
        raise ValueError("Вы уже опубликовали момент сегодня")
    except Exception as exc:
        await db.rollback()
        logger.exception("Failed to commit post")
        raise ValueError(f"Ошибка сохранения: {exc}")

    await db.refresh(post)
    await _safe_cache_call(mark_user_posted(str(user_id), challenge_date), "mark_user_posted")
    return post


async def get_post_by_id(db: AsyncSession, post_id: uuid.UUID) -> Optional[Post]:
    result = await db.execute(select(Post).where(Post.id == post_id))
    return result.scalar_one_or_none()


async def get_delete_window_minutes(db: AsyncSession) -> int:
    raw_value = await get_setting(
        db,
        "post_delete_window_minutes",
        str(DEFAULT_DELETE_WINDOW_MINUTES),
    )
    try:
        value = int(raw_value)
    except (TypeError, ValueError):
        return DEFAULT_DELETE_WINDOW_MINUTES
    return max(value, 0)


async def soft_delete_post(
    db: AsyncSession,
    post_id: uuid.UUID,
    user_id: Optional[uuid.UUID] = None,
) -> bool:
    post = await get_post_by_id(db, post_id)
    if not post:
        return False
    if user_id and post.user_id != user_id:
        return False
    if post.created_at:
        created_at = _as_utc(post.created_at)
        delete_window_minutes = await get_delete_window_minutes(db)
        if datetime.now(timezone.utc) - created_at > timedelta(minutes=delete_window_minutes):
            return False
    post.status = "deleted"
    await CounterService(db).sync_post_counts(post)
    await db.commit()
    return True


def can_delete_post(
    post: Post,
    user_id: Optional[uuid.UUID],
    delete_window_minutes: int = DEFAULT_DELETE_WINDOW_MINUTES,
) -> bool:
    if not user_id or post.user_id != user_id or post.status != "active":
        return False
    if not post.created_at:
        return True
    return datetime.now(timezone.utc) - _as_utc(post.created_at) <= timedelta(
        minutes=max(delete_window_minutes, 0)
    )


async def get_feed_posts(
    db: AsyncSession,
    challenge_date: date,
    cursor: Optional[str] = None,
    limit: int = 20,
    country: Optional[str] = None,
) -> tuple[list[Post], Optional[str]]:
    query = select(Post).where(Post.challenge_date == challenge_date, Post.status == "active")
    if country:
        query = query.where(Post.country == country)
    cursor_dt = parse_cursor_datetime(cursor)
    if cursor_dt:
        query = query.where(Post.created_at < cursor_dt)
    return await _run_post_page(db, query, limit)


async def get_recent_feed_posts(
    db: AsyncSession,
    cursor: Optional[str] = None,
    limit: int = 20,
    country: Optional[str] = None,
) -> tuple[list[Post], Optional[str]]:
    query = select(Post).where(Post.status == "active")
    if country:
        query = query.where(Post.country == country)
    cursor_dt = parse_cursor_datetime(cursor)
    if cursor_dt:
        query = query.where(Post.created_at < cursor_dt)
    return await _run_post_page(db, query, limit)


async def like_post(db: AsyncSession, post_id: uuid.UUID, user_id: uuid.UUID) -> dict:
    post = await get_post_by_id(db, post_id)
    if not post or post.status != "active":
        return {"liked": False, "likes_count": 0}

    existing = await db.execute(
        select(Reaction).where(
            Reaction.post_id == post_id,
            Reaction.user_id == user_id,
            Reaction.type == "like",
        )
    )
    if existing.scalar_one_or_none():
        likes_count = await CounterService(db).sync_post_likes(post)
        await db.commit()
        return {"liked": True, "likes_count": likes_count}

    db.add(Reaction(id=uuid.uuid4(), post_id=post_id, user_id=user_id, type="like"))
    try:
        await db.flush()
    except IntegrityError:
        await db.rollback()
        post = await get_post_by_id(db, post_id)
        return {"liked": True, "likes_count": post.likes_count if post else 0}

    likes_count = await CounterService(db).sync_post_likes(post)
    await db.commit()
    await _safe_cache_call(increment_counter(f"post:likes:{post_id}"), "increment_like_counter")
    return {"liked": True, "likes_count": likes_count}


async def unlike_post(db: AsyncSession, post_id: uuid.UUID, user_id: uuid.UUID) -> dict:
    post = await get_post_by_id(db, post_id)
    if not post or post.status != "active":
        return {"liked": False, "likes_count": 0}

    existing = await db.execute(
        select(Reaction).where(
            Reaction.post_id == post_id,
            Reaction.user_id == user_id,
            Reaction.type == "like",
        )
    )
    reaction = existing.scalar_one_or_none()
    if not reaction:
        likes_count = await CounterService(db).sync_post_likes(post)
        await db.commit()
        return {"liked": False, "likes_count": likes_count}

    await db.delete(reaction)
    await db.flush()
    likes_count = await CounterService(db).sync_post_likes(post)
    await db.commit()
    return {"liked": False, "likes_count": likes_count}


async def recalculate_post_likes(db: AsyncSession, post: Post) -> int:
    return await CounterService(db).sync_post_likes(post)


async def increment_views(db: AsyncSession, post_id: uuid.UUID):
    await _safe_cache_call(increment_counter(f"post:views:{post_id}"), "increment_view_counter")


async def _safe_cache_call(awaitable, operation: str) -> None:
    try:
        await awaitable
    except Exception:
        logger.warning("Cache operation failed: %s", operation, exc_info=True)


async def _run_post_page(db: AsyncSession, query, limit: int) -> tuple[list[Post], Optional[str]]:
    query = query.order_by(desc(Post.created_at)).limit(limit + 1)
    result = await db.execute(query)
    posts = list(result.scalars().all())
    next_cursor = None
    if len(posts) > limit:
        posts = posts[:limit]
        next_cursor = posts[-1].created_at.isoformat() if posts[-1].created_at else None
    return posts, next_cursor


def _as_utc(value: datetime) -> datetime:
    if value.tzinfo is None:
        return value.replace(tzinfo=timezone.utc)
    return value.astimezone(timezone.utc)
