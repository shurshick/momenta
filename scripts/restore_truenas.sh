#!/usr/bin/env bash
set -Eeuo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: MOMENTA_RESTORE_CONFIRM=YES $0 /path/to/backup" >&2
  exit 1
fi
if [[ "${MOMENTA_RESTORE_CONFIRM:-}" != "YES" ]]; then
  echo "Restore refused. Set MOMENTA_RESTORE_CONFIRM=YES explicitly." >&2
  exit 1
fi

BACKUP_DIR="$(readlink -f "$1")"
DATA_ROOT="${MOMENTA_DATA_ROOT:-/mnt/pool/app/momenta}"
POSTGRES_CONTAINER="${MOMENTA_POSTGRES_CONTAINER:-momenta-postgres}"
POSTGRES_USER="${MOMENTA_POSTGRES_USER:-momenta}"
POSTGRES_DB="${MOMENTA_POSTGRES_DB:-momenta}"
SERVICES_STOPPED=0

case "$DATA_ROOT" in
  /mnt/*/app/momenta) ;;
  *)
    echo "Unsafe MOMENTA_DATA_ROOT: $DATA_ROOT" >&2
    exit 1
    ;;
esac

for file in SHA256SUMS postgres.dump minio.tar.gz api.tar.gz manifest.txt; do
  if [[ ! -f "$BACKUP_DIR/$file" ]]; then
    echo "Backup file not found: $BACKUP_DIR/$file" >&2
    exit 1
  fi
done

echo "Verifying backup checksums..."
(cd "$BACKUP_DIR" && sha256sum -c SHA256SUMS)

finish() {
  if [[ "$SERVICES_STOPPED" == "1" ]]; then
    docker start momenta-minio >/dev/null || true
    docker start momenta-api >/dev/null || true
    docker start momenta-worker >/dev/null || true
  fi
}
trap finish EXIT

echo "Stopping application services..."
docker stop momenta-worker momenta-api momenta-minio >/dev/null
SERVICES_STOPPED=1

echo "Restoring PostgreSQL..."
docker exec -i "$POSTGRES_CONTAINER" pg_restore \
  --username "$POSTGRES_USER" \
  --dbname "$POSTGRES_DB" \
  --clean \
  --if-exists \
  --no-owner <"$BACKUP_DIR/postgres.dump"

echo "Restoring media and API data..."
for target in "$DATA_ROOT/minio" "$DATA_ROOT/api"; do
  mkdir -p "$target"
  find "$target" -mindepth 1 -maxdepth 1 -exec rm -rf -- {} +
done
tar -C "$DATA_ROOT/minio" -xzf "$BACKUP_DIR/minio.tar.gz"
tar -C "$DATA_ROOT/api" -xzf "$BACKUP_DIR/api.tar.gz"

finish
SERVICES_STOPPED=0
trap - EXIT
echo "Restore complete from: $BACKUP_DIR"
