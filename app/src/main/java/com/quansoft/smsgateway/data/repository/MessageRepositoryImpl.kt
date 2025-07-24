package com.quansoft.smsgateway.data.repository

import com.quansoft.smsgateway.data.local.dao.SmsDao
import com.quansoft.smsgateway.data.local.toDomain
import com.quansoft.smsgateway.data.local.toEntity
import com.quansoft.smsgateway.domain.model.Message
import com.quansoft.smsgateway.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val smsDao: SmsDao,

) : MessageRepository {

    override fun getAllMessages(): Flow<List<Message>> {
        return smsDao.getAllMessages().map { entities -> entities.map { it.toDomain() } }
    }
    override suspend fun deleteMessage(message: Message) {
        // We need a way to map back or just use the ID
        smsDao.deleteMessageById(message.id)
    }

    override suspend fun delete(message: Message) {
        smsDao.deleteMessageById(message.id)
    }

    override suspend fun deleteMessagesByCampaignId(id: String) {
        smsDao.deleteMessagesByBulkId(id)
    }

    override suspend fun insert(message: Message) {
        smsDao.insert(message.toEntity())
    }

    override suspend fun updateStatus(id: String, newStatus: String) {
        smsDao.updateStatus(id, newStatus)
    }

    override suspend fun getQueuedMessages(): List<Message> {
        return smsDao.getQueuedMessages().map { it.toDomain() }
    }

    override suspend fun clearQueuedMessages() {
        smsDao.clearQueuedMessages()
    }

    override suspend fun insertAll(messages: List<Message>) {
        smsDao.insertAll(messages.map { it.toEntity() })
    }

}