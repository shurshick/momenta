# API Documentation

Base URL: `/api/v1`

Production-like deploy URL: `https://momenta.bghitech.ru/api/v1`

Most user endpoints require:

```http
Authorization: Bearer <access_token>
```

## Health

- `GET /health` — Service health
- `GET /ready` — Readiness check (DB, Redis, S3)
- `GET /api/v1/meta` — App metadata

## Auth

- `POST /api/v1/auth/register` — Register new user
- `POST /api/v1/auth/login` — Login
- `POST /api/v1/auth/refresh` — Refresh token
- `GET /api/v1/me` — Current user info

## Challenges

- `GET /api/v1/challenges/today` — Today's challenge
- `GET /api/v1/challenges/{id}` — Challenge by ID
- `GET /api/v1/challenges/by-date/{yyyy-mm-dd}` — Challenge by date

## Posts

- `POST /api/v1/posts` — Upload post (multipart)
- `GET /api/v1/posts/{id}` — Get post
- `DELETE /api/v1/posts/{id}` — Delete own post within 24 hours

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

- `GET /api/v1/feed/today?cursor=&limit=20` — Global feed
- `GET /api/v1/feed/today/best-random` — Random post from today's top moments
- `GET /api/v1/feed/country/{code}?cursor=&limit=20` — Country feed
- `GET /api/v1/feed/user/{id}?cursor=&limit=20` — User feed

## Reactions

- `POST /api/v1/posts/{id}/like` — Like post
- `DELETE /api/v1/posts/{id}/like` — Unlike post

## Reports

- `POST /api/v1/posts/{id}/report` — Report post

## Comments

- `GET /api/v1/posts/{id}/comments` — List post comments
- `POST /api/v1/posts/{id}/comments` — Add comment
- `DELETE /api/v1/posts/{post_id}/comments/{comment_id}` — Delete own comment

## Users

- `GET /api/v1/users/{id}` — User profile
- `GET /api/v1/me/profile` — Own profile
- `PATCH /api/v1/me/profile` — Update profile
- `GET /api/v1/avatars` — List predefined avatars
- `PATCH /api/v1/me/avatar` — Set predefined avatar

## Operational Notes

- Uploaded originals go to MinIO/S3 first.
- Worker creates preview/thumb media asynchronously.
- Feed should prefer preview/thumb URLs when available.
- `/ready` is the best endpoint for deployment health checks because it verifies PostgreSQL, Redis, and S3.
