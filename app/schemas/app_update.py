from pydantic import BaseModel, Field


class AppLatestResponse(BaseModel):
    app_name: str
    package_name: str
    platform: str = "android"
    channel: str = "stable"
    version_name: str
    version_code: int = Field(ge=1)
    min_supported_version_code: int = Field(ge=1)
    mandatory: bool = False
    apk_url: str
    apk_sha256: str | None = None
    apk_size_bytes: int | None = Field(default=None, ge=0)
    release_url: str | None = None
    release_notes: list[str] = Field(default_factory=list)
    published_at: str
