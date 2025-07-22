package com.quansoft.smsgateway.domain.usecase

import com.quansoft.smsgateway.domain.model.Campaign
import com.quansoft.smsgateway.domain.repository.CampaignRepository
import com.quansoft.smsgateway.domain.repository.MessageRepository

/**
 * A use case for deleting a campaign and all of its associated messages.
 * This encapsulates the business rule that deleting a campaign must also cascade
 * and delete its child messages.
 */
class DeleteCampaignUseCase(
    private val campaignRepository: CampaignRepository,
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(campaign: Campaign) {
        // The order is important: delete messages first, then the campaign.
        messageRepository.deleteMessagesByCampaignId(campaign.id)
        campaignRepository.delete(campaign)
    }
}