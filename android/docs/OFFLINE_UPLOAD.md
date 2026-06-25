# Offline Upload

## Flow

1. User captures photo → Preview screen
2. Image is compressed (maxWidth=1440, quality=82)
3. User taps "Опубликовать"
4. `UploadQueueEntity` created in Room with status "pending"
5. `WorkManager` enqueued with `NetworkType.CONNECTED`
6. UI shows uploading state
7. On success → UploadSuccess screen
8. On failure → retry with exponential backoff

## UploadQueueEntity

| Field | Description |
|-------|-------------|
| localId | UUID generated locally |
| challengeId | Today's challenge ID |
| filePath | Path to compressed image |
| caption | Optional caption |
| status | pending / uploading / uploaded / failed / cancelled |
| retryCount | Number of retries |

## Worker

`UploadPostWorker` extends `CoroutineWorker`:
- Required network: CONNECTED
- Backoff: EXPONENTIAL
- Max retries: 3 (configurable)
