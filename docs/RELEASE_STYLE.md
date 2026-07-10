# Release Style

Короткие правила для GitHub Releases, README и документации.

## GitHub Release

- Заголовок: `vX.Y.Z`.
- Описание: 3-5 коротких пунктов по делу.
- Писать по-русски, без длинных технических простыней.
- Пользовательские фиксы выше внутренних tooling-правок.
- APK-блок всегда одинаковый:

```text
APK: app-prod-debug.apk
sha256: <sha>
size: <bytes>
Docker: ghcr.io/shurshick/momenta:vX.Y.Z
```

## README

- Показывать только текущую стабильную версию.
- Для деталей отправлять в `CHANGELOG.md`.
- Не копировать полный changelog на главную.

## Docs

- В `docs/API.md` держать актуальный пример `/api/v1/app/latest`.
- В `docs/DEPLOY_TRUENAS.md` держать актуальный Docker tag и env-блок.
- Release notes в env писать одной строкой через `|`, без markdown.

## Encoding

- Все текстовые файлы: UTF-8.
- Перед коммитом запускать `python scripts/check_mojibake.py`.
- В Windows PowerShell профиль должен задавать UTF-8 для console IO и `Get-Content`.
