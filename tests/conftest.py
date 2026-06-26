import asyncio
import pytest
from httpx import ASGITransport, AsyncClient
from sqlalchemy.ext.asyncio import create_async_engine, async_sessionmaker, AsyncSession
from app.main import app
from app.db import get_db
from sqlalchemy import select, text
from app.models.base import Base
from app.models.user import User
from app.models.challenge import Challenge
from app.models.post import Post
from app.models.reaction import Reaction
from app.models.report import Report
from app.models.user_streak import UserStreak
from app.models.media_asset import MediaAsset
from app.models.audit_log import AuditLog
from app.security import get_password_hash
import uuid
from datetime import date, datetime, timezone


TEST_DATABASE_URL = "sqlite+aiosqlite:///./test.db"


@pytest.fixture(scope="session")
def event_loop():
    loop = asyncio.new_event_loop()
    yield loop
    loop.close()


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
