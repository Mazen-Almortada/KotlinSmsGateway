package com.quansoft.smsgateway.data

import android.content.Context
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val SERVER_PORT_KEY = intPreferencesKey("server_port")
        val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token") // Key for the auth token
        const val DEFAULT_PORT = 8080
    }

    suspend fun setServerPort(port: Int) {
        dataStore.edit { settings ->
            settings[SERVER_PORT_KEY] = port
        }
    }

    val serverPortFlow: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[SERVER_PORT_KEY] ?: DEFAULT_PORT
        }

    // Flow to read the auth token
    val authTokenFlow: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[AUTH_TOKEN_KEY] ?: generateAndStoreInitialToken()
        }

    // Generates a new token and saves it
    suspend fun regenerateAuthToken(): String {
        val newToken = createNewToken()
        dataStore.edit { settings ->
            settings[AUTH_TOKEN_KEY] = newToken
        }
        return newToken
    }

    // Creates an initial token if one doesn't exist
    private suspend fun generateAndStoreInitialToken(): String {
        val initialToken = createNewToken()
        dataStore.edit { settings ->
            // only_if_needed is not a function, we check for existence before calling
            if (settings[AUTH_TOKEN_KEY] == null) {
                settings[AUTH_TOKEN_KEY] = initialToken
            }
        }
        return initialToken
    }

    // The method to create a token
    private fun createNewToken(): String {
        return "${Build.BOARD.take(4)}-${UUID.randomUUID()}"
    }
}
