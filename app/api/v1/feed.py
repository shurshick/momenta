from datetime import date
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from app.db import get_db
from app.models.post import Post
from app.models.user import User
from app.models.reaction import Reaction
from app.schemas.post import FeedResponse, PostFeedItem
from app.services.post_service import get_feed_posts
from app.api.v1.auth import get_current_user_id

router = APIRouter(prefix="/api/v1/feed", tags=["feed"])


@router.get("/today", response_model=FeedResponse)
async def today_feed(cursor: str = Query(None), limit: int = Query(default=20, le=50),
                     user_id: str = Depends(get_current_user_id), db: AsyncSession = Depends(get_db)):
    posts, next_cursor = await get_feed_posts(db, date.today(), cursor=cursor, limit=limit)
    items = await _build_feed_items(db, posts, user_id)
    return {"items": items, "next_cursor": next_cursor}


@router.get("/country/{country_code}", response_model=FeedResponse)
async def country_feed(country_code: str, cursor: str = Query(None), limit: int = Query(default=20, le=50),
                       user_id: str = Depends(get_current_user_id), db: AsyncSession = Depends(get_db)):
    posts, next_cursor = await get_feed_posts(db, date.today(), cursor=cursor, limit=limit, country=country_code.upper())
    items = await _build_feed_items(db, posts, user_id)
    return {"items": items, "next_cursor": next_cursor}


@router.get("/user/{target_user_id}", response_model=FeedResponse)
async def user_feed(target_user_id: str, cursor: str = Query(None), limit: int = Query(default=20, le=50),
                    user_id: str = Depends(get_current_user_id), db: AsyncSession = Depends(get_db)):
    query = select(Post).where(Post.user_id == target_user_id, Post.status == "active").order_by(Post.created_at.desc())
    if cursor:
        from datetime import datetime, timezone
        query = query.where(Post.created_at < datetime.fromisoformat(cursor))
    query = query.limit(limit + 1)
    result = await db.execute(query)
    posts = result.scalars().all()
    next_cursor = None
    if len(posts) > limit:
        posts = posts[:limit]
        next_cursor = posts[-1].created_at.isoformat() if posts[-1].created_at else None
    items = await _build_feed_items(db, list(posts), user_id)
    return {"items": items, "next_cursor": next_cursor}


async def _build_feed_items(db: AsyncSession, posts: list, current_user_id: str | None = None) -> list:
    items = []

    liked_post_ids: set = set()
    if current_user_id and posts:
        import uuid
        post_ids = [p.id for p in posts]
        likes_result = await db.execute(
            select(Reaction.post_id).where(
                Reaction.post_id.in_(post_ids),
                Reaction.user_id == uuid.UUID(current_user_id),
                Reaction.type == "like",
            )
        )
        liked_post_ids = {row[0] for row in likes_result.all()}

    for post in posts:
        user_result = await db.execute(select(User).where(User.id == post.user_id))
        user = user_result.scalar_one_or_none()
        items.append(PostFeedItem(
            id=str(post.id),
            user={
                "id": str(post.user_id),
                "username": user.username if user else "unknown",
                "display_name": user.display_name if user else "Unknown",
                "avatar_url": user.avatar_url if user else None,
            },
            media_type=post.media_type,
            preview_url=post.preview_url,
            thumb_url=post.thumb_url,
            caption=post.caption,
            country=post.country,
            city=post.city,
            likes_count=post.likes_count,
            comments_count=post.comments_count,
            views_count=post.views_count,
            created_at=post.created_at,
            is_liked=post.id in liked_post_ids,
        ))
    return items
