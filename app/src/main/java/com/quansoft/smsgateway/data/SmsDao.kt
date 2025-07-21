package com.quansoft.smsgateway.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: SmsMessage)

    @Query("SELECT * FROM sms_messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<SmsMessage>>

    @Query("UPDATE sms_messages SET status = :newStatus WHERE id = :id")
    suspend fun updateStatus(id: String, newStatus: String)

    @Query("SELECT * FROM sms_messages WHERE status = 'queued' ORDER BY timestamp ASC")
    suspend fun getQueuedMessages(): List<SmsMessage>

    @Query("DELETE FROM sms_messages WHERE status = 'queued'")
    suspend fun clearQueuedMessages()
}
