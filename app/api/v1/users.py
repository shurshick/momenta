import uuid
from datetime import timedelta

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.api.v1.auth import get_current_user_id
from app.db import get_db
from app.models.post import Post
from app.models.user import User
from app.schemas.user import (
    AvatarListResponse,
    UpdateAvatarRequest,
    UpdateProfileRequest,
    UserProfile,
    UserSummaryListResponse,
)
from app.services.auth_service import get_user_by_id
from app.services.challenge_service import current_app_date

router = APIRouter(prefix="/api/v1", tags=["users"])

AVATAR_KEYS = [f"avatar_{index:02d}" for index in range(1, 41)]


async def _build_profile(db: AsyncSession, user: User) -> UserProfile:
    moments_count = (
        await db.execute(
            select(func.count(Post.id)).where(Post.user_id == user.id, Post.status == "active")
        )
    ).scalar() or 0

    likes_count = (
        await db.execute(
            select(func.coalesce(func.sum(Post.likes_count), 0)).where(
                Post.user_id == user.id,
                Post.status == "active",
            )
        )
    ).scalar() or 0

    recent_result = await db.execute(
        select(Post.id, Post.preview_url, Post.thumb_url, Post.created_at)
        .where(Post.user_id == user.id, Post.status == "active")
        .order_by(Post.created_at.desc())
        .limit(9)
    )

    return UserProfile(
        id=str(user.id),
        username=user.username,
        display_name=user.display_name,
        avatar_key=user.avatar_key,
        avatar_url=user.avatar_url,
        bio=user.bio,
        country=user.country,
        city=user.city,
        locale=user.locale,
        moments_count=moments_count,
        streak_count=await _calculate_streak_count(db, user.id),
        likes_count=likes_count,
        recent_posts=[
            {"id": str(row[0]), "preview_url": row[1], "thumb_url": row[2], "created_at": row[3]}
            for row in recent_result.all()
        ],
        created_at=user.created_at,
        last_seen_at=user.last_seen_at,
    )


async def _calculate_streak_count(db: AsyncSession, user_id: uuid.UUID) -> int:
    since = current_app_date() - timedelta(days=365)
    result = await db.execute(
        select(Post.challenge_date)
        .where(
            Post.user_id == user_id,
            Post.status == "active",
            Post.challenge_date >= since,
        )
        .group_by(Post.challenge_date)
    )
    posted_dates = {row[0] for row in result.all()}
    streak_count = 0
    cursor = current_app_date()
    while cursor in posted_dates:
        streak_count += 1
        cursor -= timedelta(days=1)
    return streak_count


@router.get("/users/suggestions", response_model=UserSummaryListResponse)
async def list_user_suggestions(
    user_id: str = Depends(get_current_user_id),
    db: AsyncSession = Depends(get_db),
):
    active_posts = (
        select(
            Post.user_id.label("user_id"),
            func.count(Post.id).label("posts_count"),
            func.max(Post.created_at).label("last_post_at"),
        )
        .where(Post.status == "active")
        .group_by(Post.user_id)
        .subquery()
    )
    result = await db.execute(
        select(User)
        .outerjoin(active_posts, active_posts.c.user_id == User.id)
        .where(User.status == "active")
        .order_by(
            func.coalesce(active_posts.c.posts_count, 0).desc(),
            active_posts.c.last_post_at.desc().nullslast(),
            User.last_seen_at.desc().nullslast(),
            User.created_at.desc(),
        )
        .limit(20)
    )
    users = result.scalars().all()
    return {
        "items": [
            {
                "id": str(user.id),
                "username": user.username,
                "display_name": user.display_name,
                "avatar_key": user.avatar_key,
                "avatar_url": user.avatar_url,
            }
            for user in users
        ]
    }


@router.get("/users/{user_id}", response_model=UserProfile)
async def get_user_profile(user_id: str, db: AsyncSession = Depends(get_db)):
    user = await get_user_by_id(db, uuid.UUID(user_id))
    if not user or user.status == "deleted":
        raise HTTPException(status_code=404)
    return await _build_profile(db, user)


@router.get("/me/profile", response_model=UserProfile)
async def get_my_profile(
    user_id: str = Depends(get_current_user_id), db: AsyncSession = Depends(get_db)
):
    user = await get_user_by_id(db, uuid.UUID(user_id))
    if not user:
        raise HTTPException(status_code=404)
    return await _build_profile(db, user)


@router.patch("/me/profile", response_model=UserProfile)
async def update_my_profile(
    req: UpdateProfileRequest,
    user_id: str = Depends(get_current_user_id),
    db: AsyncSession = Depends(get_db),
):
    user = await get_user_by_id(db, uuid.UUID(user_id))
    if not user:
        raise HTTPException(status_code=404)
    if req.display_name is not None:
        user.display_name = req.display_name.strip()
    if req.bio is not None:
        user.bio = req.bio.strip()
    if req.avatar_url is not None:
        user.avatar_url = req.avatar_url
    if req.country is not None:
        user.country = req.country.upper()
    if req.city is not None:
        user.city = req.city.strip()
    if req.locale is not None:
        user.locale = req.locale.strip()
    await db.commit()
    await db.refresh(user)
    return await _build_profile(db, user)


@router.get("/avatars", response_model=AvatarListResponse)
async def list_avatars():
    return {"items": [{"key": key} for key in AVATAR_KEYS]}


@router.patch("/me/avatar", response_model=UserProfile)
async def update_my_avatar(
    req: UpdateAvatarRequest,
    user_id: str = Depends(get_current_user_id),
    db: AsyncSession = Depends(get_db),
):
    if req.avatar_key not in AVATAR_KEYS:
        raise HTTPException(status_code=400, detail="Unknown avatar")
    user = await get_user_by_id(db, uuid.UUID(user_id))
    if not user:
        raise HTTPException(status_code=404)
    user.avatar_key = req.avatar_key
    await db.commit()
    await db.refresh(user)
    return await _build_profile(db, user)
