package com.quansoft.smsgateway.util

object MessageStatuses {
    val ALL = null // Represents the "All" tab
    const val QUEUED = "queued"
    const val SENDING = "sending"
    const val SENT = "sent"
    const val DELIVERED = "delivered"
    const val FAILED = "failed"

    // This list will now be the single source of truth for UI tabs.
    val UI_TABS = listOf("All", QUEUED, SENDING, SENT, DELIVERED, FAILED)
}