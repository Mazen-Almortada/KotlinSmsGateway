package com.example.kotlinsmsgateway.service

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.kotlinsmsgateway.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsStatusReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val smsDao = AppDatabase.getDatabase(context).smsDao()
        val messageId = intent.getStringExtra("id") ?: return

        val newStatus = when (intent.action) {
            "SMS_SENT" -> {
                if (resultCode == Activity.RESULT_OK) "sent" else "failed"
            }
            "SMS_DELIVERED" -> {
                if (resultCode == Activity.RESULT_OK) "delivered" else "failed"
            }
            else -> ""
        }

        if (newStatus.isNotEmpty()) {
            scope.launch {
                smsDao.updateStatus(messageId, newStatus)
            }
        }
    }
}