package com.quansoft.smsgateway.service

import android.content.Intent

/**
 * Interface for the Strategy Pattern. Each implementation will handle a specific
 * SMS status update logic based on the intent action.
 */
interface SmsStatusStrategy {
    fun handle(intent: Intent, resultCode: Int): String?
}