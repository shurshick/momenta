from fastapi import APIRouter

from app.config import settings
from app.services.health_service import readiness_status
from app.version import RELEASE_VERSION

router = APIRouter()


@router.get("/health")
async def health():
    return {"status": "ok"}


@router.get("/ready")
async def ready():
    return await readiness_status()


@router.get("/api/v1/meta")
async def meta():
    return {
        "name": settings.app_name,
        "version": RELEASE_VERSION,
        "environment": settings.app_env,
    }
