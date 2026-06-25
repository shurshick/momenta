import uuid
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from app.db import get_db
from app.services.post_service import toggle_like
from app.api.v1.auth import get_current_user_id

router = APIRouter(prefix="/api/v1/posts", tags=["reactions"])


@router.post("/{post_id}/like")
async def like_post(post_id: str, user_id: str = Depends(get_current_user_id), db: AsyncSession = Depends(get_db)):
    return await toggle_like(db, uuid.UUID(post_id), uuid.UUID(user_id))


@router.delete("/{post_id}/like")
async def unlike_post(post_id: str, user_id: str = Depends(get_current_user_id), db: AsyncSession = Depends(get_db)):
    return await toggle_like(db, uuid.UUID(post_id), uuid.UUID(user_id))
