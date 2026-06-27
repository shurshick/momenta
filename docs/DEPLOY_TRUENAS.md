# Deploy Momenta on TrueNAS SCALE

## 1. Требования

- TrueNAS SCALE 24.04+ (Electric Eel) или новее
- Включён сервис **Apps**
- Минимум 4 GB RAM, 20 GB свободного места на pool
- Загруженный образ `ghcr.io/shurshick/momenta:latest`

---

## 2. Создание Dataset'ов

Создайте dataset'ы для хранения данных приложения. Это обязательно, потому что контейнеры используют bind mounts.

### Через веб-интерфейс

**Storage → Create Dataset** для каждого:

| Dataset | Путь | Назначение |
|---|---|---|
| `momenta/postgres` | `/mnt/pool/app/momenta/postgres` | PostgreSQL данные |
| `momenta/redis` | `/mnt/pool/app/momenta/redis` | Redis RDB/append-only |
| `momenta/minio` | `/mnt/pool/app/momenta/minio` | MinIO объекты |
| `momenta/api` | `/mnt/pool/app/momenta/api` | API temp/logs |

Где `pool` — имя вашего storage pool (например, `tank`, `storage`).

### Через CLI (Shell)

```bash
zfs create pool/app/momenta
zfs create pool/app/momenta/postgres
zfs create pool/app/momenta/redis
zfs create pool/app/momenta/minio
zfs create pool/app/momenta/api
```

---

## 3. Настройка доступа к ghcr.io

Образ приложения хранится в GitHub Container Registry (`ghcr.io/shurshick/momenta`).
Перед развёртыванием нужно сделать его публичным, иначе TrueNAS не сможет его загрузить.

### 3.1. Сделать образ публичным (обязательно)

1. Откройте страницу пакетов GitHub:
   https://github.com/users/shurshick/packages/container/momenta

   Если ссылка не открывается — зайдите в **GitHub → Profile → Packages** и выберите `momenta`.

2. Нажмите **Package settings** (шестерёнка справа).

3. В разделе **Danger Zone** нажмите **Change visibility** → выберите **Public**.

4. Подтвердите изменение.

После этого образ можно будет скачать без аутентификации:

```bash
docker pull ghcr.io/shurshick/momenta:latest
```

> Если по каким-то причинам вы не хотите делать образ публичным, настройте в TrueNAS
> **Apps → Settings → Registry Credentials**, добавьте `ghcr.io` с вашим GitHub
> username и PAT-токеном (scope: `read:packages`).

---

## 4. Развёртывание через TrueNAS Custom App

### 4.1. Откройте Apps

**Apps → Discover Apps → Custom App**

### 4.2. Настройка Application Name

| Поле | Значение |
|---|---|
| Application Name | `momenta` |
| Version | `0.2.22` |

### 4.3. Вставьте YAML конфигурацию

Переключитесь на **Custom YAML** и вставьте содержимое файла [`deploy/truenas/docker-compose.truenas.yml`](../deploy/truenas/docker-compose.truenas.yml).

Полный YAML:

```yaml
services:
  momenta-api:
    image: ghcr.io/shurshick/momenta:latest
    container_name: momenta-api
    restart: unless-stopped
    depends_on:
      momenta-postgres:
        condition: service_healthy
      momenta-redis:
        condition: service_healthy
      momenta-minio:
        condition: service_healthy
    environment:
      APP_NAME: Momenta
      APP_ENV: production
      APP_VERSION: 0.2.22
      API_HOST: 0.0.0.0
      API_PORT: 8000
      PUBLIC_BASE_URL: https://momenta.example.com
      DATABASE_URL: postgresql+psycopg://momenta:CHANGE_ME_DB_PASSWORD@momenta-postgres:5432/momenta
      REDIS_URL: redis://momenta-redis:6379/0
      JWT_SECRET: CHANGE_ME_JWT_SECRET
      JWT_ACCESS_TTL_MINUTES: 30
      JWT_REFRESH_TTL_DAYS: 30
      CORS_ORIGINS: https://momenta.example.com
      S3_ENDPOINT: http://momenta-minio:9000
      S3_PUBLIC_ENDPOINT: https://momenta-media.example.com
      S3_ACCESS_KEY: CHANGE_ME_MINIO_ACCESS
      S3_SECRET_KEY: CHANGE_ME_MINIO_SECRET
      S3_BUCKET: momenta-media
      S3_REGION: us-east-1
      S3_SECURE: "false"
      MEDIA_MAX_IMAGE_MB: 15
      MEDIA_MAX_VIDEO_MB: 80
      ADMIN_EMAIL: admin@example.com
      ADMIN_PASSWORD: CHANGE_ME_ADMIN_PASSWORD
    ports:
      - "8010:8000"
    volumes:
      - /mnt/pool/app/momenta/api:/app/data
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8000/health"]
      interval: 30s
      timeout: 10s
      retries: 5

  momenta-worker:
    image: ghcr.io/shurshick/momenta:latest
    container_name: momenta-worker
    restart: unless-stopped
    command: ["python", "-c", "from app.worker.tasks import run_worker; run_worker()"]
    depends_on:
      momenta-postgres:
        condition: service_healthy
      momenta-redis:
        condition: service_healthy
      momenta-minio:
        condition: service_healthy
    environment:
      APP_NAME: Momenta
      APP_ENV: production
      DATABASE_URL: postgresql+psycopg://momenta:CHANGE_ME_DB_PASSWORD@momenta-postgres:5432/momenta
      REDIS_URL: redis://momenta-redis:6379/0
      S3_ENDPOINT: http://momenta-minio:9000
      S3_ACCESS_KEY: CHANGE_ME_MINIO_ACCESS
      S3_SECRET_KEY: CHANGE_ME_MINIO_SECRET
      S3_BUCKET: momenta-media
    volumes:
      - /mnt/pool/app/momenta/api:/app/data

  momenta-postgres:
    image: postgres:16-alpine
    container_name: momenta-postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB: momenta
      POSTGRES_USER: momenta
      POSTGRES_PASSWORD: CHANGE_ME_DB_PASSWORD
    volumes:
      - /mnt/pool/app/momenta/postgres:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U momenta -d momenta"]
      interval: 30s
      timeout: 10s
      retries: 5

  momenta-redis:
    image: redis:7-alpine
    container_name: momenta-redis
    restart: unless-stopped
    command: ["redis-server", "--appendonly", "yes"]
    volumes:
      - /mnt/pool/app/momenta/redis:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 30s
      timeout: 10s
      retries: 5

  momenta-minio:
    image: minio/minio:latest
    container_name: momenta-minio
    restart: unless-stopped
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: CHANGE_ME_MINIO_ACCESS
      MINIO_ROOT_PASSWORD: CHANGE_ME_MINIO_SECRET
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - /mnt/pool/app/momenta/minio:/data
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9000/minio/health/live"]
      interval: 30s
      timeout: 10s
      retries: 5
```

### 4.4. Обязательные замены

Перед запуском замените все `CHANGE_ME_*` на реальные значения:

| Переменная | Где используется | Рекомендация |
|---|---|---|
| `CHANGE_ME_DB_PASSWORD` | postgres, api, worker | 32+ символа, буквы+цифры |
| `CHANGE_ME_JWT_SECRET` | api | 64+ символа, рандом |
| `CHANGE_ME_MINIO_ACCESS` | minio, api, worker | логин для S3 |
| `CHANGE_ME_MINIO_SECRET` | minio, api, worker | 32+ символа |
| `CHANGE_ME_ADMIN_PASSWORD` | api | 16+ символов |

Сгенерировать пароли можно командой в TrueNAS Shell:

```bash
openssl rand -base64 32
```

### 4.5. Настройка доменов

Замените `momenta.example.com` и `momenta-media.example.com` на ваши реальные домены.

| Переменная | Значение |
|---|---|
| `PUBLIC_BASE_URL` | `https://momenta.example.com` |
| `CORS_ORIGINS` | `https://momenta.example.com` |
| `S3_PUBLIC_ENDPOINT` | `https://momenta-media.example.com` |

### 4.6. Запуск

Нажмите **Install**. TrueNAS запустит все 5 контейнеров в порядке зависимостей.

Проверка статуса:

```
Apps → Installed → momenta → показан статус 5/5 контейнеров (green)
```

### 4.7. Если YAML не подходит

Если в вашей версии TrueNAS Custom App использует другой формат (не Docker Compose), разверните каждый сервис по отдельности через **Launch Docker Image**, используя параметры из YAML как руководство.

---

## 5. Пост-деплой проверка

### 5.1. Health Check API

```bash
curl http://<trueNAS-IP>:8010/health
# → {"status": "ok"}
```

### 5.2. Ready Check

```bash
curl http://<trueNAS-IP>:8010/ready
# → {"status":"ok","postgres":true,"redis":true,"s3":true}
```

### 5.3. Админ-панель

```
http://<trueNAS-IP>:8010/admin
```

Логин: `admin`, пароль: тот, что указали в `ADMIN_PASSWORD`.

### 5.4. MinIO Console

```
http://<trueNAS-IP>:9011
```

Логин/пароль: `MINIO_ROOT_USER` / `MINIO_ROOT_SECRET`.

### 5.5. Инициализация S3 Bucket

В админ-панели: **System → Init S3 Bucket** (однократно).

Либо через MinIO Console: создайте bucket `momenta-media` руками.

---

## 6. Reverse Proxy (Nginx Proxy Manager)

### 6.1. Установка NPM

Установите **Nginx Proxy Manager** из каталога Apps TrueNAS.

### 6.2. Прокси для API

| Поле | Значение |
|---|---|
| Domain | `momenta.example.com` |
| Scheme | `http` |
| Forward IP | IP вашей TrueNAS |
| Forward Port | `8010` |
| Cache Assets | `No` |
| Block Common Exploits | `Yes` |
| Websockets Support | `No` |
| SSL | Let's Encrypt |

### 6.3. Прокси для MinIO Media

| Поле | Значение |
|---|---|
| Domain | `momenta-media.example.com` |
| Scheme | `http` |
| Forward IP | IP вашей TrueNAS |
| Forward Port | `9010` |
| Cache Assets | `Yes` |
| SSL | Let's Encrypt |

После настройки NPM обновите в API:

| Переменная | Новое значение |
|---|---|
| `PUBLIC_BASE_URL` | `https://momenta.example.com` |
| `S3_PUBLIC_ENDPOINT` | `https://momenta-media.example.com` |

Для этого остановите app, отредактируйте переменные окружения и запустите снова.

---

## 7. Обновление

### 7.1. Остановите приложение

**Apps → Installed → momenta → Stop**

### 7.2. Обновите образ

Для тестового стенда можно оставить `latest`. Для production лучше заменить `latest` на конкретную версию:

```yaml
image: ghcr.io/shurshick/momenta:v0.2.22
```

### 7.3. Запустите

**Start**. TrueNAS перезапустит контейнеры с новым образом.

---

## 8. Бэкап

### 8.1. PostgreSQL

Через TrueNAS Shell:

```bash
docker exec momenta-postgres pg_dump -U momenta momenta > /mnt/pool/backups/momenta-$(date +%F).sql
```

### 8.2. Данные

Достаточно бекапить dataset'ы:

```bash
zfs snapshot pool/app/momenta@$(date +%F)
```

### 8.3. Redis и MinIO

Redis — AOF файл в `/mnt/pool/app/momenta/redis/`.
MinIO — все объекты в `/mnt/pool/app/momenta/minio/`.

Бекап через ZFS snapshot покрывает всё.

---

## 9. Устранение проблем

### 9.1. Ошибка unauthorized при запуске

**Ошибка:** `Failed 'up' action for 'momenta' app ... unauthorized`

**Причина:** образ на `ghcr.io` приватный, TrueNAS не может его скачать.

**Решение:** 
1. Сделайте образ публичным (см. раздел 3.1).
2. В TrueNAS: **Apps → Installed → нажмите на момент → Stop → Start** для повторной попытки.

Если не хотите делать образ публичным — настройте Registry Credentials в TrueNAS:
**Apps → Settings → Registry Credentials → Add**, укажите `ghcr.io`, ваш GitHub username и PAT-токен (scope: `read:packages`).

### 9.2. Контейнеры не стартуют

Проверьте логи:

```
Apps → Installed → momenta → Logs (выберите контейнер)
```

### 9.3. PostgreSQL не отвечает

```bash
docker exec momenta-postgres pg_isready -U momenta
```

Если нет — проверьте, нет ли другого Postgres на порту 5432.

### 9.4. Ошибка S3 bucket not found

Перейдите в админ-панель `/admin/system` и нажмите **Init S3 Bucket**, либо создайте bucket `momenta-media` в MinIO Console.

### 9.5. Медиа не грузятся

Проверьте `S3_ENDPOINT` в API — должен указывать на `http://momenta-minio:9000` (внутренний), а `S3_PUBLIC_ENDPOINT` — на внешний URL.

### 9.6. Rate limit срабатывает

Настройки в API:

```
RATE_LIMIT_LOGIN_PER_MINUTE=10
RATE_LIMIT_UPLOAD_PER_HOUR=20
```

Увеличьте при необходимости, затем перезапустите app.

---

## 10. Порты (сводка)

| Контейнер | Внутренний порт | Внешний порт | Доступ |
|---|---:|---:|---|
| momenta-api | 8000 | 8010 | Да |
| momenta-postgres | 5432 | — | Нет, только внутри compose-сети |
| momenta-redis | 6379 | — | Нет, только внутри compose-сети |
| momenta-minio | 9000 | 9010 | Да, S3 API |
| momenta-minio | 9001 | 9011 | Да, MinIO Console |

**Важно:** PostgreSQL и Redis не публикуются наружу — к ним подключаются только контейнеры API и Worker через внутреннюю Docker-сеть.

> Если какой-либо порт уже занят (ошибка `port is already allocated`), измените
> внешний порт в секции `ports` YAML-конфигурации. Например, для MinIO Console:
> `"9011:9001"` → `"9012:9001"` (меняется только левая часть — внешний порт).
