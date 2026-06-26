import uuid
import logging
from datetime import date, datetime, timezone
from typing import Optional
from sqlalchemy import select, desc, func
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.exc import IntegrityError
from app.models.post import Post
from app.models.reaction import Reaction
from app.services.redis_service import add_to_feed, increment_counter, mark_user_posted
from app.services.setting_service import get_setting

logger = logging.getLogger(__name__)


async def create_post(db: AsyncSession, user_id: uuid.UUID, challenge_id: uuid.UUID, challenge_date: date,
                      media_type: str, original_url: str, preview_url: Optional[str] = None,
                      thumb_url: Optional[str] = None, caption: Optional[str] = None,
                      country: Optional[str] = None, city: Optional[str] = None) -> Post:
    limit_str = await get_setting(db, "daily_post_limit", "1")
    try:
        daily_limit = int(limit_str)
    except (ValueError, TypeError):
        daily_limit = 1

    if daily_limit > 0:
        today_count = (await db.execute(
            select(func.count(Post.id)).where(
                Post.user_id == user_id,
                Post.challenge_date == challenge_date,
                Post.status.in_(["active", "processing", "uploading"])
            )
        )).scalar() or 0
        logger.info(f"Daily limit check: user={user_id}, date={challenge_date}, count={today_count}, limit={daily_limit}")
        if today_count >= daily_limit:
            raise ValueError(f"Лимит {daily_limit} моментов в день исчерпан")
    else:
        logger.info(f"Daily limit disabled (0) for user={user_id}")

    post = Post(
        id=uuid.uuid4(),
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
    except Exception as e:
        await db.rollback()
        logger.exception("Failed to commit post")
        raise ValueError(f"Ошибка сохранения: {e}")
    await db.refresh(post)
    await mark_user_posted(str(user_id), challenge_date)
    return post


async def get_post_by_id(db: AsyncSession, post_id: uuid.UUID) -> Optional[Post]:
    result = await db.execute(select(Post).where(Post.id == post_id))
    return result.scalar_one_or_none()


async def soft_delete_post(db: AsyncSession, post_id: uuid.UUID, user_id: Optional[uuid.UUID] = None) -> bool:
    post = await get_post_by_id(db, post_id)
    if not post:
        return False
    if user_id and post.user_id != user_id:
        return False
    post.status = "deleted"
    await db.commit()
    return True


async def get_feed_posts(db: AsyncSession, challenge_date: date, cursor: Optional[str] = None, limit: int = 20, country: Optional[str] = None) -> tuple[list[Post], Optional[str]]:
    query = select(Post).where(Post.challenge_date == challenge_date, Post.status == "active")
    if country:
        query = query.where(Post.country == country)
    if cursor:
        query = query.where(Post.created_at < datetime.fromisoformat(cursor))
    query = query.order_by(desc(Post.created_at)).limit(limit + 1)
    result = await db.execute(query)
    posts = result.scalars().all()
    next_cursor = None
    if len(posts) > limit:
        posts = posts[:limit]
        next_cursor = posts[-1].created_at.isoformat() if posts[-1].created_at else None
    return list(posts), next_cursor


async def toggle_like(db: AsyncSession, post_id: uuid.UUID, user_id: uuid.UUID) -> dict:
    existing = await db.execute(
        select(Reaction).where(Reaction.post_id == post_id, Reaction.user_id == user_id, Reaction.type == "like")
    )
    reaction = existing.scalar_one_or_none()
    if reaction:
        await db.delete(reaction)
        post = await get_post_by_id(db, post_id)
        if post and post.likes_count > 0:
            post.likes_count -= 1
        await db.commit()
        return {"liked": False}
    reaction = Reaction(
        id=uuid.uuid4(),
        post_id=post_id,
        user_id=user_id,
        type="like",
    )
    db.add(reaction)
    post = await get_post_by_id(db, post_id)
    if post:
        post.likes_count += 1
    await db.commit()
    key = f"post:likes:{post_id}"
    await increment_counter(key)
    return {"liked": True}


async def increment_views(db: AsyncSession, post_id: uuid.UUID):
    key = f"post:views:{post_id}"
    await increment_counter(key)
