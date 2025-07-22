package com.quansoft.smsgateway.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quansoft.smsgateway.data.AppDatabase
import com.quansoft.smsgateway.data.BulkCampaign
import com.quansoft.smsgateway.data.SmsMessageUiItem
import com.quansoft.smsgateway.util.ContactsUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val smsDao = AppDatabase.getDatabase(application).smsDao()
    private val bulkCampaignDao = AppDatabase.getDatabase(application).bulkCampaignDao()

    // This is the correct way: Combine the two flows.
    private val allMessagesWithDetails: Flow<List<SmsMessageUiItem>> =
        combine(smsDao.getAllMessages(), smsDao.getAllCampaigns()) { messages, campaigns ->
            // 1. Create a fast lookup map from campaign ID to campaign name.
            val campaignMap = campaigns.associateBy { it.id }

            // 2. Map each message to its UI item.
            messages.map { message ->
                val contactName = ContactsUtil.findContactName(application, message.recipient)
                // 3. Use the map to find the campaign name instantly (not a DB call).
                val campaignName = message.bulkId?.let { campaignMap[it]?.name }
                SmsMessageUiItem(
                    message = message,
                    contactName = contactName,
                    bulkName = campaignName
                )
            }
        }

    // Flow for the list of campaigns to show in the dropdown
    val campaigns: StateFlow<List<BulkCampaign>> = bulkCampaignDao.getAllCampaigns()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // State for the status filter (e.g., "sent", "failed")
    private val _selectedStatus = MutableStateFlow<String?>(null)

    // State for the campaign filter
    private val _selectedCampaignId = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // The final list of messages, filtered by both status and campaign
    val filteredMessages: StateFlow<List<SmsMessageUiItem>> =
        combine(
            allMessagesWithDetails,
            _selectedStatus,
            _selectedCampaignId,
            _searchQuery
        ) { messages, status, campaignId, query ->
            messages.filter { item ->
                val statusMatch = status == null || item.message.status == status
                val campaignMatch = campaignId == null || item.message.bulkId == campaignId
                val searchMatch = query.isBlank() ||
                        item.message.recipient.contains(query, ignoreCase = true) ||
                        (item.contactName?.contains(query, ignoreCase = true) == true)
                statusMatch && campaignMatch && searchMatch
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun deleteMessage(item: SmsMessageUiItem) {
        viewModelScope.launch {
            smsDao.delete(item.message)
        }
    }

    fun deleteCampaign(campaign: BulkCampaign) {
        viewModelScope.launch {
            // First, delete all messages associated with the campaign
            smsDao.deleteMessagesByBulkId(campaign.id)
            // Then, delete the campaign itself
            bulkCampaignDao.delete(campaign)
        }
    }

    fun selectStatus(status: String?) {
        _selectedStatus.value = status
    }

    fun selectCampaign(campaignId: String?) {
        _selectedCampaignId.value = campaignId
    }
}
