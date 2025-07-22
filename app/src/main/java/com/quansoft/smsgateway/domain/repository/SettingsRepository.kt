package com.quansoft.smsgateway.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getServerPort(): Flow<Int>
    suspend fun setServerPort(port: Int)
    fun getAuthToken(): Flow<String>
    suspend fun regenerateAuthToken(): String
    suspend fun generateAndStoreInitialToken(): String
}