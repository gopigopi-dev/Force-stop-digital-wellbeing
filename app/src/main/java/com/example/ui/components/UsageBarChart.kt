package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralLimit
import com.example.ui.theme.IndigoPrimary

data class DayUsageBarData(
    val dayLabel: String, // e.g. "Mon", "Tue"
    val usedMinutes: Int,
    val isToday: Boolean = false,
    val hasViolation: Boolean = false
)

@Composable
fun UsageBarChart(
    data: List<DayUsageBarData>,
    dailyLimitMinutes: Int = 120,
    modifier: Modifier = Modifier
) {
    val maxMinutes = (data.maxOfOrNull { it.usedMinutes } ?: 120).coerceAtLeast(dailyLimitMinutes).coerceAtLeast(60)

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animProgress.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { item ->
                val barFraction = ((item.usedMinutes.toFloat() / maxMinutes) * animProgress.value).coerceIn(0.06f, 1f)
                val isOverLimit = item.usedMinutes > dailyLimitMinutes

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    // Minutes tooltip label for peak bars
                    Text(
                        text = if (item.usedMinutes > 0) "${item.usedMinutes}m" else "0",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = if (item.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // The Bar itself
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height((100 * barFraction).dp)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                            .background(
                                when {
                                    isOverLimit -> CoralLimit
                                    item.hasViolation -> AmberWarning
                                    item.isToday -> IndigoPrimary
                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                                }
                            )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Day of Week
                    Text(
                        text = item.dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (item.isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (item.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
