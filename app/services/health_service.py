from sqlalchemy import text

from app.config import settings
from app.db import async_session_factory


async def readiness_status() -> dict:
    status = {"status": "ok", "postgres": False, "redis": False, "s3": False}

    try:
        async with async_session_factory() as db:
            await db.execute(text("SELECT 1"))
            status["postgres"] = True
    except Exception:
        status["status"] = "degraded"

    try:
        from app.services.redis_service import get_redis

        redis = await get_redis()
        await redis.ping()
        status["redis"] = True
    except Exception:
        status["status"] = "degraded"

    try:
        from app.services.s3_service import get_s3

        s3 = get_s3()
        s3.head_bucket(Bucket=settings.s3_bucket)
        status["s3"] = True
    except Exception:
        status["status"] = "degraded"

    return status
