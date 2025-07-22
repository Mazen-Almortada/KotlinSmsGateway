package com.quansoft.smsgateway.domain.model

/**
 * A domain model that combines a Message with its related details
 * like contact and campaign names for UI presentation.
 */
data class MessageWithDetails(
    val message: Message,
    val contactName: String?,
    val campaignName: String?
)