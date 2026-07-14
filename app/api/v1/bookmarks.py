import uuid

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.api.v1.auth import get_current_user_id
from app.db import get_db
from app.schemas.post import FeedResponse
from app.services.bookmark_service import add_bookmark, get_bookmarked_posts, remove_bookmark
from app.services.feed_item_service import build_feed_items

router = APIRouter(tags=["bookmarks"])


@router.put("/api/v1/posts/{post_id}/bookmark")
async def bookmark_post(
    post_id: uuid.UUID,
    user_id: str = Depends(get_current_user_id),
    db: AsyncSession = Depends(get_db),
):
    bookmark = await add_bookmark(db, uuid.UUID(user_id), post_id)
    if bookmark is None:
        raise HTTPException(status_code=404, detail="Post not found")
    return {"status": "bookmarked"}


@router.delete("/api/v1/posts/{post_id}/bookmark")
async def unbookmark_post(
    post_id: uuid.UUID,
    user_id: str = Depends(get_current_user_id),
    db: AsyncSession = Depends(get_db),
):
    await remove_bookmark(db, uuid.UUID(user_id), post_id)
    return {"status": "removed"}


@router.get("/api/v1/me/bookmarks", response_model=FeedResponse)
async def my_bookmarks(
    cursor: str | None = Query(None),
    limit: int = Query(default=20, ge=1, le=50),
    user_id: str = Depends(get_current_user_id),
    db: AsyncSession = Depends(get_db),
):
    posts, next_cursor = await get_bookmarked_posts(db, uuid.UUID(user_id), cursor, limit)
    return {
        "items": await build_feed_items(db, posts, user_id),
        "next_cursor": next_cursor,
    }
