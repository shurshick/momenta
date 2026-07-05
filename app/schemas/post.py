from datetime import date, datetime
from pydantic import BaseModel


class PostOut(BaseModel):
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
    challenge_date: date | None = None
    created_at: datetime | None = None
    is_liked: bool = False
    is_mine: bool = False
    can_delete: bool = False

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
    challenge_date: date | None = None
    created_at: datetime | None = None
    is_liked: bool = False
    is_mine: bool = False
    can_delete: bool = False


class FeedResponse(BaseModel):
    items: list[PostFeedItem]
    next_cursor: str | None = None


class BestMomentResponse(BaseModel):
    post: PostFeedItem | None = None


class CreatePostResponse(BaseModel):
    id: str
    status: str
