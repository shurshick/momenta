import uuid
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from app.db import get_db
from app.schemas.report import CreateReportRequest
from app.models.report import Report
from app.models.post import Post
from app.services.post_service import get_post_by_id
from app.api.v1.auth import get_current_user_id

router = APIRouter(prefix="/api/v1/posts", tags=["reports"])


@router.post("/{post_id}/report")
async def create_report(post_id: str, req: CreateReportRequest, user_id: str = Depends(get_current_user_id),
                        db: AsyncSession = Depends(get_db)):
    post = await get_post_by_id(db, uuid.UUID(post_id))
    if not post:
        raise HTTPException(status_code=404, detail="Post not found")
    report = Report(
        id=uuid.uuid4(),
        post_id=uuid.UUID(post_id),
        user_id=uuid.UUID(user_id),
        reason=req.reason,
        comment=req.comment,
    )
    db.add(report)
    post.reports_count += 1
    await db.commit()
    return {"status": "reported"}
