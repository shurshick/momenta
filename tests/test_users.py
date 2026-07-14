from datetime import date, timedelta

import pytest

from app.models.post import Post
from app.models.reaction import Reaction
from app.models.user import User
from app.services.counter_service import CounterService


async def _add_streak_post(db_session, user, challenge, challenge_date, status="active"):
    db_session.add(
        Post(
            user_id=user.id,
            challenge_id=challenge.id,
            challenge_date=challenge_date,
            media_type="image",
            original_url=f"https://media.test/{challenge_date}-{status}.jpg",
            preview_url=f"https://media.test/{challenge_date}-{status}-preview.jpg",
            status=status,
        )
    )


@pytest.mark.asyncio
async def test_user_suggestions_returns_active_users(client, auth_headers, test_user):
    response = await client.get("/api/v1/users/suggestions", headers=auth_headers)

    assert response.status_code == 200
    data = response.json()
    assert "items" in data
    assert any(item["username"] == test_user.username for item in data["items"])


@pytest.mark.asyncio
async def test_user_suggestions_sort_by_post_activity(
    client,
    auth_headers,
    db_session,
    test_user,
    test_challenge,
):
    quieter_user = User(
        username="quietuser",
        email="quiet@example.com",
        display_name="Quiet User",
        password_hash="test",
        role="user",
        status="active",
    )
    active_user = User(
        username="activeuser",
        email="active@example.com",
        display_name="Active User",
        password_hash="test",
        role="user",
        status="active",
    )
    db_session.add_all([quieter_user, active_user])
    await db_session.flush()
    db_session.add_all(
        [
            Post(
                user_id=active_user.id,
                challenge_id=test_challenge.id,
                challenge_date=test_challenge.challenge_date,
                media_type="image",
                original_url="https://media.test/active-one.jpg",
                preview_url="https://media.test/active-one-preview.jpg",
                status="active",
            ),
            Post(
                user_id=active_user.id,
                challenge_id=test_challenge.id,
                challenge_date=test_challenge.challenge_date,
                media_type="image",
                original_url="https://media.test/active-two.jpg",
                preview_url="https://media.test/active-two-preview.jpg",
                status="active",
            ),
            Post(
                user_id=quieter_user.id,
                challenge_id=test_challenge.id,
                challenge_date=test_challenge.challenge_date,
                media_type="image",
                original_url="https://media.test/quiet-one.jpg",
                preview_url="https://media.test/quiet-one-preview.jpg",
                status="active",
            ),
        ]
    )
    await db_session.commit()

    response = await client.get("/api/v1/users/suggestions", headers=auth_headers)

    assert response.status_code == 200
    usernames = [item["username"] for item in response.json()["items"]]
    assert usernames.index("activeuser") < usernames.index("quietuser")


@pytest.mark.asyncio
async def test_avatar_list_contains_reference_set(client):
    response = await client.get("/api/v1/avatars")

    assert response.status_code == 200
    data = response.json()
    assert len(data["items"]) == 40
    assert data["items"][0]["key"] == "avatar_01"
    assert data["items"][-1]["key"] == "avatar_40"


@pytest.mark.asyncio
async def test_my_profile_counts_likes_from_reactions_on_active_posts(
    client,
    auth_headers,
    db_session,
    test_user,
    test_challenge,
):
    active_one = Post(
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=test_challenge.challenge_date,
        media_type="image",
        original_url="https://media.test/one.jpg",
        preview_url="https://media.test/one-preview.jpg",
        likes_count=40,
        status="active",
    )
    active_two = Post(
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=test_challenge.challenge_date,
        media_type="image",
        original_url="https://media.test/two.jpg",
        preview_url="https://media.test/two-preview.jpg",
        likes_count=70,
        status="active",
    )
    deleted = Post(
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=test_challenge.challenge_date,
        media_type="image",
        original_url="https://media.test/deleted.jpg",
        preview_url="https://media.test/deleted-preview.jpg",
        likes_count=99,
        status="deleted",
    )
    db_session.add_all([active_one, active_two, deleted])
    await db_session.flush()
    db_session.add_all(
        [
            Reaction(post_id=active_one.id, user_id=test_user.id, type="like"),
            Reaction(post_id=active_two.id, user_id=test_user.id, type="like"),
            Reaction(post_id=deleted.id, user_id=test_user.id, type="like"),
        ]
    )
    await db_session.commit()

    response = await client.get("/api/v1/me/profile", headers=auth_headers)

    assert response.status_code == 200
    data = response.json()
    assert data["moments_count"] == 2
    assert data["likes_count"] == 2


@pytest.mark.asyncio
@pytest.mark.parametrize(
    ("day_offsets", "expected"),
    [
        ([0, -1, -2], 3),
        ([0, 0, -1], 2),
        ([-1, -2], 2),
        ([-1], 1),
        ([-2, -3], 0),
    ],
)
async def test_profile_streak_allows_current_day_grace_period(
    db_session,
    test_user,
    test_challenge,
    day_offsets,
    expected,
):
    today = date(2026, 7, 14)
    for offset in day_offsets:
        await _add_streak_post(
            db_session,
            test_user,
            test_challenge,
            today + timedelta(days=offset),
        )
    await db_session.commit()

    values = await CounterService(db_session).user_counter_values(test_user.id, app_date=today)
    assert values.streak_count == expected


@pytest.mark.asyncio
async def test_profile_streak_ignores_deleted_and_hidden_posts(
    db_session,
    test_user,
    test_challenge,
):
    today = date(2026, 7, 14)
    await _add_streak_post(db_session, test_user, test_challenge, today, status="deleted")
    await _add_streak_post(
        db_session,
        test_user,
        test_challenge,
        today - timedelta(days=1),
        status="hidden",
    )
    await db_session.commit()

    values = await CounterService(db_session).user_counter_values(test_user.id, app_date=today)
    assert values.streak_count == 0
