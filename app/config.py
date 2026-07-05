from pydantic_settings import BaseSettings
from typing import List
from app.version import RELEASE_VERSION


class Settings(BaseSettings):
    app_name: str = "Momenta"
    app_env: str = "development"
    app_version: str = RELEASE_VERSION
    app_timezone: str = "Europe/Moscow"
    app_latest_android_app_name: str = "Момент"
    app_latest_android_package_name: str = "com.bghitech.momenta"
    app_latest_android_channel: str = "stable"
    app_latest_android_version_name: str = RELEASE_VERSION
    app_latest_android_version_code: int = 45
    app_min_supported_android_version_code: int = 1
    app_latest_android_mandatory: bool = False
    app_latest_android_apk_url: str = "https://github.com/shurshick/momenta/releases/download/v0.2.45/app-prod-debug.apk"
    app_latest_android_apk_sha256: str = "46082ec0a3a058768e7403cd2e266f5e3731117a92a1e6f274f01e894457270f"
    app_latest_android_apk_size_bytes: int | None = 30940653
    app_latest_android_release_url: str = "https://github.com/shurshick/momenta/releases/tag/v0.2.45"
    app_latest_android_release_notes: str = (
        "Уменьшен заголовок ленты|"
        "В профиль добавлен заголовок|"
        "Лента больше не подмешивает посты прошлых дней"
    )
    app_latest_android_published_at: str = "2026-07-05T00:00:00Z"

    api_host: str = "0.0.0.0"
    api_port: int = 8000
    public_base_url: str = "http://localhost:8000"

    database_url: str = "postgresql+psycopg://momenta:momenta_password@localhost:5432/momenta"
    redis_url: str = "redis://localhost:6379/0"

    jwt_secret: str = "change_me_jwt_secret_32charsminimum!"
    jwt_access_ttl_minutes: int = 30
    jwt_refresh_ttl_days: int = 30

    cors_origins: str = "http://localhost:3000,http://localhost:8000"

    s3_endpoint: str = "http://localhost:9000"
    s3_public_endpoint: str = "http://localhost:9000"
    s3_access_key: str = "minioadmin"
    s3_secret_key: str = "minioadmin"
    s3_bucket: str = "momenta-media"
    s3_region: str = "us-east-1"
    s3_secure: bool = False

    media_max_image_mb: int = 15
    media_max_video_mb: int = 80
    media_allowed_image_types: str = "image/jpeg,image/png,image/webp"
    media_allowed_video_types: str = "video/mp4,video/quicktime"

    admin_username: str = "admin"
    admin_email: str = "admin@example.com"
    admin_password: str = "change_me_admin_password"

    rate_limit_login_per_minute: int = 10
    rate_limit_upload_per_hour: int = 20

    worker_concurrency: int = 2

    @property
    def cors_origin_list(self) -> List[str]:
        return [o.strip() for o in self.cors_origins.split(",") if o.strip()]

    @property
    def allowed_image_types(self) -> List[str]:
        return [t.strip() for t in self.media_allowed_image_types.split(",") if t.strip()]

    @property
    def allowed_video_types(self) -> List[str]:
        return [t.strip() for t in self.media_allowed_video_types.split(",") if t.strip()]

    @property
    def app_latest_android_release_note_list(self) -> List[str]:
        return [note.strip() for note in self.app_latest_android_release_notes.split("|") if note.strip()]

    model_config = {"env_file": ".env", "env_file_encoding": "utf-8"}


settings = Settings()
