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

## App

- `GET /api/v1/app/latest` — Public Android update metadata. Does not require auth.
  Backend reads the latest GitHub Release that contains `android-update.json`, caches it,
  and skips server-only releases without APK metadata.

Example response:

```json
{
  "app_name": "Момент",
  "package_name": "com.bghitech.momenta",
  "platform": "android",
  "channel": "stable",
  "version_name": "0.2.60",
  "version_code": 60,
  "min_supported_version_code": 1,
  "mandatory": false,
  "apk_url": "https://github.com/shurshick/momenta/releases/download/v0.2.60/app-prod-debug.apk",
  "apk_sha256": "26fc5a418f37d41f336e690b1711ce4d833a6a31e2713fc81486170888bfcb0d",
  "apk_size_bytes": 27253273,
  "release_url": "https://github.com/shurshick/momenta/releases/tag/v0.2.60",
  "release_notes": [
    "Вернулась кнопка удаления своего свежего момента",
    "Исправлены артефакты у встроенных аватаров",
    "Шестеренка профиля перенесена в правый верхний угол"
  ],
  "published_at": "2026-07-11T00:00:00Z"
}
```

## Challenges

- `GET /api/v1/challenges/today` — Today's challenge. If no active challenge exists for the current `APP_TIMEZONE` date, backend creates one automatically from local templates. Manual/admin challenge for the date has priority over auto generation.
- `GET /api/v1/challenges/{id}` — Challenge by ID
- `GET /api/v1/challenges/by-date/{yyyy-mm-dd}` — Challenge by date

`GET /api/v1/challenges/today` response includes `date`, `challenge_date`, `title`, `description`, `prompt`, `source`, `ends_at`, `user_posted`, and `participants_count`. `source` is `manual` or `auto`; the date is calculated by `APP_TIMEZONE`, default `Europe/Moscow`.

## Posts

- `POST /api/v1/posts` — Upload post (multipart)
- `GET /api/v1/posts/{id}` — Get post
- `DELETE /api/v1/posts/{id}` — Delete own post within configured `post_delete_window_minutes`

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
- `GET /api/v1/feed/today/best-random` — Random post from today's top moments. Returns `post: null` if today has no active posts.
- `GET /api/v1/feed/country/{code}?cursor=&limit=20` — Country feed
- `GET /api/v1/feed/user/{id}?cursor=&limit=20` — User feed

## Reactions

- `POST /api/v1/posts/{id}/like` — Like post
- `DELETE /api/v1/posts/{id}/like` — Unlike post

## Reports

- `POST /api/v1/posts/{id}/report` — Report post

The same user can report the same post only once. Duplicate reports return `409`.

## Comments

- `GET /api/v1/posts/{id}/comments` — List post comments
- `POST /api/v1/posts/{id}/comments` — Add comment
- `DELETE /api/v1/posts/{post_id}/comments/{comment_id}` — Delete own comment

## Users

- `GET /api/v1/users/{id}` — User profile
- `GET /api/v1/users/suggestions` — Active users for feed header suggestions
- `GET /api/v1/me/profile` — Own profile
- `PATCH /api/v1/me/profile` — Update profile
- `GET /api/v1/avatars` — List predefined avatars
- `PATCH /api/v1/me/avatar` — Set predefined avatar

## Operational Notes

- Uploaded originals go to MinIO/S3 first.
- Worker creates preview/thumb media asynchronously.
- Feed should prefer preview/thumb URLs when available.
- `/ready` is the best endpoint for deployment health checks because it verifies PostgreSQL, Redis, and S3.

# Избранное

Избранное приватно и доступно только владельцу токена. Удалённые и неактивные посты в список не попадают.

```http
PUT /api/v1/posts/{post_id}/bookmark
DELETE /api/v1/posts/{post_id}/bookmark
GET /api/v1/me/bookmarks?cursor={cursor}&limit=20
```

Добавление и удаление идемпотентны. Элементы возвращаются от новых закладок к старым; `next_cursor` передаётся без изменений в следующий запрос.
