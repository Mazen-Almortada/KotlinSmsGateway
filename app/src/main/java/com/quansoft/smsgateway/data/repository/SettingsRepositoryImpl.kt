package com.quansoft.smsgateway.data.repository

import android.content.Context
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.quansoft.smsgateway.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
@Singleton
class SettingsRepositoryImpl @Inject constructor(@ApplicationContext context: Context) : SettingsRepository {
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

}