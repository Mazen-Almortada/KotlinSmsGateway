package com.quansoft.smsgateway.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.quansoft.smsgateway.data.local.CampaignEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CampaignDao {
    /**
     * Inserts a new campaign into the database. If a campaign with the same
     * ID already exists, it will be ignored.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(campaign: CampaignEntity)

    @Query("SELECT * FROM bulk_campaigns ORDER BY timestamp DESC")
    fun getAllCampaigns(): Flow<List<CampaignEntity>>

    @Query("SELECT * FROM bulk_campaigns Where id = :id")
    fun getCampaignByID(id: String): Flow<CampaignEntity>

    // --- New Delete Function ---
    @Query("DELETE FROM bulk_campaigns WHERE id = :campaignId")
    suspend fun deleteCampaignById(campaignId: String)


    @Query("DELETE FROM bulk_campaigns")
    suspend fun deleteAllCampaigns()

    @Query("UPDATE bulk_campaigns SET name = :name WHERE id = :id")
    suspend fun updateCampaignName(id: String, name: String)

    @Update
    suspend fun update(campaign: CampaignEntity)


}
