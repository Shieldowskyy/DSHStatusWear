package yt.dsh.statuswear.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import yt.dsh.statuswear.data.DshStatusUiState
import yt.dsh.statuswear.data.StatusRepository

private const val AUTO_REFRESH_INTERVAL_MS = 60_000L

class StatusViewModel(
    private val repository: StatusRepository = StatusRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DshStatusUiState())
    val uiState: StateFlow<DshStatusUiState> = _uiState

    private var refreshJob: Job? = null
    private var autoRefreshJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.fetchStatus()
                .onSuccess { result ->
                    _uiState.value = result
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "Nieznany błąd")
                    }
                }
            restartAutoRefreshTimer()
        }
    }

    private fun restartAutoRefreshTimer() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                delay(AUTO_REFRESH_INTERVAL_MS)
                if (isActive) {
                    _uiState.update { it.copy(isLoading = true) }
                    repository.fetchStatus()
                        .onSuccess { result ->
                            _uiState.value = result
                        }
                        .onFailure { e ->
                            _uiState.update {
                                it.copy(isLoading = false, error = e.message ?: "Nieznany błąd")
                            }
                        }
                }
            }
        }
    }
}

