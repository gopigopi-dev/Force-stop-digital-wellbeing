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
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.data.model.DailyUsageSnapshot
import com.example.ui.components.DayUsageBarData
import com.example.ui.components.UsageBarChart
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralLimit
import com.example.ui.theme.EntertainmentPurple
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.MintAccent
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    onSelectApp: (String) -> Unit
) {
    val historySnapshots by viewModel.historySnapshots.collectAsState()
    val rawApps by viewModel.rawApps.collectAsState()

    var selectedRange by remember { mutableStateOf("Last 7 Days") }
    val rangeOptions = listOf("Today", "Yesterday", "Last 7 Days")

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val dayLabelFormat = remember { SimpleDateFormat("EEE", Locale.getDefault()) }

    val todayStr = remember { dateFormat.format(Calendar.getInstance().time) }
    val yesterdayStr = remember {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        dateFormat.format(cal.time)
    }

    // Weekly bar data
    val weeklyBarData = remember(historySnapshots) {
        val cal = Calendar.getInstance()
        (6 downTo 0).map { dayOffset ->
            val dayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -dayOffset) }
            val dateStr = dateFormat.format(dayCal.time)
            val label = dayLabelFormat.format(dayCal.time)

            val daySnapshots = historySnapshots.filter { it.dateString == dateStr }
            val totalMins = if (dayOffset == 0) {
                // Combine today's live minutes
                val liveSum = rawApps.sumOf { it.usedTodayMinutes }
                if (liveSum > 0) liveSum else daySnapshots.sumOf { it.usedMinutes }
            } else {
                daySnapshots.sumOf { it.usedMinutes }
            }
            val hasViolations = daySnapshots.any { it.violationsCount > 0 }

            DayUsageBarData(
                dayLabel = label,
                usedMinutes = totalMins,
                isToday = dayOffset == 0,
                hasViolation = hasViolations
            )
        }
    }

    // Filter snapshots based on selected range
    val displaySnapshots = when (selectedRange) {
        "Today" -> {
            // Map live apps to snapshot format
            rawApps.map {
                DailyUsageSnapshot(
                    dateString = todayStr,
                    packageName = it.packageName,
                    appName = it.appName,
                    isEntertainment = it.isEntertainment,
                    usedMinutes = it.usedTodayMinutes,
                    limitMinutes = it.dailyLimitMinutes,
                    extraMinutesUsed = it.extraTimeMinutesGrantedToday,
                    violationsCount = if (it.isLimitReached) 1 else 0
                )
            }.sortedByDescending { it.usedMinutes }
        }
        "Yesterday" -> {
            historySnapshots.filter { it.dateString == yesterdayStr }.sortedByDescending { it.usedMinutes }
        }
        else -> {
            // Aggregate Last 7 Days per package
            historySnapshots.groupBy { it.packageName }.map { (pkg, list) ->
                DailyUsageSnapshot(
                    dateString = "Last 7 Days",
                    packageName = pkg,
                    appName = list.firstOrNull()?.appName ?: pkg,
                    isEntertainment = list.any { it.isEntertainment },
                    usedMinutes = list.sumOf { it.usedMinutes },
                    limitMinutes = list.map { it.limitMinutes }.average().toInt(),
                    extraMinutesUsed = list.sumOf { it.extraMinutesUsed },
                    violationsCount = list.sumOf { it.violationsCount }
                )
            }.sortedByDescending { it.usedMinutes }
        }
    }

    val totalTimeUsed = displaySnapshots.sumOf { it.usedMinutes }
    val totalEntertainmentUsed = displaySnapshots.filter { it.isEntertainment }.sumOf { it.usedMinutes }
    val totalViolations = displaySnapshots.sumOf { it.violationsCount }
    val totalExtraTime = displaySnapshots.sumOf { it.extraMinutesUsed }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("history_screen"),
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
                        imageVector = Icons.Outlined.BarChart,
                        contentDescription = null,
                        tint = IndigoPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Usage History",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Track your daily focus habits and boundary enforcement",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Time Range Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rangeOptions.forEach { range ->
                    FilterChip(
                        selected = selectedRange == range,
                        onClick = { selectedRange = range },
                        label = { Text(range, fontSize = 13.sp) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("history_range_$range")
                    )
                }
            }
        }

        // Weekly Trends Bar Chart Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "7-Day Screen Time Pattern",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CoralLimit))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Limit exceeded", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    UsageBarChart(
                        data = weeklyBarData,
                        dailyLimitMinutes = 120
                    )
                }
            }
        }

        // Summary Metric Tiles
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Total Screen Time Tile
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.HourglassBottom, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Total Time", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val h = totalTimeUsed / 60
                        val m = totalTimeUsed % 60
                        Text(
                            text = if (h > 0) "${h}h ${m}m" else "${m}m",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Entertainment Time Tile
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Movie, contentDescription = null, tint = EntertainmentPurple, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Entertainment", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val h = totalEntertainmentUsed / 60
                        val m = totalEntertainmentUsed % 60
                        Text(
                            text = if (h > 0) "${h}h ${m}m" else "${m}m",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = EntertainmentPurple
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Violations Tile
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = CoralLimit, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Violations", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$totalViolations blocked",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (totalViolations > 0) CoralLimit else MintAccent
                        )
                    }
                }

                // Extra Time Granted Tile
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Timer, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Extra Time", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "+${totalExtraTime}m added",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AmberWarning
                        )
                    }
                }
            }
        }

        // App-by-App Breakdown List
        item {
            Text(
                text = "App-by-App Usage Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(displaySnapshots) { item ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectApp(item.packageName) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.appName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (item.isEntertainment) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = EntertainmentPurple.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "Fun",
                                            fontSize = 10.sp,
                                            color = EntertainmentPurple,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            if (item.extraMinutesUsed > 0) {
                                Text(
                                    text = "Includes +${item.extraMinutesUsed}m extra time",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AmberWarning
                                )
                            }
                        }

                        val h = item.usedMinutes / 60
                        val m = item.usedMinutes % 60
                        val timeStr = if (h > 0) "${h}h ${m}m" else "${m}m"
                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (totalTimeUsed > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val percentOfTotal = (item.usedMinutes.toFloat() / totalTimeUsed.toFloat()).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { percentOfTotal },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(CircleShape),
                            color = if (item.isEntertainment) EntertainmentPurple else IndigoPrimary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
