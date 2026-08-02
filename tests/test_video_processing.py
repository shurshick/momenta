import shutil
import subprocess
import tempfile
import uuid

import pytest
from PIL import Image

from app.services.challenge_service import current_app_date
from app.worker.tasks import _build_video_variants, _process_post_media


def _sample_video_bytes() -> bytes:
    if not shutil.which("ffmpeg") or not shutil.which("ffprobe"):
        pytest.skip("ffmpeg and ffprobe are required")
    with tempfile.TemporaryDirectory(prefix="momenta-test-video-") as temp_dir:
        path = f"{temp_dir}/sample.mp4"
        subprocess.run(
            [
                "ffmpeg",
                "-v",
                "error",
                "-y",
                "-f",
                "lavfi",
                "-i",
                "color=c=red:s=320x240:d=2",
                "-c:v",
                "mpeg4",
                path,
            ],
            check=True,
            timeout=20,
        )
        with open(path, "rb") as video_file:
            return video_file.read()

@pytest.mark.asyncio
async def test_build_video_variants_generates_thumbnails():
    video_bytes = _sample_video_bytes()
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

    assert width == 320
    assert height == 240
    assert duration_sec == 2
    assert preview_size > 0
    assert thumb_size > 0
    assert preview_width == 320
    assert preview_height == 240
    assert thumb_width == 320
    assert thumb_height == 240
    with Image.open(preview_buf) as preview:
        red, _, blue = preview.resize((1, 1)).getpixel((0, 0))
        assert red > blue

@pytest.mark.asyncio
async def test_worker_processes_video_post_successfully(
    test_user,
    test_challenge,
    db_session,
    monkeypatch,
):
    from app.models.post import Post

    video_bytes = _sample_video_bytes()

    class DummyS3:
        def get_object(self, **kwargs):
            return {"Body": type("Stream", (), {"read": lambda self: video_bytes})()}

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
    assert post.duration_sec == 2
    assert post.media_pipeline_version == 2
    assert post.preview_url is not None
    assert post.thumb_url is not None
