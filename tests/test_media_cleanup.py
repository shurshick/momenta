import uuid
from datetime import datetime, timedelta, timezone

import pytest

from app.models.media_asset import MediaAsset
from app.models.post import Post
from app.services.challenge_service import current_app_date
from app.services.media_cleanup_service import cleanup_media_for_session


@pytest.mark.asyncio
async def test_cleanup_media_dry_run_and_delete(
    test_user, test_challenge, db_session, monkeypatch
):
    old_date = datetime.now(timezone.utc) - timedelta(days=40)
    post = Post(
        id=uuid.uuid4(),
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=current_app_date(),
        media_type="photo",
        original_url="https://media.test/original.jpg",
        status="deleted",
        updated_at=old_date,
    )
    asset = MediaAsset(
        owner_user_id=test_user.id,
        post_id=post.id,
        storage_bucket="momenta-media",
        object_key="media/original.jpg",
        public_url="https://media.test/original.jpg",
        media_type="photo",
        mime_type="image/jpeg",
        size_bytes=100,
        status="uploaded",
        created_at=old_date,
        updated_at=old_date,
    )
    db_session.add_all([post, asset])
    await db_session.commit()

    deleted_keys = []

    async def record_delete(object_key):
        deleted_keys.append(object_key)

    monkeypatch.setattr("app.services.media_cleanup_service.delete_object_async", record_delete)

    dry_run = await cleanup_media_for_session(db_session, dry_run=True, older_than_days=30)
    assert dry_run.assets_checked == 1
    assert dry_run.assets_deleted == 0
    assert deleted_keys == []

    result = await cleanup_media_for_session(db_session, older_than_days=30)
    await db_session.refresh(asset)
    assert result.assets_deleted == 1
    assert result.delete_failures == 0
    assert asset.status == "deleted"
    assert deleted_keys == ["media/original.jpg"]
