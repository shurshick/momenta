"""counters indexes and report uniqueness

Revision ID: 005
Revises: 004
Create Date: 2026-07-10
"""

from alembic import op

revision = "005"
down_revision = "004"
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.execute(
        "CREATE INDEX IF NOT EXISTS ix_posts_challenge_status_created_desc "
        "ON posts (challenge_date, status, created_at DESC)"
    )
    op.execute(
        "CREATE INDEX IF NOT EXISTS ix_posts_user_status_created_desc "
        "ON posts (user_id, status, created_at DESC)"
    )
    op.execute(
        "CREATE INDEX IF NOT EXISTS ix_posts_status_likes_created_desc "
        "ON posts (status, likes_count DESC, created_at DESC)"
    )
    op.execute(
        "CREATE INDEX IF NOT EXISTS ix_posts_status_created_desc "
        "ON posts (status, created_at DESC)"
    )
    op.execute(
        "CREATE INDEX IF NOT EXISTS ix_comments_post_status_created "
        "ON comments (post_id, status, created_at)"
    )
    op.execute(
        "CREATE INDEX IF NOT EXISTS ix_reports_post_status "
        "ON reports (post_id, status)"
    )
    op.execute(
        "CREATE UNIQUE INDEX IF NOT EXISTS uq_report_post_user "
        "ON reports (post_id, user_id)"
    )
    op.execute(
        "CREATE UNIQUE INDEX IF NOT EXISTS uq_reaction_post_user_type "
        "ON reactions (post_id, user_id, type)"
    )


def downgrade() -> None:
    op.execute("DROP INDEX IF EXISTS uq_reaction_post_user_type")
    op.execute("DROP INDEX IF EXISTS uq_report_post_user")
    op.execute("DROP INDEX IF EXISTS ix_reports_post_status")
    op.execute("DROP INDEX IF EXISTS ix_comments_post_status_created")
    op.execute("DROP INDEX IF EXISTS ix_posts_status_created_desc")
    op.execute("DROP INDEX IF EXISTS ix_posts_status_likes_created_desc")
    op.execute("DROP INDEX IF EXISTS ix_posts_user_status_created_desc")
    op.execute("DROP INDEX IF EXISTS ix_posts_challenge_status_created_desc")
