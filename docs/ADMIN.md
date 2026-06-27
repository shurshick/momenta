# Admin Panel

Local access: `http://localhost:8000/admin`

TrueNAS/default reverse-proxy access:

- Direct: `http://<truenas-ip>:8010/admin`
- Public: `https://momenta.bghitech.ru/admin`

## Login

Default credentials from `.env`:

- Username: `admin`
- Password: `change_me_admin_password`

For production, set `ADMIN_USERNAME`, `ADMIN_EMAIL`, and `ADMIN_PASSWORD` explicitly.

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

## System Checks

Use the System section after deploy:

- Check environment summary.
- Initialize S3 bucket if needed.
- Flush feed cache after manual moderation or data fixes.
- Verify that sensitive values are not printed in full.
