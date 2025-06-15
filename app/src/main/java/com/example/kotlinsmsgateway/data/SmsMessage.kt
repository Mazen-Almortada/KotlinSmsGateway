package com.example.kotlinsmsgateway.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "sms_messages")
data class SmsMessage(
    @PrimaryKey val id: String,
    val recipient: String,
    val content: String,
    var status: String,
    val timestamp: Long
)
