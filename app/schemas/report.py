from pydantic import BaseModel, Field


class CreateReportRequest(BaseModel):
    reason: str = Field(..., pattern=r"^(spam|nudity|violence|hate|harassment|copyright|other)$")
    comment: str | None = None
