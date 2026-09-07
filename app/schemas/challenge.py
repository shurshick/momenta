from datetime import date as Date
from datetime import datetime

from pydantic import BaseModel


class ChallengeOut(BaseModel):
    id: str
    date: Date
    challenge_date: Date | None = None
    title: str
    description: str | None = None
    prompt: str | None = None
    source: str = "manual"
    ends_at: datetime | None = None
    user_posted: bool = False
    participants_count: int = 0

    model_config = {"from_attributes": True}


class ChallengeAdminOut(BaseModel):
    id: str
    challenge_date: Date
    title_ru: str
    description_ru: str | None = None
    prompt_ru: str | None = None
    title_en: str | None = None
    description_en: str | None = None
    prompt_en: str | None = None
    cover_url: str | None = None
    source: str = "manual"
    status: str
    created_by: str | None = None
    created_at: datetime | None = None
    updated_at: datetime | None = None

    model_config = {"from_attributes": True}


class CreateChallengeRequest(BaseModel):
    challenge_date: Date
    title_ru: str
    description_ru: str | None = None
    prompt_ru: str | None = None
    title_en: str | None = None
    description_en: str | None = None
    prompt_en: str | None = None
    cover_url: str | None = None
    status: str = "draft"
