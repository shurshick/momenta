"""add settings table

Revision ID: 001
Revises: 
Create Date: 2026-06-26
"""
from alembic import op
import sqlalchemy as sa

revision = "001"
down_revision = None
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.execute("CREATE TABLE IF NOT EXISTS settings (key VARCHAR(100) NOT NULL, value TEXT NOT NULL, created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL, updated_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL, PRIMARY KEY (key))")
    op.execute("INSERT INTO settings (key, value) VALUES ('daily_post_limit', '1') ON CONFLICT (key) DO NOTHING")


def downgrade() -> None:
    op.drop_table("settings")
