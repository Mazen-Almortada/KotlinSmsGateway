package com.quansoft.smsgateway.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quansoft.smsgateway.domain.model.Campaign
import com.quansoft.smsgateway.domain.model.MessageWithDetails
import com.quansoft.smsgateway.domain.usecase.DeleteCampaignUseCase
import com.quansoft.smsgateway.domain.usecase.DeleteMessageUseCase
import com.quansoft.smsgateway.domain.usecase.GetCampaignsUseCase
import com.quansoft.smsgateway.domain.usecase.GetFilteredMessagesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getCampaignsUseCase: GetCampaignsUseCase,
    private val deleteCampaignUseCase: DeleteCampaignUseCase,
    private val deleteMessageUseCase: DeleteMessageUseCase,
    private val getFilteredMessagesUseCase: GetFilteredMessagesUseCase
) : ViewModel() {


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
