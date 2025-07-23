package com.quansoft.smsgateway.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.quansoft.smsgateway.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsStatusReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(Dispatchers.IO)

    // A map that holds the strategies. This is the core of the pattern.
    private val strategies: Map<String, SmsStatusStrategy> = mapOf(
        "SMS_SENT" to SentStatusStrategy(),
        "SMS_DELIVERED" to DeliveredStatusStrategy()
    )

    override fun onReceive(context: Context, intent: Intent) {
        val smsDao = AppDatabase.getDatabase(context).smsDao()
        val messageId = intent.getStringExtra("id") ?: return

        // Find the correct strategy for the given action
        val strategy = strategies[intent.action]

        // Use the strategy to get the new status
        val newStatus = strategy?.handle(intent, resultCode)

        if (newStatus != null) {
            scope.launch {
                smsDao.updateStatus(messageId, newStatus)
            }
        }
    }
}