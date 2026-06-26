"""drop unique constraint uq_user_post_per_day

Revision ID: 002
Revises: 001
Create Date: 2026-06-26
"""
from alembic import op

revision = "002"
down_revision = "001"
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.drop_constraint("uq_user_post_per_day", "posts", type_="unique")


def downgrade() -> None:
    op.create_unique_constraint("uq_user_post_per_day", "posts", ["user_id", "challenge_date"])
