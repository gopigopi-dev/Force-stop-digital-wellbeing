package com.example

import android.app.Application
import android.content.Intent
import android.os.Build
import com.example.data.db.ForceStopDatabase
import com.example.data.repository.ForceStopRepository
import com.example.service.NotificationHelper
import com.example.service.UsageMonitorService

class ForceStopApp : Application() {

    lateinit var database: ForceStopDatabase
        private set

    lateinit var repository: ForceStopRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = ForceStopDatabase.getDatabase(this)
        repository = ForceStopRepository(
            context = this,
            appLimitDao = database.appLimitDao(),
            dailyUsageDao = database.dailyUsageDao(),
            appSettingDao = database.appSettingDao()
        )

        NotificationHelper.createNotificationChannels(this)
    }

    fun startMonitorServiceIfPermitted() {
        try {
            val serviceIntent = Intent(this, UsageMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } catch (_: Exception) {
            // Android 14+ background launch restrictions or permission pending
        }
    }

    companion object {
        lateinit var instance: ForceStopApp
            private set
    }
}
