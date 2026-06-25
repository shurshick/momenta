import uuid
from datetime import date, timedelta
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import select, func
from sqlalchemy.ext.asyncio import AsyncSession
from app.db import get_db
from app.schemas.user import UserProfile, UpdateProfileRequest
from app.services.auth_service import get_user_by_id
from app.api.v1.auth import get_current_user_id
from app.models.post import Post
from app.models.reaction import Reaction

router = APIRouter(prefix="/api/v1", tags=["users"])


async def _build_profile(db: AsyncSession, user, viewer_id: str | None = None) -> UserProfile:
    from datetime import date, timedelta

    moments_result = await db.execute(
        select(func.count(Post.id)).where(Post.user_id == user.id, Post.status == "active")
    )
    moments_count = moments_result.scalar() or 0

    likes_result = await db.execute(
        select(func.count(Reaction.id)).where(
            Reaction.post_id.in_(
                select(Post.id).where(Post.user_id == user.id, Post.status == "active")
            ),
            Reaction.type == "like",
        )
    )
    likes_count = likes_result.scalar() or 0

    streak_count = 0
    today = date.today()
    for i in range(365):
        d = today - timedelta(days=i)
        has_post = await db.execute(
            select(func.count(Post.id)).where(
                Post.user_id == user.id,
                Post.challenge_date == d,
                Post.status == "active",
            )
        )
        if (has_post.scalar() or 0) > 0:
            streak_count += 1
        else:
            break

    recent_result = await db.execute(
        select(Post.id, Post.preview_url, Post.thumb_url, Post.created_at)
        .where(Post.user_id == user.id, Post.status == "active")
        .order_by(Post.created_at.desc())
        .limit(9)
    )
    recent_posts = [
        {"id": str(r[0]), "preview_url": r[1], "thumb_url": r[2], "created_at": r[3]}
        for r in recent_result.all()
    ]

    return UserProfile(
        id=str(user.id),
        username=user.username,
        display_name=user.display_name,
        avatar_url=user.avatar_url,
        bio=getattr(user, "bio", None),
        country=user.country,
        city=user.city,
        locale=user.locale,
        moments_count=moments_count,
        streak_count=streak_count,
        likes_count=likes_count,
        recent_posts=recent_posts,
        created_at=user.created_at,
        last_seen_at=user.last_seen_at,
    )


@router.get("/users/{user_id}", response_model=UserProfile)
async def get_user_profile(user_id: str, db: AsyncSession = Depends(get_db)):
    user = await get_user_by_id(db, uuid.UUID(user_id))
    if not user or user.status == "deleted":
        raise HTTPException(status_code=404)
    return await _build_profile(db, user)


@router.get("/me/profile", response_model=UserProfile)
async def get_my_profile(user_id: str = Depends(get_current_user_id), db: AsyncSession = Depends(get_db)):
    user = await get_user_by_id(db, uuid.UUID(user_id))
    if not user:
        raise HTTPException(status_code=404)
    return await _build_profile(db, user)


@router.patch("/me/profile", response_model=UserProfile)
async def update_my_profile(req: UpdateProfileRequest, user_id: str = Depends(get_current_user_id),
                             db: AsyncSession = Depends(get_db)):
    user = await get_user_by_id(db, uuid.UUID(user_id))
    if not user:
        raise HTTPException(status_code=404)
    if req.display_name is not None:
        user.display_name = req.display_name
    if req.bio is not None:
        user.bio = req.bio
    if req.avatar_url is not None:
        user.avatar_url = req.avatar_url
    if req.country is not None:
        user.country = req.country
    if req.city is not None:
        user.city = req.city
    if req.locale is not None:
        user.locale = req.locale
    await db.commit()
    await db.refresh(user)
    return await _build_profile(db, user)
