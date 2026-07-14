import uuid

import pytest

from app.models.post import Post
from app.services.challenge_service import current_app_date


async def _create_post(db_session, test_user, test_challenge, *, status="active", suffix="1"):
    post = Post(
        id=uuid.uuid4(),
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=current_app_date(),
        media_type="photo",
        original_url=f"https://example.com/bookmark-{suffix}.jpg",
        preview_url=f"https://example.com/bookmark-{suffix}.jpg",
        status=status,
    )
    db_session.add(post)
    await db_session.commit()
    return post


@pytest.mark.asyncio
async def test_bookmark_add_remove_is_idempotent(
    client, auth_headers, test_user, test_challenge, db_session
):
    post = await _create_post(db_session, test_user, test_challenge)

    first = await client.put(f"/api/v1/posts/{post.id}/bookmark", headers=auth_headers)
    second = await client.put(f"/api/v1/posts/{post.id}/bookmark", headers=auth_headers)
    feed = await client.get("/api/v1/feed/today", headers=auth_headers)
    removed = await client.delete(f"/api/v1/posts/{post.id}/bookmark", headers=auth_headers)
    removed_again = await client.delete(f"/api/v1/posts/{post.id}/bookmark", headers=auth_headers)

    assert first.status_code == 200
    assert second.status_code == 200
    assert feed.json()["items"][0]["is_bookmarked"] is True
    assert feed.json()["items"][0]["bookmarked_at"] is not None
    assert removed.status_code == 200
    assert removed_again.status_code == 200


@pytest.mark.asyncio
async def test_bookmarks_are_private_and_hide_inactive_posts(
    client, auth_headers, test_user, test_challenge, db_session
):
    active = await _create_post(db_session, test_user, test_challenge, suffix="active")
    deleted = await _create_post(
        db_session, test_user, test_challenge, status="deleted", suffix="deleted"
    )
    await client.put(f"/api/v1/posts/{active.id}/bookmark", headers=auth_headers)
    missing = await client.put(f"/api/v1/posts/{deleted.id}/bookmark", headers=auth_headers)

    register = await client.post(
        "/api/v1/auth/register",
        json={
            "username": "otheruser",
            "email": "other@example.com",
            "password": "password123",
            "display_name": "Other User",
        },
    )
    assert register.status_code in (200, 201)
    other_headers = {"Authorization": f"Bearer {register.json()['access_token']}"}

    own = await client.get("/api/v1/me/bookmarks", headers=auth_headers)
    other = await client.get("/api/v1/me/bookmarks", headers=other_headers)

    assert missing.status_code == 404
    assert [item["id"] for item in own.json()["items"]] == [str(active.id)]
    assert other.json()["items"] == []


@pytest.mark.asyncio
async def test_bookmarks_list_is_newest_first_and_paginated(
    client, auth_headers, test_user, test_challenge, db_session
):
    posts = [
        await _create_post(db_session, test_user, test_challenge, suffix=str(index))
        for index in range(3)
    ]
    for post in posts:
        await client.put(f"/api/v1/posts/{post.id}/bookmark", headers=auth_headers)

    first = await client.get("/api/v1/me/bookmarks?limit=2", headers=auth_headers)
    first_data = first.json()
    second = await client.get(
        "/api/v1/me/bookmarks",
        params={"limit": 2, "cursor": first_data["next_cursor"]},
        headers=auth_headers,
    )

    assert [item["id"] for item in first_data["items"]] == [str(posts[2].id), str(posts[1].id)]
    assert first_data["next_cursor"] is not None
    assert [item["id"] for item in second.json()["items"]] == [str(posts[0].id)]
