# API Documentation

Base URL: `/api/v1`

Production-like deploy URL: `https://momenta.bghitech.ru/api/v1`

Most user endpoints require:

```http
Authorization: Bearer <access_token>
```

## Health

- `GET /health` вЂ” Service health
- `GET /ready` вЂ” Readiness check (DB, Redis, S3)
- `GET /api/v1/meta` вЂ” App metadata

## Auth

- `POST /api/v1/auth/register` вЂ” Register new user
- `POST /api/v1/auth/login` вЂ” Login
- `POST /api/v1/auth/refresh` вЂ” Refresh token
- `GET /api/v1/me` вЂ” Current user info

## Challenges

- `GET /api/v1/challenges/today` — Today's challenge. If no active challenge exists for the current `APP_TIMEZONE` date, backend creates one automatically from local templates. Manual/admin challenge for the date has priority over auto generation.
- `GET /api/v1/challenges/{id}` вЂ” Challenge by ID
- `GET /api/v1/challenges/by-date/{yyyy-mm-dd}` вЂ” Challenge by date

`GET /api/v1/challenges/today` response includes `date`, `challenge_date`, `title`, `description`, `prompt`, `source`, `ends_at`, `user_posted`, and `participants_count`. `source` is `manual` or `auto`; the date is calculated by `APP_TIMEZONE`, default `Europe/Moscow`.

## Posts

- `POST /api/v1/posts` вЂ” Upload post (multipart)
- `GET /api/v1/posts/{id}` вЂ” Get post
- `DELETE /api/v1/posts/{id}` вЂ” Delete own post within 24 hours

Upload form fields:

| Field | Type | Required | Notes |
|---|---|---:|---|
| `challenge_id` | string | yes | UUID or `today` |
| `media` | file | yes | `image/jpeg`, `image/png`, `image/webp`, video types if enabled |
| `caption` | string | no | User caption |
| `country` | string | no | Country code |
| `city` | string | no | City name |

Only one active/processing/uploading post per user per challenge date is allowed by default.

## Feed

- `GET /api/v1/feed/today?cursor=&limit=20` вЂ” Global feed
- `GET /api/v1/feed/today/best-random` вЂ” Random post from today's top moments
- `GET /api/v1/feed/country/{code}?cursor=&limit=20` вЂ” Country feed
- `GET /api/v1/feed/user/{id}?cursor=&limit=20` вЂ” User feed

## Reactions

- `POST /api/v1/posts/{id}/like` вЂ” Like post
- `DELETE /api/v1/posts/{id}/like` вЂ” Unlike post

## Reports

- `POST /api/v1/posts/{id}/report` вЂ” Report post

## Comments

- `GET /api/v1/posts/{id}/comments` вЂ” List post comments
- `POST /api/v1/posts/{id}/comments` вЂ” Add comment
- `DELETE /api/v1/posts/{post_id}/comments/{comment_id}` вЂ” Delete own comment

## Users

- `GET /api/v1/users/{id}` вЂ” User profile
- `GET /api/v1/users/suggestions` вЂ” Active users for feed header suggestions
- `GET /api/v1/me/profile` вЂ” Own profile
- `PATCH /api/v1/me/profile` вЂ” Update profile
- `GET /api/v1/avatars` вЂ” List predefined avatars
- `PATCH /api/v1/me/avatar` вЂ” Set predefined avatar

## Operational Notes

- Uploaded originals go to MinIO/S3 first.
- Worker creates preview/thumb media asynchronously.
- Feed should prefer preview/thumb URLs when available.
- `/ready` is the best endpoint for deployment health checks because it verifies PostgreSQL, Redis, and S3.
