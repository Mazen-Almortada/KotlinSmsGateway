package com.quansoft.smsgateway.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quansoft.smsgateway.data.local.SmsMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: SmsMessageEntity)

    @Query("SELECT * FROM sms_messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<SmsMessageEntity>>

    @Query("UPDATE sms_messages SET status = :newStatus WHERE id = :id")
    suspend fun updateStatus(id: String, newStatus: String)

    @Query("SELECT * FROM sms_messages WHERE status = 'queued' ORDER BY timestamp ASC")
    suspend fun getQueuedMessages(): List<SmsMessageEntity>

    @Query("DELETE FROM sms_messages WHERE status = 'queued'")
    suspend fun clearQueuedMessages()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<SmsMessageEntity>)

    @Delete
    suspend fun delete(message: SmsMessageEntity)

    @Query("DELETE FROM sms_messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: String)

    @Query("DELETE FROM sms_messages WHERE bulkId = :bulkId")
    suspend fun deleteMessagesByBulkId(bulkId: String)
}
