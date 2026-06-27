import pytest


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
    assert len(data["items"]) == 25
    assert data["items"][0]["key"] == "avatar_01"
    assert data["items"][-1]["key"] == "avatar_25"
