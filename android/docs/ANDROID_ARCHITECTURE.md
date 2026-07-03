# Android Architecture

## Clean-ish Architecture

```
feature/ → domain/ → data/
   │          │         │
   UI      use cases   API/DTO/Room
  ViewModel  models    repositories
```

- **feature/** — экраны, ViewModel, State, Composable-функции
- **domain/** — бизнес-логика, модели, UseCase-ы, интерфейсы репозиториев
- **data/** — реализация репозиториев, Retrofit API, Room DAO, DTO, мапперы

## DI

Hilt: `@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`, `@Module @InstallIn`

## State Management

Каждый экран имеет `UiState` data class. ViewModel управляет состоянием через `MutableStateFlow`. Compose подписывается через `collectAsStateWithLifecycle()`.

## Offline-first

- Challenge кэшируется в Room
- Feed кэшируется в Room
- При отсутствии сети показывается закэшированные данные
- Ошибки сети не скрывают кэш

## v0.2.41 Notes

- Today screen loads the daily challenge and the best moment as independent states.
- Android never generates the daily challenge locally; it always asks `/api/v1/challenges/today`.
- Only a `PostPublished` refresh can trigger one-time scroll to top in the feed.
- Retry/background/return refreshes must preserve feed scroll position.
- Empty `/api/v1/feed/today` responses must not erase the local feed cache.
