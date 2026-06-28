import uuid
from datetime import date, datetime
from typing import Optional
from sqlalchemy import String, Text, Date, DateTime, func
from sqlalchemy.orm import Mapped, mapped_column
from sqlalchemy.dialects.postgresql import UUID
from app.models.base import Base, TimestampMixin, pk_uuid


class Challenge(Base, TimestampMixin):
    __tablename__ = "challenges"

    id: Mapped[uuid.UUID] = pk_uuid()
    challenge_date: Mapped[date] = mapped_column(Date, unique=True, nullable=False, index=True)
    title_ru: Mapped[str] = mapped_column(Text, nullable=False)
    description_ru: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    prompt_ru: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    title_en: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    description_en: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    prompt_en: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    cover_url: Mapped[Optional[str]] = mapped_column(String(500), nullable=True)
    source: Mapped[str] = mapped_column(String(20), default="manual", nullable=False, index=True)
    status: Mapped[str] = mapped_column(String(20), default="active", nullable=False, index=True)
    created_by: Mapped[Optional[uuid.UUID]] = mapped_column(UUID(as_uuid=True), nullable=True)
