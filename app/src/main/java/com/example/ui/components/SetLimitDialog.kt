package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IndigoPrimary

@Composable
fun SetLimitDialog(
    appName: String,
    currentLimitMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedMinutes by remember {
        mutableFloatStateOf(if (currentLimitMinutes > 0) currentLimitMinutes.toFloat() else 30f)
    }

    val presets = listOf(15, 20, 30, 45, 60, 90, 120)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Daily Limit: $appName",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Choose the maximum time allowed per day. Once reached, Force Stop will restrict access.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                val hours = selectedMinutes.toInt() / 60
                val mins = selectedMinutes.toInt() % 60
                val displayStr = when {
                    selectedMinutes.toInt() == 0 -> "No limit (unrestricted)"
                    hours > 0 && mins > 0 -> "${hours}h ${mins}m per day"
                    hours > 0 -> "${hours}h per day"
                    else -> "${mins}m per day"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Configured Limit:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = displayStr,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = IndigoPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = selectedMinutes,
                    onValueChange = { selectedMinutes = it },
                    valueRange = 0f..180f,
                    steps = 35, // 5 min increments
                    colors = SliderDefaults.colors(
                        thumbColor = IndigoPrimary,
                        activeTrackColor = IndigoPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("limit_slider")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Quick Presets",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.take(4).forEach { p ->
                        FilterChip(
                            selected = selectedMinutes.toInt() == p,
                            onClick = { selectedMinutes = p.toFloat() },
                            label = { Text("${p}m", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.drop(4).forEach { p ->
                        FilterChip(
                            selected = selectedMinutes.toInt() == p,
                            onClick = { selectedMinutes = p.toFloat() },
                            label = {
                                val str = if (p >= 60) "${p / 60}h" else "${p}m"
                                Text(str, fontSize = 12.sp)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    FilterChip(
                        selected = selectedMinutes.toInt() == 0,
                        onClick = { selectedMinutes = 0f },
                        label = { Text("Off", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedMinutes.toInt()) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_limit_button")
            ) {
                Text("Set Daily Limit")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_limit_button")
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
