import uuid
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from app.db import get_db
from app.schemas.user import UserProfile, UpdateProfileRequest
from app.services.auth_service import get_user_by_id
from app.api.v1.auth import get_current_user_id

router = APIRouter(prefix="/api/v1", tags=["users"])


@router.get("/users/{user_id}", response_model=UserProfile)
async def get_user_profile(user_id: str, db: AsyncSession = Depends(get_db)):
    user = await get_user_by_id(db, uuid.UUID(user_id))
    if not user or user.status == "deleted":
        raise HTTPException(status_code=404)
    return UserProfile(
        id=str(user.id),
        username=user.username,
        display_name=user.display_name,
        avatar_url=user.avatar_url,
        country=user.country,
        city=user.city,
        locale=user.locale,
        created_at=user.created_at,
        last_seen_at=user.last_seen_at,
    )


@router.get("/me/profile", response_model=UserProfile)
async def get_my_profile(user_id: str = Depends(get_current_user_id), db: AsyncSession = Depends(get_db)):
    user = await get_user_by_id(db, uuid.UUID(user_id))
    if not user:
        raise HTTPException(status_code=404)
    return UserProfile(
        id=str(user.id),
        username=user.username,
        display_name=user.display_name,
        avatar_url=user.avatar_url,
        country=user.country,
        city=user.city,
        locale=user.locale,
        created_at=user.created_at,
        last_seen_at=user.last_seen_at,
    )


@router.patch("/me/profile", response_model=UserProfile)
async def update_my_profile(req: UpdateProfileRequest, user_id: str = Depends(get_current_user_id),
                             db: AsyncSession = Depends(get_db)):
    user = await get_user_by_id(db, uuid.UUID(user_id))
    if not user:
        raise HTTPException(status_code=404)
    if req.display_name is not None:
        user.display_name = req.display_name
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
    return UserProfile(
        id=str(user.id),
        username=user.username,
        display_name=user.display_name,
        avatar_url=user.avatar_url,
        country=user.country,
        city=user.city,
        locale=user.locale,
        created_at=user.created_at,
        last_seen_at=user.last_seen_at,
    )
