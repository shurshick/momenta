# Release Style

Короткие правила для GitHub Releases, README и документации.

## GitHub Release

- Заголовок: `vX.Y.Z`.
- Описание: 3-5 коротких пунктов по делу.
- Писать по-русски, без длинных технических простыней.
- Пользовательские фиксы выше внутренних tooling-правок.
- Server-only релиз не должен прикреплять APK. В описании достаточно Docker:

```text
Docker: ghcr.io/shurshick/momenta:vX.Y.Z
```

- Android-релиз публикуется только когда менялся APK. Workflow прикрепляет:

```text
APK: app-prod-release.apk
Metadata: android-update.json
Docker: ghcr.io/shurshick/momenta:vX.Y.Z
```

- Docker image публикуется workflow `Docker Publish`.
- APK и `android-update.json` прикрепляются ручным workflow `Android APK Publish` с нужным release tag.

## README

- Показывать только текущую стабильную версию.
- Для деталей отправлять в `CHANGELOG.md`.
- Не копировать полный changelog на главную.

## Docs

- В `docs/API.md` держать пример `/api/v1/app/latest`.
- В `docs/DEPLOY_TRUENAS.md` держать актуальный Docker tag и env-блок без ручных APK sha/size/version.

## Encoding

- Все текстовые файлы: UTF-8.
- Перед коммитом запускать `python scripts/check_mojibake.py`.
- В Windows PowerShell профиль должен задавать UTF-8 для console IO и `Get-Content`.
