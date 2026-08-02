import uuid
import pytest
from app.services.challenge_service import current_app_date
from app.worker.tasks import _process_post_media, _build_video_variants

@pytest.mark.asyncio
async def test_build_video_variants_generates_thumbnails():
    video_bytes = b"fake_mp4_video_data"
    (
        width,
        height,
        duration_sec,
        preview_buf,
        preview_size,
        preview_width,
        preview_height,
        thumb_buf,
        thumb_size,
        thumb_width,
        thumb_height,
    ) = _build_video_variants(video_bytes)

    assert width == 720
    assert height == 1280
    assert duration_sec == 10
    assert preview_size > 0
    assert thumb_size > 0
    assert preview_width == 720
    assert thumb_height == 400
    assert thumb_width == 225

@pytest.mark.asyncio
async def test_worker_processes_video_post_successfully(
    test_user,
    test_challenge,
    db_session,
    monkeypatch,
):
    from app.models.post import Post

    class DummyS3:
        def get_object(self, **kwargs):
            return {"Body": type("Stream", (), {"read": lambda self: b"fake_video_data"})()}

    async def mock_upload_async(buf, key, mime):
        return f"https://media.test/{key}"

    async def mock_add_to_feed(*args, **kwargs):
        pass

    monkeypatch.setattr("app.services.s3_service.get_s3", lambda: DummyS3())
    monkeypatch.setattr("app.worker.tasks.upload_fileobj_async", mock_upload_async)
    monkeypatch.setattr("app.worker.tasks.add_to_feed", mock_add_to_feed)

    post = Post(
        id=uuid.uuid4(),
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=current_app_date(),
        media_type="video",
        original_url="https://media.test/momenta-media/2026-08-02/orig.mp4",
        status="processing",
        processing_owner="test_worker",
    )
    db_session.add(post)
    await db_session.commit()

    await _process_post_media(db_session, post)
    await db_session.refresh(post)

    assert post.status == "active"
    assert post.media_type == "video"
    assert post.duration_sec == 10
    assert post.preview_url is not None
    assert post.thumb_url is not None
