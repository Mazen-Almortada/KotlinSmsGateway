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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.quansoft.smsgateway.data.SmsMessage
import com.quansoft.smsgateway.data.SmsMessageUiItem
import com.quansoft.smsgateway.service.GatewayService
import com.quansoft.smsgateway.ui.MainViewModel
import com.quansoft.smsgateway.ui.settings.SettingsScreen
import com.quansoft.smsgateway.ui.theme.Theme
import com.quansoft.smsgateway.ui.theme.StatusDelivered
import com.quansoft.smsgateway.ui.theme.StatusFailed
import com.quansoft.smsgateway.ui.theme.StatusQueued
import com.quansoft.smsgateway.ui.theme.StatusSending
import com.quansoft.smsgateway.ui.theme.StatusSent

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
    val ipAddress by viewModel.ipAddress.collectAsState()
    val deviceToken by viewModel.deviceToken.collectAsState()
    val serverPort by viewModel.serverPort.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SMS Gateway") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
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
            Spacer(Modifier.height(16.dp))
            val serverUrl = "http://$ipAddress:$serverPort"
            InfoCard(
                title = "Server URL",
                content = serverUrl,
                icon = Icons.Default.Home,
                onCopy = { clipboardManager.setText(AnnotatedString(serverUrl)) }
            )
            Spacer(Modifier.height(8.dp))
            InfoCard(
                title = "Authorization Token",
                content = deviceToken,
                icon = Icons.Default.Lock,
                isMonospace = true,
                onCopy = { clipboardManager.setText(AnnotatedString(deviceToken)) }
            )
            Spacer(Modifier.height(16.dp))
            MessageLogSection(viewModel = viewModel)
        }
    }
}

@Composable
fun MessageLogSection(viewModel: MainViewModel) {
    val tabs = listOf("All", "Queued", "Sending", "Sent", "Delivered", "Failed")
    var selectedTabIndex by remember { mutableStateOf(0) }
    val messages by viewModel.filteredMessages.collectAsState()

    LaunchedEffect(selectedTabIndex) {
        val status = if (tabs[selectedTabIndex] == "All") {
            null
        } else {
            tabs[selectedTabIndex].lowercase()
        }
        viewModel.selectStatus(status)
    }

    Column {
        Text("Messages Log", style = MaterialTheme.typography.titleLarge)
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        MessageList(messages = messages)
    }
}

@Composable
fun InfoCard(
    title: String,
    content: String,
    icon: ImageVector,
    isMonospace: Boolean = false,
    onCopy: (() -> Unit)? = null
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp).padding(end = 16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    content,
                    style = if (isMonospace) MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                    else MaterialTheme.typography.bodyMedium,
                    softWrap = true
                )
            }
            if (onCopy != null) {
                IconButton(onClick = onCopy) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Copy"
                    )
                }
            }
        }
    }
}

@Composable
fun MessageList(messages: List<SmsMessageUiItem>) {
    if (messages.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "No Messages",
                    modifier = Modifier.size(64.dp),
                    tint = Color.Gray
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "No messages in this category",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Messages matching this status will appear here.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(messages, key = { it.message.id }) { message ->
                MessageItem(message)
            }
        }
    }
}

@Composable
fun MessageItem(item: SmsMessageUiItem) {
    val message = item.message
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "To: ${message.recipient}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace)
                )
                Text(
                    "To: ${item.contactName ?: "لايوجد اسم"}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace)
                )
                Text(
                    formatTimestamp(message.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(message.content, style = MaterialTheme.typography.bodyMedium, maxLines = 3)
            Spacer(Modifier.height(8.dp))
            StatusChip(status = message.status)
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
