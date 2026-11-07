package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.DaylightDeepIndigo
import com.example.ui.theme.DaylightLavenderPill
import com.example.ui.theme.DaylightPeriwinkle
import com.example.ui.theme.DaylightSageGreen
import com.example.ui.theme.DaylightSoftMint
import com.example.ui.theme.DaylightSunWarm
import com.example.ui.theme.DaylightSunYellow
import kotlinx.coroutines.delay

@Composable
fun DaylightPlantArt(
    modifier: Modifier = Modifier,
    size: Dp = 130.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Ceramic Pot
        val potPath = Path().apply {
            moveTo(w * 0.30f, h * 0.62f)
            lineTo(w * 0.70f, h * 0.62f)
            lineTo(w * 0.64f, h * 0.90f)
            lineTo(w * 0.36f, h * 0.90f)
            close()
        }
        drawPath(potPath, color = Color(0xFFE2DACF))

        // Pot Rim
        drawRoundRect(
            color = Color(0xFFD3C7BA),
            topLeft = Offset(w * 0.28f, h * 0.58f),
            size = Size(w * 0.44f, h * 0.06f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )

        // Stems & Leaves
        // Center tall leaf
        val leafCenter = Path().apply {
            moveTo(w * 0.50f, h * 0.60f)
            cubicTo(w * 0.45f, h * 0.38f, w * 0.40f, h * 0.20f, w * 0.50f, h * 0.08f)
            cubicTo(w * 0.60f, h * 0.20f, w * 0.55f, h * 0.38f, w * 0.50f, h * 0.60f)
            close()
        }
        drawPath(leafCenter, color = DaylightSageGreen)

        // Left broad leaf
        val leafLeft = Path().apply {
            moveTo(w * 0.48f, h * 0.60f)
            cubicTo(w * 0.26f, h * 0.45f, w * 0.20f, h * 0.26f, w * 0.32f, h * 0.16f)
            cubicTo(w * 0.44f, h * 0.22f, w * 0.42f, h * 0.45f, w * 0.48f, h * 0.60f)
            close()
        }
        drawPath(leafLeft, color = Color(0xFF4C8F66))

        // Right arched leaf
        val leafRight = Path().apply {
            moveTo(w * 0.52f, h * 0.60f)
            cubicTo(w * 0.72f, h * 0.46f, w * 0.82f, h * 0.28f, w * 0.70f, h * 0.18f)
            cubicTo(w * 0.58f, h * 0.24f, w * 0.58f, h * 0.46f, w * 0.52f, h * 0.60f)
            close()
        }
        drawPath(leafRight, color = Color(0xFF68B385))

        // Lower gentle leaf
        val leafLow = Path().apply {
            moveTo(w * 0.46f, h * 0.60f)
            cubicTo(w * 0.22f, h * 0.54f, w * 0.18f, h * 0.42f, w * 0.26f, h * 0.36f)
            cubicTo(w * 0.38f, h * 0.40f, w * 0.42f, h * 0.52f, w * 0.46f, h * 0.60f)
            close()
        }
        drawPath(leafLow, color = DaylightSoftMint)
    }
}

@Composable
fun DaylightLandscapeArt(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Glowing morning sun
        drawCircle(
            color = DaylightSunYellow,
            radius = h * 0.26f,
            center = Offset(w * 0.76f, h * 0.32f)
        )

        // Distant soft mountain
        val distantMountain = Path().apply {
            moveTo(w * 0.40f, h)
            lineTo(w * 0.65f, h * 0.38f)
            lineTo(w * 0.85f, h * 0.55f)
            lineTo(w, h * 0.42f)
            lineTo(w, h)
            close()
        }
        drawPath(distantMountain, color = Color(0xFFB8B3E4))

        // Forefront rounded hill
        val foreHill = Path().apply {
            moveTo(0f, h)
            cubicTo(w * 0.20f, h * 0.55f, w * 0.50f, h * 0.65f, w * 0.75f, h * 0.50f)
            lineTo(w, h * 0.65f)
            lineTo(w, h)
            close()
        }
        drawPath(foreHill, color = Color(0xFF6B65B2))

        // Left soft dark crest
        val leftCrest = Path().apply {
            moveTo(0f, h)
            lineTo(0f, h * 0.72f)
            cubicTo(w * 0.18f, h * 0.68f, w * 0.32f, h * 0.82f, w * 0.48f, h)
            close()
        }
        drawPath(leftCrest, color = Color(0xFF403C78))
    }
}

@Composable
fun DaylightLeafBranch(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    tint: Color = DaylightSageGreen
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Stem
        drawLine(
            color = tint,
            start = Offset(w * 0.2f, h * 0.85f),
            end = Offset(w * 0.85f, h * 0.15f),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )

        // Leaf pairs
        drawOval(
            color = tint,
            topLeft = Offset(w * 0.65f, h * 0.10f),
            size = Size(w * 0.25f, h * 0.18f)
        )
        drawOval(
            color = tint,
            topLeft = Offset(w * 0.45f, h * 0.30f),
            size = Size(w * 0.24f, h * 0.16f)
        )
        drawOval(
            color = tint,
            topLeft = Offset(w * 0.22f, h * 0.45f),
            size = Size(w * 0.22f, h * 0.16f)
        )
    }
}

@Composable
fun DaylightBalanceCard(
    percentage: Int,
    usedMinutes: Int,
    dailyTargetMinutes: Int,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(26.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Multi-segment Arc Balance Gauge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(140.dp)
                ) {
                    Canvas(modifier = Modifier.size(136.dp)) {
                        val stroke = 14.dp.toPx()
                        val arcSize = Size(size.width - stroke, size.height - stroke)
                        val topLeft = Offset(stroke / 2, stroke / 2)

                        // Base track
                        drawArc(
                            color = Color(0xFFECE7F6),
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )

                        // Segment 1: Deep Indigo portion
                        drawArc(
                            color = DaylightDeepIndigo,
                            startAngle = 135f,
                            sweepAngle = 120f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )

                        // Segment 2: Warm Sunny Yellow portion
                        drawArc(
                            color = DaylightSunYellow,
                            startAngle = 260f,
                            sweepAngle = 90f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )

                        // Segment 3: Calm Sage Mint endpoint
                        drawArc(
                            color = DaylightSageGreen,
                            startAngle = 355f,
                            sweepAngle = 45f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$percentage%",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Daily balance",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = null,
                        tint = DaylightSageGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "More presence today",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Right: Serene Potted Plant with Quote Pill
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(0.95f)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = DaylightLavenderPill.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = "A kinder\nday lives\nhere",
                        fontSize = 11.sp,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Medium,
                        color = DaylightDeepIndigo,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                DaylightPlantArt(size = 120.dp)
            }
        }
    }
}

@Composable
fun DaylightFourPillars(
    screenTimeMinutes: Int,
    entertainmentMinutes: Int,
    restrictedCount: Int,
    calmScorePercent: Int = 85,
    onPillarClick: (String) -> Unit = {}
) {
    val h = screenTimeMinutes / 60
    val m = screenTimeMinutes % 60
    val screenStr = if (h > 0) "${h}h ${m}m" else "${m}m"

    val entH = entertainmentMinutes / 60
    val entM = entertainmentMinutes % 60
    val entStr = if (entH > 0) "${entH}h ${entM}m" else "${entM}m"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PillarItem(
            icon = Icons.Default.Spa,
            title = "Mind",
            value = "Calmer",
            tint = DaylightSageGreen,
            modifier = Modifier.weight(1f),
            onClick = { onPillarClick("Mind") }
        )
        PillarItem(
            icon = Icons.Outlined.HourglassBottom,
            title = "Screen",
            value = screenStr,
            tint = DaylightPeriwinkle,
            modifier = Modifier.weight(1f),
            onClick = { onPillarClick("Screen") }
        )
        PillarItem(
            icon = Icons.Outlined.Movie,
            title = "Entertainment",
            value = entStr,
            tint = DaylightSunWarm,
            modifier = Modifier.weight(1f),
            onClick = { onPillarClick("Entertainment") }
        )
        PillarItem(
            icon = Icons.Outlined.Shield,
            title = "Limits",
            value = "$restrictedCount Safe",
            tint = DaylightDeepIndigo,
            modifier = Modifier.weight(1f),
            onClick = { onPillarClick("Limits") }
        )
    }
}

@Composable
private fun PillarItem(
    icon: ImageVector,
    title: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
fun DaylightTakeABreathBanner(
    onStartBreathing: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDDD9F3)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(145.dp)
        ) {
            // Landscape Art in the right portion
            DaylightLandscapeArt(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(145.dp)
                    .align(Alignment.CenterEnd)
            )

            // Left Text Content
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.68f)
                    .padding(20.dp)
                    .align(Alignment.CenterStart)
            ) {
                Text(
                    text = "TAKE A BREATH",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = DaylightDeepIndigo.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "A calmer you\nin 3 minutes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DaylightDeepIndigo,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onStartBreathing,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DaylightDeepIndigo,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("start_breath_session_button")
                ) {
                    Text("Start session", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DaylightWeeklyCard(
    activeDays: Set<Int> = setOf(1, 4, 6), // Mon, Thu, Sat
    modifier: Modifier = Modifier
) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(26.dp))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "This week",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Screen limits respected",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Checkmark circles row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEachIndexed { index, _ ->
                    val isChecked = activeDays.contains(index)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isChecked) DaylightSunYellow else Color(0xFFF1EEF8))
                    ) {
                        if (isChecked) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Goal met",
                                tint = DaylightDeepIndigo,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Soft pastel vertical bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val heights = listOf(0.45f, 0.60f, 0.40f, 0.95f, 0.65f, 0.40f, 0.55f)
                heights.forEachIndexed { index, fraction ->
                    val isPeak = index == 3 // Thursday peak/highlighted in Daylight image
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height((65 * fraction).dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isPeak) DaylightSunYellow else Color(0xFFC7C2EA))
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Day labels under bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEach { day ->
                    Text(
                        text = day,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Quote line with leaf motif
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "“Progress feels lighter in good company.”",
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                DaylightLeafBranch(size = 28.dp)
            }
        }
    }
}

@Composable
fun DaylightGuidedBreathModal(
    onDismiss: () -> Unit
) {
    var breathPhase by remember { mutableStateOf("Breathe in...") }
    var secondsLeft by remember { mutableStateOf(180) }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            breathPhase = "Breathe in slowly..."
            delay(4000)
            breathPhase = "Hold gently..."
            delay(3000)
            breathPhase = "Release and relax..."
            delay(5000)
            secondsLeft -= 12
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "breath")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1C38)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "3-MINUTE MINDFUL PAUSE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = Color(0xFFA5A0CE)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(160.dp)
                        .scale(breathScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    DaylightPeriwinkle.copy(alpha = 0.45f),
                                    DaylightSunYellow.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2C2854))
                    ) {
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = null,
                            tint = DaylightSunYellow,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = breathPhase,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                val mins = secondsLeft / 60
                val secs = secondsLeft % 60
                Text(
                    text = String.format("%02d:%02d remaining", mins, secs),
                    fontSize = 13.sp,
                    color = Color(0xFFA5A0CE)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DaylightSunYellow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Complete Pause", color = DaylightDeepIndigo, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
