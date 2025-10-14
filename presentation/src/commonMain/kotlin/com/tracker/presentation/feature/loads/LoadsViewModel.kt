package com.tracker.presentation.feature.loads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tracker.domain.model.load.Load
import com.tracker.domain.usecase.load.GetLoadsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Loads screen
 * Manages loading and displaying loads list
 */
class LoadsViewModel(
    private val getLoadsUseCase: GetLoadsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<LoadsUiState>(LoadsUiState.Loading)
    val uiState: StateFlow<LoadsUiState> = _uiState.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    init {
        println("🎯 LoadsViewModel: Initialized")
        loadLoads()
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
            getLoadsUseCase().fold(
                onSuccess = { loads ->
                    println("✅ LoadsViewModel: Successfully loaded ${loads.size} loads")
                    _isRefreshing.value = false
                    if (loads.isEmpty()) {
                        _uiState.value = LoadsUiState.Empty
                    } else {
                        _uiState.value = LoadsUiState.Success(loads)
                    }
                },
                onFailure = { error ->
                    println("❌ LoadsViewModel: Failed to load loads: ${error.message}")
                    _isRefreshing.value = false
                    _uiState.value = LoadsUiState.Error(
                        error.message ?: "Failed to load loads"
                    )
                }
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
}

/**
 * UI State for Loads screen
 */
sealed class LoadsUiState {
    data object Loading : LoadsUiState()
    data object Empty : LoadsUiState()
    data class Success(val loads: List<Load>) : LoadsUiState()
    data class Error(val message: String) : LoadsUiState()
}

