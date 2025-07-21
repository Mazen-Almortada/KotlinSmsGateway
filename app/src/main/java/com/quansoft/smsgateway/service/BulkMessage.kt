package com.quansoft.smsgateway.service

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class BulkMessage(
    val to: String,
    val message: String
)

@Serializable
data class BulkRequest(
    @SerialName("Bulk Name") val bulkName: String,
    @SerialName("Bulk ID") val bulkId: String,
    @SerialName("Bulk Messages") val messages: List<BulkMessage>
)
