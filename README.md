# Momenta

**Момента** — социальное camera-приложение для ежедневных фото-моментов.

> Один момент. Один день. Весь мир.

## Репозиторий

Монорепозиторий содержит:

- **`/`** — Python бэкенд (FastAPI)
- **`android/`** — Android-приложение (Kotlin, Jetpack Compose)

## Бэкенд

```bash
cp .env.example .env
docker compose up -d --build
```

- API: http://localhost:8000
- Docs: http://localhost:8000/docs
- Admin: http://localhost:8000/admin
- MinIO: http://localhost:9011

### Tech Stack

Python 3.12+, FastAPI, SQLAlchemy 2.x, Alembic, PostgreSQL 16, Redis, MinIO

## Android

```bash
cd android
./gradlew assembleDevDebug
```

APK: `android/app/build/outputs/apk/dev/debug/`

### Tech Stack

Kotlin, Jetpack Compose, Hilt, Room, Retrofit, CameraX, WorkManager

### Flavor-ы

| Flavor | App ID | app_name | Cleartext |
|--------|--------|----------|-----------|
| dev | com.bghitech.momenta.dev | Момента Dev | Да |
| staging | com.bghitech.momenta.staging | Момента Staging | Нет |
| prod | com.bghitech.momenta | Момента | Нет |

## CI/CD

При создании GitHub Release автоматически:

1. **Docker image** → `ghcr.io/shurshick/momenta:latest`
2. **Android APK** → attached к релизу

## Документация

- [API](docs/API.md)
- [Admin Panel](docs/ADMIN.md)
- [Architecture](docs/ARCHITECTURE.md)
- [TrueNAS Deploy](docs/DEPLOY_TRUENAS.md)
- [Security](docs/SECURITY.md)
- [Media Storage](docs/MEDIA_STORAGE.md)
- [Android Architecture](android/docs/ANDROID_ARCHITECTURE.md)
- [API Integration](android/docs/API_INTEGRATION.md)
- [Build & Install](android/docs/BUILD_AND_INSTALL.md)
- [Design System](android/docs/DESIGN_SYSTEM.md)
- [Offline Upload](android/docs/OFFLINE_UPLOAD.md)
