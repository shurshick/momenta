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
- **Worker** — async image processing (preview/thumb generation, counter flush)
- **Cursor pagination** — for feed endpoints (no offset pagination)
- **Soft delete** — posts and users are never physically deleted
- **JWT** — access + refresh token auth

## Database

Tables: users, challenges, posts, reactions, reports, user_streaks, media_assets, audit_logs

Posts are indexed by `challenge_date` for partitioning readiness.
