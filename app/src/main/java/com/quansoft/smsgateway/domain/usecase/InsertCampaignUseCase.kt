package com.quansoft.smsgateway.domain.usecase

import com.quansoft.smsgateway.domain.model.Campaign
import com.quansoft.smsgateway.domain.model.Message
import com.quansoft.smsgateway.domain.repository.CampaignRepository
import com.quansoft.smsgateway.domain.repository.MessageRepository
import com.quansoft.smsgateway.service.BulkRequest
import java.util.UUID
import javax.inject.Inject

class InsertCampaignUseCase @Inject constructor(
    private val campaignRepository: CampaignRepository,
    private val messageRepository: MessageRepository
) {
    suspend operator fun invoke(request: BulkRequest) {
        val newCampaign = Campaign(
            id = request.bulkId,
            name = request.bulkName,
            timestamp = System.currentTimeMillis()
        )
        campaignRepository.insert(newCampaign)

        val newMessages = request.messages.map { bulkMsg ->
            Message(
                id = UUID.randomUUID().toString(),
                recipient = bulkMsg.to,
                content = bulkMsg.message,
                status = "queued",
                timestamp = System.currentTimeMillis(),
                bulkId = request.bulkId
            )
        }
        messageRepository.insertAll(newMessages)
    }
}