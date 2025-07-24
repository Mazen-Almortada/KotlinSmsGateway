package com.quansoft.smsgateway.data.repository

import android.content.Context
import android.net.wifi.WifiManager
import com.quansoft.smsgateway.domain.repository.NetworkInfoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.math.BigInteger
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkInfoRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : NetworkInfoRepository {

    override fun getIpAddress(): Flow<String> = flow {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        var ipAddress = wifiManager.connectionInfo.ipAddress
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            ipAddress = Integer.reverseBytes(ipAddress)
        }
        val ipByteArray = BigInteger.valueOf(ipAddress.toLong()).toByteArray()
        try {
            val ip = InetAddress.getByAddress(ipByteArray).hostAddress
            emit(ip ?: "N/A")
        } catch (ex: UnknownHostException) {
            emit("Error")
        }
    }
}