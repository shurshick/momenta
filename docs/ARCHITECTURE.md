# Architecture

## Data Flow

```
Client → FastAPI API → PostgreSQL (metadata)
                     → Redis (hot data, counters, rate-limit)
                     → MinIO/S3 (media files)
                     → Worker (async media processing)
```

## Key Decisions

- **PostgreSQL** — source of truth for all metadata
- **Redis** — today's feed, counters, rate-limits, cache
- **MinIO/S3** — all media files (never in DB)
- **Worker** — async image processing (preview/thumb generation, media asset updates)
- **Cursor pagination** — for feed endpoints (no offset pagination)
- **Soft delete** — posts and users are never physically deleted
- **JWT** — access + refresh token auth

## Runtime Services

| Service | Responsibility |
|---|---|
| `momenta-api` | FastAPI app, admin panel, auth, upload endpoints |
| `momenta-worker` | Background media processing |
| `momenta-postgres` | Metadata and auth data |
| `momenta-redis` | Cache, feed hot data, counters |
| `momenta-minio` | Original media and derived assets |

## Database

Tables: users, challenges, posts, reactions, reports, user_streaks, media_assets, audit_logs

Posts are indexed by `challenge_date` for partitioning readiness.

## Media Flow

1. Android uploads original media to `POST /api/v1/posts`.
2. API validates auth, MIME type, size, challenge, and daily post limit.
3. API stores original media in S3-compatible storage.
4. API creates a post in `processing` status.
5. Worker creates preview/thumb assets and updates post/media metadata.
6. Feed returns active posts and media URLs for display.
