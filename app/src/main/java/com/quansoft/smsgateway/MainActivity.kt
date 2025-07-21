package com.quansoft.smsgateway

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.quansoft.smsgateway.data.SmsMessage
import com.quansoft.smsgateway.service.GatewayService
import com.quansoft.smsgateway.ui.MainViewModel
import com.quansoft.smsgateway.ui.settings.SettingsScreen
import com.quansoft.smsgateway.ui.theme.KotlinSmsGatewayTheme

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
                    Manifest.permission.POST_NOTIFICATIONS
                )
            )
        } else {
            requestPermissions.launch(arrayOf(Manifest.permission.SEND_SMS))
        }

        setContent {
            KotlinSmsGatewayTheme {
                // إعداد نظام التنقل
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
fun GatewayScreen(
    viewModel: MainViewModel,
    navController: NavController
) {
    val messages by viewModel.filteredMessages.collectAsState()
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
                actions = { // <-- إضافة أيقونة الإعدادات هنا
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
                .padding(16.dp)
        ) {
            val serverUrl = "http://$ipAddress:$serverPort"
            InfoCard(
                title = "Server IP Address",
                content = serverUrl,
                onCopy = { clipboardManager.setText(AnnotatedString(serverUrl)) }
            )
            Spacer(Modifier.height(8.dp))
            InfoCard(
                title = "Authorization Token",
                content = deviceToken,
                isMonospace = true,
                onCopy = { clipboardManager.setText(AnnotatedString(deviceToken)) }
            )
            Spacer(Modifier.height(16.dp))
            Text("Messages Log", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            MessageList(messages = messages)
        }
    }
}


@Composable
fun MessageList(messages: List<SmsMessage>) {
    if (messages.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No messages yet.")
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(messages, key = { it.id }) { message ->
                MessageItem(message)
            }
        }
    }
}

@Composable
fun MessageItem(message: SmsMessage) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("To: ${message.recipient}", style = MaterialTheme.typography.bodyLarge)
                Text(message.content, style = MaterialTheme.typography.bodySmall, maxLines = 2)
            }
            Spacer(Modifier.width(8.dp))
            StatusChip(status = message.status)
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    content: String,
    isMonospace: Boolean = false,
    onCopy: (() -> Unit)? = null
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onCopy) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "Copy"
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val color = when (status) {
        "sent" -> Color(0xFFB3E5FC)
        "delivered" -> Color(0xFFC8E6C9)
        "failed" -> Color(0xFFFFCDD2)
        "sending" -> Color(0xFFFFE0B2)
        else -> Color(0xFFF5F5F5)
    }
    Surface(shape = MaterialTheme.shapes.small, color = color) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
