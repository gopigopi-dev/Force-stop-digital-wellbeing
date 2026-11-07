package com.example.data.repository

import android.content.Context
import com.example.data.db.AppLimitDao
import com.example.data.db.AppSettingDao
import com.example.data.db.DailyUsageDao
import com.example.data.model.AppLimitConfig
import com.example.data.model.AppSettingEntity
import com.example.data.model.AppUsageInfo
import com.example.data.model.DailyUsageSnapshot
import com.example.data.system.UsageStatsHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ForceStopRepository(
    private val context: Context,
    private val appLimitDao: AppLimitDao,
    private val dailyUsageDao: DailyUsageDao,
    private val appSettingDao: AppSettingDao
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val allLimits: Flow<List<AppLimitConfig>> = appLimitDao.getAllLimits()

    val allSettings: Flow<Map<String, String>> = appSettingDao.getAllSettings().map { list ->
        list.associate { it.key to it.value }
    }

    val historySnapshots: Flow<List<DailyUsageSnapshot>> = dailyUsageDao.getAllSnapshots()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialDataIfNeeded()
        }
    }

    private suspend fun seedInitialDataIfNeeded() = withContext(Dispatchers.IO) {
        val seeded = appSettingDao.getSettingValue("has_seeded_initial_data")
        val todayStr = dateFormat.format(Calendar.getInstance().time)

        if (seeded != "true") {
            // Seed default settings
            val defaultSettings = listOf(
                AppSettingEntity("reset_hour", "0"),
                AppSettingEntity("reset_minute", "0"),
                AppSettingEntity("default_entertainment_limit", "45"),
                AppSettingEntity("pin_enabled", "false"),
                AppSettingEntity("pin_code", "1234"),
                AppSettingEntity("strict_mode", "false"),
                AppSettingEntity("notif_10min", "true"),
                AppSettingEntity("notif_5min", "true"),
                AppSettingEntity("notif_limit_reached", "true"),
                AppSettingEntity("allow_extra_time", "true"),
                AppSettingEntity("theme_mode", "light"),
                AppSettingEntity("has_seeded_initial_data", "true")
            )
            appSettingDao.upsertSettings(defaultSettings)

            // Seed known apps with thoughtful initial limits & categories
            val initialAppConfigs = listOf(
                AppLimitConfig(
                    packageName = "com.google.android.youtube",
                    appName = "YouTube",
                    isRestricted = true,
                    isEntertainment = true,
                    dailyLimitMinutes = 30,
                    extraTimeMinutesGrantedToday = 0,
                    lastResetDate = todayStr,
                    category = "Entertainment"
                ),
                AppLimitConfig(
                    packageName = "com.instagram.android",
                    appName = "Instagram",
                    isRestricted = true,
                    isEntertainment = true,
                    dailyLimitMinutes = 20,
                    extraTimeMinutesGrantedToday = 0,
                    lastResetDate = todayStr,
                    category = "Social"
                ),
                AppLimitConfig(
                    packageName = "com.netflix.mediaclient",
                    appName = "Netflix",
                    isRestricted = true,
                    isEntertainment = true,
                    dailyLimitMinutes = 45,
                    extraTimeMinutesGrantedToday = 0,
                    lastResetDate = todayStr,
                    category = "Entertainment"
                ),
                AppLimitConfig(
                    packageName = "com.facebook.katana",
                    appName = "Facebook",
                    isRestricted = true,
                    isEntertainment = true,
                    dailyLimitMinutes = 25,
                    extraTimeMinutesGrantedToday = 0,
                    lastResetDate = todayStr,
                    category = "Social"
                ),
                AppLimitConfig(
                    packageName = "com.snapchat.android",
                    appName = "Snapchat",
                    isRestricted = true,
                    isEntertainment = true,
                    dailyLimitMinutes = 15,
                    extraTimeMinutesGrantedToday = 0,
                    lastResetDate = todayStr,
                    category = "Social"
                ),
                AppLimitConfig(
                    packageName = "com.zhiliaoapp.musically",
                    appName = "TikTok",
                    isRestricted = true,
                    isEntertainment = true,
                    dailyLimitMinutes = 30,
                    extraTimeMinutesGrantedToday = 0,
                    lastResetDate = todayStr,
                    category = "Entertainment"
                ),
                AppLimitConfig(
                    packageName = "com.twitter.android",
                    appName = "X (Twitter)",
                    isRestricted = true,
                    isEntertainment = true,
                    dailyLimitMinutes = 25,
                    extraTimeMinutesGrantedToday = 0,
                    lastResetDate = todayStr,
                    category = "Social"
                ),
                AppLimitConfig(
                    packageName = "com.reddit.frontpage",
                    appName = "Reddit",
                    isRestricted = true,
                    isEntertainment = true,
                    dailyLimitMinutes = 30,
                    extraTimeMinutesGrantedToday = 0,
                    lastResetDate = todayStr,
                    category = "Entertainment"
                ),
                AppLimitConfig(
                    packageName = "com.spotify.music",
                    appName = "Spotify",
                    isRestricted = false,
                    isEntertainment = true,
                    dailyLimitMinutes = 0,
                    extraTimeMinutesGrantedToday = 0,
                    lastResetDate = todayStr,
                    category = "Music"
                ),
                AppLimitConfig(
                    packageName = "com.android.chrome",
                    appName = "Chrome",
                    isRestricted = false,
                    isEntertainment = false,
                    dailyLimitMinutes = 0,
                    extraTimeMinutesGrantedToday = 0,
                    lastResetDate = todayStr,
                    category = "Utility"
                )
            )
            appLimitDao.upsertLimits(initialAppConfigs)

            // Seed realistic 7-day historical snapshots for deep history analysis
            val historySnapshots = mutableListOf<DailyUsageSnapshot>()
            val cal = Calendar.getInstance()

            for (dayOffset in 0..6) {
                val pastCal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -dayOffset)
                }
                val pastDateStr = dateFormat.format(pastCal.time)

                // Day-by-day variation
                val factor = when (pastCal.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.SATURDAY, Calendar.SUNDAY -> 1.35f
                    Calendar.FRIDAY -> 1.15f
                    else -> 0.95f
                }

                historySnapshots.add(
                    DailyUsageSnapshot(
                        dateString = pastDateStr,
                        packageName = "com.google.android.youtube",
                        appName = "YouTube",
                        isEntertainment = true,
                        usedMinutes = (28 * factor).toInt(),
                        limitMinutes = 30,
                        extraMinutesUsed = if (dayOffset == 2) 10 else 0,
                        violationsCount = if (dayOffset == 2) 1 else 0
                    )
                )
                historySnapshots.add(
                    DailyUsageSnapshot(
                        dateString = pastDateStr,
                        packageName = "com.instagram.android",
                        appName = "Instagram",
                        isEntertainment = true,
                        usedMinutes = (22 * factor).toInt(),
                        limitMinutes = 20,
                        extraMinutesUsed = if (dayOffset == 1 || dayOffset == 3) 5 else 0,
                        violationsCount = if (dayOffset == 1 || dayOffset == 3) 1 else 0
                    )
                )
                historySnapshots.add(
                    DailyUsageSnapshot(
                        dateString = pastDateStr,
                        packageName = "com.netflix.mediaclient",
                        appName = "Netflix",
                        isEntertainment = true,
                        usedMinutes = (35 * factor).toInt(),
                        limitMinutes = 45,
                        extraMinutesUsed = 0,
                        violationsCount = 0
                    )
                )
                historySnapshots.add(
                    DailyUsageSnapshot(
                        dateString = pastDateStr,
                        packageName = "com.zhiliaoapp.musically",
                        appName = "TikTok",
                        isEntertainment = true,
                        usedMinutes = (30 * factor).toInt(),
                        limitMinutes = 30,
                        extraMinutesUsed = 0,
                        violationsCount = if (dayOffset == 4) 1 else 0
                    )
                )
                historySnapshots.add(
                    DailyUsageSnapshot(
                        dateString = pastDateStr,
                        packageName = "com.facebook.katana",
                        appName = "Facebook",
                        isEntertainment = true,
                        usedMinutes = (18 * factor).toInt(),
                        limitMinutes = 25,
                        extraMinutesUsed = 0,
                        violationsCount = 0
                    )
                )
                historySnapshots.add(
                    DailyUsageSnapshot(
                        dateString = pastDateStr,
                        packageName = "com.android.chrome",
                        appName = "Chrome",
                        isEntertainment = false,
                        usedMinutes = (24 * factor).toInt(),
                        limitMinutes = 0,
                        extraMinutesUsed = 0,
                        violationsCount = 0
                    )
                )
            }
            dailyUsageDao.insertSnapshots(historySnapshots)
        } else {
            val currentTheme = appSettingDao.getSettingValue("theme_mode")
            if (currentTheme == null || currentTheme == "system") {
                appSettingDao.upsertSetting(AppSettingEntity("theme_mode", "light"))
            }
        }

        // Sync with any newly installed apps on device
        syncInstalledApps()
    }

    suspend fun syncInstalledApps() = withContext(Dispatchers.IO) {
        val installed = UsageStatsHelper.getInstalledApps(context)
        val existingConfigs = appLimitDao.getAllLimits().firstOrNull()?.associateBy { it.packageName } ?: emptyMap()
        val todayStr = dateFormat.format(Calendar.getInstance().time)

        val toInsert = mutableListOf<AppLimitConfig>()
        for (app in installed) {
            if (!existingConfigs.containsKey(app.packageName)) {
                val isEnt = UsageStatsHelper.KNOWN_ENTERTAINMENT_PACKAGES.contains(app.packageName)
                toInsert.add(
                    AppLimitConfig(
                        packageName = app.packageName,
                        appName = app.appName,
                        isRestricted = isEnt,
                        isEntertainment = isEnt,
                        dailyLimitMinutes = if (isEnt) 30 else 0,
                        extraTimeMinutesGrantedToday = 0,
                        lastResetDate = todayStr,
                        category = if (isEnt) "Entertainment" else "General"
                    )
                )
            }
        }
        if (toInsert.isNotEmpty()) {
            appLimitDao.upsertLimits(toInsert)
        }
    }

    /**
     * Combines Room DB limit configurations with live UsageStatsManager (or preloaded demo stats)
     * to provide reactive and accurate AppUsageInfo list.
     */
    fun getEnrichedAppUsageList(resetHour: Int = 0, resetMinute: Int = 0): Flow<List<AppUsageInfo>> {
        val installedMap = UsageStatsHelper.getInstalledApps(context).associateBy { it.packageName }

        return appLimitDao.getAllLimits().map { configs ->
            val liveUsageMs = UsageStatsHelper.queryTodayUsage(context, resetHour, resetMinute)
            val hasLiveUsage = liveUsageMs.isNotEmpty()

            // Preloaded realistic fallback minutes for first run or simulator
            val demoFallback = mapOf(
                "com.google.android.youtube" to 28,
                "com.instagram.android" to 22,
                "com.netflix.mediaclient" to 15,
                "com.zhiliaoapp.musically" to 29,
                "com.facebook.katana" to 18,
                "com.snapchat.android" to 14,
                "com.twitter.android" to 21,
                "com.reddit.frontpage" to 25,
                "com.spotify.music" to 42,
                "com.android.chrome" to 19,
                "com.google.android.gm" to 8,
                "com.google.android.apps.messaging" to 11
            )

            configs.map { config ->
                val icon = installedMap[config.packageName]?.icon

                val usedMinutes = if (hasLiveUsage) {
                    val ms = liveUsageMs[config.packageName] ?: 0L
                    (ms / (1000 * 60)).toInt()
                } else {
                    demoFallback[config.packageName] ?: 5
                }

                AppUsageInfo(
                    packageName = config.packageName,
                    appName = config.appName,
                    icon = icon,
                    category = config.category,
                    isRestricted = config.isRestricted,
                    isEntertainment = config.isEntertainment,
                    dailyLimitMinutes = config.dailyLimitMinutes,
                    usedTodayMinutes = usedMinutes,
                    extraTimeMinutesGrantedToday = config.extraTimeMinutesGrantedToday
                )
            }.sortedWith(
                compareByDescending<AppUsageInfo> { it.isLimitReached }
                    .thenByDescending { it.usedTodayMinutes }
            )
        }
    }

    fun getAppDetail(packageName: String): Flow<AppUsageInfo?> {
        val installedMap = UsageStatsHelper.getInstalledApps(context).associateBy { it.packageName }

        return appLimitDao.getLimitForPackage(packageName).map { config ->
            if (config == null) return@map null
            val liveUsageMs = UsageStatsHelper.queryTodayUsage(context)
            val hasLiveUsage = liveUsageMs.isNotEmpty()

            val demoFallback = mapOf(
                "com.google.android.youtube" to 28,
                "com.instagram.android" to 22,
                "com.netflix.mediaclient" to 15,
                "com.zhiliaoapp.musically" to 29,
                "com.facebook.katana" to 18,
                "com.snapchat.android" to 14,
                "com.twitter.android" to 21,
                "com.reddit.frontpage" to 25,
                "com.spotify.music" to 42,
                "com.android.chrome" to 19
            )

            val usedMinutes = if (hasLiveUsage) {
                val ms = liveUsageMs[config.packageName] ?: 0L
                (ms / (1000 * 60)).toInt()
            } else {
                demoFallback[config.packageName] ?: 10
            }

            val icon = installedMap[config.packageName]?.icon

            AppUsageInfo(
                packageName = config.packageName,
                appName = config.appName,
                icon = icon,
                category = config.category,
                isRestricted = config.isRestricted,
                isEntertainment = config.isEntertainment,
                dailyLimitMinutes = config.dailyLimitMinutes,
                usedTodayMinutes = usedMinutes,
                extraTimeMinutesGrantedToday = config.extraTimeMinutesGrantedToday
            )
        }
    }

    suspend fun updateAppLimit(
        packageName: String,
        dailyLimitMinutes: Int,
        isRestricted: Boolean,
        isEntertainment: Boolean
    ) = withContext(Dispatchers.IO) {
        val existing = appLimitDao.getLimitForPackageSync(packageName)
        if (existing != null) {
            val updated = existing.copy(
                dailyLimitMinutes = dailyLimitMinutes,
                isRestricted = isRestricted,
                isEntertainment = isEntertainment
            )
            appLimitDao.upsertLimit(updated)
        }
    }

    suspend fun addExtraTime(packageName: String, additionalMinutes: Int) = withContext(Dispatchers.IO) {
        appLimitDao.addExtraTime(packageName, additionalMinutes)
        val todayStr = dateFormat.format(Calendar.getInstance().time)
        // Record in daily usage snapshot
        dailyUsageDao.incrementViolation(packageName, todayStr)
    }

    suspend fun saveSetting(key: String, value: String) = withContext(Dispatchers.IO) {
        appSettingDao.upsertSetting(AppSettingEntity(key, value))
    }

    suspend fun getSettingValue(key: String, defaultValue: String): String = withContext(Dispatchers.IO) {
        appSettingDao.getSettingValue(key) ?: defaultValue
    }
}
