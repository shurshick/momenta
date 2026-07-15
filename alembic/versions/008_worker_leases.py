"""worker processing leases

Revision ID: 008
Revises: 007
Create Date: 2026-07-15
"""

from alembic import op

revision = "008"
down_revision = "007"
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.execute("ALTER TABLE posts ADD COLUMN IF NOT EXISTS processing_owner VARCHAR(100)")
    op.execute(
        "ALTER TABLE posts ADD COLUMN IF NOT EXISTS processing_started_at "
        "TIMESTAMP WITH TIME ZONE"
    )
    op.execute(
        "CREATE INDEX IF NOT EXISTS ix_posts_processing_claim "
        "ON posts (status, processing_started_at, created_at)"
    )


def downgrade() -> None:
    op.execute("DROP INDEX IF EXISTS ix_posts_processing_claim")
    op.execute("ALTER TABLE posts DROP COLUMN IF EXISTS processing_started_at")
    op.execute("ALTER TABLE posts DROP COLUMN IF EXISTS processing_owner")
