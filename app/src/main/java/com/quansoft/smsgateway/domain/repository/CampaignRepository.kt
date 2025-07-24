package com.quansoft.smsgateway.domain.repository
import com.quansoft.smsgateway.domain.model.Campaign
import kotlinx.coroutines.flow.Flow

interface CampaignRepository {
    fun getAllCampaigns(): Flow<List<Campaign>>
    suspend fun delete(campaign: Campaign)
    suspend fun deleteCampaignById(id: String)
    suspend fun insert(campaign: Campaign)
    suspend fun update(campaign: Campaign)
}