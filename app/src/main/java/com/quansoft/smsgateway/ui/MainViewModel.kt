package com.quansoft.smsgateway.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quansoft.smsgateway.data.local.AppDatabase
import com.quansoft.smsgateway.data.repository.CampaignRepositoryImpl
import com.quansoft.smsgateway.data.repository.MessageRepositoryImpl
import com.quansoft.smsgateway.domain.model.Campaign
import com.quansoft.smsgateway.domain.model.MessageWithDetails
import com.quansoft.smsgateway.domain.usecase.DeleteCampaignUseCase
import com.quansoft.smsgateway.domain.usecase.DeleteMessageUseCase
import com.quansoft.smsgateway.domain.usecase.GetCampaignsUseCase
import com.quansoft.smsgateway.domain.usecase.GetFilteredMessagesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    // Manual instantiation of dependencies (to be replaced by Hilt)
    private val db = AppDatabase.getDatabase(application)
    private val messageRepository = MessageRepositoryImpl(db.smsDao())
    private val campaignRepository = CampaignRepositoryImpl(db.bulkCampaignDao(),db.smsDao())

    private val getCampaignsUseCase = GetCampaignsUseCase(campaignRepository)
    private val deleteCampaignUseCase = DeleteCampaignUseCase(campaignRepository, messageRepository)
    private val deleteMessageUseCase = DeleteMessageUseCase(messageRepository)
    private val getFilteredMessagesUseCase = GetFilteredMessagesUseCase(messageRepository, campaignRepository, application)
    // --- End of Dependencies ---


    // --- UI State Holders ---
    private val _selectedStatus = MutableStateFlow<String?>(null)
    private val _selectedCampaignId = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()


    // --- Data Exposed to the UI ---
    // The ViewModel gets data by calling the relevant use cases.
    val campaigns: StateFlow<List<Campaign>> = getCampaignsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredMessages: StateFlow<List<MessageWithDetails>> = getFilteredMessagesUseCase(
        statusFlow = _selectedStatus,
        campaignIdFlow = _selectedCampaignId,
        searchQueryFlow = _searchQuery
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // --- Public Functions (Events from the UI) ---
    // The UI calls these functions, which then delegate the work to the use cases.
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun deleteMessage(item: MessageWithDetails) {
        viewModelScope.launch {
            deleteMessageUseCase(item.message)
        }
    }

    fun deleteCampaign(campaign: Campaign) {
        viewModelScope.launch {
            deleteCampaignUseCase(campaign)
        }
    }

    fun selectStatus(status: String?) {
        _selectedStatus.value = status
    }

    fun selectCampaign(campaignId: String?) {
        _selectedCampaignId.value = campaignId
    }
}
