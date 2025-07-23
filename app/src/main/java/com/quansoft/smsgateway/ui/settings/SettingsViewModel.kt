package com.quansoft.smsgateway.ui.settings

import android.app.Application
import android.content.Context
import android.net.wifi.WifiManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quansoft.smsgateway.data.repository.SettingsRepositoryImpl
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigInteger
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.ByteOrder

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepositoryImpl(application)

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

    val ipAddress: StateFlow<String> = flow {
        val wifiManager = application.getSystemService(Context.WIFI_SERVICE) as WifiManager
        var ipAddress = wifiManager.connectionInfo.ipAddress
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress)
        }
        val ipByteArray = BigInteger.valueOf(ipAddress.toLong()).toByteArray()
        try {
            val ip = InetAddress.getByAddress(ipByteArray).hostAddress
            emit(ip ?: "N/A")
        } catch (ex: UnknownHostException) {
            emit("Error")
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "Loading...")
}
