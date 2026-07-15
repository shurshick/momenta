import uuid
from datetime import date

from fastapi import APIRouter, Depends, Query
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.api.v1.auth import get_current_user_id
from app.db import get_db
from app.models.post import Post
from app.schemas.post import BestMomentResponse, FeedResponse
from app.services.challenge_service import current_app_date
from app.services.feed_item_service import build_feed_items
from app.services.post_service import get_feed_posts
from app.utils.dates import parse_cursor_datetime

router = APIRouter(prefix="/api/v1/feed", tags=["feed"])


@router.get("/today", response_model=FeedResponse)
async def today_feed(
    cursor: str = Query(None),
    limit: int = Query(default=20, le=50),
    user_id: str = Depends(get_current_user_id),
    db: AsyncSession = Depends(get_db),
):
    posts, next_cursor = await get_feed_posts(db, current_app_date(), cursor=cursor, limit=limit)
    items = await build_feed_items(db, posts, user_id)
    return {"items": items, "next_cursor": next_cursor}


@router.get("/today/best", response_model=BestMomentResponse)
@router.get("/today/best-random", response_model=BestMomentResponse, include_in_schema=False)
async def today_best(
    user_id: str = Depends(get_current_user_id), db: AsyncSession = Depends(get_db)
):
    posts = await _top_active_posts(db, challenge_date=current_app_date())
    if not posts:
        return {"post": None}
    items = await build_feed_items(db, [posts[0]], user_id)
    return {"post": items[0] if items else None}


async def _top_active_posts(db: AsyncSession, challenge_date: date) -> list[Post]:
    query = select(Post).where(
        Post.status == "active",
        Post.challenge_date == challenge_date,
    )
    query = query.order_by(Post.likes_count.desc(), Post.created_at.desc()).limit(10)
    result = await db.execute(query)
    return list(result.scalars().all())


@router.get("/country/{country_code}", response_model=FeedResponse)
async def country_feed(
    country_code: str,
    cursor: str = Query(None),
    limit: int = Query(default=20, le=50),
    user_id: str = Depends(get_current_user_id),
    db: AsyncSession = Depends(get_db),
):
    posts, next_cursor = await get_feed_posts(
        db, current_app_date(), cursor=cursor, limit=limit, country=country_code.upper()
    )
    items = await build_feed_items(db, posts, user_id)
    return {"items": items, "next_cursor": next_cursor}


@router.get("/user/{target_user_id}", response_model=FeedResponse)
async def user_feed(
    target_user_id: str,
    cursor: str = Query(None),
    limit: int = Query(default=20, le=50),
    user_id: str = Depends(get_current_user_id),
    db: AsyncSession = Depends(get_db),
):
    target_uuid = uuid.UUID(target_user_id)
    query = (
        select(Post)
        .where(Post.user_id == target_uuid, Post.status == "active")
        .order_by(Post.created_at.desc())
    )
    cursor_dt = parse_cursor_datetime(cursor)
    if cursor_dt:
        query = query.where(Post.created_at < cursor_dt)
    query = query.limit(limit + 1)
    result = await db.execute(query)
    posts = result.scalars().all()
    next_cursor = None
    if len(posts) > limit:
        posts = posts[:limit]
        next_cursor = posts[-1].created_at.isoformat() if posts[-1].created_at else None
    items = await build_feed_items(db, list(posts), user_id)
    return {"items": items, "next_cursor": next_cursor}
