package com.quansoft.smsgateway.service

import android.app.Activity
import android.content.Intent

class SentStatusStrategy : SmsStatusStrategy {
    override fun handle(intent: Intent, resultCode: Int): String {
        return if (resultCode == Activity.RESULT_OK) "sent" else "failed"
    }
}