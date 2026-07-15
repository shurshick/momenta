import asyncio
import io
import json
import logging
import os
import socket
import uuid
from datetime import datetime, timedelta, timezone
from typing import Any

from PIL import Image
from sqlalchemy import or_, select

from app.config import settings
from app.db import async_session_factory
from app.models.media_asset import MediaAsset
from app.models.post import Post
from app.services.counter_service import CounterService
from app.services.redis_service import add_to_feed, get_redis
from app.services.s3_service import make_object_key, upload_fileobj_async

logger = logging.getLogger(__name__)
WORKER_HEARTBEAT_KEY = "worker:heartbeat"
WORKER_HEARTBEAT_TTL_SECONDS = 45
WORKER_ID = f"{socket.gethostname()}:{os.getpid()}:{uuid.uuid4().hex[:8]}"


def _log_event(level: int, event: str, status: str, **fields: Any) -> None:
    payload = " ".join(f"{key}={value}" for key, value in fields.items() if value is not None)
    message = f"worker event={event} status={status}"
    if payload:
        message = f"{message} {payload}"
    logger.log(level, message)


def run_worker():
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s %(levelname)s [%(name)s] %(message)s",
        force=True,
    )
    _log_event(logging.INFO, "start", "starting")
    asyncio.run(_run_worker())


async def _run_worker():
    await asyncio.gather(_worker_loop(), _heartbeat_loop())


async def _worker_loop():
    _log_event(logging.INFO, "loop", "running")
    while True:
        try:
            processing = await process_pending_media()
            reprocessed = await reprocess_broken_posts()
            flushed_views = await flush_counters()
            _log_event(
                logging.INFO,
                "cycle",
                "done",
                processing=processing,
                reprocessed=reprocessed,
                flushed_views=flushed_views,
            )
        except Exception:
            logger.exception("worker event=cycle status=error")
        await asyncio.sleep(10)


async def _heartbeat_loop():
    while True:
        await publish_worker_heartbeat()
        await asyncio.sleep(15)


async def process_pending_media():
    try:
        post_ids = await _claim_pending_posts()
        if post_ids:
            _log_event(logging.INFO, "find_processing", "claimed", count=len(post_ids))
            await asyncio.gather(*(_process_claimed_post(post_id) for post_id in post_ids))
        return len(post_ids)
    except Exception:
        logger.exception("worker event=process_pending_media status=db_error")
        return 0


async def reprocess_broken_posts():
    try:
        post_ids = await _claim_broken_posts()
        if post_ids:
            _log_event(logging.INFO, "find_broken_posts", "claimed", count=len(post_ids))
            await asyncio.gather(*(_process_claimed_post(post_id) for post_id in post_ids))
        return len(post_ids)
    except Exception:
        logger.exception("worker event=reprocess_broken_posts status=db_error")
        return 0


async def _claim_pending_posts() -> list[uuid.UUID]:
    now = datetime.now(timezone.utc)
    stale_before = now - timedelta(seconds=max(settings.worker_lease_seconds, 30))
    async with async_session_factory() as db:
        result = await db.execute(
            select(Post)
            .where(
                Post.status == "processing",
                or_(
                    Post.processing_started_at.is_(None),
                    Post.processing_started_at < stale_before,
                ),
            )
            .order_by(Post.created_at)
            .with_for_update(skip_locked=True)
            .limit(max(settings.worker_concurrency, 1))
        )
        posts = list(result.scalars().all())
        for post in posts:
            post.processing_owner = WORKER_ID
            post.processing_started_at = now
        await db.commit()
        return [post.id for post in posts]


async def _claim_broken_posts() -> list[uuid.UUID]:
    now = datetime.now(timezone.utc)
    async with async_session_factory() as db:
        result = await db.execute(
            select(Post)
            .where(
                Post.status == "active",
                Post.media_type == "photo",
                Post.processing_started_at.is_(None),
                or_(Post.preview_url.is_(None), Post.preview_url == ""),
            )
            .order_by(Post.created_at)
            .with_for_update(skip_locked=True)
            .limit(max(settings.worker_concurrency, 1))
        )
        posts = list(result.scalars().all())
        for post in posts:
            post.status = "processing"
            post.processing_owner = WORKER_ID
            post.processing_started_at = now
        await db.commit()
        return [post.id for post in posts]


async def _process_claimed_post(post_id: uuid.UUID) -> None:
    async with async_session_factory() as db:
        post = await db.get(Post, post_id)
        if not post or post.status != "processing" or post.processing_owner != WORKER_ID:
            _log_event(logging.WARNING, "process_media", "claim_lost", post_id=post_id)
            return
        try:
            await _process_post_media(db, post)
        except Exception:
            logger.exception("worker event=process_media status=error post_id=%s", post.id)
            await _record_media_failure(db, post, RuntimeError("unexpected worker error"))


async def _process_post_media(db, post):
    post.processing_attempts = (post.processing_attempts or 0) + 1
    post.last_error = None
    await db.commit()
    _log_event(
        logging.INFO,
        "process_media",
        "started",
        post_id=post.id,
        media_type=post.media_type,
        attempt=post.processing_attempts,
        max_attempts=settings.worker_media_max_attempts,
    )
    if post.media_type != "photo":
        await _activate_post(db, post)
        return
    try:
        bucket = settings.s3_bucket
        key_marker = f"/{bucket}/"
        if key_marker in post.original_url:
            object_key = post.original_url.split(key_marker, 1)[1]
        else:
            parts = post.original_url.split("/")
            object_key = "/".join(parts[3:])
        _log_event(
            logging.INFO,
            "download_media",
            "started",
            post_id=post.id,
            object_key=object_key,
        )
        img_data = await asyncio.to_thread(_download_object, bucket, object_key)
        _log_event(logging.INFO, "download_media", "done", post_id=post.id, bytes=len(img_data))
    except Exception as exc:
        logger.exception("worker event=download_media status=error post_id=%s", post.id)
        await _record_media_failure(db, post, exc)
        return
    try:
        (
            width,
            height,
            preview_buf,
            preview_size,
            preview_width,
            preview_height,
            thumb_buf,
            thumb_size,
            thumb_width,
            thumb_height,
        ) = await asyncio.to_thread(_build_photo_variants, img_data)
        post.width = width
        post.height = height
        d = post.challenge_date
        preview_key = make_object_key(d, str(post.id), "preview", "webp")
        thumb_key = make_object_key(d, str(post.id), "thumb", "webp")
        post.preview_url = await upload_fileobj_async(preview_buf, preview_key, "image/webp")
        post.thumb_url = await upload_fileobj_async(thumb_buf, thumb_key, "image/webp")
        asset = MediaAsset(
            id=uuid.uuid4(),
            owner_user_id=post.user_id,
            post_id=post.id,
            storage_bucket=settings.s3_bucket,
            object_key=preview_key,
            public_url=post.preview_url,
            media_type="photo",
            mime_type="image/webp",
            size_bytes=preview_size,
            width=preview_width,
            height=preview_height,
            status="ready",
        )
        db.add(asset)
        db.add(
            MediaAsset(
                id=uuid.uuid4(),
                owner_user_id=post.user_id,
                post_id=post.id,
                storage_bucket=settings.s3_bucket,
                object_key=thumb_key,
                public_url=post.thumb_url,
                media_type="photo",
                mime_type="image/webp",
                size_bytes=thumb_size,
                width=thumb_width,
                height=thumb_height,
                status="ready",
            )
        )
        await _activate_post(db, post)
    except Exception as exc:
        logger.exception("worker event=process_image status=error post_id=%s", post.id)
        await _record_media_failure(db, post, exc)


async def _activate_post(db, post):
    post.status = "active"
    post.last_error = None
    post.processed_at = datetime.now(timezone.utc)
    post.processing_owner = None
    post.processing_started_at = None
    await db.commit()
    score = (
        post.created_at.timestamp() if post.created_at else datetime.now(timezone.utc).timestamp()
    )
    try:
        await add_to_feed(post.challenge_date, str(post.id), score, post.country)
    except Exception:
        logger.warning("worker event=add_to_feed status=error post_id=%s", post.id, exc_info=True)
    _log_event(
        logging.INFO,
        "process_media",
        "active",
        post_id=post.id,
        attempt=post.processing_attempts,
    )


async def _record_media_failure(db, post, exc: Exception):
    post.last_error = _safe_error_message(exc)
    if (post.processing_attempts or 0) >= settings.worker_media_max_attempts:
        post.status = "failed"
        post.processed_at = datetime.now(timezone.utc)
        status = "failed"
    else:
        post.status = "processing"
        status = "retry_pending"
    post.processing_owner = None
    post.processing_started_at = None
    await db.commit()
    _log_event(
        logging.INFO,
        "process_media",
        status,
        post_id=post.id,
        attempt=post.processing_attempts,
        max_attempts=settings.worker_media_max_attempts,
        error=post.last_error,
    )


async def retry_failed_media(db, post: Post) -> bool:
    if post.status != "failed":
        return False
    post.status = "processing"
    post.processing_attempts = 0
    post.last_error = None
    post.processed_at = None
    post.processing_owner = None
    post.processing_started_at = None
    await db.commit()
    _log_event(logging.INFO, "retry_media", "queued", post_id=post.id)
    return True


def _safe_error_message(exc: Exception) -> str:
    message = f"{exc.__class__.__name__}: {exc}"
    return message[:1000]


def _download_object(bucket: str, object_key: str) -> bytes:
    from app.services.s3_service import get_s3

    obj = get_s3().get_object(Bucket=bucket, Key=object_key)
    return obj["Body"].read()


def _build_photo_variants(img_data: bytes):
    with Image.open(io.BytesIO(img_data)) as img:
        width, height = img.size
        preview = img.copy()
        preview.thumbnail((1440, 1440), Image.LANCZOS)
        thumb = img.copy()
        thumb.thumbnail((400, 400), Image.LANCZOS)

        preview_buf = io.BytesIO()
        preview.save(preview_buf, format="WEBP", quality=85)
        preview_size = preview_buf.tell()
        preview_width, preview_height = preview.size
        preview_buf.seek(0)

        thumb_buf = io.BytesIO()
        thumb.save(thumb_buf, format="WEBP", quality=75)
        thumb_size = thumb_buf.tell()
        thumb_width, thumb_height = thumb.size
        thumb_buf.seek(0)
    return (
        width,
        height,
        preview_buf,
        preview_size,
        preview_width,
        preview_height,
        thumb_buf,
        thumb_size,
        thumb_width,
        thumb_height,
    )


async def flush_counters():
    redis = await get_redis()
    claimed: list[tuple[str, int]] = []
    flushed = 0
    try:
        async with async_session_factory() as db:
            async for key in redis.scan_iter(match="post:views:*", count=100):
                raw_count = await redis.getdel(key)
                if not raw_count:
                    continue
                count = int(raw_count)
                claimed.append((key, count))
                try:
                    post_id = uuid.UUID(key.rsplit(":", 1)[-1])
                except ValueError:
                    _log_event(logging.WARNING, "flush_views", "invalid_key", key=key)
                    continue
                result = await db.execute(select(Post).where(Post.id == post_id))
                post = result.scalar_one_or_none()
                if post:
                    await CounterService(db).add_post_views(post, count)
                    flushed += count
            await db.commit()
    except Exception:
        for key, count in claimed:
            try:
                await redis.incrby(key, count)
            except Exception:
                logger.exception("worker event=restore_views status=error key=%s", key)
        raise
    return flushed


async def publish_worker_heartbeat() -> None:
    try:
        redis = await get_redis()
        payload = json.dumps({"updated_at": datetime.now(timezone.utc).isoformat()})
        await redis.setex(WORKER_HEARTBEAT_KEY, WORKER_HEARTBEAT_TTL_SECONDS, payload)
    except Exception:
        logger.warning("worker event=heartbeat status=error", exc_info=True)


async def clean_stuck_posts():
    async with async_session_factory() as db:
        result = await db.execute(select(Post).where(Post.status == "uploading").limit(50))
        posts = result.scalars().all()
        for post in posts:
            if post.created_at:
                age = (datetime.now(timezone.utc) - post.created_at).total_seconds()
                if age > 300:
                    post.status = "failed"
                    post.last_error = "Upload did not finish within 300 seconds"
                    post.processed_at = datetime.now(timezone.utc)
        await db.commit()
