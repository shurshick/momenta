import uuid
from datetime import datetime, timezone
from typing import Optional

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.user import User
from app.security import (
    create_access_token,
    create_refresh_token,
    decode_token,
    get_password_hash,
    verify_password,
)


async def register_user(db: AsyncSession, username: str, email: str, password: str) -> dict:
    existing = await db.execute(
        select(User).where((User.username == username) | (User.email == email))
    )
    if existing.scalar_one_or_none():
        raise ValueError("Имя пользователя или email уже заняты")
    user = User(
        id=uuid.uuid4(),
        username=username,
        email=email,
        display_name=username,
        password_hash=get_password_hash(password),
    )
    db.add(user)
    await db.commit()
    await db.refresh(user)
    return _auth_response(user)


async def login_user(db: AsyncSession, username_or_email: str, password: str) -> dict:
    result = await db.execute(
        select(User).where((User.username == username_or_email) | (User.email == username_or_email))
    )
    user = result.scalar_one_or_none()
    if not user or not verify_password(password, user.password_hash):
        raise ValueError("Неверные учётные данные")
    if user.status != "active":
        raise ValueError("Аккаунт заблокирован")
    user.last_seen_at = datetime.now(timezone.utc)
    await db.commit()
    await db.refresh(user)
    return _auth_response(user)


async def refresh_token(db: AsyncSession, token: str) -> dict:
    payload = decode_token(token)
    if not payload or payload.get("type") != "refresh":
        raise ValueError("Недействительный токен обновления")
    user_id = payload.get("sub")
    if not user_id:
        raise ValueError("Неверный формат токена")
    result = await db.execute(select(User).where(User.id == uuid.UUID(user_id)))
    user = result.scalar_one_or_none()
    if not user or user.status != "active":
        raise ValueError("Пользователь не найден или заблокирован")
    return _auth_response(user)


async def get_user_by_id(db: AsyncSession, user_id: uuid.UUID) -> Optional[User]:
    result = await db.execute(select(User).where(User.id == user_id))
    return result.scalar_one_or_none()


def _auth_response(user: User) -> dict:
    return {
        "access_token": create_access_token({"sub": str(user.id), "role": user.role}),
        "refresh_token": create_refresh_token({"sub": str(user.id)}),
        "token_type": "bearer",
        "user": {
            "id": str(user.id),
            "username": user.username,
            "display_name": user.display_name,
            "avatar_url": user.avatar_url,
        },
    }
