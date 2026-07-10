import uuid

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.api.v1.auth import get_current_user_id
from app.db import get_db
from app.models.comment import Comment
from app.models.post import Post
from app.models.user import User
from app.schemas.comment import CommentListResponse, CommentOut, CreateCommentRequest
from app.services.counter_service import CounterService

router = APIRouter(prefix="/api/v1/posts", tags=["comments"])


def _build_comment_out(
    comment: Comment, user: User | None, current_user_id: uuid.UUID
) -> CommentOut:
    return CommentOut(
        id=str(comment.id),
        post_id=str(comment.post_id),
        user={
            "id": str(comment.user_id),
            "username": user.username if user else "unknown",
            "display_name": user.display_name if user else "Unknown",
            "avatar_url": user.avatar_url if user else None,
            "avatar_key": user.avatar_key if user else None,
        },
        text=comment.text,
        created_at=comment.created_at,
        is_mine=comment.user_id == current_user_id,
        can_delete=comment.user_id == current_user_id,
    )


@router.get("/{post_id}/comments", response_model=CommentListResponse)
async def list_comments(
    post_id: str,
    user_id: str = Depends(get_current_user_id),
    db: AsyncSession = Depends(get_db),
):
    current_user_id = uuid.UUID(user_id)
    post_uuid = uuid.UUID(post_id)
    post_result = await db.execute(
        select(Post).where(Post.id == post_uuid, Post.status == "active")
    )
    if not post_result.scalar_one_or_none():
        raise HTTPException(status_code=404, detail="Post not found")

    result = await db.execute(
        select(Comment)
        .where(Comment.post_id == post_uuid, Comment.status == "active")
        .order_by(Comment.created_at.asc())
    )
    comments = list(result.scalars().all())
    users = await _load_comment_users(db, comments)
    return {
        "items": [
            _build_comment_out(comment, users.get(comment.user_id), current_user_id)
            for comment in comments
        ]
    }


@router.post("/{post_id}/comments", response_model=CommentOut)
async def create_comment(
    post_id: str,
    req: CreateCommentRequest,
    user_id: str = Depends(get_current_user_id),
    db: AsyncSession = Depends(get_db),
):
    current_user_id = uuid.UUID(user_id)
    post_uuid = uuid.UUID(post_id)
    post_result = await db.execute(
        select(Post).where(Post.id == post_uuid, Post.status == "active")
    )
    post = post_result.scalar_one_or_none()
    if not post:
        raise HTTPException(status_code=404, detail="Post not found")

    comment = Comment(post_id=post.id, user_id=current_user_id, text=req.text.strip())
    if not comment.text:
        raise HTTPException(status_code=400, detail="Comment cannot be empty")
    db.add(comment)
    await db.flush()
    await CounterService(db).sync_post_comments(post)
    await db.commit()
    await db.refresh(comment)
    user_result = await db.execute(select(User).where(User.id == current_user_id))
    return _build_comment_out(comment, user_result.scalar_one_or_none(), current_user_id)


@router.delete("/{post_id}/comments/{comment_id}")
async def delete_comment(
    post_id: str,
    comment_id: str,
    user_id: str = Depends(get_current_user_id),
    db: AsyncSession = Depends(get_db),
):
    current_user_id = uuid.UUID(user_id)
    post_uuid = uuid.UUID(post_id)
    comment_uuid = uuid.UUID(comment_id)
    result = await db.execute(
        select(Comment).where(
            Comment.id == comment_uuid,
            Comment.post_id == post_uuid,
            Comment.status == "active",
        )
    )
    comment = result.scalar_one_or_none()
    if not comment:
        raise HTTPException(status_code=404, detail="Comment not found")
    if comment.user_id != current_user_id:
        raise HTTPException(status_code=403, detail="You can delete only your own comment")

    comment.status = "deleted"
    post_result = await db.execute(select(Post).where(Post.id == post_uuid))
    post = post_result.scalar_one_or_none()
    if post:
        await CounterService(db).sync_post_comments(post)
    await db.commit()
    return {"status": "deleted"}


async def _load_comment_users(db: AsyncSession, comments: list[Comment]) -> dict[uuid.UUID, User]:
    user_ids = {comment.user_id for comment in comments}
    if not user_ids:
        return {}
    result = await db.execute(select(User).where(User.id.in_(user_ids)))
    return {user.id: user for user in result.scalars().all()}
