import uuid
from datetime import date

import pytest
from httpx import ASGITransport, AsyncClient
from sqlalchemy import text
from sqlalchemy.ext.asyncio import AsyncSession, async_sessionmaker, create_async_engine

from app.db import get_db
from app.main import app
from app.models.base import Base
from app.models.challenge import Challenge
from app.models.user import User
from app.security import get_password_hash

TEST_DATABASE_URL = "sqlite+aiosqlite:///./test.db"


@pytest.fixture(scope="session")
async def engine():
    engine = create_async_engine(TEST_DATABASE_URL, echo=False)
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    yield engine
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.drop_all)
    await engine.dispose()


@pytest.fixture(autouse=True)
async def cleanup_db(engine):
    yield
    async with engine.begin() as conn:
        for table in (
            "audit_logs", "media_assets", "reactions", "reports",
            "posts", "user_streaks", "challenges", "users",
        ):
            await conn.execute(text(f"DELETE FROM {table}"))


@pytest.fixture
async def db_session(engine):
    session_factory = async_sessionmaker(engine, class_=AsyncSession, expire_on_commit=False)
    async with session_factory() as session:
        yield session


@pytest.fixture
async def client(db_session):
    async def override_get_db():
        yield db_session

    app.dependency_overrides[get_db] = override_get_db
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        yield ac
    app.dependency_overrides.clear()


@pytest.fixture(autouse=True)
def mock_external_services(monkeypatch):
    async def noop_async(*args, **kwargs):
        return None

    async def empty_feed(*args, **kwargs):
        return []

    async def no_cached_challenge(*args, **kwargs):
        return None

    async def user_has_not_posted(*args, **kwargs):
        return False

    def fake_upload(fileobj, object_key: str, content_type: str) -> str:
        return f"https://media.test/{object_key}"

    monkeypatch.setattr("app.services.redis_service.set_today_challenge", noop_async)
    monkeypatch.setattr("app.services.redis_service.get_today_challenge", no_cached_challenge)
    monkeypatch.setattr("app.services.redis_service.add_to_feed", noop_async)
    monkeypatch.setattr("app.services.redis_service.get_feed", empty_feed)
    monkeypatch.setattr("app.services.redis_service.mark_user_posted", noop_async)
    monkeypatch.setattr("app.services.redis_service.check_user_posted", user_has_not_posted)
    monkeypatch.setattr("app.services.redis_service.flush_feed_cache", noop_async)
    monkeypatch.setattr("app.services.challenge_service.set_today_challenge", noop_async)
    monkeypatch.setattr("app.services.challenge_service.get_today_challenge", no_cached_challenge)
    monkeypatch.setattr("app.services.post_service.add_to_feed", noop_async)
    monkeypatch.setattr("app.services.post_service.mark_user_posted", noop_async)
    monkeypatch.setattr("app.api.v1.posts.upload_fileobj", fake_upload)


@pytest.fixture
async def test_user(db_session):
    user = User(
        id=uuid.uuid4(),
        username="testuser",
        email="test@example.com",
        display_name="Test User",
        password_hash=get_password_hash("password123"),
        role="user",
        status="active",
    )
    db_session.add(user)
    await db_session.commit()
    await db_session.refresh(user)
    return user


@pytest.fixture
async def test_admin(db_session):
    admin = User(
        id=uuid.uuid4(),
        username="admin",
        email="admin@example.com",
        display_name="Admin",
        password_hash=get_password_hash("admin123"),
        role="admin",
        status="active",
    )
    db_session.add(admin)
    await db_session.commit()
    await db_session.refresh(admin)
    return admin


@pytest.fixture
async def test_challenge(db_session):
    challenge = Challenge(
        id=uuid.uuid4(),
        challenge_date=date.today(),
        title_ru="Тестовый челлендж",
        description_ru="Описание",
        status="active",
    )
    db_session.add(challenge)
    await db_session.commit()
    await db_session.refresh(challenge)
    return challenge


@pytest.fixture
async def auth_headers(client, test_user):
    response = await client.post("/api/v1/auth/login", json={
        "username_or_email": "testuser",
        "password": "password123",
    })
    data = response.json()
    return {"Authorization": f"Bearer {data['access_token']}"}
