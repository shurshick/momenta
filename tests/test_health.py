import pytest


@pytest.mark.asyncio
async def test_health_returns_ok(client):
    response = await client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "ok"


@pytest.mark.asyncio
async def test_ready_check(client):
    response = await client.get("/ready")
    assert response.status_code == 200


@pytest.mark.asyncio
async def test_meta(client):
    response = await client.get("/api/v1/meta")
    assert response.status_code == 200
    data = response.json()
    assert "name" in data
    assert "version" in data
