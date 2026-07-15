# Release Checklist

Use this checklist before publishing a release or redeploying production.

## Backend

- `uv run --python 3.12 --extra dev pytest -q` passes.
- `/health` returns `{"status":"ok"}`.
- `/ready` returns `postgres=true`, `redis=true`, `s3=true`.
- `/docs` opens.
- Admin login works.
- Admin can create or verify today's challenge.
- Admin can hide/restore posts and see audit log entries.

## Docker / TrueNAS

- `momenta-api`, `momenta-worker`, `momenta-postgres`, `momenta-redis`, `momenta-minio` are running.
- `APP_VERSION` matches the release tag.
- `PUBLIC_BASE_URL`, `CORS_ORIGINS`, and `S3_PUBLIC_ENDPOINT` match the deployed domains.
- PostgreSQL and Redis are not exposed publicly.
- MinIO bucket `momenta-media` exists.
- Worker logs show media processing cycles without repeated errors.

## Android

- `./gradlew assembleDevDebug` passes.
- `./gradlew assembleProdRelease verifyInstallableProdReleaseApk` passes.
- APK installs on a real device.
- App can register and log in.
- App loads today's challenge.
- Camera capture opens and returns preview.
- Upload creates a post.
- Uploaded post appears in feed.
- Like and report actions work.
- Logout clears local auth state.

## Media

- Original media uploads to MinIO.
- Worker creates preview/thumb assets.
- Feed uses preview/thumb URLs, not only original URLs.
- Public media URLs open through `S3_PUBLIC_ENDPOINT`.

## Release

- Docker image tag exists: `ghcr.io/shurshick/momenta:<version>`.
- Docker image `latest` points to the intended build.
- Release notes explain user-visible changes and deployment notes.
- APK is attached to the release or available as a workflow artifact.
- After fixes or rebuilds, release artifacts must be refreshed to the current build without a separate confirmation step.
- No secrets are committed.

## Compatibility Policy

- Before the first production deploy, breaking server, API, database, and Android app changes are allowed when they keep the project cleaner.
- After the first production deploy, every breaking API or schema change must include compatibility handling or an explicit migration path.

## Production

- Use a clean database and clean datasets for first production deploy.
- Generate fresh `ADMIN_PASSWORD`, `JWT_SECRET`, PostgreSQL password, and MinIO keys.
- Prefer a fixed image tag over `latest`.
- Keep backups configured before opening public access.
