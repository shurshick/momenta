from datetime import datetime

from pydantic import BaseModel, Field


class CommentOut(BaseModel):
    id: str
    post_id: str
    user: dict
    text: str
    created_at: datetime | None = None
    is_mine: bool = False
    can_delete: bool = False


class CommentListResponse(BaseModel):
    items: list[CommentOut]


class CreateCommentRequest(BaseModel):
    text: str = Field(..., min_length=1, max_length=500)
