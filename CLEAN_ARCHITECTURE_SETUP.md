# 🏗️ Clean Architecture Setup для GPS Tracker

## 📁 Структура проекта

```
Tracker/
├── domain/                          # Domain слой (платформо-независимый)
│   ├── src/commonMain/kotlin/
│   │   └── com/tracker/domain/
│   │       ├── model/               # Domain модели
│   │       ├── repository/          # Интерфейсы репозиториев
│   │       ├── usecase/             # Use Cases
│   │       └── di/                  # Domain DI модуль
│   └── build.gradle.kts
├── data/                            # Data слой
│   ├── src/
│   │   ├── commonMain/kotlin/       # Общие Data Sources
│   │   ├── androidMain/kotlin/      # Android реализации
│   │   └── iosMain/kotlin/          # iOS реализации
│   │       └── com/tracker/data/
│   │           ├── datasource/      # Data Source интерфейсы
│   │           ├── repository/      # Реализации репозиториев
│   │           └── di/              # Data DI модули
│   └── build.gradle.kts
├── presentation/                    # Presentation слой
│   ├── src/commonMain/kotlin/
│   │   └── com/tracker/presentation/
│   │       ├── feature/             # Feature modules
│   │       │   ├── home/            # Главный экран
│   │       │   └── tracking/        # Экран трекинга
│   │       ├── component/           # Переиспользуемые компоненты
│   │       └── di/                  # Presentation DI модуль
│   └── build.gradle.kts
└── composeApp/                      # Главное приложение
    ├── src/
    │   ├── commonMain/kotlin/       # Общий код приложения
    │   ├── androidMain/kotlin/      # Android-специфичный код
    │   └── iosMain/kotlin/          # iOS-специфичный код
    └── build.gradle.kts
```

## 🎯 Принципы Clean Architecture

### 1. Domain Layer (Слой бизнес-логики)
- **Независим от платформы** - не содержит Android/iOS зависимостей
- **Содержит Use Cases** - бизнес-логика приложения
- **Интерфейсы репозиториев** - контракты для работы с данными
- **Domain модели** - основные сущности приложения

### 2. Data Layer (Слой данных)
- **Реализации репозиториев** - конкретные реализации интерфейсов из Domain
- **Data Sources** - источники данных (локальные, сетевые)
- **Платформо-специфичные реализации** - Android/iOS код

### 3. Presentation Layer (Слой представления)
- **Feature Modules** - каждый экран = отдельный feature
- **ViewModels** - управление состоянием UI
- **Compose UI** - пользовательский интерфейс
- **Компоненты** - переиспользуемые UI элементы

## 🔄 Поток данных

```
UI (Compose) 
    ↓
ViewModel (Presentation)
    ↓
UseCase (Domain)
    ↓
Repository (Data)
    ↓
DataSource (Data)
    ↓
External (API/Database)
```

## 📦 Модули и зависимости

### Domain Module
```kotlin
// Зависимости
- kotlinx-serialization-json
- kotlinx-datetime
- kotlinx-coroutines-core

// Содержит
- Use Cases
- Интерфейсы репозиториев
- Domain модели
```

### Data Module
```kotlin
// Зависимости
- domain (implementation)
- ktor-client-*
- kotlinx-serialization-json
- kotlinx-datetime
- kotlinx-coroutines-core

// Содержит
- Реализации репозиториев
- Data Sources
- Платформо-специфичные реализации
```

### Presentation Module
```kotlin
// Зависимости
- domain (implementation)
- compose-*
- androidx-lifecycle-*
- koin-core

// Содержит
- ViewModels
- Compose UI
- Feature modules
```

## 🎨 Feature Modules

### Home Feature
- **HomeViewModel** - управление главным экраном
- **HomeScreen** - UI главного экрана
- **Функции**: запрос разрешений, управление трекингом

### Tracking Feature
- **TrackingViewModel** - управление экраном трекинга
- **TrackingScreen** - UI экрана трекинга
- **Функции**: отображение статистики, синхронизация данных

## 🔧 Use Cases

### Permission Use Cases
- `GetPermissionStatusUseCase` - получение статуса разрешений
- `RequestAllPermissionsUseCase` - запрос всех разрешений

### Tracking Use Cases
- `StartTrackingUseCase` - запуск GPS трекинга
- `StopTrackingUseCase` - остановка GPS трекинга
- `GetTrackingStatusUseCase` - получение статуса трекинга

### Location Use Cases
- `GetRecentLocationsUseCase` - получение последних координат
- `SyncLocationsUseCase` - синхронизация с сервером

## 🗄️ Data Sources

### Local Data Sources
- `LocalLocationDataSource` - локальное хранение GPS данных
- `PermissionDataSource` - работа с разрешениями
- `TrackingDataSource` - управление GPS трекингом

### Remote Data Sources
- `RemoteLocationDataSource` - отправка данных на сервер

## 🎭 ViewModels

### HomeViewModel
```kotlin
class HomeViewModel(
    private val getPermissionStatusUseCase: GetPermissionStatusUseCase,
    private val getTrackingStatusUseCase: GetTrackingStatusUseCase,
    private val requestAllPermissionsUseCase: RequestAllPermissionsUseCase,
    private val startTrackingUseCase: StartTrackingUseCase,
    private val stopTrackingUseCase: StopTrackingUseCase
) : ViewModel()
```

### TrackingViewModel
```kotlin
class TrackingViewModel(
    private val getRecentLocationsUseCase: GetRecentLocationsUseCase,
    private val getTrackingStatusUseCase: GetTrackingStatusUseCase,
    private val syncLocationsUseCase: SyncLocationsUseCase,
    private val locationRepository: LocationRepository,
    private val trackingRepository: TrackingRepository
) : ViewModel()
```

## 🔌 Dependency Injection

### Модули
- `domainModule` - Use Cases
- `dataModule` - Repositories и Data Sources
- `presentationModule` - ViewModels
- `androidDataModule` - Android-специфичные реализации
- `iosDataModule` - iOS-специфичные реализации

### Инициализация
```kotlin
// Android
AndroidKoinApp.init() // В MainActivity

// iOS
IOSKoinApp.init() // В iOSApp.swift
```

## 🚀 Преимущества архитектуры

### ✅ Масштабируемость
- Легко добавлять новые features
- Независимые модули
- Четкое разделение ответственности

### ✅ Тестируемость
- Use Cases легко тестировать
- Моки для репозиториев
- Изолированная бизнес-логика

### ✅ Поддерживаемость
- Понятная структура
- Слабая связанность
- Высокая когезия

### ✅ Платформо-независимость
- Domain слой не зависит от платформы
- Легко добавлять новые платформы
- Переиспользование бизнес-логики

## 🔄 Миграция с предыдущей архитектуры

### Что изменилось:
1. **Разделение на модули** - domain, data, presentation
2. **Use Cases** - бизнес-логика вынесена в отдельные классы
3. **Feature modules** - каждый экран = отдельный feature
4. **Data Sources** - четкое разделение источников данных
5. **DI структура** - модульная система зависимостей

### Что осталось:
1. **Koin DI** - система внедрения зависимостей
2. **Ktor** - сетевой слой
3. **Compose UI** - пользовательский интерфейс
4. **GPS функциональность** - основная логика трекинга

## 📋 Следующие шаги

1. **Добавить базу данных** - SQLDelight для локального хранения
2. **Добавить навигацию** - Compose Navigation между экранами
3. **Добавить тесты** - Unit тесты для Use Cases
4. **Добавить карты** - отображение GPS треков на карте
5. **Оптимизировать производительность** - кэширование и пагинация

## 🎯 Заключение

Новая архитектура обеспечивает:
- **Четкое разделение слоев**
- **Легкое тестирование**
- **Масштабируемость**
- **Поддерживаемость кода**
- **Платформо-независимость**

Проект готов для дальнейшего развития с соблюдением принципов Clean Architecture! 🚀
