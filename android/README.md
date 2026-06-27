# Момента (Momenta)

**Момента** — социальное camera-приложение для ежедневных фото-моментов.

> Один момент. Один день. Весь мир.

Текущий визуальный стандарт: **Momenta Design Concept v1**.

Референс: [`../docs/design/momenta_visual_concept.png`](../docs/design/momenta_visual_concept.png)

Каждый день — новый вызов. Пользователи делают один снимок, отражающий их день, и делятся им с миром. Никаких лент алгоритмов, никакого бесконечного скроллинга — только сегодня, только сейчас.

## Возможности (v0.2.17)

- **Момента дня** — ежедневное задание для вдохновения
- **Камера** — съёмка фото с переключением камеры и вспышкой
- **Публикация** — сжатие, предпросмотр, подпись и загрузка
- **Мир сейчас** — лента моментов со всего мира (лайки, пагинация)
- **Профиль** — статистика и недавние моменты
- **Авторизация** — регистрация / вход (email + пароль)
- **Настройки** — API URL, отладка, выход
- **Тёмная тема** — фирменный стиль Momenta

## Сборка APK

```bash
# production debug (основной вариант для установки)
./gradlew assembleProdDebug

# проверка, что APK устанавливаемый и не testOnly
./gradlew verifyInstallableProdApk

# staging
./gradlew assembleStagingDebug

# dev
./gradlew assembleDevDebug
```

APK будет в `app/build/outputs/apk/prod/debug/app-prod-debug.apk`.

Для установки на телефон:
1. Скопируй APK на устройство через USB или файловый менеджер
2. Открой APK файл и нажми "Установить"

> **Примечание:** На Android 12+ может потребоваться разрешение на установку из неизвестных источников.

## Настройка API URL

В debug-режиме можно указать URL сервера в настройках приложения.

По-умолчанию:
- Dev: `http://10.0.2.2:8000` (localhost эмулятора)
- Staging: `https://momenta.bghitech.ru`
- Prod: `https://momenta.bghitech.ru`

## Технический стек

- **Kotlin** + **Jetpack Compose** + **Material 3**
- **Hilt** (DI)
- **Navigation Compose**
- **ViewModel** + **Kotlin Coroutines** + **Flow**
- **Retrofit** + **OkHttp** + **Kotlinx Serialization**
- **Room** (кэш)
- **DataStore Preferences** (токены, настройки)
- **CameraX** (фото)
- **Coil** (загрузка изображений)
- **WorkManager** (фоновые загрузки)

## Архитектура

```
app/
├── core/          # design, navigation, network, datastore, database, media, permissions
├── data/          # remote (API, DTO), local (Room DAO, Entity), repository impl, mappers
├── domain/        # model, repository interfaces, use cases, AppResult
└── feature/       # splash, onboarding, auth, today, camera, publish, feed, profile, settings
```

## Flavor-ы

| Flavor   | App ID suffix | app_name          | Cleartext | Server |
|----------|---------------|-------------------|-----------|--------|
| dev      | `.dev`        | Момента Dev       | Да        | localhost:8000 |
| staging  | `.staging`    | Момента Staging   | Нет       | momenta.bghitech.ru |
| prod     | (none)        | Момента           | Нет       | momenta.bghitech.ru |

## API Endpoints

Base: `/api/v1`

- `POST /auth/register` — регистрация
- `POST /auth/login` — вход
- `POST /auth/refresh` — обновление токена
- `GET /challenges/today` — задание дня
- `GET /feed/today` — лента
- `POST /posts` — загрузка момента
- `POST /posts/{id}/like` — лайк
- `DELETE /posts/{id}/like` — убрать лайк
- `POST /posts/{id}/report` — жалоба
- `GET /me/profile` — профиль
- `PATCH /me/profile` — обновление профиля

## Roadmap

- **v0.1.0** — MVP: auth, today, camera, publish, feed, profile, settings
- **v0.2.0** — Android APK + Backend integration
- **v0.2.3** — Critical fixes: APK install, admin auth, worker startup
- **v0.2.4** — testOnly fix, admin panel Jinja2 fix, adaptive icon crash fix
- **v0.2.17** — исправления Момента дня, профиля, публикации, галереи и эффектов камеры
- **v0.3.0** — Видео 3-5 сек, VerticalPager, шаринг, инвайты
- **v1.0.0** — Production: refresh токенов, модерация, приватность, аналитика, Google Play
