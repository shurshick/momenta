from datetime import datetime
from pydantic import BaseModel, Field


class UserProfile(BaseModel):
    id: str
    username: str
    display_name: str
    avatar_key: str | None = None
    avatar_url: str | None = None
    bio: str | None = None
    country: str | None = None
    city: str | None = None
    locale: str = "ru"
    moments_count: int = 0
    streak_count: int = 0
    likes_count: int = 0
    recent_posts: list[dict] = []
    created_at: datetime | None = None
    last_seen_at: datetime | None = None

    model_config = {"from_attributes": True}


class UpdateProfileRequest(BaseModel):
    display_name: str | None = Field(None, min_length=1, max_length=100)
    bio: str | None = Field(None, max_length=500)
    avatar_url: str | None = None
    country: str | None = Field(None, max_length=2)
    city: str | None = Field(None, max_length=100)
    locale: str | None = None


class AvatarOption(BaseModel):
    key: str


class AvatarListResponse(BaseModel):
    items: list[AvatarOption]


class UpdateAvatarRequest(BaseModel):
    avatar_key: str


class UserSummary(BaseModel):
    id: str
    username: str
    display_name: str
    avatar_key: str | None = None
    avatar_url: str | None = None


class UserSummaryListResponse(BaseModel):
    items: list[UserSummary]
