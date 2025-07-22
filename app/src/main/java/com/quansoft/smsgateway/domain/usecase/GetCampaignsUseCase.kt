package com.quansoft.smsgateway.domain.usecase

import com.quansoft.smsgateway.domain.model.Campaign
import com.quansoft.smsgateway.domain.repository.CampaignRepository
import kotlinx.coroutines.flow.Flow

/**
 * A use case dedicated to retrieving a list of all campaigns.
 * It abstracts the data source logic from the ViewModel.
 */
class GetCampaignsUseCase(
    private val campaignRepository: CampaignRepository
) {
    operator fun invoke(): Flow<List<Campaign>> {
        return campaignRepository.getAllCampaigns()
    }
}
