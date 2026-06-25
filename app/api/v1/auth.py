from fastapi import APIRouter, Depends, HTTPException, Header
from sqlalchemy.ext.asyncio import AsyncSession
from app.db import get_db
from app.schemas.auth import RegisterRequest, LoginRequest, AuthResponse, TokenRefreshRequest
from app.services.auth_service import register_user, login_user, refresh_token, get_user_by_id
from app.security import decode_token
import uuid

router = APIRouter(prefix="/api/v1/auth", tags=["auth"])


async def get_token(authorization: str = Header(default="")) -> str:
    if not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Missing or invalid token")
    return authorization[7:]


async def get_current_user_id(token: str = Depends(get_token)) -> str:
    payload = decode_token(token)
    if not payload:
        raise HTTPException(status_code=401, detail="Invalid token")
    user_id = payload.get("sub")
    if not user_id:
        raise HTTPException(status_code=401, detail="Invalid token payload")
    return user_id


@router.post("/register", response_model=AuthResponse)
async def register(req: RegisterRequest, db: AsyncSession = Depends(get_db)):
    try:
        return await register_user(db, req.username, req.email, req.password)
    except ValueError as e:
        raise HTTPException(status_code=409, detail=str(e))


@router.post("/login", response_model=AuthResponse)
async def login(req: LoginRequest, db: AsyncSession = Depends(get_db)):
    try:
        return await login_user(db, req.username_or_email, req.password)
    except ValueError as e:
        raise HTTPException(status_code=401, detail=str(e))


@router.post("/refresh", response_model=AuthResponse)
async def refresh(req: TokenRefreshRequest, db: AsyncSession = Depends(get_db)):
    try:
        return await refresh_token(db, req.refresh_token)
    except ValueError as e:
        raise HTTPException(status_code=401, detail=str(e))


@router.get("/me")
async def get_me(user_id: str = Depends(get_current_user_id), db: AsyncSession = Depends(get_db)):
    user = await get_user_by_id(db, uuid.UUID(user_id))
    if not user:
        raise HTTPException(status_code=404)
    return {
        "id": str(user.id),
        "username": user.username,
        "display_name": user.display_name,
        "email": user.email,
        "avatar_url": user.avatar_url,
        "country": user.country,
        "city": user.city,
        "locale": user.locale,
        "role": user.role,
        "created_at": user.created_at,
    }
