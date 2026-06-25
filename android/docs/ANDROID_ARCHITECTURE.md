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
