import uuid
from fastapi import APIRouter, Depends, HTTPException, UploadFile, File, Form
from sqlalchemy.ext.asyncio import AsyncSession
from app.db import get_db
from app.schemas.post import CreatePostResponse
from app.services.post_service import create_post, get_post_by_id, soft_delete_post, increment_views
from app.services.challenge_service import get_challenge_by_id
from app.services.s3_service import upload_fileobj, make_object_key
from app.api.v1.auth import get_current_user_id
from app.config import settings
import mimetypes
import os

router = APIRouter(prefix="/api/v1/posts", tags=["posts"])


@router.post("", response_model=CreatePostResponse)
async def upload_post(
    challenge_id: str = Form(...),
    media: UploadFile = File(...),
    caption: str = Form(None),
    country: str = Form(None),
    city: str = Form(None),
    user_id: str = Depends(get_current_user_id),
    db: AsyncSession = Depends(get_db),
):
    if not media.content_type:
        raise HTTPException(status_code=400, detail="Could not determine media type")
    allowed = settings.allowed_image_types + settings.allowed_video_types
    if media.content_type not in allowed:
        raise HTTPException(status_code=400, detail=f"Media type {media.content_type} not allowed")
    max_size = settings.media_max_image_mb * 1024 * 1024
    if media.content_type.startswith("video/"):
        max_size = settings.media_max_video_mb * 1024 * 1024
    contents = await media.read()
    if len(contents) > max_size:
        raise HTTPException(status_code=400, detail="File too large")
    await media.seek(0)
    challenge = await get_challenge_by_id(db, uuid.UUID(challenge_id))
    if not challenge:
        raise HTTPException(status_code=404, detail="Challenge not found")
    media_type = "photo" if media.content_type.startswith("image/") else "video"
    ext = media.filename.split(".")[-1] if media.filename else "bin"
    post_id = uuid.uuid4()
    object_key = make_object_key(challenge.challenge_date, str(post_id), "original", ext)
    public_url = upload_fileobj(media.file, object_key, media.content_type)
    try:
        post = await create_post(
            db, uuid.UUID(user_id), challenge.id, challenge.challenge_date,
            media_type, public_url, caption=caption, country=country, city=city,
        )
        return {"id": str(post.id), "status": post.status}
    except ValueError as e:
        raise HTTPException(status_code=409, detail=str(e))


@router.get("/{post_id}")
async def get_post(post_id: str, db: AsyncSession = Depends(get_db)):
    post = await get_post_by_id(db, uuid.UUID(post_id))
    if not post or post.status == "deleted":
        raise HTTPException(status_code=404)
    await increment_views(db, post.id)
    return {
        "id": str(post.id),
        "user_id": str(post.user_id),
        "media_type": post.media_type,
        "preview_url": post.preview_url,
        "thumb_url": post.thumb_url,
        "caption": post.caption,
        "country": post.country,
        "city": post.city,
        "likes_count": post.likes_count,
        "views_count": post.views_count,
        "created_at": post.created_at,
    }


@router.delete("/{post_id}")
async def delete_post(post_id: str, user_id: str = Depends(get_current_user_id), db: AsyncSession = Depends(get_db)):
    success = await soft_delete_post(db, uuid.UUID(post_id), uuid.UUID(user_id))
    if not success:
        raise HTTPException(status_code=404, detail="Post not found or not yours")
    return {"status": "deleted"}
