# Момента (Momenta)

**Момента** — социальное camera-приложение для ежедневных фото-моментов.

> Один момент. Один день. Весь мир.

Каждый день — новый вызов. Пользователи делают один снимок, отражающий их день, и делятся им с миром. Никаких лент алгоритмов, никакого бесконечного скроллинга — только сегодня, только сейчас.

## Возможности (MVP v0.1.0)

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
# debug (dev-флейвор)
./gradlew assembleDevDebug

# staging
./gradlew assembleStagingDebug

# production release
./gradlew assembleProdRelease
```

APK будет в `app/build/outputs/apk/`.

## Настройка API URL

В debug-режиме можно указать URL сервера в настройках приложения.

По-умолчанию:
- Dev: `http://10.0.2.2:8000` (localhost эмулятора)
- Staging: `https://staging-momenta.example.com`
- Prod: `https://momenta.example.com`

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

| Flavor   | App ID suffix | app_name          | Cleartext |
|----------|---------------|-------------------|-----------|
| dev      | `.dev`        | Момента Dev       | Да        |
| staging  | `.staging`    | Момента Staging   | Нет       |
| prod     | (none)        | Момента           | Нет       |

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
- **v0.2.0** — WorkManager retry, лайки, стрик, пуши, пагинация
- **v0.3.0** — видео 3-5 сек, VerticalPager, шаринг, инвайты
- **v1.0.0** — production: refresh токенов, модерация, приватность, аналитика, Google Play
