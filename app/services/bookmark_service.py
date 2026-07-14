import uuid
from datetime import datetime

from sqlalchemy import and_, delete, or_, select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.bookmark import Bookmark
from app.models.post import Post


async def add_bookmark(db: AsyncSession, user_id: uuid.UUID, post_id: uuid.UUID) -> Bookmark | None:
    post = await db.scalar(select(Post).where(Post.id == post_id, Post.status == "active"))
    if post is None:
        return None

    bookmark = await db.scalar(
        select(Bookmark).where(Bookmark.user_id == user_id, Bookmark.post_id == post_id)
    )
    if bookmark is None:
        bookmark = Bookmark(user_id=user_id, post_id=post_id)
        db.add(bookmark)
        try:
            await db.commit()
            await db.refresh(bookmark)
        except IntegrityError:
            await db.rollback()
            bookmark = await db.scalar(
                select(Bookmark).where(
                    Bookmark.user_id == user_id,
                    Bookmark.post_id == post_id,
                )
            )
    return bookmark


async def remove_bookmark(db: AsyncSession, user_id: uuid.UUID, post_id: uuid.UUID) -> None:
    await db.execute(
        delete(Bookmark).where(Bookmark.user_id == user_id, Bookmark.post_id == post_id)
    )
    await db.commit()


async def get_bookmarked_posts(
    db: AsyncSession,
    user_id: uuid.UUID,
    cursor: str | None,
    limit: int,
) -> tuple[list[Post], str | None]:
    query = (
        select(Post, Bookmark.created_at, Bookmark.id)
        .join(Bookmark, Bookmark.post_id == Post.id)
        .where(Bookmark.user_id == user_id, Post.status == "active")
        .order_by(Bookmark.created_at.desc(), Bookmark.id.desc())
    )
    cursor_value = _parse_cursor(cursor)
    if cursor_value:
        cursor_dt, cursor_id = cursor_value
        query = query.where(
            or_(
                Bookmark.created_at < cursor_dt,
                and_(Bookmark.created_at == cursor_dt, Bookmark.id < cursor_id),
            )
        )

    rows = (await db.execute(query.limit(limit + 1))).all()
    next_cursor = None
    if len(rows) > limit:
        rows = rows[:limit]
        next_cursor = f"{rows[-1][1].isoformat()}|{rows[-1][2]}"
    return [row[0] for row in rows], next_cursor


def _parse_cursor(cursor: str | None) -> tuple[datetime, uuid.UUID] | None:
    if not cursor:
        return None
    try:
        created_at, bookmark_id = cursor.rsplit("|", 1)
        return datetime.fromisoformat(created_at.replace("Z", "+00:00")), uuid.UUID(bookmark_id)
    except (ValueError, TypeError):
        return None
