# Deploy Momenta on TrueNAS SCALE

Короткая рабочая инструкция для тестового/предпродового разворачивания Momenta на TrueNAS SCALE через Custom App.

## 1. Требования

- TrueNAS SCALE 24.04+.
- Включенный сервис **Apps**.
- Минимум 4 GB RAM и 20 GB свободного места на pool.
- Публичный Docker image `ghcr.io/shurshick/momenta`.

## 2. Dataset'ы

Создайте dataset'ы для постоянных данных:

| Dataset | Путь | Назначение |
|---|---|---|
| `momenta/postgres` | `/mnt/pool/app/momenta/postgres` | PostgreSQL данные |
| `momenta/redis` | `/mnt/pool/app/momenta/redis` | Redis AOF/RDB |
| `momenta/minio` | `/mnt/pool/app/momenta/minio` | MinIO объекты |
| `momenta/api` | `/mnt/pool/app/momenta/api` | API temp/logs |

Через shell:

```bash
zfs create pool/app/momenta
zfs create pool/app/momenta/postgres
zfs create pool/app/momenta/redis
zfs create pool/app/momenta/minio
zfs create pool/app/momenta/api
```

Замените `pool` на имя вашего storage pool.

## 3. GitHub Container Registry

Образ хранится здесь:

```text
ghcr.io/shurshick/momenta
```

Для тестового стенда удобно использовать `latest`. Для production лучше фиксировать тег:

```yaml
image: ghcr.io/shurshick/momenta:v0.2.59
```

Если TrueNAS не может скачать образ, сделайте package публичным:

1. GitHub → Profile → Packages → `momenta`.
2. Package settings.
3. Danger Zone → Change visibility → Public.

Альтернатива: добавить registry credentials в TrueNAS для `ghcr.io` с GitHub PAT и scope `read:packages`.

## 4. Custom App

В TrueNAS:

1. Apps → Discover Apps → Custom App.
2. Application name: `momenta`.
3. Выберите **Custom YAML**.
4. Вставьте YAML из [deploy/truenas/docker-compose.truenas.yml](../deploy/truenas/docker-compose.truenas.yml).

## 5. Обязательные замены

Перед запуском замените все `CHANGE_ME_*`.

| Переменная | Где используется | Рекомендация |
|---|---|---|
| `CHANGE_ME_DB_PASSWORD` | postgres, api, worker | 32+ символа |
| `CHANGE_ME_JWT_SECRET` | api | 64+ символа |
| `CHANGE_ME_MINIO_ACCESS` | minio, api, worker | 20+ символов |
| `CHANGE_ME_MINIO_SECRET` | minio, api, worker | 32+ символа |
| `CHANGE_ME_ADMIN_PASSWORD` | api | 16+ символов |

Сгенерировать секреты можно так:

```bash
openssl rand -base64 48
```

## 6. Домены и прокси

Замените:

| Переменная | Пример |
|---|---|
| `PUBLIC_BASE_URL` | `https://momenta.example.com` |
| `CORS_ORIGINS` | `https://momenta.example.com` |
| `S3_PUBLIC_ENDPOINT` | `https://momenta-media.example.com` |

Для Nginx Proxy Manager:

API:

| Поле | Значение |
|---|---|
| Domain Names | `momenta.example.com` |
| Scheme | `http` |
| Forward Hostname / IP | TrueNAS IP |
| Forward Port | `8010` |

Media:

| Поле | Значение |
|---|---|
| Domain Names | `momenta-media.example.com` |
| Scheme | `http` |
| Forward Hostname / IP | TrueNAS IP |
| Forward Port | `9010` |

Включите SSL certificates для обоих доменов.

## 7. Запуск

Нажмите **Install**. TrueNAS должен поднять 5 контейнеров:

- `momenta-api`
- `momenta-worker`
- `momenta-postgres`
- `momenta-redis`
- `momenta-minio`

Проверка:

```bash
curl -f http://TRUENAS_IP:8010/health
curl -f http://TRUENAS_IP:8010/ready
curl -f http://TRUENAS_IP:8010/api/v1/meta
```

Админка:

```text
http://TRUENAS_IP:8010/admin
```

Логин: `admin`
Пароль: значение `ADMIN_PASSWORD`.

## 8. Миграции

Для чистой установки ничего отдельно делать не нужно: схема создается при старте.

Если обновляете уже поднятую тестовую базу:

```bash
docker exec -it momenta-api alembic upgrade head
```

Текущий head: `006`.

Для `v0.2.59` добавлена миграция `006` с полями media pipeline:

- `posts.processing_attempts`
- `posts.last_error`
- `posts.processed_at`

Для `v0.2.52` добавлена миграция `005` с индексами и уникальностью жалоб/реакций:

- `posts(challenge_date, status, created_at DESC)`
- `posts(user_id, status, created_at DESC)`
- `posts(status, likes_count DESC, created_at DESC)`
- `posts(status, created_at DESC)`
- `comments(post_id, status, created_at)`
- `reports(post_id, status)`
- unique `reports(post_id, user_id)`
- unique `reactions(post_id, user_id, type)`

В `v0.2.50` добавлена новая настройка в уже существующую таблицу `settings`:

| Ключ | Значение по умолчанию | Описание |
|---|---:|---|
| `post_delete_window_minutes` | `60` | Сколько минут после публикации пользователь может удалить свой пост |

Если база уже поднята, setting создастся автоматически при старте API. Можно также выставить руками в админке: **Settings → Окно удаления своего поста, минут**.

## 8.1. Метаданные Android-обновления

Android-приложение проверяет обновления через backend:

```bash
curl -s http://TRUENAS_IP:8010/api/v1/app/latest
```

Для релиза можно задать эти переменные окружения у `momenta-api`:

```env
APP_LATEST_ANDROID_VERSION_NAME=0.2.56
APP_LATEST_ANDROID_VERSION_CODE=56
APP_MIN_SUPPORTED_ANDROID_VERSION_CODE=1
APP_LATEST_ANDROID_MANDATORY=false
APP_LATEST_ANDROID_APK_URL=https://github.com/shurshick/momenta/releases/download/v0.2.56/app-prod-debug.apk
APP_LATEST_ANDROID_APK_SHA256=33af798744b396dce95bf193cb47e6cd707da965586654f7fc39247b4d9efcc3
APP_LATEST_ANDROID_APK_SIZE_BYTES=30969405
APP_LATEST_ANDROID_RELEASE_URL=https://github.com/shurshick/momenta/releases/tag/v0.2.56
APP_LATEST_ANDROID_RELEASE_NOTES=Задание дня больше не зависит от авторизации|Карточка дня открывается даже при истекшем токене|Успешный ответ задания больше не скрывается ошибкой локального cache
APP_LATEST_ANDROID_PUBLISHED_AT=2026-07-10T00:00:00Z
```

`APP_VERSION` не используется для Android-обновлений. Версия сервера и версия APK теперь живут отдельно.

## 9. Обновление

Если используется `latest`:

1. Stop app в TrueNAS.
2. Pull/refresh image, если UI это позволяет.
3. Start app.

Если используется фиксированный тег:

1. Замените тег образа, например на `ghcr.io/shurshick/momenta:v0.2.59`.
2. Запустите app заново.

После обновления проверьте:

```bash
curl -s http://TRUENAS_IP:8010/api/v1/meta
```

Версия в админке и `/api/v1/meta` берется из образа. Старый `APP_VERSION` в окружении больше не должен ломать отображение версии.

## 10. Backup

Достаточно бэкапить dataset'ы:

- `/mnt/pool/app/momenta/postgres`
- `/mnt/pool/app/momenta/redis`
- `/mnt/pool/app/momenta/minio`
- `/mnt/pool/app/momenta/api`

Лучший вариант на TrueNAS — ZFS snapshot.

## 11. Troubleshooting

### Образ не скачивается

Скорее всего, GHCR package приватный. Сделайте package публичным или добавьте registry credentials.

### Контейнеры не стартуют

Смотрите логи:

```bash
docker logs momenta-api
docker logs momenta-worker
docker logs momenta-postgres
docker logs momenta-redis
docker logs momenta-minio
```

### PostgreSQL не отвечает

Проверьте:

```bash
docker exec -it momenta-postgres pg_isready -U momenta -d momenta
```

### MinIO bucket не создан

Откройте `/admin/system` и нажмите **Init S3 Bucket**, либо создайте bucket `momenta-media` в MinIO Console.

### Медиа не грузятся

Проверьте:

- `S3_ENDPOINT` должен смотреть на `http://momenta-minio:9000`.
- `S3_PUBLIC_ENDPOINT` должен смотреть на публичный media-домен.
- Nginx Proxy Manager должен проксировать media-домен на порт `9010`.
- `WORKER_MEDIA_MAX_ATTEMPTS` задает число попыток обработки перед статусом `failed`.
- В админке `/admin/posts?status_filter=failed` можно увидеть ошибку и запустить retry.

### Rate limit мешает тестам

Настройки:

```env
RATE_LIMIT_PER_MINUTE=120
DAILY_POST_LIMIT=1
```

После изменения окружения перезапустите app.

## 12. Порты

| Контейнер | Внутренний порт | Внешний порт | Доступ |
|---|---:|---:|---|
| momenta-api | 8000 | 8010 | HTTP API/admin |
| momenta-minio | 9000 | 9010 | S3/media |
| momenta-minio | 9001 | 9011 | MinIO console |
| momenta-postgres | 5432 | нет | только внутри compose |
| momenta-redis | 6379 | нет | только внутри compose |

