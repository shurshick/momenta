#!/usr/bin/env bash
set -Eeuo pipefail

POSTGRES_CONTAINER="${1:?PostgreSQL container id is required}"
SOURCE_DB="${2:-momenta_smoke}"
RESTORE_DB="${3:-momenta_restore_smoke}"
POSTGRES_USER="${POSTGRES_USER:-momenta}"
TMP_DIR="$(mktemp -d)"

cleanup() {
  docker exec "$POSTGRES_CONTAINER" dropdb \
    --username "$POSTGRES_USER" --if-exists "$RESTORE_DB" >/dev/null 2>&1 || true
  rm -rf "$TMP_DIR"
}
trap cleanup EXIT

docker exec "$POSTGRES_CONTAINER" pg_dump \
  --username "$POSTGRES_USER" --dbname "$SOURCE_DB" \
  --format custom --no-owner >"$TMP_DIR/postgres.dump"
sha256sum "$TMP_DIR/postgres.dump" >"$TMP_DIR/SHA256SUMS"
sha256sum -c "$TMP_DIR/SHA256SUMS"

docker exec "$POSTGRES_CONTAINER" dropdb \
  --username "$POSTGRES_USER" --if-exists "$RESTORE_DB"
docker exec "$POSTGRES_CONTAINER" createdb \
  --username "$POSTGRES_USER" "$RESTORE_DB"
docker exec -i "$POSTGRES_CONTAINER" pg_restore \
  --username "$POSTGRES_USER" --dbname "$RESTORE_DB" \
  --no-owner <"$TMP_DIR/postgres.dump"

USER_COUNT="$(docker exec "$POSTGRES_CONTAINER" psql \
  --username "$POSTGRES_USER" --dbname "$RESTORE_DB" \
  --tuples-only --no-align --command "SELECT count(*) FROM users")"
if [[ "$USER_COUNT" -lt 1 ]]; then
  echo "Restore smoke failed: users table is empty" >&2
  exit 1
fi
echo "Backup/restore smoke passed: users=$USER_COUNT"
