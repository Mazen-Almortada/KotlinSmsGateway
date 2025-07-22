package com.quansoft.smsgateway.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val currentPort by settingsViewModel.serverPort.collectAsState()
    val authToken by settingsViewModel.authToken.collectAsState()
    val ipAddress by settingsViewModel.ipAddress.collectAsState()
    var portText by remember(currentPort) { mutableStateOf(currentPort.toString()) }
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Info") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Server Info Section
            Text("Connection Info", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            val serverUrl = "http://$ipAddress:$currentPort"
            InfoCard(
                title = "Server URL",
                content = serverUrl,
                icon = Icons.Default.Home,
                onCopy = { clipboardManager.setText(AnnotatedString(serverUrl)) }
            )
            Spacer(Modifier.height(8.dp))
            InfoCard(
                title = "Authorization Token",
                content = authToken,
                icon = Icons.Default.Lock,
                isMonospace = true,
                onCopy = { clipboardManager.setText(AnnotatedString(authToken)) }
            ) {
                // Add a refresh button to the token card
                IconButton(onClick = { settingsViewModel.regenerateToken() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Regenerate Token")
                }
            }
            Spacer(Modifier.height(24.dp))

            // Server Configuration Section
            Text("Server Configuration", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = portText,
                onValueChange = { newText ->
                    if (newText.all { it.isDigit() } && newText.length <= 5) {
                        portText = newText
                        settingsViewModel.updateServerPort(newText)
                    }
                },
                label = { Text("Server Port") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Text(
                text = "The app must be restarted for port changes to take effect.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    content: String,
    icon: ImageVector,
    isMonospace: Boolean = false,
    onCopy: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
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
                    softWrap = true,
                    maxLines = 2
                )
            }
            if (onCopy != null) {
                IconButton(onClick = onCopy) {
                    Icon(Icons.Default.Done, contentDescription = "Copy")
                }
            }
            actions()
        }
    }
}
