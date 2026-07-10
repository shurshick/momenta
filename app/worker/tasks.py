import asyncio
import io
import logging
import uuid
from datetime import datetime, timezone

from PIL import Image
from sqlalchemy import select

from app.db import async_session_factory
from app.models.media_asset import MediaAsset
from app.models.post import Post
from app.services.counter_service import CounterService
from app.services.redis_service import add_to_feed, get_redis
from app.services.s3_service import make_object_key, upload_fileobj


logger = logging.getLogger(__name__)


def run_worker():
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s %(levelname)s [%(name)s] %(message)s",
        force=True,
    )
    logger.info("worker operation=start status=starting")
    asyncio.run(_worker_loop())


async def _worker_loop():
    logger.info("worker operation=loop status=running")
    while True:
        try:
            processing = await process_pending_media()
            reprocessed = await reprocess_broken_posts()
            logger.info(
                "worker operation=cycle status=done processing=%s reprocessed=%s",
                processing,
                reprocessed,
            )
        except Exception:
            logger.exception("worker operation=cycle status=error")
        await asyncio.sleep(10)


async def process_pending_media():
    try:
        async with async_session_factory() as db:
            result = await db.execute(select(Post).where(Post.status == "processing").limit(10))
            posts = result.scalars().all()
            if posts:
                logger.info("worker operation=find_processing status=found count=%s", len(posts))
            for post in posts:
                try:
                    await _process_post_media(db, post)
                except Exception:
                    logger.exception(
                        "worker operation=process_media status=error post_id=%s",
                        post.id,
                    )
                    await _fail_post(db, post)
            return len(posts)
    except Exception:
        logger.exception("worker operation=process_pending_media status=db_error")
        return 0


async def reprocess_broken_posts():
    try:
        async with async_session_factory() as db:
            from sqlalchemy import or_

            result = await db.execute(
                select(Post)
                .where(
                    Post.status == "active",
                    Post.media_type == "photo",
                    or_(Post.preview_url.is_(None), Post.preview_url == ""),
                )
                .limit(5)
            )
            posts = result.scalars().all()
            if posts:
                logger.info("worker operation=find_broken_posts status=found count=%s", len(posts))
            for post in posts:
                try:
                    await _process_post_media(db, post)
                except Exception:
                    logger.exception(
                        "worker operation=reprocess_media status=error post_id=%s",
                        post.id,
                    )
                    await _fail_post(db, post)
            return len(posts)
    except Exception:
        logger.exception("worker operation=reprocess_broken_posts status=db_error")
        return 0


async def _process_post_media(db, post):
    if post.media_type != "photo":
        await _activate_post(db, post)
        return
    try:
        from app.config import settings
        from app.services.s3_service import get_s3

        s3 = get_s3()
        bucket = settings.s3_bucket
        key_marker = f"/{bucket}/"
        if key_marker in post.original_url:
            object_key = post.original_url.split(key_marker, 1)[1]
        else:
            parts = post.original_url.split("/")
            object_key = "/".join(parts[3:])
        logger.info("worker operation=download_media status=started post_id=%s", post.id)
        obj = s3.get_object(Bucket=bucket, Key=object_key)
        img_data = obj["Body"].read()
        logger.info(
            "worker operation=download_media status=done post_id=%s bytes=%s",
            post.id,
            len(img_data),
        )
    except Exception:
        logger.exception("worker operation=download_media status=error post_id=%s", post.id)
        await _fail_post(db, post)
        return
    try:
        img = Image.open(io.BytesIO(img_data))
        width, height = img.size
        post.width = width
        post.height = height
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
        thumb_buf.seek(0)
        d = post.challenge_date
        preview_key = make_object_key(d, str(post.id), "preview", "webp")
        thumb_key = make_object_key(d, str(post.id), "thumb", "webp")
        post.preview_url = upload_fileobj(preview_buf, preview_key, "image/webp")
        post.thumb_url = upload_fileobj(thumb_buf, thumb_key, "image/webp")
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
                width=thumb.size[0],
                height=thumb.size[1],
                status="ready",
            )
        )
        await _activate_post(db, post)
    except Exception:
        logger.exception("worker operation=process_image status=error post_id=%s", post.id)
        await _fail_post(db, post)


async def _activate_post(db, post):
    post.status = "active"
    await db.commit()
    score = (
        post.created_at.timestamp() if post.created_at else datetime.now(timezone.utc).timestamp()
    )
    await add_to_feed(post.challenge_date, str(post.id), score, post.country)


async def _fail_post(db, post):
    post.status = "failed"
    await db.commit()
    logger.info("worker operation=process_media status=failed post_id=%s", post.id)


async def flush_counters():
    async with async_session_factory() as db:
        r = await get_redis()
        keys = await r.keys("post:views:*")
        for key in keys:
            post_id = key.split(":")[-1]
            count = await r.get(key)
            if count and int(count) > 0:
                result = await db.execute(select(Post).where(Post.id == uuid.UUID(post_id)))
                post = result.scalar_one_or_none()
                if post:
                    await CounterService(db).add_post_views(post, int(count))
                await r.delete(key)
        await db.commit()


async def clean_stuck_posts():
    async with async_session_factory() as db:
        result = await db.execute(select(Post).where(Post.status == "uploading").limit(50))
        posts = result.scalars().all()
        for post in posts:
            if post.created_at:
                age = (datetime.now(timezone.utc) - post.created_at).total_seconds()
                if age > 300:
                    post.status = "failed"
        await db.commit()
