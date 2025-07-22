package com.quansoft.smsgateway.ui.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp


@Composable
fun InfoCard(
    title: String,
    content: String,
    icon: ImageVector,
    isMonospace: Boolean = false,
    onCopy: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp).padding(end = 16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    content,
                    style = if (isMonospace) MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                    else MaterialTheme.typography.bodyMedium,
                    softWrap = true,
                    maxLines = 2
                )
            }
            if (onCopy != null) {
                IconButton(onClick = onCopy) {
                    Icon(Icons.Default.Done, contentDescription = "Copy")
                }
            }
            actions()
        }
    }
}
