import uuid
from collections.abc import Sequence

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.post import Post
from app.models.reaction import Reaction
from app.models.user import User
from app.schemas.post import PostFeedItem
from app.services.post_service import can_delete_post


async def build_feed_items(
    db: AsyncSession,
    posts: Sequence[Post],
    current_user_id: str | uuid.UUID | None = None,
) -> list[PostFeedItem]:
    if not posts:
        return []

    current_user_uuid = _to_uuid(current_user_id)
    post_ids = [post.id for post in posts]
    user_ids = {post.user_id for post in posts}

    users_result = await db.execute(select(User).where(User.id.in_(user_ids)))
    users = {user.id: user for user in users_result.scalars().all()}

    liked_post_ids: set[uuid.UUID] = set()
    if current_user_uuid:
        likes_result = await db.execute(
            select(Reaction.post_id).where(
                Reaction.post_id.in_(post_ids),
                Reaction.user_id == current_user_uuid,
                Reaction.type == "like",
            )
        )
        liked_post_ids = {row[0] for row in likes_result.all()}

    return [
        _build_item(post, users.get(post.user_id), current_user_uuid, liked_post_ids)
        for post in posts
    ]


def _build_item(
    post: Post,
    user: User | None,
    current_user_id: uuid.UUID | None,
    liked_post_ids: set[uuid.UUID],
) -> PostFeedItem:
    return PostFeedItem(
        id=str(post.id),
        user={
            "id": str(post.user_id),
            "username": user.username if user else "unknown",
            "display_name": user.display_name if user else "Unknown",
            "avatar_url": user.avatar_url if user else None,
            "avatar_key": user.avatar_key if user else None,
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
        challenge_date=post.challenge_date,
        created_at=post.created_at,
        is_liked=post.id in liked_post_ids,
        is_mine=current_user_id == post.user_id if current_user_id else False,
        can_delete=can_delete_post(post, current_user_id),
    )


def _to_uuid(value: str | uuid.UUID | None) -> uuid.UUID | None:
    if value is None:
        return None
    if isinstance(value, uuid.UUID):
        return value
    return uuid.UUID(value)
