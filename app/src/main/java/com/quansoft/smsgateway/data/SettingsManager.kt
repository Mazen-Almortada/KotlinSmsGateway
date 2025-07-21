package com.quansoft.smsgateway.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val SERVER_PORT_KEY = intPreferencesKey("server_port")
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
}
