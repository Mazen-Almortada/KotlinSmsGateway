package com.quansoft.smsgateway.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.quansoft.smsgateway.domain.model.Message

@Entity(
    tableName = "sms_messages",
    foreignKeys = [
        ForeignKey(
            entity = CampaignEntity::class,
            parentColumns = ["id"],
            childColumns = ["bulkId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["bulkId"])]
)
data class SmsMessageEntity(
    @PrimaryKey val id: String,
    val recipient: String,
    val content: String,
    var status: String,
    val timestamp: Long,
    val bulkId: String? = null
) {

}

fun Message.toEntity(): SmsMessageEntity {
    return SmsMessageEntity(
        id = this.id,
        recipient = this.recipient,
        content = this.content,
        status = this.status,
        timestamp = this.timestamp,
        bulkId = this.bulkId
    )
}
// Mapper function to convert Entity to Domain model
fun SmsMessageEntity.toDomain(): Message {
    return Message(
        id = this.id,
        recipient = this.recipient,
        content = this.content,
        status = this.status,
        timestamp = this.timestamp,
        bulkId = this.bulkId
    )
}