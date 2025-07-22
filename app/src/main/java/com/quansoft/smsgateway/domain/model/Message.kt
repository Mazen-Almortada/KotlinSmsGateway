package com.quansoft.smsgateway.domain.model

data class Message(
    val id: String,
    val recipient: String,
    val content: String,
    val status: String,
    val timestamp: Long,
    val bulkId: String?
)