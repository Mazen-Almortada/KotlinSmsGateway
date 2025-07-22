package com.quansoft.smsgateway.domain.repository
import com.quansoft.smsgateway.domain.model.Campaign
import kotlinx.coroutines.flow.Flow

interface CampaignRepository {
    fun getAllCampaigns(): Flow<List<Campaign>>
    suspend fun deleteCampaignAndMessages(campaign: Campaign)
    suspend fun delete(campaign: Campaign)
}