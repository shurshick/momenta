from datetime import datetime, timedelta, timezone

from app.config import settings
from app.models.setting import Setting
from app.schemas.app_update import AppLatestResponse
from app.services.app_update_service import (
    ANDROID_UPDATE_CACHE_KEY,
    ANDROID_UPDATE_FETCHED_AT_KEY,
    fetch_latest_android_update_from_github,
)


def sample_update(version_name: str = "0.2.60", version_code: int = 60) -> AppLatestResponse:
    return AppLatestResponse(
        app_name="Момент",
        package_name="com.bghitech.momenta",
        platform="android",
        channel="stable",
        version_name=version_name,
        version_code=version_code,
        min_supported_version_code=1,
        mandatory=False,
        apk_url=f"https://github.com/shurshick/momenta/releases/download/v{version_name}/app-prod-debug.apk",
        apk_sha256="26fc5a418f37d41f336e690b1711ce4d833a6a31e2713fc81486170888bfcb0d",
        apk_size_bytes=27253273,
        release_url=f"https://github.com/shurshick/momenta/releases/tag/v{version_name}",
        release_notes=["Тестовое обновление"],
        published_at="2026-07-11T00:00:00Z",
    )


async def test_app_latest_is_public(client, monkeypatch):
    async def fake_fetch():
        return sample_update()

    monkeypatch.setattr(
        "app.services.app_update_service.fetch_latest_android_update_from_github",
        fake_fetch,
    )

    response = await client.get("/api/v1/app/latest")

    assert response.status_code == 200
    data = response.json()
    assert data["app_name"] == settings.app_latest_android_app_name
    assert data["package_name"] == "com.bghitech.momenta"
    assert data["platform"] == "android"
    assert data["version_name"] == "0.2.60"
    assert data["version_code"] == 60
    assert data["apk_url"].endswith("/app-prod-debug.apk")
    assert "jwt" not in data
    assert "secret" not in data


async def test_app_latest_uses_fresh_cache(client, db_session, monkeypatch):
    cached = sample_update("9.9.9", 999)
    db_session.add(Setting(key=ANDROID_UPDATE_CACHE_KEY, value=cached.model_dump_json()))
    db_session.add(
        Setting(
            key=ANDROID_UPDATE_FETCHED_AT_KEY,
            value=datetime.now(timezone.utc).isoformat(),
        )
    )
    await db_session.commit()

    async def fail_fetch():
        raise AssertionError("fresh cache should avoid GitHub fetch")

    monkeypatch.setattr(
        "app.services.app_update_service.fetch_latest_android_update_from_github",
        fail_fetch,
    )

    response = await client.get("/api/v1/app/latest")

    assert response.status_code == 200
    data = response.json()
    assert data["version_name"] == "9.9.9"
    assert data["version_code"] == 999


async def test_app_latest_returns_stale_cache_when_github_fails(client, db_session, monkeypatch):
    cached = sample_update("8.8.8", 888)
    db_session.add(Setting(key=ANDROID_UPDATE_CACHE_KEY, value=cached.model_dump_json()))
    db_session.add(
        Setting(
            key=ANDROID_UPDATE_FETCHED_AT_KEY,
            value=(datetime.now(timezone.utc) - timedelta(hours=1)).isoformat(),
        )
    )
    await db_session.commit()

    async def fail_fetch():
        raise RuntimeError("github is down")

    monkeypatch.setattr(
        "app.services.app_update_service.fetch_latest_android_update_from_github",
        fail_fetch,
    )

    response = await client.get("/api/v1/app/latest")

    assert response.status_code == 200
    data = response.json()
    assert data["version_name"] == "8.8.8"
    assert data["version_code"] == 888


async def test_app_latest_returns_503_without_github_or_cache(client, monkeypatch):
    async def fail_fetch():
        raise RuntimeError("github is down")

    monkeypatch.setattr(
        "app.services.app_update_service.fetch_latest_android_update_from_github",
        fail_fetch,
    )

    response = await client.get("/api/v1/app/latest")

    assert response.status_code == 503


async def test_fetch_latest_android_update_skips_server_only_releases(monkeypatch):
    metadata = sample_update("0.2.60", 60).model_dump()

    class FakeResponse:
        def __init__(self, payload):
            self.payload = payload

        def raise_for_status(self):
            return None

        def json(self):
            return self.payload

    class FakeClient:
        async def __aenter__(self):
            return self

        async def __aexit__(self, *args):
            return None

        async def get(self, url, params=None):
            if url.endswith("/repos/shurshick/momenta/releases"):
                return FakeResponse(
                    [
                        {"tag_name": "v0.2.61", "draft": False, "prerelease": False, "assets": []},
                        {
                            "tag_name": "v0.2.60",
                            "draft": False,
                            "prerelease": False,
                            "assets": [
                                {
                                    "name": "android-update.json",
                                    "browser_download_url": "https://example.test/android-update.json",
                                }
                            ],
                        },
                    ]
                )
            assert url == "https://example.test/android-update.json"
            return FakeResponse(metadata)

    monkeypatch.setattr(
        "app.services.app_update_service.httpx.AsyncClient",
        lambda **kwargs: FakeClient(),
    )

    latest = await fetch_latest_android_update_from_github()

    assert latest.version_name == "0.2.60"
    assert latest.version_code == 60


def test_empty_cache_ttl_env_uses_default(monkeypatch):
    monkeypatch.setenv("APP_UPDATE_CACHE_TTL_SECONDS", "")

    parsed = settings.__class__()

    assert parsed.app_update_cache_ttl_seconds == 300
    monkeypatch.delenv("APP_UPDATE_CACHE_TTL_SECONDS", raising=False)
