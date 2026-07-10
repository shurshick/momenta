import uuid

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.ext.asyncio import AsyncSession

from app.api.v1.auth import get_current_user_id
from app.db import get_db
from app.models.report import Report
from app.schemas.report import CreateReportRequest
from app.services.counter_service import CounterService
from app.services.post_service import get_post_by_id

router = APIRouter(prefix="/api/v1/posts", tags=["reports"])


@router.post("/{post_id}/report")
async def create_report(
    post_id: str,
    req: CreateReportRequest,
    user_id: str = Depends(get_current_user_id),
    db: AsyncSession = Depends(get_db),
):
    post = await get_post_by_id(db, uuid.UUID(post_id))
    if not post:
        raise HTTPException(status_code=404, detail="Post not found")
    post_uuid = uuid.UUID(post_id)
    user_uuid = uuid.UUID(user_id)
    existing = await db.execute(
        select(Report.id).where(
            Report.post_id == post_uuid,
            Report.user_id == user_uuid,
        )
    )
    if existing.scalar_one_or_none():
        raise HTTPException(status_code=409, detail="Post already reported by this user")
    report = Report(
        id=uuid.uuid4(),
        post_id=post_uuid,
        user_id=user_uuid,
        reason=req.reason,
        comment=req.comment,
    )
    db.add(report)
    try:
        await db.flush()
    except IntegrityError:
        await db.rollback()
        raise HTTPException(status_code=409, detail="Post already reported by this user")
    await CounterService(db).sync_post_reports(post)
    await db.commit()
    return {"status": "reported"}
