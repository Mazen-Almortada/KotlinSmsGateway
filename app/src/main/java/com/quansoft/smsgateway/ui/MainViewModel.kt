package com.quansoft.smsgateway.ui

import android.app.Application
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quansoft.smsgateway.data.AppDatabase
import com.quansoft.smsgateway.data.SettingsManager
import com.quansoft.smsgateway.data.SmsMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import java.math.BigInteger
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.ByteOrder

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val smsDao = AppDatabase.getDatabase(application).smsDao()
    private val settingsManager = SettingsManager(application)

    private val allMessages = smsDao.getAllMessages()

    private val _selectedStatus = MutableStateFlow<String?>(null)

    val filteredMessages: StateFlow<List<SmsMessage>> =
        combine(allMessages, _selectedStatus) { messages, status ->
            if (status == null) {
                messages
            } else {
                messages.filter { it.status == status }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectStatus(status: String?) {
        _selectedStatus.value = status
    }


    val serverPort: StateFlow<Int> = settingsManager.serverPortFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsManager.DEFAULT_PORT
        )

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


    val deviceToken: StateFlow<String> = flow {
        val token = "${Build.BOARD}-${Build.ID}-${Build.BOOTLOADER}"
        emit(token)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "Loading...")
}
