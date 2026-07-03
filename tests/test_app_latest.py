from app.config import settings


async def test_app_latest_is_public(client):
    response = await client.get("/api/v1/app/latest")

    assert response.status_code == 200
    data = response.json()
    assert data["app_name"] == settings.app_latest_android_app_name
    assert data["package_name"] == "com.bghitech.momenta"
    assert data["platform"] == "android"
    assert data["version_name"] == settings.app_latest_android_version_name
    assert data["version_code"] == settings.app_latest_android_version_code
    assert data["apk_url"].endswith(".apk")
    assert "jwt" not in data
    assert "secret" not in data


async def test_app_latest_uses_config_values(client, monkeypatch):
    monkeypatch.setattr(settings, "app_latest_android_version_name", "9.9.9")
    monkeypatch.setattr(settings, "app_latest_android_version_code", 999)
    monkeypatch.setattr(settings, "app_latest_android_mandatory", True)
    monkeypatch.setattr(settings, "app_latest_android_apk_sha256", "abc123")
    monkeypatch.setattr(settings, "app_latest_android_apk_size_bytes", 123456)

    response = await client.get("/api/v1/app/latest")

    assert response.status_code == 200
    data = response.json()
    assert data["version_name"] == "9.9.9"
    assert data["version_code"] == 999
    assert data["mandatory"] is True
    assert data["apk_sha256"] == "abc123"
    assert data["apk_size_bytes"] == 123456
