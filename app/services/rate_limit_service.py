import hashlib
import logging
from dataclasses import dataclass

from app.services.redis_service import get_redis, make_rate_key

logger = logging.getLogger(__name__)

_RATE_LIMIT_SCRIPT = """
local current = redis.call('INCR', KEYS[1])
if current == 1 then
    redis.call('EXPIRE', KEYS[1], ARGV[1])
end
local ttl = redis.call('TTL', KEYS[1])
return {current, ttl}
"""


@dataclass(frozen=True)
class RateLimitResult:
    allowed: bool
    retry_after_seconds: int


async def check_rate_limit(
    scope: str,
    identifier: str,
    limit: int,
    window_seconds: int,
) -> RateLimitResult:
    if limit <= 0:
        return RateLimitResult(allowed=True, retry_after_seconds=0)

    digest = hashlib.sha256(identifier.encode("utf-8")).hexdigest()[:32]
    key = make_rate_key(scope, digest)
    try:
        redis = await get_redis()
        current, ttl = await redis.eval(_RATE_LIMIT_SCRIPT, 1, key, window_seconds)
    except Exception:
        logger.warning("Rate limit unavailable: scope=%s", scope, exc_info=True)
        return RateLimitResult(allowed=True, retry_after_seconds=0)

    allowed = int(current) <= limit
    return RateLimitResult(
        allowed=allowed,
        retry_after_seconds=max(int(ttl), 1) if not allowed else 0,
    )
