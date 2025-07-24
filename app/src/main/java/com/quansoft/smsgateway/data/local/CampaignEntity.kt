package com.quansoft.smsgateway.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.quansoft.smsgateway.domain.model.Campaign

@Entity(tableName = "bulk_campaigns")
data class CampaignEntity(
    @PrimaryKey val id: String,
    val name: String,
    val timestamp: Long
)

// Mapper function
fun CampaignEntity.toDomain(): Campaign {
    return Campaign(id = this.id, name = this.name, timestamp = this.timestamp)
}

fun Campaign.toEntity(): CampaignEntity {
    return CampaignEntity(id = this.id, name = this.name, timestamp = this.timestamp)
}