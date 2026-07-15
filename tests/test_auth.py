import pytest


@pytest.mark.asyncio
async def test_register_success(client):
    response = await client.post(
        "/api/v1/auth/register",
        json={
            "username": "newuser",
            "email": "new@example.com",
            "password": "password123",
        },
    )
    assert response.status_code == 200
    data = response.json()
    assert "access_token" in data
    assert "refresh_token" in data
    assert data["token_type"] == "bearer"
    assert data["user"]["username"] == "newuser"


@pytest.mark.asyncio
async def test_register_duplicate_username(client, test_user):
    response = await client.post(
        "/api/v1/auth/register",
        json={
            "username": "testuser",
            "email": "other@example.com",
            "password": "password123",
        },
    )
    assert response.status_code == 409


@pytest.mark.asyncio
async def test_login_success(client, test_user):
    response = await client.post(
        "/api/v1/auth/login",
        json={
            "username_or_email": "testuser",
            "password": "password123",
        },
    )
    assert response.status_code == 200
    data = response.json()
    assert "access_token" in data
    assert "refresh_token" in data


@pytest.mark.asyncio
async def test_login_wrong_password(client, test_user):
    response = await client.post(
        "/api/v1/auth/login",
        json={
            "username_or_email": "testuser",
            "password": "wrongpassword",
        },
    )
    assert response.status_code == 401


@pytest.mark.asyncio
async def test_login_rate_limit_returns_retry_after(client, monkeypatch):
    from app.services.rate_limit_service import RateLimitResult

    async def deny_request(*args, **kwargs):
        return RateLimitResult(allowed=False, retry_after_seconds=37)

    monkeypatch.setattr("app.api.v1.auth.check_rate_limit", deny_request)
    response = await client.post(
        "/api/v1/auth/login",
        json={"username_or_email": "testuser", "password": "password123"},
    )

    assert response.status_code == 429
    assert response.headers["Retry-After"] == "37"


@pytest.mark.asyncio
async def test_get_me_with_token(client, auth_headers):
    response = await client.get("/api/v1/auth/me", headers=auth_headers)
    assert response.status_code == 200
    data = response.json()
    assert data["username"] == "testuser"


@pytest.mark.asyncio
async def test_get_me_without_token(client):
    response = await client.get("/api/v1/auth/me")
    assert response.status_code == 401


@pytest.mark.asyncio
async def test_invalid_token_returns_401(client):
    response = await client.get("/api/v1/auth/me", headers={"Authorization": "Bearer broken.token"})
    assert response.status_code == 401


@pytest.mark.asyncio
async def test_refresh_token_returns_access_token(client, test_user):
    login = await client.post(
        "/api/v1/auth/login",
        json={
            "username_or_email": "testuser",
            "password": "password123",
        },
    )
    refresh_token = login.json()["refresh_token"]

    response = await client.post("/api/v1/auth/refresh", json={"refresh_token": refresh_token})

    assert response.status_code == 200
    data = response.json()
    assert data["access_token"]
    assert data["refresh_token"]


@pytest.mark.asyncio
async def test_refresh_token_cannot_be_used_as_access_token(client, test_user):
    login = await client.post(
        "/api/v1/auth/login",
        json={
            "username_or_email": "testuser",
            "password": "password123",
        },
    )
    refresh_token = login.json()["refresh_token"]

    response = await client.get(
        "/api/v1/auth/me",
        headers={"Authorization": f"Bearer {refresh_token}"},
    )

    assert response.status_code == 401


@pytest.mark.asyncio
async def test_refreshed_access_token_works_with_today_challenge(client, test_user, test_challenge):
    login = await client.post(
        "/api/v1/auth/login",
        json={
            "username_or_email": "testuser",
            "password": "password123",
        },
    )
    refresh = await client.post(
        "/api/v1/auth/refresh",
        json={"refresh_token": login.json()["refresh_token"]},
    )
    access_token = refresh.json()["access_token"]

    response = await client.get(
        "/api/v1/challenges/today",
        headers={"Authorization": f"Bearer {access_token}"},
    )

    assert response.status_code == 200
    assert response.json()["title"] == "Тестовый челлендж"
