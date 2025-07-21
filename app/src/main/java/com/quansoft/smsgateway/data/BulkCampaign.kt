package com.quansoft.smsgateway.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single bulk message campaign.
 * This table stores the unique ID and name for each campaign.
 */
@Entity(tableName = "bulk_campaigns")
data class BulkCampaign(
    @PrimaryKey val id: String, // This corresponds to the Bulk ID from the API request
    val name: String  ,
    val timestamp: Long,
)
