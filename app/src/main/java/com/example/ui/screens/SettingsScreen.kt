package com.example.ui.screens

import android.app.TimePickerDialog
import android.content.Context
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.system.UsageStatsHelper
import com.example.service.NotificationHelper
import com.example.ui.components.PinEntryDialog
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralLimit
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.MintAccent
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateToPermissionsSetup: () -> Unit = {}
) {
    val context = LocalContext.current
    val settings by viewModel.allSettings.collectAsState()

    val hasUsagePerm by viewModel.hasUsagePermission.collectAsState()
    val hasOverlayPerm by viewModel.hasOverlayPermission.collectAsState()
    val hasNotifPerm by viewModel.hasNotificationPermission.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkPermissions(context)
    }

    val resetHour = settings["reset_hour"]?.toIntOrNull() ?: 0
    val resetMinute = settings["reset_minute"]?.toIntOrNull() ?: 0
    val defaultEntLimit = settings["default_entertainment_limit"]?.toIntOrNull() ?: 45
    val pinEnabled = settings["pin_enabled"] == "true"
    val actualPin = settings["pin_code"] ?: "1234"

    val notif80Percent = settings["notif_80_percent"] != "false"
    val notif10Min = settings["notif_10min"] != "false"
    val notif5Min = settings["notif_5min"] != "false"
    val notifLimit = settings["notif_limit_reached"] != "false"

    var showPinDialogForToggle by remember { mutableStateOf(false) }
    var pinError by remember { mutableStateOf(false) }
    var isConfiguringNewPin by remember { mutableStateOf(false) }

    var entSliderValue by remember(defaultEntLimit) { mutableFloatStateOf(defaultEntLimit.toFloat()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(IndigoPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = null,
                        tint = IndigoPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Configure reset cycle, notifications & safety protection",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Section: Appearance & Theme
        item {
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            val currentThemeMode = settings["theme_mode"] ?: "light"
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "UI Theme",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Select your preferred interface style",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("light" to "Light Mode", "dark" to "Dark Mode").forEach { (key, label) ->
                            val isSelected = currentThemeMode == key
                            Button(
                                onClick = { viewModel.saveSetting("theme_mode", key) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("theme_option_$key")
                            ) {
                                Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // Section: Daily Cycle Reset Time (Core Requirement #7)
        item {
            Text(
                text = "Daily Usage Reset",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                viewModel.saveSetting("reset_hour", hourOfDay.toString())
                                viewModel.saveSetting("reset_minute", minute.toString())
                            },
                            resetHour,
                            resetMinute,
                            false
                        ).show()
                    }
                    .testTag("reset_time_picker_card")
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(IndigoPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = IndigoPrimary)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Daily Reset Time",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            val formattedTime = String.format("%02d:%02d", resetHour, resetMinute)
                            Text(
                                text = "Screen limits reset daily at $formattedTime",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val amPm = if (resetHour >= 12) "PM" else "AM"
                        val displayH = if (resetHour % 12 == 0) 12 else resetHour % 12
                        Text(
                            text = String.format("%d:%02d %s", displayH, resetMinute, amPm),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Section: Safety & Bypass Protection (Core Requirement #12)
        item {
            Text(
                text = "Safety & Bypass Protection",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(AmberWarning.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = AmberWarning)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "PIN Passcode Protection",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Requires 4-digit PIN to alter limits or bypass restrictions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = pinEnabled,
                            onCheckedChange = { desired ->
                                if (pinEnabled && !desired) {
                                    isConfiguringNewPin = false
                                    showPinDialogForToggle = true
                                } else if (!pinEnabled && desired) {
                                    isConfiguringNewPin = true
                                    showPinDialogForToggle = true
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = IndigoPrimary),
                            modifier = Modifier.testTag("pin_protection_switch")
                        )
                    }
                }
            }
        }

        // Section: Notifications (Core Requirement #14)
        item {
            Text(
                text = "Screen Time Notifications",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // 80% Entertainment Limit Alert
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "80% Entertainment Limit Alert", fontWeight = FontWeight.Medium)
                            Text(text = "Triggers when an entertainment app reaches 80% of its daily limit", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = notif80Percent,
                            onCheckedChange = { viewModel.saveSetting("notif_80_percent", it.toString()) },
                            modifier = Modifier.testTag("notif_80_percent_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 100% Limit Reached
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "100% Daily Limit Reached Alert", fontWeight = FontWeight.Medium)
                            Text(text = "Triggers when an entertainment app hits 100% and is Force Stopped", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = notifLimit,
                            onCheckedChange = { viewModel.saveSetting("notif_limit_reached", it.toString()) },
                            modifier = Modifier.testTag("notif_100_percent_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 10 Min Warning
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "10 Minutes Remaining Alert", fontWeight = FontWeight.Medium)
                            Text(text = "Gentle nudge before app is restricted", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = notif10Min,
                            onCheckedChange = { viewModel.saveSetting("notif_10min", it.toString()) }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 5 Min Warning
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "5 Minutes Remaining Alert", fontWeight = FontWeight.Medium)
                            Text(text = "Urgent heads-up to wrap up activity", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = notif5Min,
                            onCheckedChange = { viewModel.saveSetting("notif_5min", it.toString()) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Test 80% and 100% Notifications Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                NotificationHelper.show80PercentLimitNotification(
                                    context = context,
                                    targetName = "YouTube",
                                    usedMinutes = 24,
                                    limitMinutes = 30,
                                    isCategoryBudget = false
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AmberWarning),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_80_notif_button")
                        ) {
                            Text("Test 80% Alert", fontSize = 12.sp, color = Color.White)
                        }

                        Button(
                            onClick = {
                                NotificationHelper.show100PercentLimitNotification(
                                    context = context,
                                    targetName = "YouTube",
                                    limitMinutes = 30,
                                    isCategoryBudget = false
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CoralLimit),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_100_notif_button")
                        ) {
                            Text("Test 100% Alert", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // Section: Permissions Manager (Core Requirement #9)
        item {
            Text(
                text = "Android System Permissions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Usage Access
                    PermissionRow(
                        title = "Usage Access",
                        description = "Enables screen time tracking and foreground detection",
                        isGranted = hasUsagePerm,
                        onGrant = { UsageStatsHelper.openUsageAccessSettings(context) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Overlay / Display Over Apps
                    PermissionRow(
                        title = "Display Over Other Apps",
                        description = "Displays the full-screen 'Limit Reached' block overlay",
                        isGranted = hasOverlayPerm,
                        onGrant = { UsageStatsHelper.openOverlaySettings(context) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Notifications
                    PermissionRow(
                        title = "Notifications",
                        description = "Delivers 10m/5m warning cues and limit alerts",
                        isGranted = hasNotifPerm,
                        onGrant = { viewModel.checkPermissions(context) }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onNavigateToPermissionsSetup,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("settings_open_permission_guide_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Open Illustrated Permission Setup Guide",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Section: About & Android API Transparency (Core Requirement #8 & #10)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FORCE STOP v1.0",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "“Take control of your screen time.”",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = IndigoPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Force Stop relies on official Android system APIs (UsageStatsManager and Display Overlays) to provide respectful, transparent Digital Wellbeing enforcement without invasive exploits or battery drain.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showPinDialogForToggle) {
        if (isConfiguringNewPin) {
            PinEntryDialog(
                title = "Create New PIN",
                subtitle = "Set a 4-digit PIN code to secure your restrictions.",
                confirmText = "Set PIN",
                onDismiss = { showPinDialogForToggle = false },
                onConfirm = { newPin ->
                    viewModel.saveSetting("pin_code", newPin)
                    viewModel.saveSetting("pin_enabled", "true")
                    showPinDialogForToggle = false
                }
            )
        } else {
            PinEntryDialog(
                title = "Enter Current PIN",
                subtitle = "Enter your 4-digit PIN to turn off protection.",
                hasError = pinError,
                confirmText = "Disable",
                onDismiss = {
                    showPinDialogForToggle = false
                    pinError = false
                },
                onConfirm = { entered ->
                    if (entered == actualPin) {
                        viewModel.saveSetting("pin_enabled", "false")
                        showPinDialogForToggle = false
                        pinError = false
                    } else {
                        pinError = true
                    }
                }
            )
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    description: String,
    isGranted: Boolean,
    onGrant: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = title, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isGranted) MintAccent else CoralLimit,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (!isGranted) {
            Button(
                onClick = onGrant,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralLimit),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Grant", fontSize = 12.sp, color = Color.White)
            }
        } else {
            Text(
                text = "Granted",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MintAccent,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
