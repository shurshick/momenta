from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def test_backup_creates_database_dump_and_checksums():
    source = (ROOT / "scripts/backup_truenas.sh").read_text(encoding="utf-8")

    assert "pg_dump" in source
    assert "sha256sum" in source
    assert "docker stop momenta-worker momenta-api" in source


def test_restore_requires_confirmation_and_verifies_checksums():
    source = (ROOT / "scripts/restore_truenas.sh").read_text(encoding="utf-8")

    assert 'MOMENTA_RESTORE_CONFIRM:-}" != "YES"' in source
    assert "sha256sum -c SHA256SUMS" in source
    assert 'case "$DATA_ROOT" in' in source
