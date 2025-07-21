package com.quansoft.smsgateway.ui.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.quansoft.smsgateway.R
import com.quansoft.smsgateway.util.AppUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(navController: NavController) {
    val context = LocalContext.current
    val appVersion = AppUtils.getAppVersion(context)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About This App") },
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
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AppHeader(version = appVersion ?: "0")
            }
            item {
                InfoCard(
                    icon = Icons.Default.Info,
                    title = "What is this app?",
                    content = "This application transforms your Android device into a powerful, simple-to-use SMS Gateway. It runs a local web server, allowing you to send SMS messages programmatically by making HTTP requests from any other system or application."
                )
            }
            item {
                InfoCard(
                    icon = Icons.Default.Person,
                    title = "Developed By",
                    content = "This application was collaboratively developed by Salman Aboholiqah and Mazen Almortada, showcasing a modern approach to software creation."
                )
            }
            item {
                InfoCard(
                    icon = Icons.Default.Info,
                    title = "Open Source",
                    content = "The source code for this project is available on GitHub. Feel free to explore, contribute, or fork the project."
                )
            }
        }
    }
}

@Composable
fun AppHeader(version: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "App Logo",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "SMS Gateway",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Version $version",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun InfoCard(icon: ImageVector, title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
