package com.quansoft.smsgateway.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quansoft.smsgateway.data.SettingsManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsManager = SettingsManager(application)

    val serverPort: StateFlow<Int> = settingsManager.serverPortFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsManager.DEFAULT_PORT
        )

    fun updateServerPort(port: String) {
        viewModelScope.launch {
            val portNumber = port.toIntOrNull() ?: SettingsManager.DEFAULT_PORT
            settingsManager.setServerPort(portNumber)
        }
    }
}
