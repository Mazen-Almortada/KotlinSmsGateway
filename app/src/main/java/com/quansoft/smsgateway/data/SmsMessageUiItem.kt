package com.quansoft.smsgateway.data

/**
 * A wrapper class for the UI layer that holds the original SmsMessage
 * and the resolved contact name.
 */
data class SmsMessageUiItem(
    val message: SmsMessage,
    val contactName: String?
)
