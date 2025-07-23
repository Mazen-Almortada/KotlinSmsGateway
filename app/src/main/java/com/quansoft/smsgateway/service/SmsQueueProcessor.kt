package com.quansoft.smsgateway.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import com.quansoft.smsgateway.data.local.dao.SmsDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * A class with the single responsibility of processing the SMS message queue.
 * It periodically checks the database for queued messages and sends them.
 */
class SmsQueueProcessor(
    private val context: Context,
    private val smsDao: SmsDao,
    private val coroutineScope: CoroutineScope
) {
    private val smsManager: SmsManager by lazy {
        context.getSystemService(SmsManager::class.java)
    }

    fun start() {
        coroutineScope.launch(Dispatchers.IO) {
            while (isActive) {
                processQueue()
                delay(5000) // The 5-second delay remains for periodic checks
            }
        }
    }

    private suspend fun processQueue() {
        val queuedMessages = smsDao.getQueuedMessages()
        if (queuedMessages.isNotEmpty()) {
            for (message in queuedMessages) {
                sendSms(message.id, message.recipient, message.content)
                smsDao.updateStatus(message.id, "sending")
            }
        }
    }

    private fun sendSms(id: String, phone: String, message: String) {
        val sentIntent = Intent("SMS_SENT").apply {
            `package` = context.packageName
            putExtra("id", id)
        }
        val sentPendingIntent = PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            sentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val deliveredIntent = Intent("SMS_DELIVERED").apply {
            `package` = context.packageName
            putExtra("id", id)
        }
        val deliveredPendingIntent = PendingIntent.getBroadcast(
            context,
            (id.hashCode() + 1),
            deliveredIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        smsManager.sendTextMessage(phone, null, message, sentPendingIntent, deliveredPendingIntent)
    }
}