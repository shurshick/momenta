import uuid

import pytest
from alembic.config import Config
from alembic.script import ScriptDirectory
from sqlalchemy import inspect
from sqlalchemy.exc import IntegrityError

from app.cli import repair_counters_for_session
from app.models.bookmark import Bookmark
from app.models.comment import Comment
from app.models.post import Post
from app.models.reaction import Reaction
from app.models.report import Report
from app.services.challenge_service import current_app_date


@pytest.mark.asyncio
async def test_reaction_unique_constraint(test_user, test_challenge, db_session):
    post = Post(
        id=uuid.uuid4(),
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=current_app_date(),
        media_type="photo",
        original_url="https://example.com/reaction-unique.jpg",
        status="active",
    )
    db_session.add(post)
    await db_session.commit()

    db_session.add_all(
        [
            Reaction(post_id=post.id, user_id=test_user.id, type="like"),
            Reaction(post_id=post.id, user_id=test_user.id, type="like"),
        ]
    )
    with pytest.raises(IntegrityError):
        await db_session.flush()
    await db_session.rollback()


@pytest.mark.asyncio
async def test_report_unique_constraint_or_documented_duplicate_policy(
    client,
    auth_headers,
    test_user,
    test_challenge,
    db_session,
):
    post = Post(
        id=uuid.uuid4(),
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=current_app_date(),
        media_type="photo",
        original_url="https://example.com/report-unique.jpg",
        status="active",
    )
    db_session.add(post)
    await db_session.commit()

    first = await client.post(
        f"/api/v1/posts/{post.id}/report",
        headers=auth_headers,
        json={"reason": "spam"},
    )
    second = await client.post(
        f"/api/v1/posts/{post.id}/report",
        headers=auth_headers,
        json={"reason": "spam"},
    )

    assert first.status_code == 200
    assert second.status_code == 409
    await db_session.refresh(post)
    assert post.reports_count == 1


@pytest.mark.asyncio
async def test_repair_counters_fixes_bad_values(test_user, test_challenge, db_session):
    post = Post(
        id=uuid.uuid4(),
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=current_app_date(),
        media_type="photo",
        original_url="https://example.com/repair.jpg",
        likes_count=99,
        comments_count=99,
        reports_count=99,
        status="active",
    )
    db_session.add(post)
    await db_session.flush()
    db_session.add_all(
        [
            Reaction(post_id=post.id, user_id=test_user.id, type="like"),
            Comment(post_id=post.id, user_id=test_user.id, text="repair"),
            Report(post_id=post.id, user_id=test_user.id, reason="spam"),
        ]
    )
    await db_session.commit()

    dry_run = await repair_counters_for_session(db_session, dry_run=True)
    await db_session.refresh(post)
    assert dry_run.dry_run is True
    assert dry_run.posts_changed == 1
    assert post.likes_count == 99
    assert post.comments_count == 99
    assert post.reports_count == 99

    repaired = await repair_counters_for_session(db_session, dry_run=False)
    await db_session.refresh(post)
    assert repaired.dry_run is False
    assert repaired.posts_changed == 1
    assert post.likes_count == 1
    assert post.comments_count == 1
    assert post.reports_count == 1


@pytest.mark.asyncio
async def test_feed_indexes_exist(engine):
    expected = {
        "ix_posts_challenge_status_created_desc",
        "ix_posts_user_status_created_desc",
        "ix_posts_status_likes_created_desc",
        "ix_posts_status_created_desc",
        "ix_comments_post_status_created",
        "ix_reports_post_status",
        "ix_bookmarks_user_created_desc",
        "ix_bookmarks_post_id",
    }

    async with engine.begin() as conn:
        names = await conn.run_sync(
            lambda sync_conn: {
                index["name"]
                for table in ("posts", "comments", "reports", "bookmarks")
                for index in inspect(sync_conn).get_indexes(table)
            }
        )

    assert expected <= names


def test_alembic_single_head():
    heads = ScriptDirectory.from_config(Config("alembic.ini")).get_heads()
    assert heads == ["007"]


@pytest.mark.asyncio
async def test_bookmark_unique_constraint(test_user, test_challenge, db_session):
    post = Post(
        id=uuid.uuid4(),
        user_id=test_user.id,
        challenge_id=test_challenge.id,
        challenge_date=current_app_date(),
        media_type="photo",
        original_url="https://example.com/bookmark-unique.jpg",
        status="active",
    )
    db_session.add(post)
    await db_session.commit()
    db_session.add_all(
        [
            Bookmark(post_id=post.id, user_id=test_user.id),
            Bookmark(post_id=post.id, user_id=test_user.id),
        ]
    )
    with pytest.raises(IntegrityError):
        await db_session.flush()
    await db_session.rollback()
