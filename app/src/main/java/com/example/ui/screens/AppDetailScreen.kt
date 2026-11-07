package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.data.model.AppUsageInfo
import com.example.ui.components.ExtraTimeDialog
import com.example.ui.components.PinEntryDialog
import com.example.ui.components.SetLimitDialog
import com.example.ui.components.UsageCircleGauge
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralLimit
import com.example.ui.theme.EntertainmentPurple
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.MintAccent
import com.example.ui.viewmodel.MainViewModel

@Composable
fun AppDetailScreen(
    packageName: String,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val apps by viewModel.rawApps.collectAsState()
    val app = apps.find { it.packageName == packageName }
    val settings by viewModel.allSettings.collectAsState()

    val pinEnabled = settings["pin_enabled"] == "true"
    val actualPin = settings["pin_code"] ?: "1234"

    var showLimitDialog by remember { mutableStateOf(false) }
    var showExtraDialog by remember { mutableStateOf(false) }
    var showConfirmDisableDialog by remember { mutableStateOf(false) }
    var showPinDialogForDisable by remember { mutableStateOf(false) }
    var pinError by remember { mutableStateOf(false) }

    if (app == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("App not found", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBack) {
                Text("Go Back")
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("app_detail_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Bar Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("app_detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "App Control",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // App Card Identity
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (app.isEntertainment) EntertainmentPurple.copy(alpha = 0.15f)
                                else IndigoPrimary.copy(alpha = 0.12f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (app.icon != null) {
                            Image(
                                bitmap = app.icon.toBitmap(128, 128).asImageBitmap(),
                                contentDescription = app.appName,
                                modifier = Modifier.size(46.dp)
                            )
                        } else {
                            Icon(
                                imageVector = if (app.isEntertainment) Icons.Outlined.Movie else Icons.Outlined.Smartphone,
                                contentDescription = null,
                                tint = if (app.isEntertainment) EntertainmentPurple else IndigoPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = app.appName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (app.isEntertainment) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = EntertainmentPurple.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Entertainment",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = EntertainmentPurple,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = app.packageName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Circular Usage Progress Gauge
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Today's App Screen Time",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    UsageCircleGauge(
                        usedMinutes = app.usedTodayMinutes,
                        totalBudgetMinutes = app.totalAllowedMinutes,
                        size = 180.dp,
                        strokeWidth = 16.dp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Used Today", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(app.formattedUsedTime(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Daily Limit", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(app.formattedLimitTime(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Status", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (app.isLimitReached) "Blocked" else "${app.remainingMinutes}m left",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (app.isLimitReached) CoralLimit else MintAccent
                            )
                        }
                    }
                }
            }
        }

        // Restriction Switch & Limit Config Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Restriction Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Strict Restriction",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Block app immediately when daily limit is exhausted",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = app.isRestricted,
                            onCheckedChange = { desired ->
                                if (!desired) {
                                    // Warning before disabling
                                    if (pinEnabled) {
                                        showPinDialogForDisable = true
                                    } else {
                                        showConfirmDisableDialog = true
                                    }
                                } else {
                                    viewModel.updateAppLimit(
                                        packageName = app.packageName,
                                        dailyLimitMinutes = if (app.dailyLimitMinutes > 0) app.dailyLimitMinutes else 30,
                                        isRestricted = true,
                                        isEntertainment = app.isEntertainment
                                    )
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = IndigoPrimary),
                            modifier = Modifier.testTag("app_restriction_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Entertainment category toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mark as Entertainment",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Include in combined entertainment budget tracking",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = app.isEntertainment,
                            onCheckedChange = { desired ->
                                viewModel.updateAppLimit(
                                    packageName = app.packageName,
                                    dailyLimitMinutes = app.dailyLimitMinutes,
                                    isRestricted = app.isRestricted,
                                    isEntertainment = desired
                                )
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = EntertainmentPurple),
                            modifier = Modifier.testTag("entertainment_category_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Buttons: Edit Daily Limit and Extra Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showLimitDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("edit_daily_limit_button")
                        ) {
                            Text("Edit Daily Limit")
                        }

                        OutlinedButton(
                            onClick = { showExtraDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("grant_extra_time_button")
                        ) {
                            Icon(Icons.Outlined.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+Extra Time", color = AmberWarning)
                        }
                    }
                }
            }
        }

        // Test Blocking Screen for this app
        item {
            OutlinedButton(
                onClick = { viewModel.testLockScreen(context, app.packageName) },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("test_block_overlay_button")
            ) {
                Icon(Icons.Default.Block, contentDescription = null, tint = CoralLimit, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Test Blocking Overlay on ${app.appName}", color = CoralLimit)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showLimitDialog) {
        SetLimitDialog(
            appName = app.appName,
            currentLimitMinutes = app.dailyLimitMinutes,
            onDismiss = { showLimitDialog = false },
            onConfirm = { newLimit ->
                showLimitDialog = false
                viewModel.updateAppLimit(
                    packageName = app.packageName,
                    dailyLimitMinutes = newLimit,
                    isRestricted = newLimit > 0,
                    isEntertainment = app.isEntertainment
                )
            }
        )
    }

    if (showExtraDialog) {
        ExtraTimeDialog(
            appName = app.appName,
            onDismiss = { showExtraDialog = false },
            onConfirm = { additional ->
                showExtraDialog = false
                viewModel.addExtraTime(app.packageName, additional)
            }
        )
    }

    if (showConfirmDisableDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDisableDialog = false },
            title = { Text("Disable Restriction?") },
            text = {
                Text("Disabling restriction will allow unlimited screen time on ${app.appName} without warnings or force stops.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDisableDialog = false
                        viewModel.updateAppLimit(
                            packageName = app.packageName,
                            dailyLimitMinutes = app.dailyLimitMinutes,
                            isRestricted = false,
                            isEntertainment = app.isEntertainment
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralLimit)
                ) {
                    Text("Disable")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDisableDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showPinDialogForDisable) {
        PinEntryDialog(
            title = "Enter Security PIN",
            subtitle = "Enter PIN to confirm disabling limits on ${app.appName}.",
            hasError = pinError,
            confirmText = "Confirm",
            onDismiss = {
                showPinDialogForDisable = false
                pinError = false
            },
            onConfirm = { entered ->
                if (entered == actualPin) {
                    showPinDialogForDisable = false
                    pinError = false
                    viewModel.updateAppLimit(
                        packageName = app.packageName,
                        dailyLimitMinutes = app.dailyLimitMinutes,
                        isRestricted = false,
                        isEntertainment = app.isEntertainment
                    )
                } else {
                    pinError = true
                }
            }
        )
    }
}
