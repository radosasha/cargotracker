package com.shiplocate.presentation.feature.loads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shiplocate.domain.model.load.Load
import com.shiplocate.domain.usecase.GetTrackingStatusUseCase
import com.shiplocate.domain.usecase.StartTrackingUseCase
import com.shiplocate.domain.usecase.load.GetCachedLoadsUseCase
import com.shiplocate.domain.usecase.load.GetLoadsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for Loads screen
 * Manages loading and displaying loads list
 */
class LoadsViewModel(
    private val getLoadsUseCase: GetLoadsUseCase,
    private val getCachedLoadsUseCase: GetCachedLoadsUseCase,
    private val getTrackingStatusUseCase: GetTrackingStatusUseCase,
    private val startTrackingUseCase: StartTrackingUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<LoadsUiState>(LoadsUiState.Loading)
    val uiState: StateFlow<LoadsUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        println("🎯 LoadsViewModel: Initialized")
        checkAndRestoreTracking()
        loadLoads()
    }

    /**
     * Проверяет, был ли запущен трекинг при предыдущем запуске
     * Если в DataStore сохранено true, автоматически запускает трекинг
     */
    private fun checkAndRestoreTracking() {
        viewModelScope.launch {
            try {
                println("LoadsViewModel: Checking if tracking was active before...")

                // Проверяем состояние из DataStore
                val currentStatus =
                    withContext(Dispatchers.Default) {
                        getTrackingStatusUseCase()
                    }
                val isTrackingActive = currentStatus == com.shiplocate.domain.model.TrackingStatus.ACTIVE

                if (isTrackingActive) {
                    println("LoadsViewModel: Tracking was active before, restoring...")

                    // Автоматически запускаем трекинг
                    val result =
                        withContext(Dispatchers.Default) {
                            startTrackingUseCase()
                        }
                    if (result.isSuccess) {
                        println("LoadsViewModel: ✅ Tracking restored successfully")
                    } else {
                        println("LoadsViewModel: ❌ Failed to restore tracking: ${result.exceptionOrNull()?.message}")
                    }
                } else {
                    println("LoadsViewModel: Tracking was not active, no restoration needed")
                }
            } catch (e: Exception) {
                println("LoadsViewModel: ❌ Error checking tracking state: ${e.message}")
            }
        }
    }

    /**
     * Load loads from server or cache
     * @param isRefresh true if triggered by pull-to-refresh
     */
    fun loadLoads(isRefresh: Boolean = false) {
        println("🔄 LoadsViewModel: Loading loads (refresh: $isRefresh)")

        if (isRefresh) {
            _isRefreshing.value = true
        } else {
            _uiState.value = LoadsUiState.Loading
        }

        viewModelScope.launch {
            val result =
                withContext(Dispatchers.Default) {
                    getLoadsUseCase()
                }

            result.fold(
                onSuccess = { loads ->
                    println("✅ LoadsViewModel: Successfully loaded ${loads.size} loads")
                    _isRefreshing.value = false
                    _uiState.value = LoadsUiState.Success(loads)
                },
                onFailure = { error ->
                    println("❌ LoadsViewModel: Failed to load loads: ${error.message}")
                    _isRefreshing.value = false
                    _uiState.value =
                        LoadsUiState.Error(
                            error.message ?: "Failed to load loads",
                        )
                },
            )
        }
    }

    /**
     * Retry loading loads
     */
    fun retry() {
        println("🔄 LoadsViewModel: Retrying")
        loadLoads()
    }

    /**
     * Refresh loads (pull-to-refresh)
     */
    fun refresh() {
        println("🔄 LoadsViewModel: Refreshing via pull-to-refresh")
        loadLoads(isRefresh = true)
    }

    /**
     * Load loads from cache only (called when returning from HomeScreen)
     */
    fun loadFromCache() {
        println("💾 LoadsViewModel: Loading from cache")

        viewModelScope.launch {
            try {
                val cachedLoads =
                    withContext(Dispatchers.Default) {
                        getCachedLoadsUseCase()
                    }
                println("✅ LoadsViewModel: Successfully loaded ${cachedLoads.size} loads from cache")
                _uiState.value = LoadsUiState.Success(cachedLoads)
            } catch (e: Exception) {
                println("❌ LoadsViewModel: Failed to load from cache: ${e.message}")
                _uiState.value =
                    LoadsUiState.Error(
                        e.message ?: "Failed to load cached data",
                    )
            }
        }
    }
}

/**
 * UI State for Loads screen
 */
sealed class LoadsUiState {
    data object Loading : LoadsUiState()

    data class Success(val loads: List<Load>) : LoadsUiState()

    data class Error(val message: String) : LoadsUiState()
}
