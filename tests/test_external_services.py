import asyncio
import io
import os
import uuid

import pytest


@pytest.mark.asyncio
async def test_real_redis_rate_limit(monkeypatch):
    redis_url = os.getenv("TEST_REDIS_URL")
    if not redis_url:
        pytest.skip("Redis integration environment is not configured")

    import redis.asyncio as aioredis

    from app.services.rate_limit_service import check_rate_limit

    redis = aioredis.from_url(redis_url, decode_responses=True)

    async def integration_redis():
        return redis

    monkeypatch.setattr("app.services.rate_limit_service.get_redis", integration_redis)
    identifier = f"ci-{uuid.uuid4()}"
    try:
        first = await check_rate_limit("integration", identifier, 2, 60)
        second = await check_rate_limit("integration", identifier, 2, 60)
        third = await check_rate_limit("integration", identifier, 2, 60)
    finally:
        await redis.aclose()

    assert first.allowed is True
    assert second.allowed is True
    assert third.allowed is False
    assert third.retry_after_seconds > 0


@pytest.mark.asyncio
async def test_real_minio_upload_download_delete():
    if not os.getenv("TEST_S3_INTEGRATION"):
        pytest.skip("MinIO integration environment is not configured")

    from app.config import settings
    from app.services.s3_service import (
        delete_object_async,
        ensure_bucket,
        get_s3,
        upload_fileobj_async,
    )

    await asyncio.to_thread(ensure_bucket)
    object_key = f"ci/{uuid.uuid4()}.txt"
    await upload_fileobj_async(io.BytesIO(b"momenta-ci"), object_key, "text/plain")
    response = await asyncio.to_thread(
        get_s3().get_object,
        Bucket=settings.s3_bucket,
        Key=object_key,
    )
    body = await asyncio.to_thread(response["Body"].read)
    await delete_object_async(object_key)

    assert body == b"momenta-ci"
