import uuid
from datetime import date, timedelta

import pytest


@pytest.mark.asyncio
async def test_upload_requires_auth(client):
    response = await client.post("/api/v1/posts")
    assert response.status_code == 401


@pytest.mark.asyncio
async def test_upload_creates_post(client, auth_headers, test_challenge):
    import io

    from PIL import Image
    img = Image.new("RGB", (100, 100), color="red")
    buf = io.BytesIO()
    img.save(buf, format="JPEG")
    buf.seek(0)
    files = {"media": ("test.jpg", buf, "image/jpeg")}
    data = {"challenge_id": str(test_challenge.id), "caption": "Test post"}
    response = await client.post("/api/v1/posts", files=files, data=data, headers=auth_headers)
    assert response.status_code == 200
    result = response.json()
    assert "id" in result


@pytest.mark.asyncio
async def test_second_post_same_day_rejected(
    client, auth_headers, test_challenge, test_user, db_session
):
    from app.models.post import Post
    post = Post(
        id=uuid.uuid4(),
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=date.today(),
        media_type="photo",
        original_url="https://example.com/test.jpg",
        status="active",
    )
    db_session.add(post)
    await db_session.commit()
    import io

    from PIL import Image
    img = Image.new("RGB", (100, 100), color="blue")
    buf = io.BytesIO()
    img.save(buf, format="JPEG")
    buf.seek(0)
    files = {"media": ("test2.jpg", buf, "image/jpeg")}
    data = {"challenge_id": str(test_challenge.id), "caption": "Second post"}
    response = await client.post("/api/v1/posts", files=files, data=data, headers=auth_headers)
    assert response.status_code == 409


@pytest.mark.asyncio
async def test_feed_returns_today_posts(client, auth_headers):
    response = await client.get("/api/v1/feed/today", headers=auth_headers)
    assert response.status_code == 200
    data = response.json()
    assert "items" in data


@pytest.mark.asyncio
async def test_hidden_posts_not_visible(client, auth_headers, test_user, db_session):
    pass


@pytest.mark.asyncio
async def test_best_random_returns_active_today_post(client, auth_headers, test_user, test_challenge, db_session):
    from app.models.post import Post

    post = Post(
        id=uuid.uuid4(),
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=date.today(),
        media_type="photo",
        original_url="https://example.com/original.jpg",
        preview_url="https://example.com/preview.webp",
        thumb_url="https://example.com/thumb.webp",
        likes_count=5,
        status="active",
    )
    db_session.add(post)
    await db_session.commit()

    response = await client.get("/api/v1/feed/today/best-random", headers=auth_headers)

    assert response.status_code == 200
    assert response.json()["post"]["id"] == str(post.id)


@pytest.mark.asyncio
async def test_best_random_fallback_returns_recent_active_post(client, auth_headers, test_user, test_challenge, db_session):
    from app.models.post import Post

    post = Post(
        id=uuid.uuid4(),
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=date.today() - timedelta(days=1),
        media_type="photo",
        original_url="https://example.com/original.jpg",
        preview_url="https://example.com/preview.webp",
        thumb_url="https://example.com/thumb.webp",
        status="active",
    )
    db_session.add(post)
    await db_session.commit()

    response = await client.get("/api/v1/feed/today/best-random", headers=auth_headers)

    assert response.status_code == 200
    assert response.json()["post"]["id"] == str(post.id)


@pytest.mark.asyncio
async def test_best_random_ignores_deleted_posts(client, auth_headers, test_user, test_challenge, db_session):
    from app.models.post import Post

    post = Post(
        id=uuid.uuid4(),
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=date.today(),
        media_type="photo",
        original_url="https://example.com/original.jpg",
        preview_url="https://example.com/preview.webp",
        status="deleted",
    )
    db_session.add(post)
    await db_session.commit()

    response = await client.get("/api/v1/feed/today/best-random", headers=auth_headers)

    assert response.status_code == 200
    assert response.json()["post"] is None


@pytest.mark.asyncio
async def test_feed_first_page_contains_newest_active_post(client, auth_headers, test_user, test_challenge, db_session):
    from app.models.post import Post

    older = Post(
        id=uuid.uuid4(),
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=date.today(),
        media_type="photo",
        original_url="https://example.com/older.jpg",
        preview_url="https://example.com/older.webp",
        status="active",
    )
    newer = Post(
        id=uuid.uuid4(),
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=date.today(),
        media_type="photo",
        original_url="https://example.com/newer.jpg",
        preview_url="https://example.com/newer.webp",
        status="active",
    )
    db_session.add_all([older, newer])
    await db_session.commit()

    response = await client.get("/api/v1/feed/today?limit=20", headers=auth_headers)

    assert response.status_code == 200
    ids = [item["id"] for item in response.json()["items"]]
    assert str(newer.id) in ids
