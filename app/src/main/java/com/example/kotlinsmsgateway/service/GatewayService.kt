package com.example.kotlinsmsgateway.service

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.telephony.SmsManager
import androidx.core.app.NotificationCompat
import com.example.kotlinsmsgateway.R
import com.example.kotlinsmsgateway.data.AppDatabase
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GatewayService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val smsManager: SmsManager by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            applicationContext.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }
    }

    private val ktorServer by lazy {
        embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
            configureRouting(database.smsDao())
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        scope.launch {
            ktorServer.start(wait = false)
        }
        scope.launch {
            processSmsQueue()
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private suspend fun processSmsQueue() {
        while (true) {
            val queuedMessages = database.smsDao().getQueuedMessages()
            if (queuedMessages.isNotEmpty()) {
                for (message in queuedMessages) {
                    sendSms(message.id, message.recipient, message.content)
                    database.smsDao().updateStatus(message.id, "sending")
                }
            }
            delay(5000)
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun sendSms(id: String, phone: String, message: String) {
        val sentIntent = PendingIntent.getBroadcast(
            this, id.hashCode(), Intent("SMS_SENT").putExtra("id", id),
            PendingIntent.FLAG_IMMUTABLE
        )
        val deliveredIntent = PendingIntent.getBroadcast(
            this, (id.hashCode() + 1), Intent("SMS_DELIVERED").putExtra("id", id),
            PendingIntent.FLAG_IMMUTABLE
        )

        val sentReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                scope.launch {
                    val newStatus = if (resultCode == RESULT_OK) "sent" else "failed"
                    database.smsDao().updateStatus(id, newStatus)
                }
                try { unregisterReceiver(this) } catch (_: Exception) {}
            }
        }

        val deliveredReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                scope.launch {
                    val newStatus = if (resultCode == RESULT_OK) "delivered" else "failed"
                    database.smsDao().updateStatus(id, newStatus)
                }
                try { unregisterReceiver(this) } catch (_: Exception) {}
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(sentReceiver, IntentFilter("SMS_SENT"), RECEIVER_EXPORTED)
            registerReceiver(deliveredReceiver, IntentFilter("SMS_DELIVERED"), RECEIVER_EXPORTED)
        } else {
            registerReceiver(sentReceiver, IntentFilter("SMS_SENT"))
            registerReceiver(deliveredReceiver, IntentFilter("SMS_DELIVERED"))
        }


        smsManager.sendTextMessage(phone, null, message, sentIntent, deliveredIntent)
    }


    private fun startForegroundService() {
        val channelId = "sms_gateway_service_channel"
        val channelName = "SMS Gateway Service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(chan)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification: Notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("SMS Gateway Running")
            .setContentText("The server is active on port 8080")
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        ktorServer.stop(1000, 2000)
        job.cancel()
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
