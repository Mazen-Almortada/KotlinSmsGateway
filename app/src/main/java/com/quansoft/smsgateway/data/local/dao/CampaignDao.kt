package com.quansoft.smsgateway.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BulkCampaignDao {
    /**
     * Inserts a new campaign into the database. If a campaign with the same
     * ID already exists, it will be ignored.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(campaign: BulkCampaign)

    @Query("SELECT * FROM bulk_campaigns ORDER BY timestamp DESC")
    fun getAllCampaigns(): Flow<List<BulkCampaign>>

    @Query("SELECT * FROM bulk_campaigns Where id = :id")
    fun getCampaignByID(id: String): Flow<BulkCampaign>

    // --- New Delete Function ---
    @Delete
    suspend fun delete(campaign: BulkCampaign)
}
