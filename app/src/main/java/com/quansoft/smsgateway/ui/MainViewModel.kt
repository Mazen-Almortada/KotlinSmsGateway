package com.quansoft.smsgateway.ui

import android.app.Application
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quansoft.smsgateway.data.AppDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import java.math.BigInteger
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.ByteOrder

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val smsDao = AppDatabase.getDatabase(application).smsDao()

    val messages = smsDao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
        val token = "${Build.BOARD}-${Build.ID}-${Build.SERIAL}-${Build.BOOTLOADER}"
        emit(token)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "Loading...")
}
