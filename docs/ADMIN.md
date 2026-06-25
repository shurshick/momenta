# Admin Panel

Access: `http://localhost:8000/admin`

## Login

Default credentials from `.env`:

- Username: `admin`
- Password: `change_me_admin_password`

## Sections

- **Dashboard** — Overview stats
- **Users** — List, search, disable, change role
- **Challenges** — CRUD challenges, create fallback
- **Posts** — Filter, hide, restore posts
- **Reports** — Handle user reports (reject, hide post, disable user)
- **Media** — View media assets
- **Audit Log** — All admin actions
- **System** — Health, env summary, flush feed cache, init S3 bucket

All admin actions are logged in `audit_logs`.
