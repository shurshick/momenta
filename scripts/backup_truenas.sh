#!/usr/bin/env bash
set -Eeuo pipefail

BACKUP_ROOT="${MOMENTA_BACKUP_ROOT:-/mnt/pool/backups/momenta}"
DATA_ROOT="${MOMENTA_DATA_ROOT:-/mnt/pool/app/momenta}"
POSTGRES_CONTAINER="${MOMENTA_POSTGRES_CONTAINER:-momenta-postgres}"
POSTGRES_USER="${MOMENTA_POSTGRES_USER:-momenta}"
POSTGRES_DB="${MOMENTA_POSTGRES_DB:-momenta}"
STAMP="$(date -u +%Y%m%dT%H%M%SZ)"
BACKUP_DIR="${BACKUP_ROOT}/${STAMP}"
SERVICES_STOPPED=0

finish() {
  if [[ "$SERVICES_STOPPED" == "1" ]]; then
    docker start momenta-api >/dev/null || true
    docker start momenta-worker >/dev/null || true
  fi
}
trap finish EXIT

for path in "$DATA_ROOT/minio" "$DATA_ROOT/api"; do
  if [[ ! -d "$path" ]]; then
    echo "Required directory not found: $path" >&2
    exit 1
  fi
done

mkdir -p "$BACKUP_DIR"
echo "Stopping API and worker for a consistent media snapshot..."
docker stop momenta-worker momenta-api >/dev/null
SERVICES_STOPPED=1

echo "Dumping PostgreSQL..."
docker exec "$POSTGRES_CONTAINER" pg_dump \
  --username "$POSTGRES_USER" \
  --dbname "$POSTGRES_DB" \
  --format custom \
  --no-owner >"$BACKUP_DIR/postgres.dump"

echo "Archiving media and API data..."
tar -C "$DATA_ROOT/minio" -czf "$BACKUP_DIR/minio.tar.gz" .
tar -C "$DATA_ROOT/api" -czf "$BACKUP_DIR/api.tar.gz" .

cat >"$BACKUP_DIR/manifest.txt" <<EOF
created_at_utc=$STAMP
data_root=$DATA_ROOT
postgres_container=$POSTGRES_CONTAINER
postgres_database=$POSTGRES_DB
EOF

(
  cd "$BACKUP_DIR"
  sha256sum postgres.dump minio.tar.gz api.tar.gz manifest.txt >SHA256SUMS
)

finish
SERVICES_STOPPED=0
trap - EXIT
echo "Backup ready: $BACKUP_DIR"
