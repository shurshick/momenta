# Momenta Server

Один момент. Все вместе.

Backend for the Momenta social daily challenge app.

## Quick Start

```bash
cp .env.example .env
docker compose up -d --build
```

Check:

- API: http://localhost:8000
- Docs: http://localhost:8000/docs
- Admin: http://localhost:8000/admin
- MinIO Console: http://localhost:9001

## Services

| Service | Port | Description |
|---|---|---|
| momenta-api | 8000 | FastAPI backend |
| momenta-worker | - | Background tasks |
| momenta-postgres | 5432 | PostgreSQL 16 |
| momenta-redis | 6379 | Redis 7 |
| momenta-minio | 9000/9001 | S3-compatible storage |

## Tech Stack

Python 3.12+, FastAPI, SQLAlchemy 2.x, Alembic, PostgreSQL 16, Redis, MinIO, Celery/RQ
