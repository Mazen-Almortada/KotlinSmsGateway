package com.quansoft.smsgateway.domain.repository

import kotlinx.coroutines.flow.Flow

interface NetworkInfoRepository {
    fun getIpAddress(): Flow<String>
}