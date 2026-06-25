# Deploy on TrueNAS SCALE

## Prerequisites

- TrueNAS SCALE with Apps enabled
- Custom App support

## Steps

1. Create datasets:

```bash
zfs create pool/app/momenta
zfs create pool/app/momenta/postgres
zfs create pool/app/momenta/redis
zfs create pool/app/momenta/minio
zfs create pool/app/momenta/api
```

2. In TrueNAS SCALE UI, go to **Apps > Launch Docker Image**.

3. Use the YAML from `deploy/truenas/docker-compose.truenas.yml`.

4. Replace all `CHANGE_ME_*` values with your secrets.

5. Set environment variables.

6. Click **Install**.

## Post-Deploy

- Check `/health` and `/ready`
- Create MinIO bucket automatically or via admin panel
- Set up reverse proxy (Nginx Proxy Manager) for HTTPS
- Configure `PUBLIC_BASE_URL` and `S3_PUBLIC_ENDPOINT`
