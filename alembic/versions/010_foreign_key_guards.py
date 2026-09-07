"""add foreign key guards without blocking legacy rows

Revision ID: 010
Revises: 009
Create Date: 2026-09-07
"""

from alembic import op

revision = "010"
down_revision = "009"
branch_labels = None
depends_on = None


CONSTRAINTS = (
    ("audit_logs", "fk_audit_logs_actor_user", "actor_user_id", "users", "id", "SET NULL"),
    ("bookmarks", "fk_bookmarks_user", "user_id", "users", "id", "CASCADE"),
    ("bookmarks", "fk_bookmarks_post", "post_id", "posts", "id", "CASCADE"),
    ("challenges", "fk_challenges_created_by", "created_by", "users", "id", "SET NULL"),
    ("comments", "fk_comments_post", "post_id", "posts", "id", "CASCADE"),
    ("comments", "fk_comments_user", "user_id", "users", "id", "CASCADE"),
    ("media_assets", "fk_media_assets_owner", "owner_user_id", "users", "id", "RESTRICT"),
    ("media_assets", "fk_media_assets_post", "post_id", "posts", "id", "SET NULL"),
    ("posts", "fk_posts_user", "user_id", "users", "id", "RESTRICT"),
    ("posts", "fk_posts_challenge", "challenge_id", "challenges", "id", "RESTRICT"),
    ("reactions", "fk_reactions_post", "post_id", "posts", "id", "CASCADE"),
    ("reactions", "fk_reactions_user", "user_id", "users", "id", "CASCADE"),
    ("reports", "fk_reports_post", "post_id", "posts", "id", "CASCADE"),
    ("reports", "fk_reports_user", "user_id", "users", "id", "CASCADE"),
    ("reports", "fk_reports_reviewer", "reviewed_by", "users", "id", "SET NULL"),
    ("user_streaks", "fk_user_streaks_user", "user_id", "users", "id", "CASCADE"),
)


def upgrade() -> None:
    for table, name, column, target_table, target_column, on_delete in CONSTRAINTS:
        op.execute(
            f"ALTER TABLE {table} ADD CONSTRAINT {name} "
            f"FOREIGN KEY ({column}) REFERENCES {target_table} ({target_column}) "
            f"ON DELETE {on_delete} NOT VALID"
        )


def downgrade() -> None:
    for table, name, *_ in reversed(CONSTRAINTS):
        op.execute(f"ALTER TABLE {table} DROP CONSTRAINT IF EXISTS {name}")
