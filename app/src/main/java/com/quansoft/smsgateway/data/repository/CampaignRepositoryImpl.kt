package com.quansoft.smsgateway.data.repository

import com.quansoft.smsgateway.data.local.dao.CampaignDao
import com.quansoft.smsgateway.data.local.dao.SmsDao
import com.quansoft.smsgateway.data.local.toDomain
import com.quansoft.smsgateway.data.local.toEntity
import com.quansoft.smsgateway.domain.model.Campaign
import com.quansoft.smsgateway.domain.repository.CampaignRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CampaignRepositoryImpl @Inject constructor (private val campaignDao: CampaignDao, private val smsDao: SmsDao) : CampaignRepository{
    override fun getAllCampaigns(): Flow<List<Campaign>> {
        return campaignDao.getAllCampaigns().map { entities -> entities.map{it.toDomain()} }
    }

    override suspend fun deleteCampaignAndMessages(campaign: Campaign) {
        smsDao.deleteMessagesByBulkId(campaign.id)
        campaignDao.deleteCampaignById(campaign.id)
    }

    override suspend fun delete(campaign: Campaign) {
        campaignDao.deleteCampaignById(campaign.id)
    }

    override suspend fun insert(campaign: Campaign) {
        campaignDao.insert(campaign.toEntity())
    }
}