package com.example.ui.blocked

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ForceStopApp
import com.example.MainActivity
import com.example.ui.components.PinEntryDialog
import com.example.ui.theme.ForceStopTheme
import kotlinx.coroutines.launch
import java.util.Calendar

class BlockedAppActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val packageNameExtra = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: "com.google.android.youtube"
        val appNameExtra = intent.getStringExtra(EXTRA_APP_NAME) ?: "App"
        val usedMinutes = intent.getIntExtra(EXTRA_USED_MINUTES, 30)
        val limitMinutes = intent.getIntExtra(EXTRA_LIMIT_MINUTES, 30)

        setContent {
            ForceStopTheme(darkTheme = false) {
                BlockedScreen(
                    packageName = packageNameExtra,
                    appName = appNameExtra,
                    usedMinutes = usedMinutes,
                    limitMinutes = limitMinutes,
                    onGoHome = {
                        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_HOME)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(homeIntent)
                        finish()
                    },
                    onOpenDashboard = {
                        val mainIntent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        startActivity(mainIntent)
                        finish()
                    },
                    onExtraTimeGranted = {
                        finish()
                    }
                )
            }
        }
    }

    override fun onBackPressed() {
        // Prevent bypassing the limit via Back button — return to Home
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_APP_NAME = "extra_app_name"
        const val EXTRA_USED_MINUTES = "extra_used_minutes"
        const val EXTRA_LIMIT_MINUTES = "extra_limit_minutes"
    }
}

@Composable
fun BlockedScreen(
    packageName: String,
    appName: String,
    usedMinutes: Int,
    limitMinutes: Int,
    onGoHome: () -> Unit,
    onOpenDashboard: () -> Unit,
    onExtraTimeGranted: () -> Unit
) {
    val repository = ForceStopApp.instance.repository
    val settings by repository.allSettings.collectAsState(initial = emptyMap())
    val scope = rememberCoroutineScope()

    var showPinDialog by remember { mutableStateOf(false) }
    var pinError by remember { mutableStateOf(false) }

    val resetHour = settings["reset_hour"]?.toIntOrNull() ?: 0
    val resetMinute = settings["reset_minute"]?.toIntOrNull() ?: 0
    val pinEnabled = settings["pin_enabled"] == "true"
    val actualPin = settings["pin_code"] ?: "1234"
    val strictMode = settings["strict_mode"] == "true"

    // Calculate time until next cycle
    val now = Calendar.getInstance()
    val nextReset = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, resetHour)
        set(Calendar.MINUTE, resetMinute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (timeInMillis <= now.timeInMillis) {
            add(Calendar.DAY_OF_YEAR, 1)
        }
    }
    val diffMs = nextReset.timeInMillis - now.timeInMillis
    val diffHours = (diffMs / (1000 * 60 * 60)).toInt()
    val diffMins = ((diffMs / (1000 * 60)) % 60).toInt()

    // Gentle breathing animation for mindfulness
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("blocked_screen"),
        color = Color(0xFFF8FAFC)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFEEF2FF),
                            Color(0xFFF8FAFC),
                            Color(0xFFFFFFFF)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Brand
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = "Mindful Focus",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FORCE STOP • DIGITAL BALANCE",
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 2.sp,
                        color = Color(0xFF475569),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Center Card & Message
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Pulsing Mindfulness Lock Icon
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(110.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFEF4444).copy(alpha = 0.25f),
                                        Color(0xFF6366F1).copy(alpha = 0.10f),
                                        Color.Transparent
                                    )
                                )
                            )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF2F2))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Daily Limit Reached",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "You've reached your daily limit for $appName.\nTake a mindful break and reconnect with your day.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF475569),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Stats breakdown card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassTop,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Time spent today",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Text(
                                    text = "${usedMinutes}m / ${limitMinutes}m",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Timer,
                                        contentDescription = null,
                                        tint = Color(0xFF4F46E5),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Next usage cycle",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                val formattedReset = String.format("%02d:%02d", resetHour, resetMinute)
                                Text(
                                    text = "In ${diffHours}h ${diffMins}m ($formattedReset)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF4F46E5)
                                )
                            }
                        }
                    }
                }

                // Actions Column
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Primary Return Home Button
                    Button(
                        onClick = onGoHome,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("return_home_button")
                    ) {
                        Icon(Icons.Outlined.Home, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Return to Home Screen",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Secondary Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenDashboard,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("open_dashboard_button")
                        ) {
                            Text(
                                text = "Open Force Stop",
                                color = Color(0xFF334155),
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }

                        if (!strictMode) {
                            OutlinedButton(
                                onClick = {
                                    if (pinEnabled) {
                                        showPinDialog = true
                                    } else {
                                        scope.launch {
                                            repository.addExtraTime(packageName, 10)
                                            onExtraTimeGranted()
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("request_extra_time_button")
                            ) {
                                Text(
                                    text = "+10 min Extra",
                                    color = Color(0xFFF59E0B),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPinDialog) {
        PinEntryDialog(
            title = "Enter Security PIN",
            subtitle = "Enter your 4-digit PIN to approve 10 minutes of extra time.",
            hasError = pinError,
            onDismiss = {
                showPinDialog = false
                pinError = false
            },
            onConfirm = { enteredPin ->
                if (enteredPin == actualPin) {
                    showPinDialog = false
                    pinError = false
                    scope.launch {
                        repository.addExtraTime(packageName, 10)
                        onExtraTimeGranted()
                    }
                } else {
                    pinError = true
                }
            }
        )
    }
}
