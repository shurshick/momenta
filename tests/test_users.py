import pytest

from app.models.post import Post


@pytest.mark.asyncio
async def test_user_suggestions_returns_active_users(client, auth_headers, test_user):
    response = await client.get("/api/v1/users/suggestions", headers=auth_headers)

    assert response.status_code == 200
    data = response.json()
    assert "items" in data
    assert any(item["username"] == test_user.username for item in data["items"])


@pytest.mark.asyncio
async def test_avatar_list_contains_reference_set(client):
    response = await client.get("/api/v1/avatars")

    assert response.status_code == 200
    data = response.json()
    assert len(data["items"]) == 40
    assert data["items"][0]["key"] == "avatar_01"
    assert data["items"][-1]["key"] == "avatar_40"


@pytest.mark.asyncio
async def test_my_profile_likes_count_sums_active_post_likes(
    client,
    auth_headers,
    db_session,
    test_user,
    test_challenge,
):
    db_session.add_all(
        [
            Post(
                user_id=test_user.id,
                challenge_id=test_challenge.id,
                challenge_date=test_challenge.challenge_date,
                media_type="image",
                original_url="https://media.test/one.jpg",
                preview_url="https://media.test/one-preview.jpg",
                likes_count=4,
                status="active",
            ),
            Post(
                user_id=test_user.id,
                challenge_id=test_challenge.id,
                challenge_date=test_challenge.challenge_date,
                media_type="image",
                original_url="https://media.test/two.jpg",
                preview_url="https://media.test/two-preview.jpg",
                likes_count=7,
                status="active",
            ),
            Post(
                user_id=test_user.id,
                challenge_id=test_challenge.id,
                challenge_date=test_challenge.challenge_date,
                media_type="image",
                original_url="https://media.test/deleted.jpg",
                preview_url="https://media.test/deleted-preview.jpg",
                likes_count=99,
                status="deleted",
            ),
        ]
    )
    await db_session.commit()

    response = await client.get("/api/v1/me/profile", headers=auth_headers)

    assert response.status_code == 200
    data = response.json()
    assert data["moments_count"] == 2
    assert data["likes_count"] == 11
