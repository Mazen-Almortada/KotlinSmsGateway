package com.quansoft.smsgateway.ui

import android.app.Application
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quansoft.smsgateway.data.AppDatabase
import com.quansoft.smsgateway.data.SettingsManager
import com.quansoft.smsgateway.data.SmsMessageUiItem
import com.quansoft.smsgateway.util.ContactsUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.math.BigInteger
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.ByteOrder

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val smsDao = AppDatabase.getDatabase(application).smsDao()
    private val settingsManager = SettingsManager(application)

    // A flow that maps database messages to UI items with resolved contact names.
    // This runs on a background thread provided by Room.
    private val allMessagesWithContactNames: Flow<List<SmsMessageUiItem>> = smsDao.getAllMessages()
        .map { messages ->
            messages.map { message ->
                val contactName = ContactsUtil.findContactName(application, message.recipient)
                SmsMessageUiItem(message = message, contactName = contactName)
            }
        }

    // Holds the currently selected status filter. A null value means "All".
    private val _selectedStatus = MutableStateFlow<String?>(null)

    // A derived flow that emits a filtered list of messages based on the selected status.
    val filteredMessages: StateFlow<List<SmsMessageUiItem>> =
        combine(allMessagesWithContactNames, _selectedStatus) { messages, status ->
            if (status == null) {
                messages
            } else {
                messages.filter { it.message.status == status }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Updates the active status filter, triggering the filteredMessages flow to re-evaluate.
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
