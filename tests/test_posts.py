import uuid
from datetime import date, datetime, timedelta, timezone

import pytest

from app.services.challenge_service import current_app_date
from app.services.setting_service import set_setting


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
async def test_upload_today_uses_app_date(client, auth_headers, db_session, monkeypatch):
    import io

    from PIL import Image

    from app.models.post import Post

    app_date = date(2026, 7, 8)
    monkeypatch.setattr("app.api.v1.posts.current_app_date", lambda: app_date)

    img = Image.new("RGB", (100, 100), color="green")
    buf = io.BytesIO()
    img.save(buf, format="JPEG")
    buf.seek(0)

    files = {"media": ("today.jpg", buf, "image/jpeg")}
    data = {"challenge_id": "today", "caption": "Today post"}
    response = await client.post("/api/v1/posts", files=files, data=data, headers=auth_headers)

    assert response.status_code == 200
    post = await db_session.get(Post, uuid.UUID(response.json()["id"]))
    assert post is not None
    assert post.challenge_date == app_date


@pytest.mark.asyncio
async def test_second_post_same_day_rejected(
    client, auth_headers, test_challenge, test_user, db_session
):
    from app.models.post import Post

    post = Post(
        id=uuid.uuid4(),
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=current_app_date(),
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
async def test_daily_post_limit_allows_configured_number(
    client, auth_headers, test_challenge, test_user, db_session
):
    import io

    from PIL import Image

    from app.models.post import Post

    await set_setting(db_session, "daily_post_limit", "2")
    db_session.add(
        Post(
            id=uuid.uuid4(),
            user_id=test_user.id,
            challenge_id=test_challenge.id,
            challenge_date=current_app_date(),
            media_type="photo",
            original_url="https://example.com/first.jpg",
            status="active",
        )
    )
    await db_session.commit()

    img = Image.new("RGB", (100, 100), color="green")
    buf = io.BytesIO()
    img.save(buf, format="JPEG")
    buf.seek(0)
    files = {"media": ("second.jpg", buf, "image/jpeg")}
    data = {"challenge_id": str(test_challenge.id), "caption": "Second allowed post"}

    response = await client.post("/api/v1/posts", files=files, data=data, headers=auth_headers)

    assert response.status_code == 200


@pytest.mark.asyncio
async def test_daily_post_limit_rejects_after_configured_number(
    client, auth_headers, test_challenge, test_user, db_session
):
    import io

    from PIL import Image

    from app.models.post import Post

    await set_setting(db_session, "daily_post_limit", "2")
    db_session.add_all(
        [
            Post(
                id=uuid.uuid4(),
                user_id=test_user.id,
                challenge_id=test_challenge.id,
                challenge_date=current_app_date(),
                media_type="photo",
                original_url="https://example.com/first.jpg",
                status="active",
            ),
            Post(
                id=uuid.uuid4(),
                user_id=test_user.id,
                challenge_id=test_challenge.id,
                challenge_date=current_app_date(),
                media_type="photo",
                original_url="https://example.com/second.jpg",
                status="processing",
            ),
        ]
    )
    await db_session.commit()

    img = Image.new("RGB", (100, 100), color="blue")
    buf = io.BytesIO()
    img.save(buf, format="JPEG")
    buf.seek(0)
    files = {"media": ("third.jpg", buf, "image/jpeg")}
    data = {"challenge_id": str(test_challenge.id), "caption": "Third rejected post"}

    response = await client.post("/api/v1/posts", files=files, data=data, headers=auth_headers)

    assert response.status_code == 409
    assert response.json()["detail"] == "Лимит 2 моментов в день исчерпан"


@pytest.mark.asyncio
async def test_feed_returns_today_posts(client, auth_headers):
    response = await client.get("/api/v1/feed/today", headers=auth_headers)
    assert response.status_code == 200
    data = response.json()
    assert "items" in data


@pytest.mark.asyncio
@pytest.mark.parametrize("status", ["hidden", "processing", "failed", "deleted"])
async def test_non_active_post_not_visible_by_id(
    client, auth_headers, test_user, test_challenge, db_session, status
):
    from app.models.post import Post

    post = Post(
        id=uuid.uuid4(),
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=current_app_date(),
        media_type="photo",
        original_url="https://example.com/original.jpg",
        preview_url="https://example.com/preview.webp",
        status=status,
    )
    db_session.add(post)
    await db_session.commit()

    response = await client.get(f"/api/v1/posts/{post.id}", headers=auth_headers)

    assert response.status_code == 404


@pytest.mark.asyncio
async def test_best_returns_highest_rated_active_today_post(
    client, auth_headers, test_user, test_challenge, db_session
):
    from app.models.post import Post

    post = Post(
        id=uuid.uuid4(),
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=current_app_date(),
        media_type="photo",
        original_url="https://example.com/original.jpg",
        preview_url="https://example.com/preview.webp",
        thumb_url="https://example.com/thumb.webp",
        likes_count=5,
        status="active",
    )
    db_session.add(post)
    db_session.add(
        Post(
            id=uuid.uuid4(),
            user_id=test_user.id,
            challenge_id=test_challenge.id,
            challenge_date=current_app_date(),
            media_type="photo",
            original_url="https://example.com/other-original.jpg",
            preview_url="https://example.com/other-preview.webp",
            likes_count=2,
            status="active",
        )
    )
    await db_session.commit()

    response = await client.get("/api/v1/feed/today/best", headers=auth_headers)

    assert response.status_code == 200
    assert response.json()["post"]["id"] == str(post.id)


@pytest.mark.asyncio
async def test_best_random_does_not_fallback_to_previous_day_post(
    client, auth_headers, test_user, test_challenge, db_session
):
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
    assert response.json()["post"] is None


@pytest.mark.asyncio
async def test_best_random_ignores_deleted_posts(
    client, auth_headers, test_user, test_challenge, db_session
):
    from app.models.post import Post

    post = Post(
        id=uuid.uuid4(),
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=current_app_date(),
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
async def test_feed_first_page_contains_newest_active_post(
    client, auth_headers, test_user, test_challenge, db_session
):
    from app.models.post import Post

    older = Post(
        id=uuid.uuid4(),
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=current_app_date(),
        media_type="photo",
        original_url="https://example.com/older.jpg",
        preview_url="https://example.com/older.webp",
        status="active",
    )
    newer = Post(
        id=uuid.uuid4(),
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=current_app_date(),
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


@pytest.mark.asyncio
async def test_today_feed_does_not_include_previous_days_when_today_empty(
    client, auth_headers, test_user, test_challenge, db_session
):
    from app.models.post import Post

    post = Post(
        id=uuid.uuid4(),
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=current_app_date() - timedelta(days=1),
        media_type="photo",
        original_url="https://example.com/recent.jpg",
        preview_url="https://example.com/recent.webp",
        status="active",
    )
    db_session.add(post)
    await db_session.commit()

    response = await client.get("/api/v1/feed/today?limit=20", headers=auth_headers)

    assert response.status_code == 200
    assert response.json()["items"] == []


@pytest.mark.asyncio
async def test_today_feed_accepts_z_cursor(
    client, auth_headers, test_user, test_challenge, db_session
):
    from app.models.post import Post

    post = Post(
        id=uuid.uuid4(),
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=current_app_date(),
        media_type="photo",
        original_url="https://example.com/newer.jpg",
        preview_url="https://example.com/newer.webp",
        status="active",
    )
    db_session.add(post)
    await db_session.commit()

    response = await client.get(
        "/api/v1/feed/today?cursor=2999-01-01T00:00:00Z",
        headers=auth_headers,
    )

    assert response.status_code == 200
    assert response.json()["items"][0]["id"] == str(post.id)


@pytest.mark.asyncio
async def test_like_recalculates_drifted_counter(
    client,
    auth_headers,
    test_user,
    test_challenge,
    db_session,
):
    from app.models.post import Post

    post = Post(
        id=uuid.uuid4(),
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=current_app_date(),
        media_type="photo",
        original_url="https://example.com/drift.jpg",
        likes_count=99,
        status="active",
    )
    db_session.add(post)
    await db_session.commit()

    response = await client.post(f"/api/v1/posts/{post.id}/like", headers=auth_headers)

    assert response.status_code == 200
    assert response.json()["likes_count"] == 1


@pytest.mark.asyncio
async def test_like_unlike_counter_service(
    client,
    auth_headers,
    test_user,
    test_challenge,
    db_session,
):
    from app.models.post import Post

    post = Post(
        id=uuid.uuid4(),
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=current_app_date(),
        media_type="photo",
        original_url="https://example.com/counter.jpg",
        status="active",
    )
    db_session.add(post)
    await db_session.commit()

    like_response = await client.post(f"/api/v1/posts/{post.id}/like", headers=auth_headers)
    assert like_response.status_code == 200
    assert like_response.json()["likes_count"] == 1
    await db_session.refresh(post)
    assert post.likes_count == 1

    unlike_response = await client.delete(f"/api/v1/posts/{post.id}/like", headers=auth_headers)
    assert unlike_response.status_code == 200
    assert unlike_response.json()["likes_count"] == 0
    await db_session.refresh(post)
    assert post.likes_count == 0


@pytest.mark.asyncio
async def test_comment_create_delete_counter_service(
    client,
    auth_headers,
    test_user,
    test_challenge,
    db_session,
):
    from app.models.post import Post

    post = Post(
        id=uuid.uuid4(),
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=current_app_date(),
        media_type="photo",
        original_url="https://example.com/comments.jpg",
        comments_count=99,
        status="active",
    )
    db_session.add(post)
    await db_session.commit()

    create_response = await client.post(
        f"/api/v1/posts/{post.id}/comments",
        headers=auth_headers,
        json={"text": "hello"},
    )
    assert create_response.status_code == 200
    comment_id = create_response.json()["id"]
    await db_session.refresh(post)
    assert post.comments_count == 1

    delete_response = await client.delete(
        f"/api/v1/posts/{post.id}/comments/{comment_id}",
        headers=auth_headers,
    )
    assert delete_response.status_code == 200
    await db_session.refresh(post)
    assert post.comments_count == 0


@pytest.mark.asyncio
async def test_soft_delete_post_updates_counts(
    client,
    auth_headers,
    test_user,
    test_challenge,
    db_session,
):
    from app.models.post import Post

    post = Post(
        id=uuid.uuid4(),
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=current_app_date(),
        media_type="photo",
        original_url="https://example.com/delete-counts.jpg",
        status="active",
    )
    db_session.add(post)
    await db_session.commit()

    before = await client.get("/api/v1/me/profile", headers=auth_headers)
    assert before.status_code == 200
    assert before.json()["moments_count"] == 1

    delete_response = await client.delete(f"/api/v1/posts/{post.id}", headers=auth_headers)
    assert delete_response.status_code == 200
    await db_session.refresh(post)
    assert post.status == "deleted"

    after = await client.get("/api/v1/me/profile", headers=auth_headers)
    assert after.status_code == 200
    assert after.json()["moments_count"] == 0


@pytest.mark.asyncio
async def test_delete_post_is_rejected_after_configured_window(
    client,
    auth_headers,
    test_user,
    test_challenge,
    db_session,
):
    from app.models.post import Post

    await set_setting(db_session, "post_delete_window_minutes", "60")
    post = Post(
        id=uuid.uuid4(),
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=current_app_date(),
        media_type="photo",
        original_url="https://example.com/old.jpg",
        status="active",
        created_at=datetime.now(timezone.utc) - timedelta(minutes=61),
    )
    db_session.add(post)
    await db_session.commit()

    response = await client.delete(f"/api/v1/posts/{post.id}", headers=auth_headers)

    assert response.status_code == 403


@pytest.mark.asyncio
async def test_delete_post_uses_configured_window(
    client,
    auth_headers,
    test_user,
    test_challenge,
    db_session,
):
    from app.models.post import Post

    await set_setting(db_session, "post_delete_window_minutes", "120")
    post = Post(
        id=uuid.uuid4(),
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=current_app_date(),
        media_type="photo",
        original_url="https://example.com/recent.jpg",
        status="active",
        created_at=datetime.now(timezone.utc) - timedelta(minutes=90),
    )
    db_session.add(post)
    await db_session.commit()

    response = await client.delete(f"/api/v1/posts/{post.id}", headers=auth_headers)

    assert response.status_code == 200
    assert response.json()["status"] == "deleted"
