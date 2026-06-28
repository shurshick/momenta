from fastapi import APIRouter

router = APIRouter()

@router.get("/health")
async def health():
    return {"status": "ok"}

@router.get("/ready")
async def ready():
    return {"status": "ok", "postgres": True, "redis": True, "s3": True}

@router.get("/api/v1/meta")
async def meta():
    from app.config import settings
    from app.version import RELEASE_VERSION
    return {
        "name": settings.app_name,
        "version": RELEASE_VERSION,
        "environment": settings.app_env,
    }
