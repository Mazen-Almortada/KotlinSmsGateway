package com.quansoft.smsgateway.ui.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.quansoft.smsgateway.domain.model.Campaign
import com.quansoft.smsgateway.domain.model.MessageWithDetails
import com.quansoft.smsgateway.ui.MainViewModel
import com.quansoft.smsgateway.ui.theme.StatusDelivered
import com.quansoft.smsgateway.ui.theme.StatusFailed
import com.quansoft.smsgateway.ui.theme.StatusQueued
import com.quansoft.smsgateway.ui.theme.StatusSending
import com.quansoft.smsgateway.ui.theme.StatusSent
import com.quansoft.smsgateway.ui.widgets.ConfirmationDialog
import com.quansoft.smsgateway.ui.widgets.MessageList
import com.quansoft.smsgateway.util.AppUtils
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GatewayScreen(viewModel: MainViewModel, navController: NavController) {

//    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SMS Gateway") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    // Add button to navigate to the info screen
                    IconButton(onClick = { navController.navigate("info") }) {
                        Icon(Icons.Default.Info, contentDescription = "About")
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {

            MessageLogSection(viewModel = viewModel)
        }
    }
}


@Composable
fun MessageLogSection(viewModel: MainViewModel) {
    val tabs = listOf("All", "Queued", "Sending", "Sent", "Delivered", "Failed")
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val messages by viewModel.filteredMessages.collectAsState()
    val campaigns by viewModel.campaigns.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var selectedCampaignId by remember { mutableStateOf<String?>(null) }

    var showDeleteDialog by remember { mutableStateOf<Any?>(null) }

    if (showDeleteDialog != null) {
        ConfirmationDialog(
            onDismissRequest = { showDeleteDialog = null },
            onConfirm = {
                when (val item = showDeleteDialog) {
                    is MessageWithDetails -> viewModel.deleteMessage(item)
                    is Campaign -> viewModel.deleteCampaign(item)
                }
                showDeleteDialog = null
            },
            title = if (showDeleteDialog is Campaign) "Delete Campaign?" else "Delete Message?",
            text = if (showDeleteDialog is Campaign) "Are you sure you want to delete this campaign and all of its associated messages? This action cannot be undone." else "Are you sure you want to delete this message?"
        )
    }

    LaunchedEffect(selectedTabIndex) {
        val status = if (tabs[selectedTabIndex] == "All") null else tabs[selectedTabIndex].lowercase()
        viewModel.selectStatus(status)
    }

    LaunchedEffect(selectedCampaignId) {
        viewModel.selectCampaign(selectedCampaignId)
    }

    Column {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true
            )
        }

        // Campaign Filter Tags
        if (campaigns.isNotEmpty()) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text("Campaigns", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(horizontal = 16.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCampaignId == null,
                            onClick = { selectedCampaignId = null },
                            label = { Text("All Campaigns") }
                        )
                    }
                    items(campaigns) { campaign ->
                        FilterChip(
                            selected = selectedCampaignId == campaign.id,
                            onClick = { selectedCampaignId = campaign.id },
                            label = { Text(campaign.name) },
                            trailingIcon = {
                                IconButton(onClick = { showDeleteDialog = campaign }, modifier = Modifier.size(18.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Delete Campaign")
                                }
                            }
                        )
                    }
                }
            }
        }


        ScrollableTabRow(selectedTabIndex = selectedTabIndex, edgePadding = 16.dp) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }
        MessageList(
            messages = messages,
            onDelete = { messageItem ->
                showDeleteDialog = messageItem
            }
        )
    }
}
