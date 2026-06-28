# Deploy Momenta on TrueNAS SCALE

## 1. РўСЂРµР±РѕРІР°РЅРёСЏ

- TrueNAS SCALE 24.04+ (Electric Eel) РёР»Рё РЅРѕРІРµРµ
- Р’РєР»СЋС‡С‘РЅ СЃРµСЂРІРёСЃ **Apps**
- РњРёРЅРёРјСѓРј 4 GB RAM, 20 GB СЃРІРѕР±РѕРґРЅРѕРіРѕ РјРµСЃС‚Р° РЅР° pool
- Р—Р°РіСЂСѓР¶РµРЅРЅС‹Р№ РѕР±СЂР°Р· `ghcr.io/shurshick/momenta:latest`

---

## 2. РЎРѕР·РґР°РЅРёРµ Dataset'РѕРІ

РЎРѕР·РґР°Р№С‚Рµ dataset'С‹ РґР»СЏ С…СЂР°РЅРµРЅРёСЏ РґР°РЅРЅС‹С… РїСЂРёР»РѕР¶РµРЅРёСЏ. Р­С‚Рѕ РѕР±СЏР·Р°С‚РµР»СЊРЅРѕ, РїРѕС‚РѕРјСѓ С‡С‚Рѕ РєРѕРЅС‚РµР№РЅРµСЂС‹ РёСЃРїРѕР»СЊР·СѓСЋС‚ bind mounts.

### Р§РµСЂРµР· РІРµР±-РёРЅС‚РµСЂС„РµР№СЃ

**Storage в†’ Create Dataset** РґР»СЏ РєР°Р¶РґРѕРіРѕ:

| Dataset | РџСѓС‚СЊ | РќР°Р·РЅР°С‡РµРЅРёРµ |
|---|---|---|
| `momenta/postgres` | `/mnt/pool/app/momenta/postgres` | PostgreSQL РґР°РЅРЅС‹Рµ |
| `momenta/redis` | `/mnt/pool/app/momenta/redis` | Redis RDB/append-only |
| `momenta/minio` | `/mnt/pool/app/momenta/minio` | MinIO РѕР±СЉРµРєС‚С‹ |
| `momenta/api` | `/mnt/pool/app/momenta/api` | API temp/logs |

Р“РґРµ `pool` вЂ” РёРјСЏ РІР°С€РµРіРѕ storage pool (РЅР°РїСЂРёРјРµСЂ, `tank`, `storage`).

### Р§РµСЂРµР· CLI (Shell)

```bash
zfs create pool/app/momenta
zfs create pool/app/momenta/postgres
zfs create pool/app/momenta/redis
zfs create pool/app/momenta/minio
zfs create pool/app/momenta/api
```

---

## 3. РќР°СЃС‚СЂРѕР№РєР° РґРѕСЃС‚СѓРїР° Рє ghcr.io

РћР±СЂР°Р· РїСЂРёР»РѕР¶РµРЅРёСЏ С…СЂР°РЅРёС‚СЃСЏ РІ GitHub Container Registry (`ghcr.io/shurshick/momenta`).
РџРµСЂРµРґ СЂР°Р·РІС‘СЂС‚С‹РІР°РЅРёРµРј РЅСѓР¶РЅРѕ СЃРґРµР»Р°С‚СЊ РµРіРѕ РїСѓР±Р»РёС‡РЅС‹Рј, РёРЅР°С‡Рµ TrueNAS РЅРµ СЃРјРѕР¶РµС‚ РµРіРѕ Р·Р°РіСЂСѓР·РёС‚СЊ.

### 3.1. РЎРґРµР»Р°С‚СЊ РѕР±СЂР°Р· РїСѓР±Р»РёС‡РЅС‹Рј (РѕР±СЏР·Р°С‚РµР»СЊРЅРѕ)

1. РћС‚РєСЂРѕР№С‚Рµ СЃС‚СЂР°РЅРёС†Сѓ РїР°РєРµС‚РѕРІ GitHub:
   https://github.com/users/shurshick/packages/container/momenta

   Р•СЃР»Рё СЃСЃС‹Р»РєР° РЅРµ РѕС‚РєСЂС‹РІР°РµС‚СЃСЏ вЂ” Р·Р°Р№РґРёС‚Рµ РІ **GitHub в†’ Profile в†’ Packages** Рё РІС‹Р±РµСЂРёС‚Рµ `momenta`.

2. РќР°Р¶РјРёС‚Рµ **Package settings** (С€РµСЃС‚РµСЂС‘РЅРєР° СЃРїСЂР°РІР°).

3. Р’ СЂР°Р·РґРµР»Рµ **Danger Zone** РЅР°Р¶РјРёС‚Рµ **Change visibility** в†’ РІС‹Р±РµСЂРёС‚Рµ **Public**.

4. РџРѕРґС‚РІРµСЂРґРёС‚Рµ РёР·РјРµРЅРµРЅРёРµ.

РџРѕСЃР»Рµ СЌС‚РѕРіРѕ РѕР±СЂР°Р· РјРѕР¶РЅРѕ Р±СѓРґРµС‚ СЃРєР°С‡Р°С‚СЊ Р±РµР· Р°СѓС‚РµРЅС‚РёС„РёРєР°С†РёРё:

```bash
docker pull ghcr.io/shurshick/momenta:latest
```

> Р•СЃР»Рё РїРѕ РєР°РєРёРј-С‚Рѕ РїСЂРёС‡РёРЅР°Рј РІС‹ РЅРµ С…РѕС‚РёС‚Рµ РґРµР»Р°С‚СЊ РѕР±СЂР°Р· РїСѓР±Р»РёС‡РЅС‹Рј, РЅР°СЃС‚СЂРѕР№С‚Рµ РІ TrueNAS
> **Apps в†’ Settings в†’ Registry Credentials**, РґРѕР±Р°РІСЊС‚Рµ `ghcr.io` СЃ РІР°С€РёРј GitHub
> username Рё PAT-С‚РѕРєРµРЅРѕРј (scope: `read:packages`).

---

## 4. Р Р°Р·РІС‘СЂС‚С‹РІР°РЅРёРµ С‡РµСЂРµР· TrueNAS Custom App

### 4.1. РћС‚РєСЂРѕР№С‚Рµ Apps

**Apps в†’ Discover Apps в†’ Custom App**

### 4.2. РќР°СЃС‚СЂРѕР№РєР° Application Name

| РџРѕР»Рµ | Р—РЅР°С‡РµРЅРёРµ |
|---|---|
| Application Name | `momenta` |
| Version | `0.2.28` |

### 4.3. Р’СЃС‚Р°РІСЊС‚Рµ YAML РєРѕРЅС„РёРіСѓСЂР°С†РёСЋ

РџРµСЂРµРєР»СЋС‡РёС‚РµСЃСЊ РЅР° **Custom YAML** Рё РІСЃС‚Р°РІСЊС‚Рµ СЃРѕРґРµСЂР¶РёРјРѕРµ С„Р°Р№Р»Р° [`deploy/truenas/docker-compose.truenas.yml`](../deploy/truenas/docker-compose.truenas.yml).

РџРѕР»РЅС‹Р№ YAML:

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

      APP_TIMEZONE: Europe/Moscow
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

### 4.4. РћР±СЏР·Р°С‚РµР»СЊРЅС‹Рµ Р·Р°РјРµРЅС‹

РџРµСЂРµРґ Р·Р°РїСѓСЃРєРѕРј Р·Р°РјРµРЅРёС‚Рµ РІСЃРµ `CHANGE_ME_*` РЅР° СЂРµР°Р»СЊРЅС‹Рµ Р·РЅР°С‡РµРЅРёСЏ:

| РџРµСЂРµРјРµРЅРЅР°СЏ | Р“РґРµ РёСЃРїРѕР»СЊР·СѓРµС‚СЃСЏ | Р РµРєРѕРјРµРЅРґР°С†РёСЏ |
|---|---|---|
| `CHANGE_ME_DB_PASSWORD` | postgres, api, worker | 32+ СЃРёРјРІРѕР»Р°, Р±СѓРєРІС‹+С†РёС„СЂС‹ |
| `CHANGE_ME_JWT_SECRET` | api | 64+ СЃРёРјРІРѕР»Р°, СЂР°РЅРґРѕРј |
| `CHANGE_ME_MINIO_ACCESS` | minio, api, worker | Р»РѕРіРёРЅ РґР»СЏ S3 |
| `CHANGE_ME_MINIO_SECRET` | minio, api, worker | 32+ СЃРёРјРІРѕР»Р° |
| `CHANGE_ME_ADMIN_PASSWORD` | api | 16+ СЃРёРјРІРѕР»РѕРІ |

РЎРіРµРЅРµСЂРёСЂРѕРІР°С‚СЊ РїР°СЂРѕР»Рё РјРѕР¶РЅРѕ РєРѕРјР°РЅРґРѕР№ РІ TrueNAS Shell:

```bash
openssl rand -base64 32
```

### 4.5. РќР°СЃС‚СЂРѕР№РєР° РґРѕРјРµРЅРѕРІ

Р—Р°РјРµРЅРёС‚Рµ `momenta.example.com` Рё `momenta-media.example.com` РЅР° РІР°С€Рё СЂРµР°Р»СЊРЅС‹Рµ РґРѕРјРµРЅС‹.

| РџРµСЂРµРјРµРЅРЅР°СЏ | Р—РЅР°С‡РµРЅРёРµ |
|---|---|
| `PUBLIC_BASE_URL` | `https://momenta.example.com` |
| `CORS_ORIGINS` | `https://momenta.example.com` |
| `S3_PUBLIC_ENDPOINT` | `https://momenta-media.example.com` |

### 4.6. Р—Р°РїСѓСЃРє

РќР°Р¶РјРёС‚Рµ **Install**. TrueNAS Р·Р°РїСѓСЃС‚РёС‚ РІСЃРµ 5 РєРѕРЅС‚РµР№РЅРµСЂРѕРІ РІ РїРѕСЂСЏРґРєРµ Р·Р°РІРёСЃРёРјРѕСЃС‚РµР№.

РџСЂРѕРІРµСЂРєР° СЃС‚Р°С‚СѓСЃР°:

```
Apps в†’ Installed в†’ momenta в†’ РїРѕРєР°Р·Р°РЅ СЃС‚Р°С‚СѓСЃ 5/5 РєРѕРЅС‚РµР№РЅРµСЂРѕРІ (green)
```

### 4.7. Р•СЃР»Рё YAML РЅРµ РїРѕРґС…РѕРґРёС‚

Р•СЃР»Рё РІ РІР°С€РµР№ РІРµСЂСЃРёРё TrueNAS Custom App РёСЃРїРѕР»СЊР·СѓРµС‚ РґСЂСѓРіРѕР№ С„РѕСЂРјР°С‚ (РЅРµ Docker Compose), СЂР°Р·РІРµСЂРЅРёС‚Рµ РєР°Р¶РґС‹Р№ СЃРµСЂРІРёСЃ РїРѕ РѕС‚РґРµР»СЊРЅРѕСЃС‚Рё С‡РµСЂРµР· **Launch Docker Image**, РёСЃРїРѕР»СЊР·СѓСЏ РїР°СЂР°РјРµС‚СЂС‹ РёР· YAML РєР°Рє СЂСѓРєРѕРІРѕРґСЃС‚РІРѕ.

---

## 5. РџРѕСЃС‚-РґРµРїР»РѕР№ РїСЂРѕРІРµСЂРєР°

### 5.1. Health Check API

```bash
curl http://<trueNAS-IP>:8010/health
# в†’ {"status": "ok"}
```

### 5.2. Ready Check

```bash
curl http://<trueNAS-IP>:8010/ready
# в†’ {"status":"ok","postgres":true,"redis":true,"s3":true}
```

### 5.3. РђРґРјРёРЅ-РїР°РЅРµР»СЊ

```
http://<trueNAS-IP>:8010/admin
```

Р›РѕРіРёРЅ: `admin`, РїР°СЂРѕР»СЊ: С‚РѕС‚, С‡С‚Рѕ СѓРєР°Р·Р°Р»Рё РІ `ADMIN_PASSWORD`.

### 5.4. MinIO Console

```
http://<trueNAS-IP>:9011
```

Р›РѕРіРёРЅ/РїР°СЂРѕР»СЊ: `MINIO_ROOT_USER` / `MINIO_ROOT_SECRET`.

### 5.5. РРЅРёС†РёР°Р»РёР·Р°С†РёСЏ S3 Bucket

Р’ Р°РґРјРёРЅ-РїР°РЅРµР»Рё: **System в†’ Init S3 Bucket** (РѕРґРЅРѕРєСЂР°С‚РЅРѕ).

Р›РёР±Рѕ С‡РµСЂРµР· MinIO Console: СЃРѕР·РґР°Р№С‚Рµ bucket `momenta-media` СЂСѓРєР°РјРё.

---

## 6. Reverse Proxy (Nginx Proxy Manager)

### 6.1. РЈСЃС‚Р°РЅРѕРІРєР° NPM

РЈСЃС‚Р°РЅРѕРІРёС‚Рµ **Nginx Proxy Manager** РёР· РєР°С‚Р°Р»РѕРіР° Apps TrueNAS.

### 6.2. РџСЂРѕРєСЃРё РґР»СЏ API

| РџРѕР»Рµ | Р—РЅР°С‡РµРЅРёРµ |
|---|---|
| Domain | `momenta.example.com` |
| Scheme | `http` |
| Forward IP | IP РІР°С€РµР№ TrueNAS |
| Forward Port | `8010` |
| Cache Assets | `No` |
| Block Common Exploits | `Yes` |
| Websockets Support | `No` |
| SSL | Let's Encrypt |

### 6.3. РџСЂРѕРєСЃРё РґР»СЏ MinIO Media

| РџРѕР»Рµ | Р—РЅР°С‡РµРЅРёРµ |
|---|---|
| Domain | `momenta-media.example.com` |
| Scheme | `http` |
| Forward IP | IP РІР°С€РµР№ TrueNAS |
| Forward Port | `9010` |
| Cache Assets | `Yes` |
| SSL | Let's Encrypt |

РџРѕСЃР»Рµ РЅР°СЃС‚СЂРѕР№РєРё NPM РѕР±РЅРѕРІРёС‚Рµ РІ API:

| РџРµСЂРµРјРµРЅРЅР°СЏ | РќРѕРІРѕРµ Р·РЅР°С‡РµРЅРёРµ |
|---|---|
| `PUBLIC_BASE_URL` | `https://momenta.example.com` |
| `S3_PUBLIC_ENDPOINT` | `https://momenta-media.example.com` |

Р”Р»СЏ СЌС‚РѕРіРѕ РѕСЃС‚Р°РЅРѕРІРёС‚Рµ app, РѕС‚СЂРµРґР°РєС‚РёСЂСѓР№С‚Рµ РїРµСЂРµРјРµРЅРЅС‹Рµ РѕРєСЂСѓР¶РµРЅРёСЏ Рё Р·Р°РїСѓСЃС‚РёС‚Рµ СЃРЅРѕРІР°.

---

## 7. РћР±РЅРѕРІР»РµРЅРёРµ

### 7.1. РћСЃС‚Р°РЅРѕРІРёС‚Рµ РїСЂРёР»РѕР¶РµРЅРёРµ

**Apps в†’ Installed в†’ momenta в†’ Stop**

### 7.2. РћР±РЅРѕРІРёС‚Рµ РѕР±СЂР°Р·

Р”Р»СЏ С‚РµСЃС‚РѕРІРѕРіРѕ СЃС‚РµРЅРґР° РјРѕР¶РЅРѕ РѕСЃС‚Р°РІРёС‚СЊ `latest`. Р”Р»СЏ production Р»СѓС‡С€Рµ Р·Р°РјРµРЅРёС‚СЊ `latest` РЅР° РєРѕРЅРєСЂРµС‚РЅСѓСЋ РІРµСЂСЃРёСЋ:

```yaml
image: ghcr.io/shurshick/momenta:v0.2.28
```

### 7.3. Р—Р°РїСѓСЃС‚РёС‚Рµ

**Start**. TrueNAS РїРµСЂРµР·Р°РїСѓСЃС‚РёС‚ РєРѕРЅС‚РµР№РЅРµСЂС‹ СЃ РЅРѕРІС‹Рј РѕР±СЂР°Р·РѕРј.

---

## 8. Р‘СЌРєР°Рї

### 8.1. PostgreSQL

Р§РµСЂРµР· TrueNAS Shell:

```bash
docker exec momenta-postgres pg_dump -U momenta momenta > /mnt/pool/backups/momenta-$(date +%F).sql
```

### 8.2. Р”Р°РЅРЅС‹Рµ

Р”РѕСЃС‚Р°С‚РѕС‡РЅРѕ Р±РµРєР°РїРёС‚СЊ dataset'С‹:

```bash
zfs snapshot pool/app/momenta@$(date +%F)
```

### 8.3. Redis Рё MinIO

Redis вЂ” AOF С„Р°Р№Р» РІ `/mnt/pool/app/momenta/redis/`.
MinIO вЂ” РІСЃРµ РѕР±СЉРµРєС‚С‹ РІ `/mnt/pool/app/momenta/minio/`.

Р‘РµРєР°Рї С‡РµСЂРµР· ZFS snapshot РїРѕРєСЂС‹РІР°РµС‚ РІСЃС‘.

---

## 9. РЈСЃС‚СЂР°РЅРµРЅРёРµ РїСЂРѕР±Р»РµРј

### 9.1. РћС€РёР±РєР° unauthorized РїСЂРё Р·Р°РїСѓСЃРєРµ

**РћС€РёР±РєР°:** `Failed 'up' action for 'momenta' app ... unauthorized`

**РџСЂРёС‡РёРЅР°:** РѕР±СЂР°Р· РЅР° `ghcr.io` РїСЂРёРІР°С‚РЅС‹Р№, TrueNAS РЅРµ РјРѕР¶РµС‚ РµРіРѕ СЃРєР°С‡Р°С‚СЊ.

**Р РµС€РµРЅРёРµ:**
1. РЎРґРµР»Р°Р№С‚Рµ РѕР±СЂР°Р· РїСѓР±Р»РёС‡РЅС‹Рј (СЃРј. СЂР°Р·РґРµР» 3.1).
2. Р’ TrueNAS: **Apps в†’ Installed в†’ РЅР°Р¶РјРёС‚Рµ РЅР° РјРѕРјРµРЅС‚ в†’ Stop в†’ Start** РґР»СЏ РїРѕРІС‚РѕСЂРЅРѕР№ РїРѕРїС‹С‚РєРё.

Р•СЃР»Рё РЅРµ С…РѕС‚РёС‚Рµ РґРµР»Р°С‚СЊ РѕР±СЂР°Р· РїСѓР±Р»РёС‡РЅС‹Рј вЂ” РЅР°СЃС‚СЂРѕР№С‚Рµ Registry Credentials РІ TrueNAS:
**Apps в†’ Settings в†’ Registry Credentials в†’ Add**, СѓРєР°Р¶РёС‚Рµ `ghcr.io`, РІР°С€ GitHub username Рё PAT-С‚РѕРєРµРЅ (scope: `read:packages`).

### 9.2. РљРѕРЅС‚РµР№РЅРµСЂС‹ РЅРµ СЃС‚Р°СЂС‚СѓСЋС‚

РџСЂРѕРІРµСЂСЊС‚Рµ Р»РѕРіРё:

```
Apps в†’ Installed в†’ momenta в†’ Logs (РІС‹Р±РµСЂРёС‚Рµ РєРѕРЅС‚РµР№РЅРµСЂ)
```

### 9.3. PostgreSQL РЅРµ РѕС‚РІРµС‡Р°РµС‚

```bash
docker exec momenta-postgres pg_isready -U momenta
```

Р•СЃР»Рё РЅРµС‚ вЂ” РїСЂРѕРІРµСЂСЊС‚Рµ, РЅРµС‚ Р»Рё РґСЂСѓРіРѕРіРѕ Postgres РЅР° РїРѕСЂС‚Сѓ 5432.

### 9.4. РћС€РёР±РєР° S3 bucket not found

РџРµСЂРµР№РґРёС‚Рµ РІ Р°РґРјРёРЅ-РїР°РЅРµР»СЊ `/admin/system` Рё РЅР°Р¶РјРёС‚Рµ **Init S3 Bucket**, Р»РёР±Рѕ СЃРѕР·РґР°Р№С‚Рµ bucket `momenta-media` РІ MinIO Console.

### 9.5. РњРµРґРёР° РЅРµ РіСЂСѓР·СЏС‚СЃСЏ

РџСЂРѕРІРµСЂСЊС‚Рµ `S3_ENDPOINT` РІ API вЂ” РґРѕР»Р¶РµРЅ СѓРєР°Р·С‹РІР°С‚СЊ РЅР° `http://momenta-minio:9000` (РІРЅСѓС‚СЂРµРЅРЅРёР№), Р° `S3_PUBLIC_ENDPOINT` вЂ” РЅР° РІРЅРµС€РЅРёР№ URL.

### 9.6. Rate limit СЃСЂР°Р±Р°С‚С‹РІР°РµС‚

РќР°СЃС‚СЂРѕР№РєРё РІ API:

```
RATE_LIMIT_LOGIN_PER_MINUTE=10
RATE_LIMIT_UPLOAD_PER_HOUR=20
```

РЈРІРµР»РёС‡СЊС‚Рµ РїСЂРё РЅРµРѕР±С…РѕРґРёРјРѕСЃС‚Рё, Р·Р°С‚РµРј РїРµСЂРµР·Р°РїСѓСЃС‚РёС‚Рµ app.

---

## 10. РџРѕСЂС‚С‹ (СЃРІРѕРґРєР°)

| РљРѕРЅС‚РµР№РЅРµСЂ | Р’РЅСѓС‚СЂРµРЅРЅРёР№ РїРѕСЂС‚ | Р’РЅРµС€РЅРёР№ РїРѕСЂС‚ | Р”РѕСЃС‚СѓРї |
|---|---:|---:|---|
| momenta-api | 8000 | 8010 | Р”Р° |
| momenta-postgres | 5432 | вЂ” | РќРµС‚, С‚РѕР»СЊРєРѕ РІРЅСѓС‚СЂРё compose-СЃРµС‚Рё |
| momenta-redis | 6379 | вЂ” | РќРµС‚, С‚РѕР»СЊРєРѕ РІРЅСѓС‚СЂРё compose-СЃРµС‚Рё |
| momenta-minio | 9000 | 9010 | Р”Р°, S3 API |
| momenta-minio | 9001 | 9011 | Р”Р°, MinIO Console |

**Р’Р°Р¶РЅРѕ:** PostgreSQL Рё Redis РЅРµ РїСѓР±Р»РёРєСѓСЋС‚СЃСЏ РЅР°СЂСѓР¶Сѓ вЂ” Рє РЅРёРј РїРѕРґРєР»СЋС‡Р°СЋС‚СЃСЏ С‚РѕР»СЊРєРѕ РєРѕРЅС‚РµР№РЅРµСЂС‹ API Рё Worker С‡РµСЂРµР· РІРЅСѓС‚СЂРµРЅРЅСЋСЋ Docker-СЃРµС‚СЊ.

> Р•СЃР»Рё РєР°РєРѕР№-Р»РёР±Рѕ РїРѕСЂС‚ СѓР¶Рµ Р·Р°РЅСЏС‚ (РѕС€РёР±РєР° `port is already allocated`), РёР·РјРµРЅРёС‚Рµ
> РІРЅРµС€РЅРёР№ РїРѕСЂС‚ РІ СЃРµРєС†РёРё `ports` YAML-РєРѕРЅС„РёРіСѓСЂР°С†РёРё. РќР°РїСЂРёРјРµСЂ, РґР»СЏ MinIO Console:
> `"9011:9001"` в†’ `"9012:9001"` (РјРµРЅСЏРµС‚СЃСЏ С‚РѕР»СЊРєРѕ Р»РµРІР°СЏ С‡Р°СЃС‚СЊ вЂ” РІРЅРµС€РЅРёР№ РїРѕСЂС‚).



## APP_TIMEZONE

APP_TIMEZONE controls the day boundary for automatic daily challenges. Default: Europe/Moscow. After changing it, restart momenta-api and momenta-worker.
