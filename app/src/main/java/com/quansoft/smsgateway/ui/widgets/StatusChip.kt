package com.quansoft.smsgateway.ui.widgets

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.quansoft.smsgateway.ui.theme.StatusDelivered
import com.quansoft.smsgateway.ui.theme.StatusFailed
import com.quansoft.smsgateway.ui.theme.StatusQueued
import com.quansoft.smsgateway.ui.theme.StatusSending
import com.quansoft.smsgateway.ui.theme.StatusSent


@Composable
fun StatusChip(status: String) {
    val (color, icon) = when (status) {
        "sent" -> StatusSent to Icons.AutoMirrored.Filled.Send
        "delivered" -> StatusDelivered to Icons.Default.CheckCircle
        "failed" -> StatusFailed to Icons.Default.Warning
        "sending" -> StatusSending to Icons.AutoMirrored.Default.Send
        else -> StatusQueued to Icons.Default.DateRange
    }

    AssistChip(
        onClick = { /* Do nothing */ },
        label = { Text(status.replaceFirstChar { it.uppercase() }) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = status,
                modifier = Modifier.size(AssistChipDefaults.IconSize)
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color,
            labelColor = Color.Black,
            leadingIconContentColor = Color.Black
        ),
        border = null
    )
}
