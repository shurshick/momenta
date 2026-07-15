import json
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]


def test_room_schema_has_explicit_current_migration():
    database_source = (
        ROOT
        / "android/app/src/main/java/com/bghitech/momenta/data/local/MomentaDatabase.kt"
    ).read_text(encoding="utf-8")
    database_module = (
        ROOT
        / "android/app/src/main/java/com/bghitech/momenta/data/local/DatabaseModule.kt"
    ).read_text(encoding="utf-8")
    version = int(re.search(r"version\s*=\s*(\d+)", database_source).group(1))
    schema_path = (
        ROOT
        / f"android/app/schemas/com.bghitech.momenta.data.local.MomentaDatabase/{version}.json"
    )

    assert schema_path.exists()
    assert json.loads(schema_path.read_text(encoding="utf-8"))["database"]["version"] == version
    assert "fallbackToDestructiveMigration" not in database_module
    assert f"MIGRATION_{version - 1}_{version}" in database_module


def test_android_unit_tests_run_in_ci():
    workflow = (ROOT / ".github/workflows/ci.yml").read_text(encoding="utf-8")
    assert "testDevDebugUnitTest" in workflow
    assert "testProdDebugUnitTest" in workflow
