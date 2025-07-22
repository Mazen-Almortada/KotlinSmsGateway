package com.quansoft.smsgateway.domain.usecase

import android.content.Context
import com.quansoft.smsgateway.domain.model.MessageWithDetails
import com.quansoft.smsgateway.domain.repository.CampaignRepository
import com.quansoft.smsgateway.domain.repository.MessageRepository
import com.quansoft.smsgateway.util.ContactsUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * A complex use case responsible for fetching, combining, and filtering all message data.
 * It combines data from messages, campaigns, and contacts, then applies filters
 * for status, campaign, and a search query.
 */
class GetFilteredMessagesUseCase(
    private val messageRepository: MessageRepository,
    private val campaignRepository: CampaignRepository,
    private val context: Context // Context is needed for resolving contact names
) {
    operator fun invoke(
        statusFlow: Flow<String?>,
        campaignIdFlow: Flow<String?>,
        searchQueryFlow: Flow<String>
    ): Flow<List<MessageWithDetails>> {

        // 1. Combine messages and campaigns to create a detailed list
        val allMessagesWithDetails = combine(
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

        // 2. Combine the detailed list with all filter flows
        return combine(
            allMessagesWithDetails,
            statusFlow,
            campaignIdFlow,
            searchQueryFlow
        ) { messages, status, campaignId, query ->
            messages.filter { item ->
                val statusMatch = status == null || item.message.status == status
                val campaignMatch = campaignId == null || item.message.bulkId == campaignId
                val searchMatch = query.isBlank() ||
                        item.message.recipient.contains(query, ignoreCase = true) ||
                        (item.contactName?.contains(query, ignoreCase = true) == true)
                statusMatch && campaignMatch && searchMatch
            }
        }
    }
}