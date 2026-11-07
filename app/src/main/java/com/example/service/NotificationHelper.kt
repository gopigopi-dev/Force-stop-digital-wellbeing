package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

object NotificationHelper {

    const val CHANNEL_MONITOR = "force_stop_monitor"
    const val CHANNEL_ALERTS = "force_stop_alerts"

    const val NOTIFICATION_ID_FOREGROUND = 1001
    const val NOTIFICATION_ID_ALERT = 2001

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val monitorChannel = NotificationChannel(
                CHANNEL_MONITOR,
                "Digital Wellbeing Protection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors app limits in real-time and keeps you mindful of screen time"
                setShowBadge(false)
            }

            val alertsChannel = NotificationChannel(
                CHANNEL_ALERTS,
                "80% & 100% Limit Warnings",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when reaching 80% or 100% of daily entertainment and app limits"
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(monitorChannel)
            notificationManager.createNotificationChannel(alertsChannel)
        }
    }

    fun buildForegroundNotification(
        context: Context,
        statusText: String = "Active • Real-time screen time & entertainment monitor"
    ): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_MONITOR)
            .setContentTitle("Force Stop • Monitoring Usage")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun updateForegroundNotification(context: Context, statusText: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(
            NOTIFICATION_ID_FOREGROUND,
            buildForegroundNotification(context, statusText)
        )
    }

    /**
     * Triggered when an app or total entertainment budget reaches 80% of its daily time limit.
     */
    fun show80PercentLimitNotification(
        context: Context,
        targetName: String,
        usedMinutes: Int,
        limitMinutes: Int,
        isCategoryBudget: Boolean = false
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            targetName.hashCode() + 80,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val remaining = (limitMinutes - usedMinutes).coerceAtLeast(1)
        val title = if (isCategoryBudget) {
            "80% Entertainment Budget Used"
        } else {
            "80% Daily Limit Reached: $targetName"
        }
        val message = if (isCategoryBudget) {
            "You've used ${usedMinutes}m of your ${limitMinutes}m Entertainment allowance (${remaining}m left)."
        } else {
            "You've used ${usedMinutes}m of ${limitMinutes}m on $targetName today (${remaining}m remaining before Force Stop)."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(targetName.hashCode() + 80, notification)
    }

    /**
     * Triggered when an app or total entertainment budget reaches 100% of its daily time limit.
     */
    fun show100PercentLimitNotification(
        context: Context,
        targetName: String,
        limitMinutes: Int,
        isCategoryBudget: Boolean = false
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            targetName.hashCode() + 100,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isCategoryBudget) {
            "100% Entertainment Limit Reached"
        } else {
            "100% Daily Limit Reached: $targetName"
        }
        val message = if (isCategoryBudget) {
            "Your ${limitMinutes}m daily Entertainment allowance is completely used. Entertainment apps are now restricted."
        } else {
            "$targetName has reached 100% of its ${limitMinutes}m daily limit and is now Force Stopped until the next daily reset."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(targetName.hashCode() + 100, notification)
    }

    fun showLimitWarningNotification(
        context: Context,
        appName: String,
        minutesRemaining: Int
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            appName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setContentTitle("Screen Time Alert: $appName")
            .setContentText("Only $minutesRemaining minutes remaining for $appName today.")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(appName.hashCode() + 10, notification)
    }

    fun showLimitReachedNotification(
        context: Context,
        appName: String,
        limitMinutes: Int
    ) {
        show100PercentLimitNotification(context, appName, limitMinutes, isCategoryBudget = false)
    }
}
