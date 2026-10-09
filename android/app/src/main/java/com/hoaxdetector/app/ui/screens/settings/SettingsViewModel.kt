package com.hoaxdetector.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hoaxdetector.app.data.local.UserSessionManager
import com.hoaxdetector.app.data.repository.FactCheckRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BackendStatus {
    object Idle : BackendStatus()
    object Checking : BackendStatus()
    data class Online(val message: String) : BackendStatus()
    data class Offline(val message: String) : BackendStatus()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: FactCheckRepository,
    private val sessionManager: UserSessionManager
) : ViewModel() {

    val selectedTheme = MutableStateFlow("Sistem")
    val aiNotificationEnabled = MutableStateFlow(true)
    private val _backendStatus = MutableStateFlow<BackendStatus>(BackendStatus.Idle)
    val backendStatus: StateFlow<BackendStatus> = _backendStatus

    val userName: String get() = sessionManager.getUserName()
    val userEmail: String get() = sessionManager.getUserEmail()
    val isGuest: Boolean get() = sessionManager.isGuest()

    fun logout(onComplete: () -> Unit) {
        sessionManager.logout()
        onComplete()
    }

    fun clearAllHistory(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteAllHistory()
            onComplete()
        }
    }

    fun checkBackendHealth() {
        viewModelScope.launch {
            _backendStatus.value = BackendStatus.Checking
            val isOnline = repository.checkHealth()
            if (isOnline) {
                _backendStatus.value = BackendStatus.Online("Terhubung ke server backend (Port 3000)")
            } else {
                _backendStatus.value = BackendStatus.Offline("Backend belum aktif. Jalankan start-backend.bat")
            }
        }
    }
}
