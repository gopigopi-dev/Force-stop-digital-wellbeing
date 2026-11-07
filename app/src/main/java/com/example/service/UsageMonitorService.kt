package com.example.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.ForceStopApp
import com.example.data.system.UsageStatsHelper
import com.example.ui.blocked.BlockedAppActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class UsageMonitorService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    // Track per-app and category notifications so we don't spam the user
    private val notified80Percent = mutableSetOf<String>()
    private val notified100Percent = mutableSetOf<String>()
    private val warned10Min = mutableSetOf<String>()
    private val warned5Min = mutableSetOf<String>()
    private var notifiedCategory80Percent = false
    private var notifiedCategory100Percent = false

    override fun onCreate() {
        super.onCreate()
        startForeground(
            NotificationHelper.NOTIFICATION_ID_FOREGROUND,
            NotificationHelper.buildForegroundNotification(
                this,
                "Active • Real-time entertainment & screen time monitor"
            )
        )
        startMonitoringLoop()
    }

    private fun startMonitoringLoop() {
        serviceScope.launch {
            val repository = (application as ForceStopApp).repository

            while (isActive) {
                try {
                    val settings = repository.allSettings.firstOrNull() ?: emptyMap()
                    val notif80Enabled = settings["notif_80_percent"] != "false"
                    val notif100Enabled = settings["notif_limit_reached"] != "false"
                    val notif10MinEnabled = settings["notif_10min"] != "false"
                    val notif5MinEnabled = settings["notif_5min"] != "false"

                    val allApps = repository.getEnrichedAppUsageList().firstOrNull() ?: emptyList()

                    // 1. Check aggregate Entertainment category usage vs total entertainment budget
                    val entertainmentApps = allApps.filter { it.isEntertainment }
                    val totalEntUsed = entertainmentApps.sumOf { it.usedTodayMinutes }
                    val totalEntLimit = entertainmentApps.filter { it.dailyLimitMinutes > 0 }
                        .sumOf { it.totalAllowedMinutes }

                    if (totalEntLimit > 0) {
                        val entRatio = totalEntUsed.toFloat() / totalEntLimit.toFloat()
                        val entPercent = (entRatio * 100).toInt()

                        // Update ongoing foreground notification with live entertainment percentage
                        NotificationHelper.updateForegroundNotification(
                            this@UsageMonitorService,
                            "Entertainment: ${totalEntUsed}m / ${totalEntLimit}m ($entPercent% used today)"
                        )

                        if (entRatio >= 1.0f && !notifiedCategory100Percent && notif100Enabled) {
                            notifiedCategory100Percent = true
                            NotificationHelper.show100PercentLimitNotification(
                                context = this@UsageMonitorService,
                                targetName = "Entertainment Category",
                                limitMinutes = totalEntLimit,
                                isCategoryBudget = true
                            )
                        } else if (entRatio >= 0.8f && entRatio < 1.0f && !notifiedCategory80Percent && notif80Enabled) {
                            notifiedCategory80Percent = true
                            NotificationHelper.show80PercentLimitNotification(
                                context = this@UsageMonitorService,
                                targetName = "Entertainment Category",
                                usedMinutes = totalEntUsed,
                                limitMinutes = totalEntLimit,
                                isCategoryBudget = true
                            )
                        }
                    }

                    // 2. Check all restricted / entertainment apps for 80% and 100% thresholds
                    for (app in allApps) {
                        if ((app.isEntertainment || app.isRestricted) && app.totalAllowedMinutes > 0) {
                            val ratio = app.usedTodayMinutes.toFloat() / app.totalAllowedMinutes.toFloat()
                            if (ratio >= 1.0f && !notified100Percent.contains(app.packageName) && notif100Enabled) {
                                notified100Percent.add(app.packageName)
                                NotificationHelper.show100PercentLimitNotification(
                                    context = this@UsageMonitorService,
                                    targetName = app.appName,
                                    limitMinutes = app.totalAllowedMinutes,
                                    isCategoryBudget = false
                                )
                            } else if (ratio >= 0.8f && ratio < 1.0f && !notified80Percent.contains(app.packageName) && notif80Enabled) {
                                notified80Percent.add(app.packageName)
                                NotificationHelper.show80PercentLimitNotification(
                                    context = this@UsageMonitorService,
                                    targetName = app.appName,
                                    usedMinutes = app.usedTodayMinutes,
                                    limitMinutes = app.totalAllowedMinutes,
                                    isCategoryBudget = false
                                )
                            }
                        }
                    }

                    // 3. Real-time Foreground App Interception & Warnings
                    val foregroundPackage = UsageStatsHelper.queryCurrentForegroundApp(this@UsageMonitorService)

                    if (foregroundPackage != null && foregroundPackage != packageName) {
                        val appDetail = allApps.find { it.packageName == foregroundPackage }
                            ?: repository.getAppDetail(foregroundPackage).firstOrNull()

                        if (appDetail != null && appDetail.isRestricted && appDetail.totalAllowedMinutes > 0) {
                            if (appDetail.isLimitReached) {
                                // Trigger 100% notification if not already sent
                                if (!notified100Percent.contains(foregroundPackage) && notif100Enabled) {
                                    notified100Percent.add(foregroundPackage)
                                    NotificationHelper.show100PercentLimitNotification(
                                        context = this@UsageMonitorService,
                                        targetName = appDetail.appName,
                                        limitMinutes = appDetail.totalAllowedMinutes,
                                        isCategoryBudget = false
                                    )
                                }

                                // Block the app by launching BlockedAppActivity
                                val blockIntent = Intent(this@UsageMonitorService, BlockedAppActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                                    putExtra(BlockedAppActivity.EXTRA_PACKAGE_NAME, foregroundPackage)
                                    putExtra(BlockedAppActivity.EXTRA_APP_NAME, appDetail.appName)
                                    putExtra(BlockedAppActivity.EXTRA_USED_MINUTES, appDetail.usedTodayMinutes)
                                    putExtra(BlockedAppActivity.EXTRA_LIMIT_MINUTES, appDetail.totalAllowedMinutes)
                                }
                                startActivity(blockIntent)
                            } else {
                                // Check 80% threshold on active foreground app
                                val ratio = appDetail.usedTodayMinutes.toFloat() / appDetail.totalAllowedMinutes.toFloat()
                                if (ratio >= 0.8f && !notified80Percent.contains(foregroundPackage) && notif80Enabled) {
                                    notified80Percent.add(foregroundPackage)
                                    NotificationHelper.show80PercentLimitNotification(
                                        context = this@UsageMonitorService,
                                        targetName = appDetail.appName,
                                        usedMinutes = appDetail.usedTodayMinutes,
                                        limitMinutes = appDetail.totalAllowedMinutes,
                                        isCategoryBudget = false
                                    )
                                }

                                // Check remaining minute warnings (10 min or 5 min)
                                val remaining = appDetail.remainingMinutes
                                if (remaining in 6..10 && !warned10Min.contains(foregroundPackage) && notif10MinEnabled) {
                                    warned10Min.add(foregroundPackage)
                                    NotificationHelper.showLimitWarningNotification(
                                        this@UsageMonitorService,
                                        appDetail.appName,
                                        remaining
                                    )
                                } else if (remaining in 1..5 && !warned5Min.contains(foregroundPackage) && notif5MinEnabled) {
                                    warned5Min.add(foregroundPackage)
                                    NotificationHelper.showLimitWarningNotification(
                                        this@UsageMonitorService,
                                        appDetail.appName,
                                        remaining
                                    )
                                }
                            }
                        }
                    }
                } catch (_: Exception) {
                    // Ignore transient exceptions in background loop
                }

                delay(3000) // Poll every 3 seconds for real-time enforcement
            }
        }
    }

    override fun onDestroy() {
        serviceJob.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
