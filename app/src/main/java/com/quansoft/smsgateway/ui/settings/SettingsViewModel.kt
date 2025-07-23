package com.quansoft.smsgateway.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quansoft.smsgateway.data.repository.SettingsRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val settingsRepository: SettingsRepositoryImpl) : ViewModel() {


    val serverPort: StateFlow<Int> = settingsRepository.getServerPort()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepositoryImpl.DEFAULT_PORT)

    val authToken: StateFlow<String> = settingsRepository.getAuthToken()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Loading...")

    fun updateServerPort(port: String) {
        viewModelScope.launch {
            val portNumber = port.toIntOrNull() ?: SettingsRepositoryImpl.DEFAULT_PORT
            settingsRepository.setServerPort(portNumber)
        }
    }

    fun regenerateToken() {
        viewModelScope.launch {
            settingsRepository.regenerateAuthToken()
        }
    }

    val ipAddress: StateFlow<String> = settingsRepository.getIpAddress()
        .stateIn(viewModelScope, SharingStarted.Eagerly, "Loading...")
}
