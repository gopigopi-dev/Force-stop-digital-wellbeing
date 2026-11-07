package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.data.system.UsageStatsHelper
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralLimit
import com.example.ui.theme.EntertainmentPurple
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.MintAccent
import com.example.ui.viewmodel.MainViewModel

@Composable
fun PermissionsSetupScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val hasUsagePerm by viewModel.hasUsagePermission.collectAsState()
    val hasOverlayPerm by viewModel.hasOverlayPermission.collectAsState()
    val hasNotifPerm by viewModel.hasNotificationPermission.collectAsState()

    // Automatically re-check permissions whenever user returns from Android System Settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermissions(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val grantedCount = (if (hasUsagePerm) 1 else 0) + (if (hasOverlayPerm) 1 else 0)
    val allCoreGranted = hasUsagePerm && hasOverlayPerm

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("permissions_setup_screen"),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Top Navigation Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("permissions_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "Permission Setup Guide",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Enable core Android APIs for real-time enforcement",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.checkPermissions(context) },
                    modifier = Modifier.testTag("refresh_permissions_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh status",
                        tint = IndigoPrimary
                    )
                }
            }
        }

        // Overall Setup Progress Banner
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (allCoreGranted) MintAccent.copy(alpha = 0.12f)
                    else IndigoPrimary.copy(alpha = 0.08f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (allCoreGranted) MintAccent else IndigoPrimary
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (allCoreGranted) Icons.Default.VerifiedUser else Icons.Default.Security,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (allCoreGranted) "All Core Permissions Active"
                                    else "Setup Progress: $grantedCount of 2 Enabled",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (allCoreGranted) "Force Stop can now track and block distracting apps automatically."
                                    else "Complete the two steps below to activate automatic app blocking.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { grantedCount / 2f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = if (allCoreGranted) MintAccent else IndigoPrimary,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }

        // STEP 1: USAGE ACCESS PERMISSION CARD WITH ILLUSTRATION
        item {
            PermissionGuideCard(
                stepNumber = 1,
                title = "Usage Access Permission",
                subtitle = "android.permission.PACKAGE_USAGE_STATS",
                isGranted = hasUsagePerm,
                accentColor = IndigoPrimary,
                illustration = {
                    UsageAccessIllustration(isGranted = hasUsagePerm)
                },
                whyNecessary = "Android sandboxes apps for privacy. Without Usage Access, Force Stop cannot see how many minutes you have spent on YouTube, Instagram, or Netflix today, nor detect when a restricted app is opened in the foreground.",
                steps = listOf(
                    "Tap 'Open Usage Access Settings' below.",
                    "Find and tap 'Force Stop' in the system list.",
                    "Toggle ON 'Permit usage access' and return here."
                ),
                buttonText = if (hasUsagePerm) "Usage Access Granted" else "Open Usage Access Settings",
                buttonTestTag = "guide_grant_usage_button",
                onActionClick = {
                    UsageStatsHelper.openUsageAccessSettings(context)
                }
            )
        }

        // STEP 2: DISPLAY OVER OTHER APPS (OVERLAY) CARD WITH ILLUSTRATION
        item {
            PermissionGuideCard(
                stepNumber = 2,
                title = "Display Over Other Apps (Overlay)",
                subtitle = "android.permission.SYSTEM_ALERT_WINDOW",
                isGranted = hasOverlayPerm,
                accentColor = EntertainmentPurple,
                illustration = {
                    OverlayBlockIllustration(isGranted = hasOverlayPerm)
                },
                whyNecessary = "When an entertainment app hits 0 minutes remaining, Android requires this permission so Force Stop can immediately display the full-screen 'Daily Limit Reached' lock screen over the distracting app to stop continued scrolling.",
                steps = listOf(
                    "Tap 'Open Overlay Settings' below.",
                    "Select 'Force Stop' from the apps list.",
                    "Switch ON 'Allow display over other apps'."
                ),
                buttonText = if (hasOverlayPerm) "Overlay Permission Granted" else "Open Overlay Settings",
                buttonTestTag = "guide_grant_overlay_button",
                onActionClick = {
                    UsageStatsHelper.openOverlaySettings(context)
                }
            )
        }

        // Privacy & Security Assurance Footer
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = IndigoPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "100% On-Device Privacy Guarantee",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Your app usage data never leaves your phone. Force Stop stores all limits and usage history locally in an offline Room database and never reads screen content, keystrokes, or personal messages.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Done / Continue Button
        item {
            Button(
                onClick = onBack,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (allCoreGranted) MintAccent else IndigoPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("finish_permission_setup_button")
            ) {
                Text(
                    text = if (allCoreGranted) "All Set — Return to App" else "Return to Dashboard",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun PermissionGuideCard(
    stepNumber: Int,
    title: String,
    subtitle: String,
    isGranted: Boolean,
    accentColor: Color,
    illustration: @Composable () -> Unit,
    whyNecessary: String,
    steps: List<String>,
    buttonText: String,
    buttonTestTag: String,
    onActionClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isGranted) 1.5.dp else 1.dp,
                color = if (isGranted) MintAccent.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Step Badge + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "STEP $stepNumber",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = if (isGranted) MintAccent.copy(alpha = 0.15f) else AmberWarning.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isGranted) MintAccent else AmberWarning,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isGranted) "Enabled" else "Action Required",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isGranted) MintAccent else AmberWarning
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Vector / Canvas Illustration Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(165.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.08f),
                                accentColor.copy(alpha = 0.03f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = accentColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                illustration()
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Why It's Necessary Box
            Text(
                text = "Why is this required?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = whyNecessary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Step-by-step walkthrough
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "How to enable:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    steps.forEachIndexed { idx, stepText ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(accentColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stepText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isGranted) {
                OutlinedButton(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag(buttonTestTag)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MintAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = buttonText,
                        color = MintAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag(buttonTestTag)
                ) {
                    Text(
                        text = buttonText,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Helpful visual diagram illustrating UsageStatsManager tracking app timers & foreground activity.
 */
@Composable
private fun UsageAccessIllustration(isGranted: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "usage_illus")
    val barPulse by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "barPulse"
    )

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Left: Illustrated Phone with App Usage Bars
        Canvas(modifier = Modifier.size(width = 110.dp, height = 130.dp)) {
            val w = size.width
            val h = size.height

            // Phone Outer Body
            drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(14f, 4f),
                size = Size(w - 28f, h - 8f),
                cornerRadius = CornerRadius(22f, 22f)
            )
            // Phone Screen (Light)
            drawRoundRect(
                color = Color(0xFFFFFFFF),
                topLeft = Offset(20f, 14f),
                size = Size(w - 40f, h - 28f),
                cornerRadius = CornerRadius(16f, 16f)
            )

            // App Row 1: YouTube Red bar
            drawRoundRect(
                color = Color(0xFFEF4444),
                topLeft = Offset(30f, 32f),
                size = Size(14f, 14f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = Color(0xFFE2E8F0),
                topLeft = Offset(50f, 35f),
                size = Size(w - 78f, 8f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = Color(0xFFEF4444),
                topLeft = Offset(50f, 35f),
                size = Size((w - 78f) * 0.85f * barPulse, 8f),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // App Row 2: Instagram Purple bar
            drawRoundRect(
                color = Color(0xFF8B5CF6),
                topLeft = Offset(30f, 58f),
                size = Size(14f, 14f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = Color(0xFFE2E8F0),
                topLeft = Offset(50f, 61f),
                size = Size(w - 78f, 8f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = Color(0xFF8B5CF6),
                topLeft = Offset(50f, 61f),
                size = Size((w - 78f) * 0.65f, 8f),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // App Row 3: Healthy Mint bar
            drawRoundRect(
                color = Color(0xFF10B981),
                topLeft = Offset(30f, 84f),
                size = Size(14f, 14f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = Color(0xFFE2E8F0),
                topLeft = Offset(50f, 87f),
                size = Size(w - 78f, 8f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = Color(0xFF10B981),
                topLeft = Offset(50f, 87f),
                size = Size((w - 78f) * 0.4f, 8f),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }

        // Right: Visual Explanation Legend
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.QueryStats,
                        contentDescription = null,
                        tint = IndigoPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Reads App Timers",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isGranted) MintAccent else AmberWarning)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "30m / 30m → Trigger",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A)
                    )
                }
            }

            Text(
                text = "Detects when a restricted app is opened & measures exact daily minutes.",
                fontSize = 11.sp,
                lineHeight = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Helpful visual diagram illustrating how the Overlay Shield covers a distracting app when time is up.
 */
@Composable
private fun OverlayBlockIllustration(isGranted: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "overlay_illus")
    val shieldOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shieldOffset"
    )

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Left: Layered Phone + Shield Overlay Illustration
        Canvas(modifier = Modifier.size(width = 110.dp, height = 130.dp)) {
            val w = size.width
            val h = size.height

            // Background Layer: Distracting App Card (dimmed)
            drawRoundRect(
                color = Color(0xFF94A3B8).copy(alpha = 0.45f),
                topLeft = Offset(10f, 14f),
                size = Size(w - 36f, h - 28f),
                cornerRadius = CornerRadius(18f, 18f)
            )

            // Foreground Layer: Force Stop Lock Overlay Card sliding over
            val overlayTop = 10f - shieldOffset
            val overlayLeft = 26f + shieldOffset
            drawRoundRect(
                color = Color(0xFF4F46E5),
                topLeft = Offset(overlayLeft, overlayTop),
                size = Size(w - 34f, h - 24f),
                cornerRadius = CornerRadius(18f, 18f)
            )

            // White Lock Circle inside Overlay
            val centerX = overlayLeft + (w - 34f) / 2f
            val centerY = overlayTop + (h - 24f) / 2f - 8f
            drawCircle(
                color = Color.White.copy(alpha = 0.2f),
                radius = 22f,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = Color.White,
                radius = 14f,
                center = Offset(centerX, centerY)
            )

            // Small coral lock dot in center
            drawCircle(
                color = Color(0xFFEF4444),
                radius = 5f,
                center = Offset(centerX, centerY)
            )

            // Bottom "Limit Reached" pill bar inside overlay
            drawRoundRect(
                color = Color.White.copy(alpha = 0.85f),
                topLeft = Offset(overlayLeft + 14f, centerY + 28f),
                size = Size(w - 62f, 10f),
                cornerRadius = CornerRadius(5f, 5f)
            )
        }

        // Right: Explanation Legend
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = EntertainmentPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Instant Lock Shield",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = CoralLimit,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Blocks Re-Opening",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A)
                    )
                }
            }

            Text(
                text = "Draws the 'Daily Limit Reached' screen over restricted apps when time expires.",
                fontSize = 11.sp,
                lineHeight = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
