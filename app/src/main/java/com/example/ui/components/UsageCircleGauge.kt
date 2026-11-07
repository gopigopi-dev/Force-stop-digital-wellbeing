package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralLimit
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.MintAccent

@Composable
fun UsageCircleGauge(
    usedMinutes: Int,
    totalBudgetMinutes: Int,
    modifier: Modifier = Modifier,
    size: Dp = 190.dp,
    strokeWidth: Dp = 16.dp,
    title: String = "Today's Usage"
) {
    val progress = if (totalBudgetMinutes > 0) {
        (usedMinutes.toFloat() / totalBudgetMinutes.toFloat()).coerceIn(0f, 1f)
    } else {
        0.5f
    }

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    val progressColor = when {
        progress >= 1.0f -> CoralLimit
        progress >= 0.85f -> AmberWarning
        else -> IndigoPrimary
    }

    val secondaryColor = when {
        progress >= 1.0f -> Color(0xFFF87171)
        progress >= 0.85f -> Color(0xFFFBBF24)
        else -> MintAccent
    }

    val hours = usedMinutes / 60
    val mins = usedMinutes % 60
    val timeDisplay = when {
        hours > 0 && mins > 0 -> "${hours}h ${mins}m"
        hours > 0 -> "${hours}h"
        else -> "${mins}m"
    }

    val budgetDisplay = if (totalBudgetMinutes > 0) {
        val bHours = totalBudgetMinutes / 60
        val bMins = totalBudgetMinutes % 60
        val str = if (bHours > 0) "${bHours}h ${bMins}m" else "${bMins}m"
        "of $str limit"
    } else {
        "tracked today"
    }

    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(size.toPx() - strokePx, size.toPx() - strokePx)
            val topLeft = Offset(strokePx / 2, strokePx / 2)

            // Background Arc (260 degrees arc for speedometer style)
            drawArc(
                color = surfaceVariant.copy(alpha = 0.5f),
                startAngle = 140f,
                sweepAngle = 260f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Active Progress Arc
            if (animatedProgress.value > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.3f to progressColor,
                        0.8f to secondaryColor,
                        center = Offset(size.toPx() / 2, size.toPx() / 2)
                    ),
                    startAngle = 140f,
                    sweepAngle = 260f * animatedProgress.value,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        // Center typography and status
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = timeDisplay,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = budgetDisplay,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )

            val percent = (progress * 100).toInt()
            Text(
                text = if (progress >= 1f) "Limit Exceeded" else "$percent% used",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (progress >= 1f) CoralLimit else MintAccent
            )
        }
    }
}
