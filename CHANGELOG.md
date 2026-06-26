# Changelog

Все значимые изменения Momenta описаны в этом файле.

Формат основан на [Keep a Changelog](https://keepachangelog.com/ru/1.1.0/).

## [v0.2.4] — 2026-06-26

### Исправлено
- **Android**: удалены adaptive icon XML (`mipmap-anydpi-v26/`) — Google Package Installer на Xiaomi крашился с `width and height must be > 0` при попытке создать Bitmap из ColorDrawable
- **Android**: добавлены raster mipmap PNG иконки (mdpi–xxxhdpi) как fallback
- **Admin**: исправлен дублирующийся Jinja2 `block title` в `base.html` — `TemplateAssertionError` при открытии дашборда
- **Admin**: добавлен `ALTER TABLE users ADD COLUMN bio` в миграцию — `UndefinedColumnError` при логине

### Добавлено
- CI проверяет `android:testOnly=true` в merged manifest и APK через aapt
- CI загружает APK как артефакт workflow при каждой сборке (30 дней хранения)
- Gradle task `verifyInstallableDevApk` + скрипт `verify_apk.py`

## [v0.2.3] — 2026-06-26

### Исправлено
- **Admin Auth**: `create_access_token()` перезаписывал поле `type` значением `"access"` — admin пользователи не могли аутентифицироваться через браузер
- **Admin Panel**: все 11 вызовов `TemplateResponse` конвертированы в new-style API `(request, name, context)` — исправлена `TypeError: unhashable type: 'dict'` в production
- **Worker**: добавлен `__main__.py` + логирование запуска — исправлена ошибка `No module named app.worker.__main__`
- **Database**: JSONB → JSON в `audit_log.py` для совместимости с SQLite в тестах
- **Tests**: добавлен `cleanup_db` fixture, 14 дополнительных тестов проходят
- **Dependencies**: зафиксирован `jinja2<3.1.6` для совместимости с Python 3.14

## [v0.2.2] — 2026-06-25

### Исправлено
- **Android**: AGP 8.x автоматически добавляет `android:testOnly=true` для debug-сборок — APK не устанавливался через файловый менеджер (только через `adb`)
- Добавлен `src/debug/AndroidManifest.xml` с `android:testOnly=false` для переопределения

## [v0.2.1] — 2026-06-25

### Исправлено
- Убран unsigned prod APK из релиза (только `app-dev-debug.apk`)
- Исправлен `AndroidManifest.xml` (`allowBackup=false` для Android 12+)
- CI собирает только один APK
- Добавлен `__main__.py` для `python -m app.worker`

## [v0.2.0] — 2026-06-25

### Добавлено
- Первый рабочий Android APK с полной интеграцией бэкенда
- Android-приложение: авторизация, камера, публикация, лента, профиль, настройки
- 3 flavor-а: dev / staging / prod

## [v0.1.0] — 2026-06-25

### Добавлено
- **API**: Health, Ready, Meta endpoints
- **Auth**: регистрация, вход, refresh токенов
- **Challenges**:today, by-id, by-date
- **Posts**: загрузка (multipart), получение, удаление
- **Feed**: глобальная лента, по стране, по пользователю (cursor pagination)
- **Reactions**: лайк/анлайк
- **Reports**: жалобы (spam, nudity, violence, hate, harassment, copyright, other)
- **Users**: профиль, редактирование
- **Admin Panel**: Dashboard, управление пользователями, CRUD челленджей, модерация постов, обработка жалоб, медиа-менеджер, audit log
- **Безопасность**: Argon2/bcrypt, JWT, CORS, rate-limit, роли
- **Инфраструктура**: PostgreSQL 16, Redis 7, MinIO/S3, Worker (image processing), Docker Compose, TrueNAS SCALE
- **CI/CD**: GitHub Actions → ghcr.io Docker image
- **Документация**: API.md, ADMIN.md, ARCHITECTURE.md, DEPLOY_TRUENAS.md, SECURITY.md, MEDIA_STORAGE.md
