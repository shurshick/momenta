import uuid
from dataclasses import dataclass

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.comment import Comment
from app.models.post import Post
from app.models.reaction import Reaction
from app.models.report import Report


@dataclass(frozen=True)
class PostCounterValues:
    likes_count: int
    comments_count: int
    reports_count: int


@dataclass(frozen=True)
class UserCounterValues:
    moments_count: int
    likes_count: int


class CounterService:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def post_counter_values(self, post_id: uuid.UUID) -> PostCounterValues:
        likes_count = (
            await self.db.execute(
                select(func.count(Reaction.id)).where(
                    Reaction.post_id == post_id,
                    Reaction.type == "like",
                )
            )
        ).scalar() or 0
        comments_count = (
            await self.db.execute(
                select(func.count(Comment.id)).where(
                    Comment.post_id == post_id,
                    Comment.status == "active",
                )
            )
        ).scalar() or 0
        reports_count = (
            await self.db.execute(
                select(func.count(Report.id)).where(
                    Report.post_id == post_id,
                    Report.status != "deleted",
                )
            )
        ).scalar() or 0
        return PostCounterValues(
            likes_count=likes_count,
            comments_count=comments_count,
            reports_count=reports_count,
        )

    async def sync_post_counts(self, post: Post) -> PostCounterValues:
        values = await self.post_counter_values(post.id)
        post.likes_count = values.likes_count
        post.comments_count = values.comments_count
        post.reports_count = values.reports_count
        return values

    async def sync_post_likes(self, post: Post) -> int:
        likes_count = (
            await self.db.execute(
                select(func.count(Reaction.id)).where(
                    Reaction.post_id == post.id,
                    Reaction.type == "like",
                )
            )
        ).scalar() or 0
        post.likes_count = likes_count
        return likes_count

    async def sync_post_comments(self, post: Post) -> int:
        comments_count = (
            await self.db.execute(
                select(func.count(Comment.id)).where(
                    Comment.post_id == post.id,
                    Comment.status == "active",
                )
            )
        ).scalar() or 0
        post.comments_count = comments_count
        return comments_count

    async def sync_post_reports(self, post: Post) -> int:
        reports_count = (
            await self.db.execute(
                select(func.count(Report.id)).where(
                    Report.post_id == post.id,
                    Report.status != "deleted",
                )
            )
        ).scalar() or 0
        post.reports_count = reports_count
        return reports_count

    async def add_post_views(self, post: Post, delta: int) -> int:
        post.views_count = max((post.views_count or 0) + delta, 0)
        return post.views_count

    async def user_counter_values(self, user_id: uuid.UUID) -> UserCounterValues:
        moments_count = (
            await self.db.execute(
                select(func.count(Post.id)).where(
                    Post.user_id == user_id,
                    Post.status == "active",
                )
            )
        ).scalar() or 0
        likes_count = (
            await self.db.execute(
                select(func.coalesce(func.sum(Post.likes_count), 0)).where(
                    Post.user_id == user_id,
                    Post.status == "active",
                )
            )
        ).scalar() or 0
        return UserCounterValues(moments_count=moments_count, likes_count=likes_count)
