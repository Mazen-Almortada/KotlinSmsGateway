package com.quansoft.smsgateway.domain.repository

import com.quansoft.smsgateway.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun getAllMessages(): Flow<List<Message>>
    suspend fun deleteMessage(message: Message)
    suspend fun delete(message: Message)
    suspend fun deleteMessagesByCampaignId(id: String)
}