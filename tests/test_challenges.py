from datetime import date

import pytest


@pytest.mark.asyncio
async def test_today_challenge_exists(client, auth_headers, test_challenge):
    response = await client.get("/api/v1/challenges/today", headers=auth_headers)
    assert response.status_code == 200
    data = response.json()
    assert data["title"] == "Тестовый челлендж"


@pytest.mark.asyncio
async def test_today_challenge_auto_create(client, auth_headers, db_session):
    from sqlalchemy import select

    from app.models.challenge import Challenge
    result = await db_session.execute(
        select(Challenge).where(Challenge.challenge_date == date.today())
    )
    existing = result.scalar_one_or_none()
    if existing:
        await db_session.delete(existing)
        await db_session.commit()
    response = await client.get("/api/v1/challenges/today", headers=auth_headers)
    assert response.status_code == 200


@pytest.mark.asyncio
async def test_get_challenge_by_date(client, test_challenge):
    response = await client.get(f"/api/v1/challenges/by-date/{date.today().isoformat()}")
    assert response.status_code == 200
    data = response.json()
    assert data["id"] == str(test_challenge.id)
