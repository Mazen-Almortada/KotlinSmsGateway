package com.quansoft.smsgateway.domain.usecase

import com.quansoft.smsgateway.domain.model.Campaign
import com.quansoft.smsgateway.domain.repository.CampaignRepository
import javax.inject.Inject

class InsertCampaignUseCase@Inject constructor(
    private val campaignRepository: CampaignRepository
) {
    /**
     * Inserts a new campaign into the repository.
     * This use case is designed to handle the insertion of a single campaign.
     *
     * @param campaign The campaign to be inserted.
     */
    suspend operator fun invoke(campaign: Campaign) {
        campaignRepository.insert(campaign)
    }
}