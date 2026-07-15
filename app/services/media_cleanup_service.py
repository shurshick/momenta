import logging
from dataclasses import dataclass
from datetime import datetime, timedelta, timezone

from sqlalchemy import and_, or_, select

from app.models.media_asset import MediaAsset
from app.models.post import Post
from app.services.s3_service import delete_object_async

logger = logging.getLogger(__name__)


@dataclass
class MediaCleanupResult:
    dry_run: bool
    assets_checked: int = 0
    assets_deleted: int = 0
    delete_failures: int = 0


async def cleanup_media_for_session(
    db,
    *,
    dry_run: bool = False,
    older_than_days: int = 30,
) -> MediaCleanupResult:
    cutoff = datetime.now(timezone.utc) - timedelta(days=max(older_than_days, 0))
    query = (
        select(MediaAsset)
        .outerjoin(Post, Post.id == MediaAsset.post_id)
        .where(
            MediaAsset.status != "deleted",
            or_(
                and_(Post.id.is_(None), MediaAsset.created_at <= cutoff),
                and_(Post.status == "deleted", Post.updated_at <= cutoff),
            ),
        )
    )
    assets = list((await db.execute(query)).scalars().all())
    result = MediaCleanupResult(dry_run=dry_run, assets_checked=len(assets))
    if dry_run:
        return result

    for asset in assets:
        try:
            await delete_object_async(asset.object_key)
        except Exception:
            result.delete_failures += 1
            logger.exception("Failed to delete media object: asset_id=%s", asset.id)
            continue
        asset.status = "deleted"
        result.assets_deleted += 1
    await db.commit()
    return result
