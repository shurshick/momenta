import uuid
from datetime import date, datetime, timezone
from pathlib import Path
from typing import Optional

from fastapi import APIRouter, Depends, Form, HTTPException, Query, Request
from fastapi.responses import HTMLResponse, RedirectResponse
from fastapi.templating import Jinja2Templates
from sqlalchemy import desc, func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.config import settings
from app.db import get_db
from app.models.audit_log import AuditLog
from app.models.challenge import Challenge
from app.models.media_asset import MediaAsset
from app.models.post import Post
from app.models.report import Report
from app.models.user import User
from app.security import create_access_token, decode_token, verify_password
from app.services.auth_service import get_user_by_id
from app.services.challenge_service import create_challenge, current_app_date, get_challenge_by_date
from app.services.s3_service import ensure_bucket
from app.services.setting_service import get_all_settings, invalidate_cache, set_setting
from app.version import RELEASE_VERSION

router = APIRouter(prefix="/admin", tags=["admin"])
templates = Jinja2Templates(directory=str(Path(__file__).parent / "templates"))
templates.env.globals["app_version"] = RELEASE_VERSION
templates.env.globals["app_env"] = settings.app_env


async def get_admin_user(request: Request, db: AsyncSession = Depends(get_db)) -> Optional[User]:
    token = request.cookies.get("admin_token")
    if not token:
        return None
    payload = decode_token(token)
    if not payload or payload.get("type") != "admin":
        return None
    user_id = payload.get("sub")
    if not user_id:
        return None
    user = await get_user_by_id(db, uuid.UUID(user_id))
    if not user or user.role not in ("admin", "moderator") or user.status != "active":
        return None
    return user


async def require_admin(request: Request, db: AsyncSession = Depends(get_db)):
    user = await get_admin_user(request, db)
    if not user:
        raise HTTPException(status_code=303, headers={"Location": "/admin/login"})
    return user


async def log_audit(
    db: AsyncSession,
    actor_user_id: Optional[uuid.UUID],
    action: str,
    entity_type: str,
    entity_id: Optional[uuid.UUID] = None,
    ip_address: Optional[str] = None,
    user_agent: Optional[str] = None,
    payload: Optional[dict] = None,
):
    log = AuditLog(
        id=uuid.uuid4(),
        actor_user_id=actor_user_id,
        action=action,
        entity_type=entity_type,
        entity_id=entity_id,
        ip_address=ip_address,
        user_agent=user_agent,
        payload_json=payload,
    )
    db.add(log)
    await db.commit()


@router.get("/login", response_class=HTMLResponse)
async def admin_login_page(request: Request):
    return templates.TemplateResponse(request, "login.html", {"error": None})


@router.post("/login")
async def admin_login(
    request: Request,
    username: str = Form(...),
    password: str = Form(...),
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(
        select(User).where(User.username == username, User.role.in_(["admin", "moderator"]))
    )
    user = result.scalar_one_or_none()
    if not user or not verify_password(password, user.password_hash):
        return templates.TemplateResponse(
            request, "login.html", {"error": "Invalid credentials"}, status_code=401
        )
    if user.status != "active":
        return templates.TemplateResponse(
            request, "login.html", {"error": "Account is disabled"}, status_code=403
        )
    token = create_access_token(
        {"sub": str(user.id), "role": user.role, "type": "admin"}, expires_delta=None
    )
    await log_audit(
        db,
        user.id,
        "admin_login",
        "user",
        user.id,
        request.client.host if request.client else None,
        request.headers.get("user-agent"),
    )
    response = RedirectResponse(url="/admin", status_code=303)
    response.set_cookie(key="admin_token", value=token, httponly=True, max_age=86400, secure=False)
    return response


@router.get("/logout")
async def admin_logout():
    response = RedirectResponse(url="/admin/login", status_code=303)
    response.delete_cookie("admin_token")
    return response


@router.get("", response_class=HTMLResponse)
@router.get("/", response_class=HTMLResponse)
async def admin_dashboard(
    request: Request, db: AsyncSession = Depends(get_db), admin: User = Depends(require_admin)
):
    total_users = (await db.execute(select(func.count(User.id)))).scalar() or 0
    today = current_app_date()
    posts_today = (
        await db.execute(
            select(func.count(Post.id)).where(Post.challenge_date == today, Post.status == "active")
        )
    ).scalar() or 0
    new_reports = (
        await db.execute(select(func.count(Report.id)).where(Report.status == "new"))
    ).scalar() or 0
    challenge = await db.execute(
        select(Challenge).where(Challenge.challenge_date == today, Challenge.status == "active")
    )
    challenge_obj = challenge.scalar_one_or_none()
    return templates.TemplateResponse(
        request,
        "dashboard.html",
        {
            "admin": admin,
            "total_users": total_users,
            "posts_today": posts_today,
            "new_reports": new_reports,
            "challenge_today": challenge_obj.title_ru if challenge_obj else "No challenge today",
        },
    )


@router.get("/users", response_class=HTMLResponse)
async def admin_users(
    request: Request,
    search: Optional[str] = Query(None),
    db: AsyncSession = Depends(get_db),
    admin: User = Depends(require_admin),
):
    query = select(User).order_by(desc(User.created_at)).limit(100)
    if search:
        query = (
            select(User)
            .where((User.username.ilike(f"%{search}%")) | (User.email.ilike(f"%{search}%")))
            .order_by(desc(User.created_at))
        )
    result = await db.execute(query)
    users = result.scalars().all()
    return templates.TemplateResponse(
        request, "users.html", {"admin": admin, "users": users, "search": search}
    )


@router.post("/users/{user_id}/toggle-status")
async def admin_toggle_user_status(
    user_id: str,
    request: Request,
    db: AsyncSession = Depends(get_db),
    admin: User = Depends(require_admin),
):
    user = await get_user_by_id(db, uuid.UUID(user_id))
    if not user:
        raise HTTPException(status_code=404)
    user.status = "disabled" if user.status == "active" else "active"
    await log_audit(
        db,
        admin.id,
        "toggle_user_status",
        "user",
        user.id,
        request.client.host if request.client else None,
        request.headers.get("user-agent"),
        {"new_status": user.status},
    )
    await db.commit()
    return RedirectResponse(url="/admin/users", status_code=303)


@router.post("/users/{user_id}/set-role")
async def admin_set_user_role(
    user_id: str,
    role: str = Form(...),
    request: Request = None,
    db: AsyncSession = Depends(get_db),
    admin: User = Depends(require_admin),
):
    if role not in ("user", "moderator", "admin"):
        raise HTTPException(status_code=400)
    user = await get_user_by_id(db, uuid.UUID(user_id))
    if not user:
        raise HTTPException(status_code=404)
    user.role = role
    await log_audit(
        db,
        admin.id,
        "set_user_role",
        "user",
        user.id,
        request.client.host if request.client else None,
        request.headers.get("user-agent"),
        {"new_role": role},
    )
    await db.commit()
    return RedirectResponse(url="/admin/users", status_code=303)


@router.get("/challenges", response_class=HTMLResponse)
async def admin_challenges(
    request: Request, db: AsyncSession = Depends(get_db), admin: User = Depends(require_admin)
):
    result = await db.execute(select(Challenge).order_by(desc(Challenge.challenge_date)).limit(100))
    challenges = result.scalars().all()
    return templates.TemplateResponse(
        request, "challenges.html", {"admin": admin, "challenges": challenges}
    )


@router.post("/challenges/create")
async def admin_create_challenge(
    request: Request,
    challenge_date: str = Form(...),
    title_ru: str = Form(...),
    description_ru: Optional[str] = Form(None),
    status: str = Form("draft"),
    db: AsyncSession = Depends(get_db),
    admin: User = Depends(require_admin),
):
    try:
        d = date.fromisoformat(challenge_date)
        challenge = await create_challenge(
            db, d, title_ru, description_ru, status=status, created_by=admin.id
        )
        await log_audit(
            db,
            admin.id,
            "create_challenge",
            "challenge",
            challenge.id,
            request.client.host if request.client else None,
            request.headers.get("user-agent"),
        )
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    return RedirectResponse(url="/admin/challenges", status_code=303)


@router.post("/challenges/create-fallback")
async def admin_create_fallback_challenge(
    request: Request, db: AsyncSession = Depends(get_db), admin: User = Depends(require_admin)
):
    today = current_app_date()
    existing = await get_challenge_by_date(db, today)
    if existing:
        return RedirectResponse(url="/admin/challenges", status_code=303)
    challenge = await create_challenge(
        db,
        today,
        "Момент дня",
        "Запечатли свой момент сегодня",
        status="active",
        created_by=admin.id,
    )
    await log_audit(db, admin.id, "create_fallback_challenge", "challenge", challenge.id)
    return RedirectResponse(url="/admin/challenges", status_code=303)


@router.get("/posts", response_class=HTMLResponse)
async def admin_posts(
    request: Request,
    status_filter: Optional[str] = Query(None),
    date_filter: Optional[str] = Query(None),
    db: AsyncSession = Depends(get_db),
    admin: User = Depends(require_admin),
):
    query = select(Post).order_by(desc(Post.created_at)).limit(100)
    if status_filter:
        query = query.where(Post.status == status_filter)
    if date_filter:
        try:
            d = date.fromisoformat(date_filter)
            query = query.where(Post.challenge_date == d)
        except ValueError:
            pass
    result = await db.execute(query)
    posts = result.scalars().all()
    return templates.TemplateResponse(
        request, "posts.html", {"admin": admin, "posts": posts, "status_filter": status_filter}
    )


@router.post("/posts/{post_id}/hide")
async def admin_hide_post(
    post_id: str,
    request: Request,
    db: AsyncSession = Depends(get_db),
    admin: User = Depends(require_admin),
):
    result = await db.execute(select(Post).where(Post.id == uuid.UUID(post_id)))
    post = result.scalar_one_or_none()
    if not post:
        raise HTTPException(status_code=404)
    post.status = "hidden"
    await log_audit(
        db,
        admin.id,
        "hide_post",
        "post",
        post.id,
        request.client.host if request.client else None,
        request.headers.get("user-agent"),
    )
    await db.commit()
    return RedirectResponse(url="/admin/posts", status_code=303)


@router.post("/posts/{post_id}/restore")
async def admin_restore_post(
    post_id: str,
    request: Request,
    db: AsyncSession = Depends(get_db),
    admin: User = Depends(require_admin),
):
    result = await db.execute(select(Post).where(Post.id == uuid.UUID(post_id)))
    post = result.scalar_one_or_none()
    if not post:
        raise HTTPException(status_code=404)
    post.status = "active"
    await log_audit(
        db,
        admin.id,
        "restore_post",
        "post",
        post.id,
        request.client.host if request.client else None,
        request.headers.get("user-agent"),
    )
    await db.commit()
    return RedirectResponse(url="/admin/posts", status_code=303)


@router.get("/reports", response_class=HTMLResponse)
async def admin_reports(
    request: Request,
    status_filter: Optional[str] = Query(None),
    db: AsyncSession = Depends(get_db),
    admin: User = Depends(require_admin),
):
    query = select(Report).order_by(desc(Report.created_at)).limit(100)
    if status_filter:
        query = query.where(Report.status == status_filter)
    result = await db.execute(query)
    reports = result.scalars().all()
    return templates.TemplateResponse(request, "reports.html", {"admin": admin, "reports": reports})


@router.post("/reports/{report_id}/resolve")
async def admin_resolve_report(
    report_id: str,
    action: str = Form(...),
    request: Request = None,
    db: AsyncSession = Depends(get_db),
    admin: User = Depends(require_admin),
):
    result = await db.execute(select(Report).where(Report.id == uuid.UUID(report_id)))
    report = result.scalar_one_or_none()
    if not report:
        raise HTTPException(status_code=404)
    report.status = action
    report.reviewed_by = admin.id
    report.reviewed_at = datetime.now(timezone.utc)
    if action == "hide_post":
        post_result = await db.execute(select(Post).where(Post.id == report.post_id))
        post = post_result.scalar_one_or_none()
        if post:
            post.status = "hidden"
    elif action == "disable_user":
        post_result = await db.execute(select(Post).where(Post.id == report.post_id))
        post = post_result.scalar_one_or_none()
        if post:
            user_result = await db.execute(select(User).where(User.id == post.user_id))
            user = user_result.scalar_one_or_none()
            if user:
                user.status = "disabled"
    await log_audit(
        db,
        admin.id,
        f"resolve_report_{action}",
        "report",
        report.id,
        request.client.host if request.client else None,
        request.headers.get("user-agent"),
    )
    await db.commit()
    return RedirectResponse(url="/admin/reports", status_code=303)


@router.get("/media", response_class=HTMLResponse)
async def admin_media(
    request: Request, db: AsyncSession = Depends(get_db), admin: User = Depends(require_admin)
):
    result = await db.execute(select(MediaAsset).order_by(desc(MediaAsset.created_at)).limit(100))
    assets = result.scalars().all()
    return templates.TemplateResponse(request, "media.html", {"admin": admin, "assets": assets})


@router.get("/audit-log", response_class=HTMLResponse)
async def admin_audit_log(
    request: Request, db: AsyncSession = Depends(get_db), admin: User = Depends(require_admin)
):
    result = await db.execute(select(AuditLog).order_by(desc(AuditLog.created_at)).limit(200))
    logs = result.scalars().all()
    return templates.TemplateResponse(request, "audit_log.html", {"admin": admin, "logs": logs})


@router.get("/system", response_class=HTMLResponse)
async def admin_system(request: Request, admin: User = Depends(require_admin)):
    env_summary = {
        k: v
        for k, v in sorted(settings.model_dump().items())
        if "secret" not in k.lower()
        and "password" not in k.lower()
        and "key" not in k.lower()
        and "jwt" not in k.lower()
    }
    return templates.TemplateResponse(
        request, "system.html", {"admin": admin, "settings": env_summary}
    )


@router.post("/system/flush-feed")
async def admin_flush_feed(
    request: Request, db: AsyncSession = Depends(get_db), admin: User = Depends(require_admin)
):
    from app.services.redis_service import flush_feed_cache

    await flush_feed_cache(current_app_date())
    await log_audit(
        db,
        admin.id,
        "flush_feed_cache",
        "system",
        ip_address=request.client.host if request.client else None,
        user_agent=request.headers.get("user-agent"),
    )
    return RedirectResponse(url="/admin/system", status_code=303)


@router.post("/system/init-bucket")
async def admin_init_bucket(request: Request, admin: User = Depends(require_admin)):
    ensure_bucket()
    return RedirectResponse(url="/admin/system", status_code=303)


@router.get("/settings", response_class=HTMLResponse)
async def admin_settings_page(
    request: Request, db: AsyncSession = Depends(get_db), admin: User = Depends(require_admin)
):
    all_settings = await get_all_settings(db)
    return templates.TemplateResponse(
        request, "settings.html", {"admin": admin, "settings": all_settings, "saved": False}
    )


@router.post("/settings")
async def admin_save_settings(
    request: Request, db: AsyncSession = Depends(get_db), admin: User = Depends(require_admin)
):
    form = await request.form()
    daily_post_limit = form.get("daily_post_limit", "1")
    delete_window_minutes = form.get("post_delete_window_minutes", "60")
    try:
        val = int(daily_post_limit)
        if val < 0:
            val = 1
    except (ValueError, TypeError):
        val = 1
    try:
        delete_window = int(delete_window_minutes)
        if delete_window < 0:
            delete_window = 60
    except (ValueError, TypeError):
        delete_window = 60
    await set_setting(db, "daily_post_limit", str(val))
    await set_setting(db, "post_delete_window_minutes", str(delete_window))
    invalidate_cache("daily_post_limit")
    invalidate_cache("post_delete_window_minutes")
    await log_audit(
        db,
        admin.id,
        "update_settings",
        "system",
        ip_address=request.client.host if request.client else None,
        user_agent=request.headers.get("user-agent"),
        payload={"daily_post_limit": val, "post_delete_window_minutes": delete_window},
    )
    all_settings = await get_all_settings(db)
    return templates.TemplateResponse(
        request, "settings.html", {"admin": admin, "settings": all_settings, "saved": True}
    )
