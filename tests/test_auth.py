import pytest


@pytest.mark.asyncio
async def test_register_success(client):
    response = await client.post("/api/v1/auth/register", json={
        "username": "newuser",
        "email": "new@example.com",
        "password": "password123",
    })
    assert response.status_code == 200
    data = response.json()
    assert "access_token" in data
    assert "refresh_token" in data
    assert data["token_type"] == "bearer"
    assert data["user"]["username"] == "newuser"


@pytest.mark.asyncio
async def test_register_duplicate_username(client, test_user):
    response = await client.post("/api/v1/auth/register", json={
        "username": "testuser",
        "email": "other@example.com",
        "password": "password123",
    })
    assert response.status_code == 409


@pytest.mark.asyncio
async def test_login_success(client, test_user):
    response = await client.post("/api/v1/auth/login", json={
        "username_or_email": "testuser",
        "password": "password123",
    })
    assert response.status_code == 200
    data = response.json()
    assert "access_token" in data


@pytest.mark.asyncio
async def test_login_wrong_password(client, test_user):
    response = await client.post("/api/v1/auth/login", json={
        "username_or_email": "testuser",
        "password": "wrongpassword",
    })
    assert response.status_code == 401


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
