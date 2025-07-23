package com.quansoft.smsgateway.data.repository

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.quansoft.smsgateway.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.math.BigInteger
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.ByteOrder
import java.util.UUID
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepositoryImpl @Inject constructor (@ApplicationContext private val context: Context) : SettingsRepository {
    private val dataStore = context.dataStore
    companion object {
        val SERVER_PORT_KEY = intPreferencesKey("server_port")
        val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token") // Key for the auth token
        const val DEFAULT_PORT = 8080
    }
    override fun getServerPort(): Flow<Int> {
        return  dataStore.data
            .map { preferences ->
                preferences[SERVER_PORT_KEY] ?: DEFAULT_PORT
            }
    }

    override suspend fun setServerPort(port: Int) {
        dataStore.edit { settings ->
            settings[SERVER_PORT_KEY] = port
        }
    }

    override fun getAuthToken(): Flow<String> {
        return dataStore.data
            .map { preferences ->
                preferences[AUTH_TOKEN_KEY] ?: generateAndStoreInitialToken()
            }
    }
    // The method to create a token
    private fun createNewToken(): String {
        return "${Build.BOARD.take(4)}-${UUID.randomUUID()}"
    }
    override suspend fun regenerateAuthToken(): String {
        val initialToken = createNewToken()
        dataStore.edit { settings ->
            // only_if_needed is not a function, we check for existence before calling
            if (settings[AUTH_TOKEN_KEY] == null) {
                settings[AUTH_TOKEN_KEY] = initialToken
            }
        }
        return initialToken
    }

    override suspend fun generateAndStoreInitialToken(): String {
        val initialToken = createNewToken()
        dataStore.edit { settings ->
            // only_if_needed is not a function, we check for existence before calling
            if (settings[AUTH_TOKEN_KEY] == null) {
                settings[AUTH_TOKEN_KEY] = initialToken
            }
        }
        return initialToken
    }

    override fun getIpAddress(): Flow<String> {
        return flow {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
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
        }
            .flowOn(Dispatchers.IO)
    }


}