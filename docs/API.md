# API Documentation

Base URL: `/api/v1`

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
- `DELETE /api/v1/posts/{id}` — Delete own post

## Feed

- `GET /api/v1/feed/today?cursor=&limit=20` — Global feed
- `GET /api/v1/feed/country/{code}?cursor=&limit=20` — Country feed
- `GET /api/v1/feed/user/{id}?cursor=&limit=20` — User feed

## Reactions

- `POST /api/v1/posts/{id}/like` — Like post
- `DELETE /api/v1/posts/{id}/like` — Unlike post

## Reports

- `POST /api/v1/posts/{id}/report` — Report post

## Users

- `GET /api/v1/users/{id}` — User profile
- `GET /api/v1/me/profile` — Own profile
- `PATCH /api/v1/me/profile` — Update profile
