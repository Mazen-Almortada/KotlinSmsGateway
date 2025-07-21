package com.example.kotlinsmsgateway.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.telephony.SmsManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
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

    private val smsStatusReceiver = SmsStatusReceiver()

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

        val intentFilter = IntentFilter().apply {
            addAction("SMS_SENT")
            addAction("SMS_DELIVERED")
        }
        ContextCompat.registerReceiver(this, smsStatusReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED)

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


        // إنشاء Intent للإرسال يستهدف الـ BroadcastReceiver المركزي
        val sentIntent = Intent(this, SmsStatusReceiver::class.java).apply {
            action = "SMS_SENT"
            putExtra("id", id)
        }
        val sentPendingIntent = PendingIntent.getBroadcast(
            this, id.hashCode(), sentIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // إنشاء Intent للتسليم يستهدف الـ BroadcastReceiver المركزي
        val deliveredIntent = Intent(this, SmsStatusReceiver::class.java).apply {
            action = "SMS_DELIVERED"
            putExtra("id", id)
        }
        val deliveredPendingIntent = PendingIntent.getBroadcast(
            this, (id.hashCode() + 1), deliveredIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // إرسال الرسالة
        smsManager.sendTextMessage(phone, null, message, sentPendingIntent, deliveredPendingIntent)
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
        unregisterReceiver(smsStatusReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
