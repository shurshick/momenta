# Момент Android

Android-приложение Momenta / Момент на Kotlin + Jetpack Compose.

> Один момент. Все вместе.

Текущий визуальный стандарт: **Momenta Design Concept v1**.  
Референс: [`../docs/design/momenta_visual_concept.png`](../docs/design/momenta_visual_concept.png)

## Возможности (v0.2.54)

- **Момент дня** — ежедневное задание и лучший момент дня.
- **Камера** — съемка фото, выбор из галереи, вспышка, переключение камеры.
- **Эффекты** — выбранный эффект применяется к снимку перед публикацией.
- **Публикация** — предпросмотр, подпись и загрузка момента.
- **Мир сейчас** — лента моментов, pull-to-refresh, лайки, комментарии, жалобы.
- **Удаление поста** — свой момент можно удалить в течение настроенного окна, по умолчанию 60 минут.
- **Профиль** — статистика, недавние моменты, выбор аватарки из 40 вариантов.
- **Настройки** — API URL, “О программе”, проверка и скачивание обновления.

## Сборка APK

```bash
# production debug, основной вариант для установки
./gradlew assembleProdDebug

# проверка, что APK устанавливаемый и не testOnly
./gradlew verifyInstallableProdApk

# staging
./gradlew assembleStagingDebug

# dev
./gradlew assembleDevDebug
```

APK будет здесь:

```text
app/build/outputs/apk/prod/debug/app-prod-debug.apk
```

## API URL

По умолчанию:

- Dev: `http://10.0.2.2:8000`
- Staging: `https://momenta.bghitech.ru`
- Prod: `https://momenta.bghitech.ru`

В debug-сборках URL сервера можно поменять в настройках приложения.

## Технический стек

- Kotlin
- Jetpack Compose + Material 3
- Hilt
- Navigation Compose
- ViewModel + Coroutines + Flow
- Retrofit + OkHttp + Kotlinx Serialization
- Room
- DataStore Preferences
- CameraX
- Coil
- WorkManager

## Архитектура

```text
app/
├── core/          # design, navigation, network, datastore, database, media, permissions
├── data/          # remote API, DTO, Room, repositories, mappers
├── domain/        # models, repository interfaces, use cases, AppResult
└── feature/       # splash, onboarding, auth, today, camera, publish, feed, profile, settings
```

## Flavor-ы

| Flavor | App ID suffix | App name | Cleartext | Server |
|---|---|---|---|---|
| dev | `.dev` | Момент Dev | Да | localhost:8000 |
| staging | `.staging` | Момент Staging | Нет | momenta.bghitech.ru |
| prod | none | Момент | Нет | momenta.bghitech.ru |

## Основные API endpoints

Base: `/api/v1`

- `POST /auth/register` — регистрация
- `POST /auth/login` — вход
- `POST /auth/refresh` — обновление токена
- `GET /challenges/today` — задание дня
- `GET /feed/today` — лента
- `GET /feed/today/best-random` — случайный лучший момент дня из топа
- `POST /posts` — загрузка момента
- `DELETE /posts/{id}` — удалить свой момент в настроенное окно удаления, по умолчанию 60 минут
- `POST /posts/{id}/like` — лайк
- `DELETE /posts/{id}/like` — убрать лайк
- `GET /posts/{id}/comments` — комментарии
- `POST /posts/{id}/comments` — добавить комментарий
- `POST /posts/{id}/report` — жалоба
- `GET /me/profile` — профиль
- `PATCH /me/profile` — обновление профиля
- `GET /avatars` — набор аватарок
- `PATCH /me/avatar` — выбрать аватарку

