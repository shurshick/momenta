"""private post bookmarks

Revision ID: 007
Revises: 006
Create Date: 2026-07-14
"""

from alembic import op

revision = "007"
down_revision = "006"
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.execute(
        "CREATE TABLE IF NOT EXISTS bookmarks ("
        "id UUID PRIMARY KEY, "
        "user_id UUID NOT NULL, "
        "post_id UUID NOT NULL, "
        "created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(), "
        "CONSTRAINT uq_bookmark_user_post UNIQUE (user_id, post_id))"
    )
    op.execute(
        "CREATE INDEX IF NOT EXISTS ix_bookmarks_user_created_desc "
        "ON bookmarks (user_id, created_at DESC)"
    )
    op.execute("CREATE INDEX IF NOT EXISTS ix_bookmarks_post_id ON bookmarks (post_id)")


def downgrade() -> None:
    op.execute("DROP TABLE IF EXISTS bookmarks")
