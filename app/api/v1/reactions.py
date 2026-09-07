import uuid

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession

from app.api.v1.auth import get_current_user_id
from app.db import get_db
from app.services.post_service import like_post as like_post_service
from app.services.post_service import unlike_post as unlike_post_service

router = APIRouter(prefix="/api/v1/posts", tags=["reactions"])


@router.post("/{post_id}/like")
async def like_post(
    post_id: uuid.UUID,
    user_id: str = Depends(get_current_user_id),
    db: AsyncSession = Depends(get_db),
):
    result = await like_post_service(db, post_id, uuid.UUID(user_id))
    if result is None:
        raise HTTPException(status_code=404, detail="Post not found")
    return result


@router.delete("/{post_id}/like")
async def unlike_post(
    post_id: uuid.UUID,
    user_id: str = Depends(get_current_user_id),
    db: AsyncSession = Depends(get_db),
):
    result = await unlike_post_service(db, post_id, uuid.UUID(user_id))
    if result is None:
        raise HTTPException(status_code=404, detail="Post not found")
    return result
