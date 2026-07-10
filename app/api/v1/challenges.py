import uuid
from datetime import date

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession

from app.api.v1.auth import get_optional_current_user_id
from app.db import get_db
from app.services.challenge_service import (
    get_challenge_by_date,
    get_challenge_by_id,
    get_today_challenge_data,
)

router = APIRouter(prefix="/api/v1/challenges", tags=["challenges"])


@router.get("/today")
async def today_challenge(
    user_id: str | None = Depends(get_optional_current_user_id),
    db: AsyncSession = Depends(get_db),
):
    return await get_today_challenge_data(db, user_id)


@router.get("/by-date/{challenge_date}")
async def get_challenge_by_date_endpoint(challenge_date: str, db: AsyncSession = Depends(get_db)):
    try:
        d = date.fromisoformat(challenge_date)
    except ValueError:
        raise HTTPException(status_code=400, detail="Invalid date format")
    challenge = await get_challenge_by_date(db, d)
    if not challenge:
        raise HTTPException(status_code=404)
    return {
        "id": str(challenge.id),
        "date": challenge.challenge_date,
        "challenge_date": challenge.challenge_date,
        "title_ru": challenge.title_ru,
        "description_ru": challenge.description_ru,
        "prompt_ru": challenge.prompt_ru,
        "source": challenge.source,
        "status": challenge.status,
    }


@router.get("/{challenge_id}")
async def get_challenge(challenge_id: str, db: AsyncSession = Depends(get_db)):
    challenge = await get_challenge_by_id(db, uuid.UUID(challenge_id))
    if not challenge:
        raise HTTPException(status_code=404)
    return {
        "id": str(challenge.id),
        "date": challenge.challenge_date,
        "challenge_date": challenge.challenge_date,
        "title_ru": challenge.title_ru,
        "description_ru": challenge.description_ru,
        "prompt_ru": challenge.prompt_ru,
        "source": challenge.source,
        "status": challenge.status,
    }
