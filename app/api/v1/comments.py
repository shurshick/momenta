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

router = APIRouter(prefix="/api/v1/posts", tags=["comments"])


async def _build_comment_out(db: AsyncSession, comment: Comment, current_user_id: uuid.UUID) -> CommentOut:
    user_result = await db.execute(select(User).where(User.id == comment.user_id))
    user = user_result.scalar_one_or_none()
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
    post_result = await db.execute(select(Post).where(Post.id == post_uuid, Post.status == "active"))
    if not post_result.scalar_one_or_none():
        raise HTTPException(status_code=404, detail="Post not found")

    result = await db.execute(
        select(Comment)
        .where(Comment.post_id == post_uuid, Comment.status == "active")
        .order_by(Comment.created_at.asc())
    )
    comments = result.scalars().all()
    return {"items": [await _build_comment_out(db, comment, current_user_id) for comment in comments]}


@router.post("/{post_id}/comments", response_model=CommentOut)
async def create_comment(
    post_id: str,
    req: CreateCommentRequest,
    user_id: str = Depends(get_current_user_id),
    db: AsyncSession = Depends(get_db),
):
    current_user_id = uuid.UUID(user_id)
    post_uuid = uuid.UUID(post_id)
    post_result = await db.execute(select(Post).where(Post.id == post_uuid, Post.status == "active"))
    post = post_result.scalar_one_or_none()
    if not post:
        raise HTTPException(status_code=404, detail="Post not found")

    comment = Comment(post_id=post.id, user_id=current_user_id, text=req.text.strip())
    if not comment.text:
        raise HTTPException(status_code=400, detail="Comment cannot be empty")
    db.add(comment)
    post.comments_count += 1
    await db.commit()
    await db.refresh(comment)
    return await _build_comment_out(db, comment, current_user_id)


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
    if post and post.comments_count > 0:
        post.comments_count -= 1
    await db.commit()
    return {"status": "deleted"}
