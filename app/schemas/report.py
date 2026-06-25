from pydantic import BaseModel, Field


class CreateReportRequest(BaseModel):
    reason: str = Field(..., min_length=1, max_length=200)
    comment: str | None = None
