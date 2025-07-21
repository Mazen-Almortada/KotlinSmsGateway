package com.quansoft.smsgateway.ui.widgets

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


@Composable
fun ConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    text: String
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = title) },
        text = { Text(text = text) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

