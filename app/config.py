from pydantic_settings import BaseSettings
from typing import List
from app.version import RELEASE_VERSION


class Settings(BaseSettings):
    app_name: str = "Momenta"
    app_env: str = "development"
    app_version: str = RELEASE_VERSION
    app_timezone: str = "Europe/Moscow"
    app_latest_android_app_name: str = "\u041c\u043e\u043c\u0435\u043d\u0442"
    app_latest_android_package_name: str = "com.bghitech.momenta"
    app_latest_android_channel: str = "stable"
    app_latest_android_version_name: str = RELEASE_VERSION
    app_latest_android_version_code: int = 47
    app_min_supported_android_version_code: int = 1
    app_latest_android_mandatory: bool = False
    app_latest_android_apk_url: str = "https://github.com/shurshick/momenta/releases/download/v0.2.47/app-prod-debug.apk"
    app_latest_android_apk_sha256: str = "c048062241d1dafc207d43bdd3f7116aefb6aaa2f03b6ce159b5ac610f44eea5"
    app_latest_android_apk_size_bytes: int | None = 31054139
    app_latest_android_release_url: str = "https://github.com/shurshick/momenta/releases/tag/v0.2.47"
    app_latest_android_release_notes: str = (
        "\u041c\u044f\u0433\u043a\u043e\u0435 \u043e\u0431\u043d\u043e\u0432\u043b\u0435\u043d\u0438\u0435 \u043b\u0435\u043d\u0442\u044b|"
        "\u041f\u043e\u043b\u043d\u043e\u044d\u043a\u0440\u0430\u043d\u043d\u044b\u0439 \u043f\u0440\u043e\u0441\u043c\u043e\u0442\u0440 \u043c\u043e\u043c\u0435\u043d\u0442\u0430 \u0438\u0437 \u043b\u0435\u043d\u0442\u044b|"
        "\u0418\u0441\u043f\u0440\u0430\u0432\u043b\u0435\u043d \u0432\u044b\u0445\u043e\u0434 \u0438\u0437 \u0430\u043a\u043a\u0430\u0443\u043d\u0442\u0430|"
        "\u0414\u043e\u0431\u0430\u0432\u043b\u0435\u043d\u043e \u043f\u0435\u0440\u0435\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u0435 \u043d\u0430 \u0444\u0440\u043e\u043d\u0442\u0430\u043b\u044c\u043d\u0443\u044e \u043a\u0430\u043c\u0435\u0440\u0443"
    )
    app_latest_android_published_at: str = "2026-07-09T00:00:00Z"

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

