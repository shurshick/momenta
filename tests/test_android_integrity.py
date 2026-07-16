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


def test_android_publish_uses_non_debuggable_release_apk():
    workflow = (ROOT / ".github/workflows/android-publish.yml").read_text(encoding="utf-8")
    build = (ROOT / "android/app/build.gradle.kts").read_text(encoding="utf-8")

    assert "assembleProdRelease" in workflow
    assert "verifyInstallableProdReleaseApk" in workflow
    assert "app-prod-release.apk" in workflow
    assert "android:debuggable" in workflow
    assert 'release {\n            signingConfig = signingConfigs.getByName("update")' in build


def test_settings_expose_real_product_controls_without_server_details():
    settings = (
        ROOT
        / "android/app/src/main/java/com/bghitech/momenta/feature/settings/SettingsScreen.kt"
    ).read_text(encoding="utf-8")
    view_model = (
        ROOT
        / "android/app/src/main/java/com/bghitech/momenta/feature/settings/SettingsViewModel.kt"
    ).read_text(encoding="utf-8")

    for label in (
        "Публикации",
        "Хранилище",
        "Редактировать профиль",
        "Скопировать диагностику",
        "Конфиденциальность",
        "Условия использования",
    ):
        assert label in settings
    assert "DEFAULT_SERVER_URL" not in settings
    assert "MEDIA_BASE_URL" not in settings
    assert "observePendingForAccount" in view_model
    assert "protectedPaths" in view_model
    assert (ROOT / "docs/PRIVACY_POLICY.md").exists()
    assert (ROOT / "docs/TERMS.md").exists()
