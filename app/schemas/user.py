from datetime import datetime
from pydantic import BaseModel, Field


class UserProfile(BaseModel):
    id: str
    username: str
    display_name: str
    avatar_url: str | None = None
    country: str | None = None
    city: str | None = None
    locale: str = "ru"
    created_at: datetime | None = None
    last_seen_at: datetime | None = None

    model_config = {"from_attributes": True}


class UpdateProfileRequest(BaseModel):
    display_name: str | None = Field(None, min_length=1, max_length=100)
    avatar_url: str | None = None
    country: str | None = Field(None, max_length=2)
    city: str | None = Field(None, max_length=100)
    locale: str | None = None
