# Momenta / Момента

**Момента** — социальное camera-приложение для ежедневных фото-моментов.

> Один момент. Один день. Весь мир.

Каждый день пользователь получает общую "Моменту дня", делает один снимок, публикует его и смотрит живую ленту моментов других людей. Без алгоритмической гонки и бесконечного скролла: один день, один момент, один общий контекст.

## Current Status

Текущая стабильная версия: **v0.2.16**.

Текущий визуальный стандарт Android: **Momenta Design Concept v1**.

Reference: [docs/design/momenta_visual_concept.png](docs/design/momenta_visual_concept.png)

Что уже работает:

- FastAPI backend с auth, posts, feed, challenges, reactions, reports и профилями.
- Admin panel для пользователей, челленджей, постов, жалоб, медиа, настроек и audit log.
- Docker/TrueNAS deploy: API, worker, PostgreSQL, Redis, MinIO.
- Android APK на Kotlin + Jetpack Compose.
- Загрузка фото из Android, обработка медиа worker'ом, отображение постов в ленте.
- CI: backend tests, Android debug build, Docker image publish в GHCR.

Что ещё не стоит считать production-ready:

- Продовая установка должна быть чистой, с новыми секретами.
- Нужен полный ручной E2E-прогон перед публичным запуском.
- Видео, шаринг, invite/privacy flow и полноценная модерация ещё впереди.

## Repository Layout

```text
/
├── app/                    # FastAPI backend
├── alembic/                # Database migrations
├── tests/                  # Backend tests
├── docs/                   # Backend/deploy docs
├── deploy/truenas/         # TrueNAS compose YAML
├── android/                # Android app
├── Dockerfile
├── docker-compose.yml
└── pyproject.toml
```

## Quick Start

### Backend

```bash
cp .env.example .env
docker compose up -d --build
```

| Service | URL |
|---|---|
| API | http://localhost:8000 |
| Swagger | http://localhost:8000/docs |
| Admin | http://localhost:8000/admin |
| MinIO Console | http://localhost:9001 |
| Health | http://localhost:8000/health |
| Ready | http://localhost:8000/ready |

Default admin credentials come from `.env`:

```env
ADMIN_USERNAME=admin
ADMIN_PASSWORD=change_me_admin_password
```

### Android

```bash
cd android
./gradlew assembleProdDebug
```

APK:

```text
android/app/build/outputs/apk/prod/debug/app-prod-debug.apk
```

Installable APK check:

```bash
./gradlew verifyInstallableProdApk
```

## TrueNAS Deploy

TrueNAS YAML lives here:

```text
deploy/truenas/docker-compose.truenas.yml
```

Production-like ports used by the current deploy:

| Service | External | Internal |
|---|---:|---:|
| API | 8010 | 8000 |
| MinIO S3 | 9010 | 9000 |
| MinIO Console | 9011 | 9001 |
| PostgreSQL | not exposed | 5432 |
| Redis | not exposed | 6379 |

For test/dev deploys `ghcr.io/shurshick/momenta:latest` is convenient. For production use a fixed tag, for example:

```yaml
image: ghcr.io/shurshick/momenta:v0.2.16
```

Full guide: [docs/DEPLOY_TRUENAS.md](docs/DEPLOY_TRUENAS.md)

## Tests And CI

Backend:

```bash
uv run --python 3.12 --extra dev pytest -q
```

Android:

```bash
cd android
./gradlew assembleDevDebug
./gradlew assembleProdDebug verifyInstallableProdApk
```

GitHub Actions:

- `CI` runs backend tests and Android debug builds on PRs and pushes to `master`.
- `Build and Publish` publishes Docker image to GHCR and uploads APK artifacts.

## Documentation

- [API](docs/API.md)
- [Admin Panel](docs/ADMIN.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Media Storage](docs/MEDIA_STORAGE.md)
- [Security](docs/SECURITY.md)
- [TrueNAS Deploy](docs/DEPLOY_TRUENAS.md)
- [Release Checklist](docs/RELEASE_CHECKLIST.md)
- [Design System](docs/DESIGN_SYSTEM.md)
- [Android UI Concept](docs/ANDROID_UI_CONCEPT.md)
- [Android Build & Install](android/docs/BUILD_AND_INSTALL.md)
- [Android Architecture](android/docs/ANDROID_ARCHITECTURE.md)
- [Android API Integration](android/docs/API_INTEGRATION.md)
- [Android Design System](android/docs/DESIGN_SYSTEM.md)
- [Android Offline Upload](android/docs/OFFLINE_UPLOAD.md)

## Releases

Latest release: [v0.2.16](https://github.com/shurshick/momenta/releases/tag/v0.2.16)

Release artifacts:

- Docker image: `ghcr.io/shurshick/momenta:v0.2.16`
- Docker image: `ghcr.io/shurshick/momenta:latest`
- Android APK: attached to the GitHub release and workflow artifacts

## Roadmap

- **v0.2.x** — stabilize Android/backend/TrueNAS flow.
- **v0.3.0** — video moments, better feed UX, sharing, invites.
- **v1.0.0** — production hardening: secrets, moderation, privacy, analytics, store-ready release.
