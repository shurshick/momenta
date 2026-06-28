import uuid
from datetime import date, timedelta

import pytest
from sqlalchemy import select

from app.models.challenge import Challenge
from app.services.challenge_service import (
    current_app_date,
    generate_auto_challenge_for_date,
    get_or_create_today_challenge,
)
from app.services.challenge_templates import AUTO_CHALLENGE_TEMPLATES


@pytest.mark.asyncio
async def test_get_today_challenge_returns_existing(client, auth_headers, test_challenge):
    response = await client.get("/api/v1/challenges/today", headers=auth_headers)

    assert response.status_code == 200
    data = response.json()
    assert data["id"] == str(test_challenge.id)
    assert data["title"] == "Тестовый челлендж"
    assert data["source"] == "manual"


@pytest.mark.asyncio
async def test_get_today_challenge_auto_creates_when_missing(client, auth_headers, db_session):
    response = await client.get("/api/v1/challenges/today", headers=auth_headers)

    assert response.status_code == 200
    data = response.json()
    assert data["title"]
    assert data["description"]
    assert data["prompt"]
    assert data["source"] == "auto"

    result = await db_session.execute(select(Challenge).where(Challenge.challenge_date == current_app_date()))
    assert result.scalar_one_or_none() is not None


@pytest.mark.asyncio
async def test_get_today_challenge_is_idempotent(client, auth_headers, db_session):
    first = await client.get("/api/v1/challenges/today", headers=auth_headers)
    second = await client.get("/api/v1/challenges/today", headers=auth_headers)

    assert first.status_code == 200
    assert second.status_code == 200
    assert first.json()["id"] == second.json()["id"]

    result = await db_session.execute(select(Challenge).where(Challenge.challenge_date == current_app_date()))
    assert len(result.scalars().all()) == 1


@pytest.mark.asyncio
async def test_get_today_challenge_unique_per_date(db_session):
    first = await get_or_create_today_challenge(db_session, date.today())
    second = await get_or_create_today_challenge(db_session, date.today())

    assert first.id == second.id


@pytest.mark.asyncio
async def test_auto_generated_challenge_has_title_description_prompt(db_session):
    challenge = await generate_auto_challenge_for_date(db_session, date.today())

    assert challenge.title_ru
    assert challenge.description_ru
    assert challenge.prompt_ru
    assert challenge.source == "auto"
    assert challenge.status == "active"


@pytest.mark.asyncio
async def test_manual_challenge_has_priority_if_exists(client, auth_headers, test_challenge):
    response = await client.get("/api/v1/challenges/today", headers=auth_headers)

    assert response.status_code == 200
    assert response.json()["id"] == str(test_challenge.id)
    assert response.json()["source"] == "manual"


@pytest.mark.asyncio
async def test_auto_challenge_does_not_repeat_recent_titles_when_possible(db_session):
    today = date.today()
    used_templates = AUTO_CHALLENGE_TEMPLATES[:7]
    for index, template in enumerate(used_templates, start=1):
        db_session.add(
            Challenge(
                id=uuid.uuid4(),
                challenge_date=today - timedelta(days=index),
                title_ru=template.title,
                description_ru=template.description,
                prompt_ru=template.prompt,
                status="active",
                source="auto",
            )
        )
    await db_session.commit()

    challenge = await generate_auto_challenge_for_date(db_session, today)

    assert challenge.title_ru not in {template.title for template in used_templates}


@pytest.mark.asyncio
async def test_get_today_challenge_handles_unique_conflict_without_500(client, auth_headers):
    first = await client.get("/api/v1/challenges/today", headers=auth_headers)
    second = await client.get("/api/v1/challenges/today", headers=auth_headers)

    assert first.status_code == 200
    assert second.status_code == 200
    assert first.json()["id"] == second.json()["id"]


@pytest.mark.asyncio
async def test_get_challenge_by_date(client, test_challenge):
    response = await client.get(f"/api/v1/challenges/by-date/{current_app_date().isoformat()}")

    assert response.status_code == 200
    data = response.json()
    assert data["id"] == str(test_challenge.id)
    assert data["source"] == "manual"
