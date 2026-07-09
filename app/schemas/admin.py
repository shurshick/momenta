from datetime import datetime

from pydantic import BaseModel


class DashboardStats(BaseModel):
    total_users: int = 0
    active_users_today: int = 0
    posts_today: int = 0
    new_reports: int = 0
    challenge_today: str | None = None
    participants_today: int = 0


class AuditLogEntry(BaseModel):
    id: str
    actor_user_id: str | None = None
    action: str
    entity_type: str
    entity_id: str | None = None
    ip_address: str | None = None
    payload_json: dict | None = None
    created_at: datetime | None = None

    model_config = {"from_attributes": True}
