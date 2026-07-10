import uuid
from datetime import date, datetime
from typing import Optional

from sqlalchemy import Date, DateTime, Index, Integer, String, Text
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column

from app.models.base import Base, TimestampMixin, pk_uuid


class Post(Base, TimestampMixin):
    __tablename__ = "posts"

    id: Mapped[uuid.UUID] = pk_uuid()
    user_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), nullable=False, index=True)
    challenge_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), nullable=False, index=True)
    challenge_date: Mapped[date] = mapped_column(Date, nullable=False, index=True)
    media_type: Mapped[str] = mapped_column(String(10), nullable=False)
    original_url: Mapped[str] = mapped_column(String(500), nullable=False)
    preview_url: Mapped[Optional[str]] = mapped_column(String(500), nullable=True)
    thumb_url: Mapped[Optional[str]] = mapped_column(String(500), nullable=True)
    width: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)
    height: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)
    duration_sec: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)
    caption: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    country: Mapped[Optional[str]] = mapped_column(String(2), nullable=True)
    city: Mapped[Optional[str]] = mapped_column(String(100), nullable=True)
    likes_count: Mapped[int] = mapped_column(Integer, default=0, nullable=False)
    comments_count: Mapped[int] = mapped_column(Integer, default=0, nullable=False)
    views_count: Mapped[int] = mapped_column(Integer, default=0, nullable=False)
    reports_count: Mapped[int] = mapped_column(Integer, default=0, nullable=False)
    status: Mapped[str] = mapped_column(String(20), default="uploading", nullable=False, index=True)
    processing_attempts: Mapped[int] = mapped_column(Integer, default=0, nullable=False)
    last_error: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    processed_at: Mapped[Optional[datetime]] = mapped_column(DateTime(timezone=True), nullable=True)

    __table_args__ = ({"extend_existing": True},)


Index(
    "ix_posts_challenge_status_created_desc",
    Post.challenge_date,
    Post.status,
    Post.created_at.desc(),
)
Index("ix_posts_user_status_created_desc", Post.user_id, Post.status, Post.created_at.desc())
Index(
    "ix_posts_status_likes_created_desc",
    Post.status,
    Post.likes_count.desc(),
    Post.created_at.desc(),
)
Index("ix_posts_status_created_desc", Post.status, Post.created_at.desc())
