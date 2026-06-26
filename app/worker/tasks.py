import asyncio
import io
import uuid
from datetime import date, datetime, timezone
from PIL import Image
from sqlalchemy import select, desc
from app.db import async_session_factory
from app.models.post import Post
from app.models.media_asset import MediaAsset
from app.services.s3_service import upload_fileobj, make_object_key
from app.services.redis_service import get_redis, get_counter
from app.config import settings


def run_worker():
    print("[worker] Starting worker...", flush=True)
    asyncio.run(_worker_loop())


async def _worker_loop():
    print("[worker] Event loop running, entering main loop", flush=True)
    while True:
        try:
            await process_pending_media()
            await reprocess_broken_posts()
            await flush_counters()
            await clean_stuck_posts()
        except Exception as e:
            print(f"[worker] Error: {e}", flush=True)
        await asyncio.sleep(10)


async def process_pending_media():
    async with async_session_factory() as db:
        result = await db.execute(
            select(Post).where(Post.status == "processing").limit(10)
        )
        posts = result.scalars().all()
        for post in posts:
            try:
                await _process_post_media(db, post)
            except Exception as e:
                print(f"Media processing error for post {post.id}: {e}")
                post.status = "active"


async def reprocess_broken_posts():
    async with async_session_factory() as db:
        result = await db.execute(
            select(Post).where(
                Post.status == "active",
                Post.media_type == "photo",
                Post.preview_url.is_(None)
            ).limit(5)
        )
        posts = result.scalars().all()
        for post in posts:
            try:
                await _process_post_media(db, post)
            except Exception as e:
                print(f"Reprocess error for post {post.id}: {e}")


async def _process_post_media(db, post):
    if post.media_type != "photo":
        post.status = "active"
        await db.commit()
        return
    try:
        from app.services.s3_service import get_s3
        from app.config import settings
        s3 = get_s3()
        bucket = settings.s3_bucket
        key_marker = f"/{bucket}/"
        if key_marker in post.original_url:
            object_key = post.original_url.split(key_marker, 1)[1]
        else:
            parts = post.original_url.split("/")
            object_key = "/".join(parts[3:])
        print(f"[worker] Downloading s3://{bucket}/{object_key}")
        obj = s3.get_object(Bucket=bucket, Key=object_key)
        img_data = obj["Body"].read()
        print(f"[worker] Downloaded {len(img_data)} bytes")
    except Exception as e:
        print(f"Failed to download from S3: {e}")
        post.status = "active"
        await db.commit()
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
        preview_buf.seek(0)
        thumb_buf = io.BytesIO()
        thumb.save(thumb_buf, format="WEBP", quality=75)
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
            size_bytes=preview_buf.tell(),
            width=1440,
            height=int(1440 * height / width) if width else None,
            status="ready",
        )
        db.add(asset)
        post.status = "active"
        await db.commit()
    except Exception as e:
        print(f"Image processing failed: {e}")
        post.status = "active"
        await db.commit()


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
                    post.views_count += int(count)
                await r.delete(key)
        await db.commit()


async def clean_stuck_posts():
    async with async_session_factory() as db:
        result = await db.execute(
            select(Post).where(Post.status == "uploading").limit(50)
        )
        posts = result.scalars().all()
        for post in posts:
            if post.created_at:
                age = (datetime.now(timezone.utc) - post.created_at).total_seconds()
                if age > 300:
                    post.status = "active"
        await db.commit()
