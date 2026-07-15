from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def test_upload_endpoint_does_not_read_entire_media_into_memory():
    source = (ROOT / "app/api/v1/posts.py").read_text(encoding="utf-8")

    assert "await media.read()" not in source
    assert "upload_fileobj_async(media.file" in source
