package com.quansoft.smsgateway.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents a single SMS message.
 * The bulkId is a foreign key that links this message to a campaign
 * in the 'bulk_campaigns' table.
 */
@Entity(
    tableName = "sms_messages",
    foreignKeys = [
        ForeignKey(
            entity = BulkCampaign::class,
            parentColumns = ["id"],
            childColumns = ["bulkId"],
            onDelete = ForeignKey.CASCADE // If a campaign is deleted, just nullify the reference
        )
    ],
    indices = [Index(value = ["bulkId"])] // Index for faster queries on bulkId
)
@Serializable
data class SmsMessage(
    @PrimaryKey val id: String,
    val recipient: String,
    val content: String,
    var status: String,
    val timestamp: Long,
    val bulkId: String? = null // This is now a foreign key
)
