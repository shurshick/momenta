# Security

## Implemented

- Password hashing with argon2/bcrypt
- JWT access + refresh tokens
- CORS via env configuration
- Rate-limiting on login, upload, report
- MIME type validation for uploads
- File size limits
- Auth required for upload endpoints
- Admin routes require admin/moderator role
- No logging of passwords, tokens, secrets
- Soft delete for users and posts
- Audit log for all admin actions

## Production Checklist

- Set `ALLOW_CLEARTEXT=false`
- Use HTTPS via reverse proxy
- Secure cookies for admin session
- Rotate JWT_SECRET regularly
- Use strong S3 credentials
- Do not expose PostgreSQL/Redis ports
