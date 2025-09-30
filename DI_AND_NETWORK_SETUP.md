# 🚀 Настройка DI и Network слоя в GPS Tracker

## 📦 Добавленные библиотеки

### Koin DI
- `koin-core` - основная библиотека DI
- `koin-android` - Android-специфичные функции
- `koin-compose` - интеграция с Compose

### Ktor Network
- `ktor-client-core` - основной HTTP клиент
- `ktor-client-android` - Android HTTP клиент
- `ktor-client-ios` - iOS HTTP клиент (Darwin)
- `ktor-client-content-negotiation` - сериализация
- `ktor-serialization-kotlinx-json` - JSON сериализация
- `ktor-client-logging` - логирование запросов

### Дополнительные библиотеки
- `kotlinx-serialization-json` - JSON сериализация
- `kotlinx-datetime` - работа с датами
- `kotlinx-coroutines-core` - корутины

## 🏗️ Архитектура

### Структура проекта
```
composeApp/src/
├── commonMain/kotlin/com/tracker/
│   ├── di/                          # DI модули
│   │   ├── AppModule.kt             # Основные зависимости
│   │   ├── ViewModelModule.kt       # ViewModels
│   │   ├── PlatformModule.kt        # Платформо-специфичные сервисы
│   │   └── KoinApp.kt               # Общие модули
│   ├── data/
│   │   ├── model/                   # Модели данных
│   │   └── repository/              # Репозитории
│   ├── domain/service/              # Интерфейсы сервисов
│   ├── network/                     # Сетевой слой
│   └── presentation/viewmodel/      # ViewModels
├── androidMain/kotlin/com/tracker/
│   ├── di/
│   │   ├── AndroidModule.kt         # Android DI модуль
│   │   └── AndroidKoinApp.kt        # Android инициализация
│   └── domain/service/              # Android реализации
└── iosMain/kotlin/com/tracker/
    ├── di/
    │   ├── IOSModule.kt             # iOS DI модуль
    │   └── IOSKoinApp.kt            # iOS инициализация
    └── domain/service/              # iOS реализации
```

## 🔧 Инициализация

### Android
```kotlin
// В MainActivity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Инициализируем Koin DI
        AndroidKoinApp.init()
        
        setContent {
            App()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        AndroidKoinApp.stop()
    }
}
```

### iOS
```swift
// В iOSApp.swift
@main
struct iOSApp: App {
    init() {
        // Инициализируем Koin DI
        IOSKoinAppKt.init()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

## 📱 Использование в Compose

### Инъекция зависимостей
```kotlin
@Composable
fun MyScreen() {
    val viewModel: MainViewModel = koinInject()
    val repository: LocationRepository = koinInject()
    
    // Использование...
}
```

### ViewModels
```kotlin
class MainViewModel(
    private val locationService: LocationService,
    private val permissionService: PermissionService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    // Логика...
}
```

## 🌐 Сетевой слой

### HTTP клиент
```kotlin
class HttpClientProviderImpl : HttpClientProvider {
    override fun createClient(): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(Logging) {
                level = LogLevel.INFO
            }
        }
    }
}
```

### API интерфейс
```kotlin
interface LocationApi {
    suspend fun sendLocationData(request: LocationRequest): Result<LocationResponse>
    suspend fun checkConnection(): Result<Boolean>
}
```

### Использование в репозитории
```kotlin
class LocationRepositoryImpl(
    private val locationApi: LocationApi
) : LocationRepository {
    
    override suspend fun syncLocationsToServer(): Result<Unit> {
        return try {
            val request = LocationRequest(locations.toList())
            val result = locationApi.sendLocationData(request)
            
            if (result.isSuccess) {
                locations.clear()
            }
            
            result.map { Unit }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## 🔄 Потоки данных

### Flow для GPS координат
```kotlin
interface LocationService {
    fun observeLocationUpdates(): Flow<LocationData>
}

// В ViewModel
viewModelScope.launch {
    locationService.observeLocationUpdates().collect { location ->
        locationRepository.saveLocation(location)
        // Обновляем UI
    }
}
```

### StateFlow для UI состояния
```kotlin
data class MainUiState(
    val isTracking: Boolean = false,
    val hasPermissions: Boolean = false,
    val message: String = ""
)

// В Compose
val uiState by viewModel.uiState.collectAsState()
```

## 🎯 Основные возможности

### ✅ Что реализовано
- [x] Koin DI контейнер с модулями
- [x] Ktor HTTP клиент с сериализацией
- [x] Платформо-специфичные сервисы
- [x] ViewModels с StateFlow
- [x] Репозиторий для GPS данных
- [x] API для отправки данных на сервер
- [x] Compose UI с инъекцией зависимостей
- [x] Обработка разрешений
- [x] Фоновый трекинг GPS

### 🚧 Что нужно доработать
- [ ] Реальная база данных (SQLDelight)
- [ ] Настройки приложения
- [ ] Обработка ошибок сети
- [ ] Кэширование данных
- [ ] Push уведомления
- [ ] Карты для отображения треков

## 🔧 Настройка сервера

### Изменение URL API
В файле `LocationApiImpl.kt` измените базовый URL:
```kotlin
private val baseUrl = "https://your-api-server.com/api"
```

### Формат данных
Сервер должен принимать JSON в формате:
```json
{
  "locations": [
    {
      "latitude": 55.7558,
      "longitude": 37.6176,
      "accuracy": 5.0,
      "altitude": 156.0,
      "speed": 10.5,
      "bearing": 45.0,
      "timestamp": "2024-01-01T12:00:00Z",
      "deviceId": "device123"
    }
  ]
}
```

## 🚀 Следующие шаги

1. **Настройте сервер** - создайте API endpoint для приема GPS данных
2. **Добавьте базу данных** - используйте SQLDelight для локального хранения
3. **Настройте карты** - добавьте отображение треков на карте
4. **Добавьте настройки** - позвольте пользователю настраивать интервалы трекинга
5. **Оптимизируйте батарею** - настройте разумные интервалы обновления GPS

## 📚 Полезные ссылки

- [Koin Documentation](https://insert-koin.io/)
- [Ktor Documentation](https://ktor.io/)
- [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
