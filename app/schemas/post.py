from datetime import datetime
from pydantic import BaseModel


class PostOut(BaseModel):
    id: str
    user_id: str
    media_type: str
    preview_url: str | None = None
    thumb_url: str | None = None
    caption: str | None = None
    country: str | None = None
    city: str | None = None
    likes_count: int = 0
    comments_count: int = 0
    views_count: int = 0
    created_at: datetime | None = None

    model_config = {"from_attributes": True}


class PostFeedItem(BaseModel):
    id: str
    user: dict
    media_type: str
    preview_url: str | None = None
    thumb_url: str | None = None
    caption: str | None = None
    country: str | None = None
    city: str | None = None
    likes_count: int = 0
    comments_count: int = 0
    views_count: int = 0
    created_at: datetime | None = None


class FeedResponse(BaseModel):
    items: list[PostFeedItem]
    next_cursor: str | None = None


class CreatePostResponse(BaseModel):
    id: str
    status: str
