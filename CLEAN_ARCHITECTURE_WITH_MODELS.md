# 🏗️ Clean Architecture с правильными моделями для GPS Tracker

## 📁 Структура моделей по слоям

### 🎯 Domain Layer (Слой бизнес-логики)
```
domain/src/commonMain/kotlin/com/tracker/domain/model/
├── Location.kt                    # Domain модель GPS координат
├── PermissionStatus.kt           # Domain модель статуса разрешений
└── TrackingStatus.kt             # Domain модель статуса трекинга
```

**Domain модели** - чистые бизнес-сущности без зависимостей от платформы или фреймворков.

### 🗄️ Data Layer (Слой данных)
```
data/src/commonMain/kotlin/com/tracker/data/
├── model/
│   ├── LocationDataModel.kt      # Data модель для сериализации
│   ├── PermissionDataModel.kt    # Data модель разрешений
│   └── TrackingDataModel.kt      # Data модель статуса трекинга
└── mapper/
    ├── LocationMapper.kt         # Маппер Location <-> LocationDataModel
    ├── PermissionMapper.kt       # Маппер Permission <-> PermissionDataModel
    └── TrackingMapper.kt         # Маппер Tracking <-> TrackingDataModel
```

**Data модели** - модели для работы с внешними источниками данных (API, база данных).

### 🎨 Presentation Layer (Слой представления)
```
presentation/src/commonMain/kotlin/com/tracker/presentation/model/
├── HomeUiState.kt               # UI состояние главного экрана
├── TrackingUiState.kt           # UI состояние экрана трекинга
└── MessageType.kt               # Типы сообщений для UI
```

**Presentation модели** - модели для управления состоянием UI.

## 🔄 Поток данных с маппингом

```
UI (Compose) 
    ↓
Presentation Model (UiState)
    ↓
ViewModel (Presentation)
    ↓
UseCase (Domain)
    ↓
Repository (Data) ←→ Mapper ←→ Data Model
    ↓
DataSource (Data)
    ↓
External (API/Database)
```

## 📦 Модели по слоям

### Domain Models
```kotlin
// Чистые бизнес-сущности
data class Location(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val altitude: Double? = null,
    val speed: Float? = null,
    val bearing: Float? = null,
    val timestamp: Instant,
    val deviceId: String? = null
)

data class PermissionStatus(
    val hasLocationPermission: Boolean,
    val hasBackgroundLocationPermission: Boolean,
    val hasNotificationPermission: Boolean,
    val isBatteryOptimizationDisabled: Boolean
) {
    val hasAllPermissions: Boolean
        get() = hasLocationPermission && hasBackgroundLocationPermission && hasNotificationPermission
}

enum class TrackingStatus {
    STOPPED, STARTING, ACTIVE, STOPPING, ERROR
}
```

### Data Models
```kotlin
// Модели для сериализации и работы с внешними источниками
@Serializable
data class LocationDataModel(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val altitude: Double? = null,
    val speed: Float? = null,
    val bearing: Float? = null,
    val timestamp: Instant,
    val deviceId: String? = null
)

@Serializable
data class LocationRequestDataModel(
    val locations: List<LocationDataModel>
)

@Serializable
data class LocationResponseDataModel(
    val success: Boolean,
    val message: String? = null,
    val processedCount: Int = 0
)

data class PermissionDataModel(
    val hasLocationPermission: Boolean,
    val hasBackgroundLocationPermission: Boolean,
    val hasNotificationPermission: Boolean,
    val isBatteryOptimizationDisabled: Boolean
)

enum class TrackingDataStatus {
    STOPPED, STARTING, ACTIVE, STOPPING, ERROR
}
```

### Presentation Models
```kotlin
// Модели для управления состоянием UI
data class HomeUiState(
    val permissionStatus: PermissionStatus? = null,
    val trackingStatus: TrackingStatus = TrackingStatus.STOPPED,
    val isLoading: Boolean = true,
    val message: String? = null,
    val messageType: MessageType? = null
)

data class TrackingUiState(
    val trackingStatus: TrackingStatus = TrackingStatus.STOPPED,
    val lastLocation: Location? = null,
    val recentLocations: List<Location> = emptyList(),
    val totalLocations: Int = 0,
    val isSyncing: Boolean = false,
    val message: String? = null,
    val messageType: MessageType? = null
)

enum class MessageType {
    SUCCESS, ERROR, INFO
}
```

## 🔄 Мапперы между слоями

### LocationMapper
```kotlin
object LocationMapper {
    
    fun toDomain(dataModel: LocationDataModel): Location {
        return Location(
            latitude = dataModel.latitude,
            longitude = dataModel.longitude,
            accuracy = dataModel.accuracy,
            altitude = dataModel.altitude,
            speed = dataModel.speed,
            bearing = dataModel.bearing,
            timestamp = dataModel.timestamp,
            deviceId = dataModel.deviceId
        )
    }
    
    fun toData(domainModel: Location): LocationDataModel {
        return LocationDataModel(
            latitude = domainModel.latitude,
            longitude = domainModel.longitude,
            accuracy = domainModel.accuracy,
            altitude = domainModel.altitude,
            speed = domainModel.speed,
            bearing = domainModel.bearing,
            timestamp = domainModel.timestamp,
            deviceId = domainModel.deviceId
        )
    }
    
    fun toDomainList(dataModels: List<LocationDataModel>): List<Location> {
        return dataModels.map { toDomain(it) }
    }
    
    fun toDataList(domainModels: List<Location>): List<LocationDataModel> {
        return domainModels.map { toData(it) }
    }
}
```

### PermissionMapper
```kotlin
object PermissionMapper {
    
    fun toDomain(dataModel: PermissionDataModel): PermissionStatus {
        return PermissionStatus(
            hasLocationPermission = dataModel.hasLocationPermission,
            hasBackgroundLocationPermission = dataModel.hasBackgroundLocationPermission,
            hasNotificationPermission = dataModel.hasNotificationPermission,
            isBatteryOptimizationDisabled = dataModel.isBatteryOptimizationDisabled
        )
    }
    
    fun toData(domainModel: PermissionStatus): PermissionDataModel {
        return PermissionDataModel(
            hasLocationPermission = domainModel.hasLocationPermission,
            hasBackgroundLocationPermission = domainModel.hasBackgroundLocationPermission,
            hasNotificationPermission = domainModel.hasNotificationPermission,
            isBatteryOptimizationDisabled = domainModel.isBatteryOptimizationDisabled
        )
    }
}
```

### TrackingMapper
```kotlin
object TrackingMapper {
    
    fun toDomain(dataStatus: TrackingDataStatus): TrackingStatus {
        return when (dataStatus) {
            TrackingDataStatus.STOPPED -> TrackingStatus.STOPPED
            TrackingDataStatus.STARTING -> TrackingStatus.STARTING
            TrackingDataStatus.ACTIVE -> TrackingStatus.ACTIVE
            TrackingDataStatus.STOPPING -> TrackingStatus.STOPPING
            TrackingDataStatus.ERROR -> TrackingStatus.ERROR
        }
    }
    
    fun toData(domainStatus: TrackingStatus): TrackingDataStatus {
        return when (domainStatus) {
            TrackingStatus.STOPPED -> TrackingDataStatus.STOPPED
            TrackingStatus.STARTING -> TrackingDataStatus.STARTING
            TrackingStatus.ACTIVE -> TrackingDataStatus.ACTIVE
            TrackingStatus.STOPPING -> TrackingDataStatus.STOPPING
            TrackingStatus.ERROR -> TrackingDataStatus.ERROR
        }
    }
}
```

## 🔧 Использование мапперов в репозиториях

### LocationRepositoryImpl
```kotlin
class LocationRepositoryImpl(
    private val localDataSource: LocationDataSource,
    private val remoteDataSource: LocationRemoteDataSource
) : LocationRepository {
    
    override suspend fun saveLocation(location: Location) {
        val dataModel = LocationMapper.toData(location)
        localDataSource.saveLocation(dataModel)
    }
    
    override suspend fun getAllLocations(): List<Location> {
        val dataModels = localDataSource.getAllLocations()
        return LocationMapper.toDomainList(dataModels)
    }
    
    override fun observeLocations(): Flow<Location> {
        return localDataSource.observeLocations().map { dataModel ->
            LocationMapper.toDomain(dataModel)
        }
    }
}
```

### PermissionRepositoryImpl
```kotlin
class PermissionRepositoryImpl(
    private val permissionDataSource: PermissionDataSource
) : PermissionRepository {
    
    override suspend fun getPermissionStatus(): PermissionStatus {
        val dataModel = permissionDataSource.getPermissionStatus()
        return PermissionMapper.toDomain(dataModel)
    }
    
    override suspend fun requestAllPermissions(): Result<PermissionStatus> {
        return permissionDataSource.requestAllPermissions().map { dataModel ->
            PermissionMapper.toDomain(dataModel)
        }
    }
}
```

## 🎯 Преимущества правильной архитектуры моделей

### ✅ **Разделение ответственности**
- **Domain модели** - чистые бизнес-сущности
- **Data модели** - для работы с внешними источниками
- **Presentation модели** - для управления UI состоянием

### ✅ **Независимость слоев**
- Domain слой не знает о Data моделях
- Data слой не знает о Presentation моделях
- Presentation слой не знает о Data моделях

### ✅ **Легкое тестирование**
- Можно мокать мапперы
- Тестировать каждый слой независимо
- Изолированная бизнес-логика

### ✅ **Гибкость**
- Легко изменить структуру API без влияния на Domain
- Можно добавить новые поля в Data модели
- UI может иметь свои специфичные поля

### ✅ **Типобезопасность**
- Компилятор проверяет правильность маппинга
- Невозможно случайно передать неправильную модель
- Четкие контракты между слоями

## 🔄 Поток маппинга

### 1. API → Data → Domain
```kotlin
// API возвращает JSON
val jsonResponse = api.getLocations()

// Десериализуется в Data модель
val dataModels: List<LocationDataModel> = json.decodeFromString(jsonResponse)

// Маппится в Domain модель
val domainModels: List<Location> = LocationMapper.toDomainList(dataModels)
```

### 2. Domain → Data → API
```kotlin
// Domain модель
val domainModel: Location = getLocationFromBusinessLogic()

// Маппится в Data модель
val dataModel: LocationDataModel = LocationMapper.toData(domainModel)

// Сериализуется и отправляется в API
val json = json.encodeToString(dataModel)
api.sendLocation(json)
```

### 3. Domain → Presentation
```kotlin
// Domain модель
val permissionStatus: PermissionStatus = getPermissionStatus()

// Используется напрямую в Presentation (без маппинга)
val uiState = HomeUiState(permissionStatus = permissionStatus)
```

## 📋 Правила архитектуры

### ✅ **Что можно делать:**
- Domain модели использовать в Presentation
- Data модели использовать только в Data слое
- Маппинг только в Data слое
- Presentation модели только для UI состояния

### ❌ **Что нельзя делать:**
- Использовать Data модели в Domain
- Использовать Presentation модели в Domain
- Маппинг в Domain или Presentation слоях
- Прямое использование Data моделей в UI

## 🎯 Заключение

Правильная архитектура моделей обеспечивает:
- **Четкое разделение ответственности**
- **Независимость слоев**
- **Легкое тестирование**
- **Гибкость и масштабируемость**
- **Типобезопасность**

Каждый слой имеет свои модели, и маппинг происходит только в Data слое, что соответствует принципам Clean Architecture! 🚀
