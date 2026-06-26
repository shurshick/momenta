# Momenta

**Момента** — социальное camera-приложение для ежедневных фото-моментов.

> Один момент. Один день. Весь мир.

Каждый день — новый вызов. Пользователи делают один снимок, отражающий их день, и делятся им с миром. Никаких лент алгоритмов, никакого бесконечного скроллинга — только сегодня, только сейчас.

## Репозиторий

Монорепозиторий содержит:

- **`/`** — Python бэкенд (FastAPI) + Docker + CI/CD
- **`android/`** — Android-приложение (Kotlin, Jetpack Compose)

## Быстрый старт (Backend)

```bash
cp .env.example .env
docker compose up -d --build
```

| Сервис | URL |
|--------|-----|
| API | http://localhost:8000 |
| Swagger Docs | http://localhost:8000/docs |
| Admin Panel | http://localhost:8000/admin |
| MinIO Console | http://localhost:9001 |
| Health Check | http://localhost:8000/health |

### Backend Tech Stack

Python 3.12+, FastAPI, SQLAlchemy 2.x (async), Alembic, PostgreSQL 16, Redis 7, MinIO (S3), Jinja2, Pydantic Settings

## Android

```bash
cd android
./gradlew assembleDevDebug
```

APK: `android/app/build/outputs/apk/dev/debug/app-dev-debug.apk`

### Android Tech Stack

Kotlin, Jetpack Compose, Material 3, Hilt (DI), Navigation Compose, ViewModel + Coroutines + Flow, Retrofit + OkHttp + Kotlinx Serialization, Room (кэш), DataStore Preferences, CameraX, Coil, WorkManager

### Flavor-ы

| Flavor | App ID | app_name | Cleartext | Server |
|--------|--------|----------|-----------|--------|
| dev | com.bghitech.momenta.dev | Момента Dev | Да | localhost:8000 |
| staging | com.bghitech.momenta.staging | Момента Staging | Нет | momenta.bghitech.ru |
| prod | com.bghitech.momenta | Момента | Нет | momenta.bghitech.ru |

## Деплой на TrueNAS

```bash
# 1. Клонируй репозиторий
git clone https://github.com/shurshick/momenta.git
cd momenta/deploy/truenas

# 2. Настрой .env (замени CHANGE_ME_* пароли)
cp ../../.env.example .env
nano .env

# 3. Запусти
docker compose -f docker-compose.truenas.yml up -d
```

Подробная инструкция: [docs/DEPLOY_TRUENAS.md](docs/DEPLOY_TRUENAS.md)

## CI/CD

GitHub Actions workflow (`.github/workflows/docker-publish.yml`) запускается при создании Release или вручную:

1. **Docker image** → `ghcr.io/shurshick/momenta:latest` (теги: semver, sha, latest)
2. **Android APK** → артефакт workflow + attachment к релизу

CI включает:
- Автоматическую проверку `android:testOnly=true` в merged manifest и APK
- Retry loop для сборки APK (3 попытки)
- Публикацию Docker image в GHCR

## Структура проекта

```
momenta-server/
├── app/                    # FastAPI backend
│   ├── api/v1/             # REST API endpoints
│   ├── admin/              # Admin panel (routes + Jinja2 templates)
│   ├── models/             # SQLAlchemy models
│   ├── schemas/            # Pydantic DTOs
│   ├── services/           # Business logic
│   ├── worker/             # Background tasks (Redis Queue)
│   ├── config.py           # Pydantic Settings
│   ├── db.py               # Async database engine
│   ├── main.py             # FastAPI app + lifespan
│   └── security.py         # JWT, password hashing
├── android/                # Android app (Kotlin/Compose)
├── tests/                  # pytest + pytest-asyncio
├── docs/                   # Backend documentation
├── deploy/truenas/         # TrueNAS docker-compose
├── alembic/                # Database migrations
├── Dockerfile              # Python 3.12-slim + uvicorn
├── docker-compose.yml      # Full stack (API + Worker + PG + Redis + MinIO)
└── pyproject.toml          # Python project metadata
```

## Тесты

```bash
# Backend tests
docker compose exec momenta-api pytest tests/ -v

# С локальной машины (нужен Python 3.12 + PostgreSQL)
pip install -e ".[dev]"
pytest tests/ -v
```

## Документация

### Backend
- [API Reference](docs/API.md) — REST endpoints
- [Admin Panel](docs/ADMIN.md) — админ-панель
- [Architecture](docs/ARCHITECTURE.md) — архитектура и data flow
- [TrueNAS Deploy](docs/DEPLOY_TRUENAS.md) — деплой на TrueNAS SCALE
- [Security](docs/SECURITY.md) — безопасность
- [Media Storage](docs/MEDIA_STORAGE.md) — S3 storage

### Android
- [Architecture](android/docs/ANDROID_ARCHITECTURE.md)
- [API Integration](android/docs/API_INTEGRATION.md)
- [Build & Install](android/docs/BUILD_AND_INSTALL.md)
- [Design System](android/docs/DESIGN_SYSTEM.md)
- [Offline Upload](android/docs/OFFLINE_UPLOAD.md)

## Roadmap

- [x] **v0.1.0** — MVP: auth, today challenge, camera, publish, feed, profile, settings
- [x] **v0.2.0** — Android APK + Backend integration
- [x] **v0.2.3** — Critical fixes: APK install, admin auth, worker startup
- [x] **v0.2.4** — testOnly fix, admin panel Jinja2 fix, adaptive icon crash fix
- [ ] **v0.3.0** — Видео 3-5 сек, VerticalPager, шаринг, инвайты
- [ ] **v1.0.0** — Production: refresh токенов, модерация, приватность, аналитика, Google Play
