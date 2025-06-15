package com.example.kotlinsmsgateway

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.kotlinsmsgateway.data.SmsMessage
import com.example.kotlinsmsgateway.service.GatewayService
import com.example.kotlinsmsgateway.ui.MainViewModel
import com.example.kotlinsmsgateway.ui.theme.KotlinSmsGatewayTheme

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
                GatewayScreen(viewModel = viewModel)
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
fun GatewayScreen(viewModel: MainViewModel) {
    val messages by viewModel.messages.collectAsState()
    val ipAddress by viewModel.ipAddress.collectAsState()
    val deviceToken by viewModel.deviceToken.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SMS Gateway") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            InfoCard(title = "Server IP Address", content = "http://$ipAddress:8080")
            Spacer(Modifier.height(8.dp))
            InfoCard(title = "Authorization Token", content = deviceToken, isMonospace = true)
            Spacer(Modifier.height(16.dp))
            Text("Messages Log", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            MessageList(messages = messages)
        }
    }
}

@Composable
fun InfoCard(title: String, content: String, isMonospace: Boolean = false) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                content,
                style = if (isMonospace) MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                else MaterialTheme.typography.bodyMedium,
                softWrap = true
            )
        }
    }
}

@Composable
fun MessageList(messages: List<SmsMessage>) {
    if (messages.isEmpty()){
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
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
