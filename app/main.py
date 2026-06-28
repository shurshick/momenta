import uuid
from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from sqlalchemy import select

from app.config import settings
from app.db import async_session_factory, engine
from app.models.base import Base
from app.models.setting import Setting
from app.models.user import User
from app.security import get_password_hash
from app.services.redis_service import close_redis
from app.services.s3_service import ensure_bucket
from app.version import RELEASE_VERSION


@asynccontextmanager
async def lifespan(app: FastAPI):
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
        from sqlalchemy import text

        await conn.execute(
            text(
                "CREATE TABLE IF NOT EXISTS alembic_version "
                "(version_num VARCHAR(32) NOT NULL PRIMARY KEY)"
            )
        )
        current_revision = await conn.execute(text("SELECT version_num FROM alembic_version LIMIT 1"))
        if current_revision.scalar_one_or_none() is None:
            await conn.execute(text("INSERT INTO alembic_version (version_num) VALUES ('004')"))

    async with async_session_factory() as db:
        try:
            ensure_bucket()
        except Exception:
            pass
        try:
            from app.services.challenge_service import get_or_create_today_challenge

            await get_or_create_today_challenge(db)
        except Exception:
            pass
        try:
            result = await db.execute(select(User).where(User.username == settings.admin_username))
            if not result.scalar_one_or_none():
                admin = User(
                    id=uuid.uuid4(),
                    username=settings.admin_username,
                    email=settings.admin_email,
                    display_name="Admin",
                    password_hash=get_password_hash(settings.admin_password),
                    role="admin",
                    status="active",
                )
                db.add(admin)
                await db.commit()
        except Exception:
            pass
        try:
            result = await db.execute(select(Setting).where(Setting.key == "daily_post_limit"))
            if not result.scalar_one_or_none():
                db.add(Setting(key="daily_post_limit", value="1"))
                await db.commit()
        except Exception:
            pass
    yield
    await close_redis()
    await engine.dispose()


app = FastAPI(
    title=settings.app_name,
    version=RELEASE_VERSION,
    lifespan=lifespan,
    docs_url="/docs",
    openapi_url="/openapi.json",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origin_list,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

from app.api.v1 import router as api_router
from app.admin.routes import router as admin_router

app.include_router(api_router)
app.include_router(admin_router)


@app.exception_handler(HTTPException)
async def http_exception_handler(request: Request, exc: HTTPException):
    if exc.status_code == 303:
        from fastapi.responses import RedirectResponse

        location = exc.headers.get("Location", "/admin/login") if exc.headers else "/admin/login"
        return RedirectResponse(url=location, status_code=303)
    return JSONResponse(status_code=exc.status_code, content={"detail": exc.detail})


@app.get("/health")
async def health():
    return {"status": "ok"}


@app.get("/ready")
async def ready():
    status = {"status": "ok", "postgres": False, "redis": False, "s3": False}
    try:
        async with async_session_factory() as db:
            await db.execute(select(1))
            status["postgres"] = True
    except Exception:
        status["status"] = "degraded"
    try:
        from app.services.redis_service import get_redis

        r = await get_redis()
        await r.ping()
        status["redis"] = True
    except Exception:
        status["status"] = "degraded"
    try:
        from app.services.s3_service import get_s3

        s3 = get_s3()
        s3.list_buckets()
        status["s3"] = True
    except Exception:
        status["status"] = "degraded"
    return status


@app.get("/api/v1/meta")
async def meta():
    return {
        "name": settings.app_name,
        "version": RELEASE_VERSION,
        "environment": settings.app_env,
    }
