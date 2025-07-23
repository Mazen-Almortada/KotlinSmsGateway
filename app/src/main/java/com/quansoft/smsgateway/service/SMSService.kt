package com.quansoft.smsgateway.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.quansoft.smsgateway.R
import com.quansoft.smsgateway.data.local.AppDatabase
import com.quansoft.smsgateway.data.repository.SettingsRepositoryImpl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The main Android Service.
 * Its responsibility is now to coordinate other components:
 * - Manage its own lifecycle as a Foreground Service.
 * - Delegate server management to SmsGatewayServer.
 * - Delegate queue processing to SmsQueueProcessor.
 * - Register and unregister the SmsStatusReceiver.
 */
@AndroidEntryPoint // 🎯 1. Make the Service an entry point for Hilt
class SMSService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val settingsManager by lazy { SettingsRepositoryImpl(this) }
    private val smsStatusReceiver = SmsStatusReceiver()

    @Inject
    lateinit var server: SmsGatewayServer

    @Inject
    lateinit var queueProcessor: SmsQueueProcessor // We would do the same for the processor

    override fun onCreate() {
        super.onCreate()
        startForegroundService()

        scope.launch {
            server.start()
        }
        queueProcessor.start()


        val intentFilter = IntentFilter().apply {
            addAction("SMS_SENT")
            addAction("SMS_DELIVERED")
        }
        ContextCompat.registerReceiver(
            this,
            smsStatusReceiver,
            intentFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        server.stop()
        job.cancel()
        unregisterReceiver(smsStatusReceiver)
    }
    private fun startForegroundService() {
        val channelId = "sms_gateway_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "SMS Gateway Service", NotificationManager.IMPORTANCE_HIGH)
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("SMS Gateway Running")
            .setContentText("The server is active and listening for requests.")
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}