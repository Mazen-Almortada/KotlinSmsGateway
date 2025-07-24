package com.quansoft.smsgateway.domain.usecase

import android.content.Context
import com.quansoft.smsgateway.domain.model.MessageWithDetails
import com.quansoft.smsgateway.domain.repository.CampaignRepository
import com.quansoft.smsgateway.domain.repository.MessageRepository
import com.quansoft.smsgateway.util.ContactsUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetMessagesWithDetailsUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
    private val campaignRepository: CampaignRepository,
    private val context: Context
) {
    operator fun invoke(): Flow<List<MessageWithDetails>> {
        return combine(
            messageRepository.getAllMessages(),
            campaignRepository.getAllCampaigns()
        ) { messages, campaigns ->
            val campaignMap = campaigns.associateBy { it.id }
            messages.map { message ->
                val contactName = ContactsUtil.findContactName(context, message.recipient)
                val campaignName = message.bulkId?.let { campaignMap[it]?.name }
                MessageWithDetails(
                    message = message,
                    contactName = contactName,
                    campaignName = campaignName
                )
            }
        }
    }
}