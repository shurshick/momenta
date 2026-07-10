"""media pipeline status fields

Revision ID: 006
Revises: 005
Create Date: 2026-07-10
"""

from alembic import op

revision = "006"
down_revision = "005"
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.execute("ALTER TABLE posts ADD COLUMN IF NOT EXISTS processing_attempts INTEGER NOT NULL DEFAULT 0")
    op.execute("ALTER TABLE posts ADD COLUMN IF NOT EXISTS last_error TEXT")
    op.execute("ALTER TABLE posts ADD COLUMN IF NOT EXISTS processed_at TIMESTAMP WITH TIME ZONE")
    op.execute("UPDATE posts SET processing_attempts = 0 WHERE processing_attempts IS NULL")


def downgrade() -> None:
    op.execute("ALTER TABLE posts DROP COLUMN IF EXISTS processed_at")
    op.execute("ALTER TABLE posts DROP COLUMN IF EXISTS last_error")
    op.execute("ALTER TABLE posts DROP COLUMN IF EXISTS processing_attempts")
