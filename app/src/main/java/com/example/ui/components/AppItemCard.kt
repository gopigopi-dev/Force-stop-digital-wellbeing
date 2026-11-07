package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.data.model.AppUsageInfo
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralLimit
import com.example.ui.theme.EntertainmentPurple
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.MintAccent

@Composable
fun AppItemCard(
    app: AppUsageInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("app_item_${app.packageName.replace('.', '_')}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Icon or stylized category avatar
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (app.isEntertainment) EntertainmentPurple.copy(alpha = 0.15f)
                            else IndigoPrimary.copy(alpha = 0.12f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (app.icon != null) {
                        Image(
                            bitmap = app.icon.toBitmap(96, 96).asImageBitmap(),
                            contentDescription = app.appName,
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        Icon(
                            imageVector = if (app.isEntertainment) Icons.Outlined.Movie else Icons.Outlined.Smartphone,
                            contentDescription = null,
                            tint = if (app.isEntertainment) EntertainmentPurple else IndigoPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // App Name and Category
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = app.appName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        if (app.isEntertainment) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = EntertainmentPurple.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Fun",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = EntertainmentPurple,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (app.dailyLimitMinutes > 0) {
                            "Limit: ${app.formattedLimitTime()}"
                        } else {
                            "No daily limit set"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status Tag or Time
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = app.formattedUsedTime(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            app.isLimitReached -> CoralLimit
                            app.isWarningState -> AmberWarning
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    if (app.isLimitReached) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CoralLimit.copy(alpha = 0.15f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = null,
                                    tint = CoralLimit,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Blocked",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CoralLimit
                                )
                            }
                        }
                    } else if (app.isWarningState) {
                        Text(
                            text = "${app.remainingMinutes}m left",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AmberWarning
                        )
                    } else if (app.dailyLimitMinutes > 0) {
                        Text(
                            text = "${app.remainingMinutes}m left",
                            fontSize = 11.sp,
                            color = MintAccent
                        )
                    }
                }
            }

            // Usage progress bar if restricted
            if (app.dailyLimitMinutes > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { app.usagePercentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = when {
                        app.isLimitReached -> CoralLimit
                        app.isWarningState -> AmberWarning
                        else -> IndigoPrimary
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}
