import json
from datetime import date, datetime, timezone
from typing import Optional
import redis.asyncio as aioredis
from app.config import settings

redis_client: Optional[aioredis.Redis] = None


async def get_redis() -> aioredis.Redis:
    global redis_client
    if redis_client is None:
        redis_client = aioredis.from_url(settings.redis_url, decode_responses=True)
    return redis_client


async def close_redis():
    global redis_client
    if redis_client:
        await redis_client.close()
        redis_client = None


async def set_today_challenge(d: date, challenge_data: dict, ttl_seconds: int = 3600):
    r = await get_redis()
    key = f"today:challenge:{d.isoformat()}"
    await r.setex(key, ttl_seconds, json.dumps(challenge_data))


async def get_today_challenge(d: date) -> Optional[dict]:
    r = await get_redis()
    key = f"today:challenge:{d.isoformat()}"
    data = await r.get(key)
    return json.loads(data) if data else None


async def add_to_feed(d: date, post_id: str, score: float, country: Optional[str] = None):
    r = await get_redis()
    pipe = r.pipeline()
    global_key = f"feed:today:global:{d.isoformat()}"
    await pipe.zadd(global_key, {post_id: score})
    if country:
        country_key = f"feed:today:country:{country}:{d.isoformat()}"
        await pipe.zadd(country_key, {post_id: score})
    ttl_seconds = int((datetime.now(timezone.utc).replace(hour=23, minute=59, second=59) - datetime.now(timezone.utc)).total_seconds()) + 3600
    await pipe.expire(global_key, ttl_seconds)
    await pipe.execute()


async def get_feed(d: date, start: int = 0, stop: int = 19, country: Optional[str] = None) -> list[str]:
    r = await get_redis()
    if country:
        key = f"feed:today:country:{country}:{d.isoformat()}"
    else:
        key = f"feed:today:global:{d.isoformat()}"
    results = await r.zrevrange(key, start, stop)
    return list(results)


async def mark_user_posted(user_id: str, d: date, ttl_seconds: int = 86400):
    r = await get_redis()
    key = f"user:posted:{user_id}:{d.isoformat()}"
    await r.setex(key, ttl_seconds, "1")


async def check_user_posted(user_id: str, d: date) -> bool:
    r = await get_redis()
    key = f"user:posted:{user_id}:{d.isoformat()}"
    return await r.exists(key) > 0


async def increment_counter(key: str, ttl_seconds: Optional[int] = None) -> int:
    r = await get_redis()
    val = await r.incr(key)
    if ttl_seconds and val == 1:
        await r.expire(key, ttl_seconds)
    return val


async def get_counter(key: str) -> int:
    r = await get_redis()
    val = await r.get(key)
    return int(val) if val else 0


async def flush_feed_cache(d: date):
    r = await get_redis()
    await r.delete(f"feed:today:global:{d.isoformat()}")


def make_rate_key(prefix: str, identifier: str) -> str:
    return f"rate:{prefix}:{identifier}"
