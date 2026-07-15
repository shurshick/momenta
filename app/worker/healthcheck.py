import asyncio
import sys

from app.services.redis_service import close_redis, get_redis
from app.worker.tasks import WORKER_HEARTBEAT_KEY


async def _check() -> bool:
    try:
        redis = await get_redis()
        return await redis.exists(WORKER_HEARTBEAT_KEY) == 1
    finally:
        await close_redis()


if __name__ == "__main__":
    sys.exit(0 if asyncio.run(_check()) else 1)
