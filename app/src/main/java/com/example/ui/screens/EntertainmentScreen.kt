package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppUsageInfo
import com.example.ui.components.AppItemCard
import com.example.ui.components.PinEntryDialog
import com.example.ui.theme.CoralLimit
import com.example.ui.theme.EntertainmentPurple
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.MintAccent
import com.example.ui.viewmodel.MainViewModel

@Composable
fun EntertainmentScreen(
    viewModel: MainViewModel,
    onSelectApp: (String) -> Unit,
    onNavigateToApps: () -> Unit
) {
    val apps by viewModel.rawApps.collectAsState()
    val settings by viewModel.allSettings.collectAsState()
    val entertainmentApps = apps.filter { it.isEntertainment }

    val entertainmentUsage by viewModel.entertainmentUsageMinutes.collectAsState()
    val entertainmentBudget by viewModel.totalEntertainmentLimitMinutes.collectAsState()

    val strictMode = settings["strict_mode"] == "true"
    val pinEnabled = settings["pin_enabled"] == "true"
    val actualPin = settings["pin_code"] ?: "1234"

    var showPinDialogForStrict by remember { mutableStateOf(false) }
    var pinError by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("entertainment_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(EntertainmentPurple.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Movie,
                        contentDescription = null,
                        tint = EntertainmentPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Entertainment Control",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Manage high-distraction video, social & gaming apps",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Aggregate Entertainment Budget Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Combined Entertainment Budget",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        val fraction = (entertainmentUsage.toFloat() / entertainmentBudget.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
                        Text(
                            text = "${(fraction * 100).toInt()}% Used",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (fraction >= 1f) CoralLimit else EntertainmentPurple
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val usedH = entertainmentUsage / 60
                    val usedM = entertainmentUsage % 60
                    val usedStr = if (usedH > 0) "${usedH}h ${usedM}m" else "${usedM}m"

                    val bH = entertainmentBudget / 60
                    val bM = entertainmentBudget % 60
                    val bStr = if (bH > 0) "${bH}h ${bM}m" else "${bM}m"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = usedStr,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Daily Limit: $bStr",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val fraction = (entertainmentUsage.toFloat() / entertainmentBudget.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = if (fraction >= 1f) CoralLimit else EntertainmentPurple,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Once an entertainment app finishes its time, Force Stop displays the full-screen block overlay immediately.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Strict Lockdown Mode Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (strictMode) CoralLimit.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (strictMode) CoralLimit.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (strictMode) CoralLimit else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Strict Lockdown Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Completely disables extra-time bypass when limit is reached",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = strictMode,
                        onCheckedChange = { desired ->
                            if (pinEnabled && strictMode && !desired) {
                                // Disabling strict mode requires PIN
                                showPinDialogForStrict = true
                            } else {
                                viewModel.saveSetting("strict_mode", desired.toString())
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CoralLimit,
                            checkedTrackColor = CoralLimit.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("strict_lockdown_switch")
                    )
                }
            }
        }

        // Entertainment Apps List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Distracting Apps (${entertainmentApps.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onNavigateToApps,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EntertainmentPurple),
                    modifier = Modifier.testTag("add_entertainment_app_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add App", fontSize = 12.sp)
                }
            }
        }

        items(entertainmentApps, key = { it.packageName }) { app ->
            AppItemCard(
                app = app,
                onClick = { onSelectApp(app.packageName) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showPinDialogForStrict) {
        PinEntryDialog(
            title = "Unlock Strict Mode",
            subtitle = "Enter your security PIN to disable strict lockdown.",
            hasError = pinError,
            onDismiss = {
                showPinDialogForStrict = false
                pinError = false
            },
            onConfirm = { entered ->
                if (entered == actualPin) {
                    showPinDialogForStrict = false
                    pinError = false
                    viewModel.saveSetting("strict_mode", "false")
                } else {
                    pinError = true
                }
            }
        )
    }
}
