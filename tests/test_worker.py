import uuid

import pytest

from app.services.challenge_service import current_app_date
from app.worker.tasks import _process_post_media, retry_failed_media


@pytest.mark.asyncio
async def test_worker_marks_unavailable_photo_media_failed(
    client,
    auth_headers,
    test_user,
    test_challenge,
    db_session,
    monkeypatch,
):
    from app.models.post import Post

    class BrokenS3:
        def get_object(self, **kwargs):
            raise RuntimeError("media unavailable")

    async def fail_if_added_to_feed(*args, **kwargs):
        raise AssertionError("failed media must not be added to feed")

    monkeypatch.setattr("app.services.s3_service.get_s3", lambda: BrokenS3())
    monkeypatch.setattr("app.worker.tasks.add_to_feed", fail_if_added_to_feed)

    post = Post(
        id=uuid.uuid4(),
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=current_app_date(),
        media_type="photo",
        original_url="https://media.test/original.jpg",
        status="processing",
    )
    db_session.add(post)
    await db_session.commit()

    await _process_post_media(db_session, post)
    await db_session.refresh(post)
    assert post.status == "processing"
    assert post.processing_attempts == 1
    assert "media unavailable" in post.last_error

    await _process_post_media(db_session, post)
    await db_session.refresh(post)
    assert post.status == "processing"
    assert post.processing_attempts == 2

    await _process_post_media(db_session, post)
    await db_session.refresh(post)

    assert post.status == "failed"
    assert post.processing_attempts == 3
    assert "media unavailable" in post.last_error

    response = await client.get("/api/v1/feed/today", headers=auth_headers)
    assert response.status_code == 200
    assert response.json()["items"] == []


@pytest.mark.asyncio
async def test_retry_failed_media_resets_post_for_processing(test_user, test_challenge, db_session):
    from datetime import datetime, timezone

    from app.models.post import Post

    post = Post(
        id=uuid.uuid4(),
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=current_app_date(),
        media_type="photo",
        original_url="https://media.test/original.jpg",
        status="failed",
        processing_attempts=3,
        last_error="RuntimeError: media unavailable",
        processed_at=datetime.now(timezone.utc),
    )
    db_session.add(post)
    await db_session.commit()

    assert await retry_failed_media(db_session, post) is True
    await db_session.refresh(post)

    assert post.status == "processing"
    assert post.processing_attempts == 0
    assert post.last_error is None
    assert post.processed_at is None
