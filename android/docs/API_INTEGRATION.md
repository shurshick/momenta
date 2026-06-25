# API Integration

## Retrofit

- OkHttp client с AuthInterceptor (добавляет Bearer token)
- Логирование в debug через HttpLoggingInterceptor
- Kotlinx Serialization конвертер

## Auth Flow

1. Login/Register → получаем `accessToken` + `refreshToken`
2. Токены сохраняются в DataStore
3. AuthInterceptor добавляет `Authorization: Bearer <token>` к каждому запросу
4. При 401 → очистка токенов (в MVP). В v1.0 refresh через `/auth/refresh`

## Error Handling

Все ответы API обёрнуты в `AppResult<T>`:
- `Success<T>` — данные
- `Error` — с типом ошибки (Network, Unauthorized, Server, Validation, Unknown)
