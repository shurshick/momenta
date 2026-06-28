# Momenta / РњРѕРјРµРЅС‚Р°

**РњРѕРјРµРЅС‚Р°** вЂ” СЃРѕС†РёР°Р»СЊРЅРѕРµ camera-РїСЂРёР»РѕР¶РµРЅРёРµ РґР»СЏ РµР¶РµРґРЅРµРІРЅС‹С… С„РѕС‚Рѕ-РјРѕРјРµРЅС‚РѕРІ.

> РћРґРёРЅ РјРѕРјРµРЅС‚. РћРґРёРЅ РґРµРЅСЊ. Р’РµСЃСЊ РјРёСЂ.

РљР°Р¶РґС‹Р№ РґРµРЅСЊ РїРѕР»СЊР·РѕРІР°С‚РµР»СЊ РїРѕР»СѓС‡Р°РµС‚ РѕР±С‰СѓСЋ "РњРѕРјРµРЅС‚Сѓ РґРЅСЏ", РґРµР»Р°РµС‚ РѕРґРёРЅ СЃРЅРёРјРѕРє, РїСѓР±Р»РёРєСѓРµС‚ РµРіРѕ Рё СЃРјРѕС‚СЂРёС‚ Р¶РёРІСѓСЋ Р»РµРЅС‚Сѓ РјРѕРјРµРЅС‚РѕРІ РґСЂСѓРіРёС… Р»СЋРґРµР№. Р‘РµР· Р°Р»РіРѕСЂРёС‚РјРёС‡РµСЃРєРѕР№ РіРѕРЅРєРё Рё Р±РµСЃРєРѕРЅРµС‡РЅРѕРіРѕ СЃРєСЂРѕР»Р»Р°: РѕРґРёРЅ РґРµРЅСЊ, РѕРґРёРЅ РјРѕРјРµРЅС‚, РѕРґРёРЅ РѕР±С‰РёР№ РєРѕРЅС‚РµРєСЃС‚.

## Current Status

РўРµРєСѓС‰Р°СЏ СЃС‚Р°Р±РёР»СЊРЅР°СЏ РІРµСЂСЃРёСЏ: **v0.2.27**.

РўРµРєСѓС‰РёР№ РІРёР·СѓР°Р»СЊРЅС‹Р№ СЃС‚Р°РЅРґР°СЂС‚ Android: **Momenta Design Concept v1**.

Reference: [docs/design/momenta_visual_concept.png](docs/design/momenta_visual_concept.png)

Р§С‚Рѕ СѓР¶Рµ СЂР°Р±РѕС‚Р°РµС‚:

- FastAPI backend СЃ auth, posts, feed, challenges, reactions, reports Рё РїСЂРѕС„РёР»СЏРјРё.
- Admin panel РґР»СЏ РїРѕР»СЊР·РѕРІР°С‚РµР»РµР№, С‡РµР»Р»РµРЅРґР¶РµР№, РїРѕСЃС‚РѕРІ, Р¶Р°Р»РѕР±, РјРµРґРёР°, РЅР°СЃС‚СЂРѕРµРє Рё audit log.
- Docker/TrueNAS deploy: API, worker, PostgreSQL, Redis, MinIO.
- Android APK РЅР° Kotlin + Jetpack Compose.
- Р—Р°РіСЂСѓР·РєР° С„РѕС‚Рѕ РёР· Android, РѕР±СЂР°Р±РѕС‚РєР° РјРµРґРёР° worker'РѕРј, РѕС‚РѕР±СЂР°Р¶РµРЅРёРµ РїРѕСЃС‚РѕРІ РІ Р»РµРЅС‚Рµ.
- Pull-to-refresh Р»РµРЅС‚С‹, РєРѕРјРјРµРЅС‚Р°СЂРёРё, РєРѕСЂСЂРµРєС‚РЅС‹Рµ Р»Р°Р№РєРё, Р¶Р°Р»РѕР±С‹ Рё СѓРґР°Р»РµРЅРёРµ СЃРІРѕРёС… РїРѕСЃС‚РѕРІ РІ С‚РµС‡РµРЅРёРµ 24 С‡Р°СЃРѕРІ.
- РџСЂРµРґРѕРїСЂРµРґРµР»РµРЅРЅС‹Рµ Р°РІР°С‚Р°СЂРєРё, СЃР»СѓС‡Р°Р№РЅС‹Р№ Р»СѓС‡С€РёР№ РјРѕРјРµРЅС‚ РґРЅСЏ РёР· С‚РѕРїР° Рё СЂРµР°Р»СЊРЅРѕ РїСЂРёРјРµРЅСЏРµРјС‹Рµ С„РѕС‚Рѕ-СЌС„С„РµРєС‚С‹.
- CI: backend tests, Android debug build, Docker image publish РІ GHCR.

Р§С‚Рѕ РµС‰С‘ РЅРµ СЃС‚РѕРёС‚ СЃС‡РёС‚Р°С‚СЊ production-ready:

- РџСЂРѕРґРѕРІР°СЏ СѓСЃС‚Р°РЅРѕРІРєР° РґРѕР»Р¶РЅР° Р±С‹С‚СЊ С‡РёСЃС‚РѕР№, СЃ РЅРѕРІС‹РјРё СЃРµРєСЂРµС‚Р°РјРё.
- РќСѓР¶РµРЅ РїРѕР»РЅС‹Р№ СЂСѓС‡РЅРѕР№ E2E-РїСЂРѕРіРѕРЅ РїРµСЂРµРґ РїСѓР±Р»РёС‡РЅС‹Рј Р·Р°РїСѓСЃРєРѕРј.
- Р’РёРґРµРѕ, С€Р°СЂРёРЅРі, invite/privacy flow Рё РїРѕР»РЅРѕС†РµРЅРЅР°СЏ РјРѕРґРµСЂР°С†РёСЏ РµС‰С‘ РІРїРµСЂРµРґРё.

## Repository Layout

```text
/
в”њв”Ђв”Ђ app/                    # FastAPI backend
в”њв”Ђв”Ђ alembic/                # Database migrations
в”њв”Ђв”Ђ tests/                  # Backend tests
в”њв”Ђв”Ђ docs/                   # Backend/deploy docs
в”њв”Ђв”Ђ deploy/truenas/         # TrueNAS compose YAML
в”њв”Ђв”Ђ android/                # Android app
в”њв”Ђв”Ђ Dockerfile
в”њв”Ђв”Ђ docker-compose.yml
в””в”Ђв”Ђ pyproject.toml
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
image: ghcr.io/shurshick/momenta:v0.2.27
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

Latest release: [v0.2.27](https://github.com/shurshick/momenta/releases/tag/v0.2.27)

Release artifacts:

- Docker image: `ghcr.io/shurshick/momenta:v0.2.27`
- Docker image: `ghcr.io/shurshick/momenta:latest`
- Android APK: attached to the GitHub release and workflow artifacts

## Roadmap

- **v0.2.x** вЂ” stabilize Android/backend/TrueNAS flow.
- **v0.3.0** вЂ” video moments, better feed UX, sharing, invites.
- **v1.0.0** вЂ” production hardening: secrets, moderation, privacy, analytics, store-ready release.
