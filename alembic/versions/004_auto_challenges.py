"""auto challenge fields

Revision ID: 004
Revises: 003
Create Date: 2026-06-28
"""
from alembic import op

revision = "004"
down_revision = "003"
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.execute("ALTER TABLE challenges ADD COLUMN IF NOT EXISTS prompt_ru TEXT")
    op.execute("ALTER TABLE challenges ADD COLUMN IF NOT EXISTS prompt_en TEXT")
    op.execute("ALTER TABLE challenges ADD COLUMN IF NOT EXISTS source VARCHAR(20) NOT NULL DEFAULT 'manual'")
    op.execute("UPDATE challenges SET source = 'manual' WHERE source IS NULL")
    op.execute("UPDATE challenges SET status = 'active' WHERE status IS NULL")
    op.execute("ALTER TABLE challenges ALTER COLUMN status SET DEFAULT 'active'")
    op.execute("CREATE INDEX IF NOT EXISTS ix_challenges_source ON challenges (source)")
    op.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_challenges_challenge_date ON challenges (challenge_date)")


def downgrade() -> None:
    op.execute("DROP INDEX IF EXISTS ix_challenges_source")
    op.execute("DROP INDEX IF EXISTS ux_challenges_challenge_date")
    op.execute("ALTER TABLE challenges DROP COLUMN IF EXISTS source")
    op.execute("ALTER TABLE challenges DROP COLUMN IF EXISTS prompt_en")
    op.execute("ALTER TABLE challenges DROP COLUMN IF EXISTS prompt_ru")
