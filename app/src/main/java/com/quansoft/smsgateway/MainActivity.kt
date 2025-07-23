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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.quansoft.smsgateway.service.SMSService
import com.quansoft.smsgateway.ui.MainViewModel
import com.quansoft.smsgateway.ui.info.InfoScreen
import com.quansoft.smsgateway.ui.messages.GatewayScreen
import com.quansoft.smsgateway.ui.settings.SettingsScreen
import com.quansoft.smsgateway.ui.theme.Theme

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
        val serviceIntent = Intent(this, SMSService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
}


