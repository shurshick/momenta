import pytest


@pytest.mark.asyncio
async def test_admin_login_page(client):
    response = await client.get("/admin/login")
    assert response.status_code == 200
    assert "Momenta" in response.text


@pytest.mark.asyncio
async def test_normal_user_cannot_access_admin(client, auth_headers):
    response = await client.get("/admin/")
    assert response.status_code in (303, 401, 403)


@pytest.mark.asyncio
async def test_admin_can_create_challenge(client, test_admin, db_session):
    from app.security import create_access_token
    token = create_access_token({"sub": str(test_admin.id), "role": "admin", "type": "admin"})
    cookies = {"admin_token": token}
    import httpx
    async with httpx.AsyncClient(transport=client._transport, base_url="http://test") as ac:
        ac.cookies = cookies
        from datetime import date, timedelta
        tomorrow = (date.today() + timedelta(days=1)).isoformat()
        response = await ac.post("/admin/challenges/create", data={
            "challenge_date": tomorrow,
            "title_ru": "Admin Challenge",
            "status": "active",
        })
        assert response.status_code == 303


@pytest.mark.asyncio
async def test_admin_can_hide_post(client, test_admin, test_user, test_challenge, db_session):
    import uuid
    from app.models.post import Post
    from app.security import create_access_token
    post = Post(
        id=uuid.uuid4(),
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=date.today(),
        media_type="photo",
        original_url="https://example.com/test.jpg",
        status="active",
    )
    db_session.add(post)
    await db_session.commit()
    token = create_access_token({"sub": str(test_admin.id), "role": "admin", "type": "admin"})
    import httpx
    async with httpx.AsyncClient(transport=client._transport, base_url="http://test") as ac:
        ac.cookies = {"admin_token": token}
        response = await ac.post(f"/admin/posts/{post.id}/hide")
        assert response.status_code == 303
    await db_session.refresh(post)
    assert post.status == "hidden"


@pytest.mark.asyncio
async def test_admin_action_creates_audit_log(client, test_admin, db_session):
    from app.models.audit_log import AuditLog
    from sqlalchemy import select
    from app.security import create_access_token
    token = create_access_token({"sub": str(test_admin.id), "role": "admin", "type": "admin"})
    import httpx
    async with httpx.AsyncClient(transport=client._transport, base_url="http://test") as ac:
        ac.cookies = {"admin_token": token}
        response = await ac.post("/admin/system/flush-feed")
        assert response.status_code == 303
    result = await db_session.execute(select(AuditLog).where(AuditLog.action == "flush_feed_cache"))
    log = result.scalar_one_or_none()
    assert log is not None
