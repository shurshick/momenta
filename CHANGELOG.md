# Changelog

Все значимые изменения Momenta описаны в этом файле.

Формат основан на [Keep a Changelog](https://keepachangelog.com/ru/1.1.0/).

## [v0.2.23] - 2026-06-27

### Added
- **Backend**: добавлен `/api/v1/users/suggestions` для верхнего ряда реальных пользователей в ленте.
- **Android**: набор предопределенных мультяшных аватарок расширен до 25 PNG из утвержденного референса.

### Changed
- **Android**: верхний ряд ленты теперь показывает реальные имена и аватарки пользователей вместо заглушек.
- **Android**: после публикации запускается короткий retry-refresh ленты, чтобы новый пост появился без ручного перехода по вкладкам.
- **Android**: главный экран добирает момент дня из свежей ленты, если endpoint лучшего момента временно вернул пусто.
- **Android**: профиль форсированно обновляется при возврате на экран, поэтому счетчик моментов не остается старым после удаления поста.

## [v0.2.22] - 2026-06-27

### Added
- **Android**: в окне "О программе" появилась кнопка скачивания APK, если проверка обновления нашла свежий релиз.
- **Backend**: добавлены тесты для `/api/v1/feed/today/best-random` и первой страницы ленты.

### Changed
- **Android**: после публикации приложение открывает свежую ленту, сбрасывает позицию списка и не возвращает старый feed state по системному Back.
- **Android**: выбор эффектов камеры заменен на горизонтальные чипы с аккуратным текстом и описанием выбранного эффекта.
- **Android**: обработка фото с эффектами нормализует EXIF-ориентацию перед сохранением.
- **Android**: главный экран держит найденный лучший момент дня и не заменяет его пустым состоянием во время догрузки.
- **Backend**: worker добавляет активированные посты в Redis feed zset.

## [v0.2.21] - 2026-06-27

### Changed
- **Android**: имя установленного приложения изменено на "Момент".
- **Android**: Authorization header добавляется только к защищенным `/api/v1/*` запросам, без auth/health endpoints.
- **Android**: 401 теперь запускает refresh token flow с одним повтором исходного запроса.
- **Android**: при провале refresh токены очищаются, приложение переводит пользователя на экран входа и показывает сообщение о завершении сессии.

### Fixed
- **Android**: убраны фоновые дублирующиеся загрузки ленты при обычном открытии экрана.
- **Backend**: добавлены тесты на login tokens, invalid token, refresh token и доступ refreshed token к `/api/v1/challenges/today`.

## [v0.2.20] - 2026-06-27

### Added
- **Android**: добавлен набор из 20 мультяшных PNG-аватарок.

### Changed
- **Android**: главный экран и нижняя навигация теперь используют подписи "Момент дня" и "Момент".
- **Android**: выбор аватара открывается по тапу на аватар, редактирование профиля вынесено в компактную иконку рядом с именем.
- **Backend**: лучший момент дня берет случайный пост из топа за сегодня, а для живой тестовой базы fallback-ом использует недавние активные посты.

### Fixed
- **Android**: лента делает повторные догрузки после открытия, чтобы новый пост появлялся после серверной обработки без перезапуска приложения.

## [v0.2.17] - 2026-06-27

### Added
- **Android**: "Момент дня" now shows the best moment of the day by likes instead of an empty screen.
- **Android**: camera gallery button opens the system image picker and imports an existing photo.
- **Android**: camera effects panel applies Natural, Warm, Vivid, and Mono effects before publishing.
- **Android**: profile editing dialog saves display name and bio through the existing profile API.

### Fixed
- **Android**: profile now renders real recent post thumbnails from `thumb_url`/`preview_url`.
- **Android**: publish action is now next to the caption field and stays visible above the keyboard.

## [v0.2.16] - 2026-06-27

### Added
- **Android**: aligned the app UI with Momenta Design Concept v1.
- **Android**: added shared `MomentaScreen`, logo mark, bottom bar, loading, and icon primitives.
- **Docs**: added the visual concept reference and Android design system notes.

### Changed
- **Android**: refreshed splash, onboarding, auth, today, camera, publish, feed, profile, and settings screens.
- **Release**: bumped Android `versionCode` to 16 and `versionName` to `0.2.16`.

## [v0.2.15] — 2026-06-27

### Исправлено
- **Android**: исправлен Compose crash в ленте.
- **Android**: debug-сборки больше не требуют release keystore.
- **Android**: `verifyInstallableProdApk` теперь сам собирает APK перед проверкой.
- **Backend**: тесты больше не зависят от живых Redis и MinIO.
- **API**: исправлен порядок routes для `/api/v1/challenges/by-date/{date}`.

### Добавлено
- **CI**: backend tests и Android debug build на push/PR.
- **Release**: Docker image `ghcr.io/shurshick/momenta:latest` пересобирается через workflow.
- **Docs**: актуализирован README, TrueNAS deploy и release checklist.

## [v0.2.14] — 2026-06-27

### Исправлено
- **Android**: pull-to-refresh переведён на custom implementation без Material2 dependency.
- **Worker**: убраны лишние diagnostic logs.

## [v0.2.13] — 2026-06-26

### Исправлено
- **Android**: исправлены проблемы Compose/Navigation/Publish flow после интеграции.
- **Backend**: уточнена версия приложения и стабилизированы runtime-настройки.

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
