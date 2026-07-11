from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession

from app.db import get_db
from app.schemas.app_update import AppLatestResponse
from app.services.app_update_service import get_latest_android_update

router = APIRouter(prefix="/api/v1/app", tags=["app"])


@router.get("/latest", response_model=AppLatestResponse)
async def get_latest_android_app(db: AsyncSession = Depends(get_db)) -> AppLatestResponse:
    try:
        return await get_latest_android_update(db)
    except Exception as exc:
        raise HTTPException(
            status_code=503,
            detail="Android update metadata is temporarily unavailable",
        ) from exc
