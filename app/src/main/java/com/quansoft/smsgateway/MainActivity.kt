package com.quansoft.smsgateway

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.format.DateUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.quansoft.smsgateway.data.BulkCampaign
import com.quansoft.smsgateway.data.SmsMessageUiItem
import com.quansoft.smsgateway.service.GatewayService
import com.quansoft.smsgateway.ui.MainViewModel
import com.quansoft.smsgateway.ui.info.InfoScreen
import com.quansoft.smsgateway.ui.settings.SettingsScreen
import com.quansoft.smsgateway.ui.theme.StatusDelivered
import com.quansoft.smsgateway.ui.theme.StatusFailed
import com.quansoft.smsgateway.ui.theme.StatusQueued
import com.quansoft.smsgateway.ui.theme.StatusSending
import com.quansoft.smsgateway.ui.theme.StatusSent
import com.quansoft.smsgateway.ui.theme.Theme
import com.quansoft.smsgateway.ui.widgets.ConfirmationDialog
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
            if (perms.getOrDefault(Manifest.permission.SEND_SMS, false)) {
                startGatewayService()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions.launch(
                arrayOf(
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            )
        } else {
            requestPermissions.launch( arrayOf(
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_CONTACTS,
            ))
        }

        setContent {
            Theme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "gateway") {
                    composable("gateway") {
                        GatewayScreen(viewModel = viewModel, navController = navController)
                    }
                    composable("settings") {
                        SettingsScreen(navController = navController)
                    }
                    composable("info") {
                        InfoScreen(navController = navController)
                    }
                }
            }
        }
    }

    private fun startGatewayService() {
        val serviceIntent = Intent(this, GatewayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
}

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
                    is SmsMessageUiItem -> viewModel.deleteMessage(item)
                    is BulkCampaign -> viewModel.deleteCampaign(item)
                }
                showDeleteDialog = null
            },
            title = if (showDeleteDialog is BulkCampaign) "Delete Campaign?" else "Delete Message?",
            text = if (showDeleteDialog is BulkCampaign) "Are you sure you want to delete this campaign and all of its associated messages? This action cannot be undone." else "Are you sure you want to delete this message?"
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
                label = { Text("Search by name or number") },
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
@Composable
fun MessageList(messages: List<SmsMessageUiItem>, onDelete: (SmsMessageUiItem) -> Unit) {
    if (messages.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Search, contentDescription = "No Messages", modifier = Modifier.size(64.dp), tint = Color.Gray)
                Spacer(Modifier.height(16.dp))
                Text("No messages found", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                Text("Try adjusting your search or filter criteria.", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = Color.Gray)
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp)
        ) {
            items(messages, key = { it.message.id }) { item ->
                var isDismissed by remember { mutableStateOf(false) }
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.EndToStart) {
                            isDismissed = true
                            true
                        } else false
                    }
                )

                LaunchedEffect(isDismissed) {
                    if (isDismissed) {
                        delay(300) // Wait for animation
                        onDelete(item)
                    }
                }

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent  = {
                        val color = when (dismissState.targetValue) {
                            SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                            else -> Color.Transparent
                        }
                        val icon = Icons.Default.Delete
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CardDefaults.shape)
                                .background(color)
                                .padding(16.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = "Delete Icon",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    },
                    content = { MessageItem(item,onDelete = { onDelete(item) }) }
                )
            }
        }
    }
}

@Composable
fun MessageItem(item: SmsMessageUiItem, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.contactName ?: item.message.recipient,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )
                    if (item.contactName != null) {
                        Text(
                            text = item.message.recipient,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    formatTimestamp(item.message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item.bulkName?.let { bulkName ->
                Text(
                    text = "Campaign: $bulkName",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(item.message.content, style = MaterialTheme.typography.bodyMedium, maxLines = 3)
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusChip(status = item.message.status)
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Message",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
@Composable
fun StatusChip(status: String) {
    val (color, icon) = when (status) {
        "sent" -> StatusSent to Icons.AutoMirrored.Filled.Send
        "delivered" -> StatusDelivered to Icons.Default.CheckCircle
        "failed" -> StatusFailed to Icons.Default.Warning
        "sending" -> StatusSending to Icons.AutoMirrored.Default.Send
        else -> StatusQueued to Icons.Default.DateRange
    }

    AssistChip(
        onClick = { /* Do nothing */ },
        label = { Text(status.replaceFirstChar { it.uppercase() }) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = status,
                modifier = Modifier.size(AssistChipDefaults.IconSize)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color,
            labelColor = Color.Black,
            leadingIconContentColor = Color.Black
        ),
        border = null
    )
}

@Composable
private fun formatTimestamp(timestamp: Long): String {
    return DateUtils.getRelativeTimeSpanString(
        timestamp,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()
}
