package com.example.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ForceStopApp
import com.example.data.model.AppUsageInfo
import com.example.data.model.DailyUsageSnapshot
import com.example.data.system.UsageStatsHelper
import com.example.ui.blocked.BlockedAppActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val repository = ForceStopApp.instance.repository

    val allSettings: StateFlow<Map<String, String>> = repository.allSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val rawApps: StateFlow<List<AppUsageInfo>> = repository.getEnrichedAppUsageList()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historySnapshots: StateFlow<List<DailyUsageSnapshot>> = repository.historySnapshots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _selectedAppPackage = MutableStateFlow<String?>(null)
    val selectedAppPackage: StateFlow<String?> = _selectedAppPackage.asStateFlow()

    private val _hasUsagePermission = MutableStateFlow(false)
    val hasUsagePermission: StateFlow<Boolean> = _hasUsagePermission.asStateFlow()

    private val _hasOverlayPermission = MutableStateFlow(false)
    val hasOverlayPermission: StateFlow<Boolean> = _hasOverlayPermission.asStateFlow()

    private val _hasNotificationPermission = MutableStateFlow(false)
    val hasNotificationPermission: StateFlow<Boolean> = _hasNotificationPermission.asStateFlow()

    // Filtered apps based on search query and category
    val filteredApps: StateFlow<List<AppUsageInfo>> = combine(
        rawApps,
        _searchQuery,
        _selectedFilter
    ) { apps, query, filter ->
        apps.filter { app ->
            val matchesQuery = query.isBlank() ||
                    app.appName.contains(query, ignoreCase = true) ||
                    app.packageName.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                "Restricted" -> app.isRestricted
                "Entertainment" -> app.isEntertainment
                "Blocked" -> app.isLimitReached
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dedicated YouTube entry
    val youTubeApp: StateFlow<AppUsageInfo?> = rawApps.combine(MutableStateFlow(Unit)) { apps, _ ->
        apps.find { it.packageName == "com.google.android.youtube" }
            ?: apps.find { it.appName.equals("YouTube", ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Aggregate dashboard totals
    val totalScreenTimeMinutes: StateFlow<Int> = rawApps.combine(MutableStateFlow(Unit)) { apps, _ ->
        apps.sumOf { it.usedTodayMinutes }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val entertainmentUsageMinutes: StateFlow<Int> = rawApps.combine(MutableStateFlow(Unit)) { apps, _ ->
        apps.filter { it.isEntertainment }.sumOf { it.usedTodayMinutes }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalEntertainmentLimitMinutes: StateFlow<Int> = rawApps.combine(MutableStateFlow(Unit)) { apps, _ ->
        val sum = apps.filter { it.isEntertainment && it.dailyLimitMinutes > 0 }.sumOf { it.dailyLimitMinutes }
        if (sum > 0) sum else 90 // Default reference budget
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 90)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun selectApp(packageName: String?) {
        _selectedAppPackage.value = packageName
    }

    fun updateAppLimit(
        packageName: String,
        dailyLimitMinutes: Int,
        isRestricted: Boolean = true,
        isEntertainment: Boolean = false
    ) {
        viewModelScope.launch {
            repository.updateAppLimit(packageName, dailyLimitMinutes, isRestricted, isEntertainment)
        }
    }

    fun addExtraTime(packageName: String, additionalMinutes: Int) {
        viewModelScope.launch {
            repository.addExtraTime(packageName, additionalMinutes)
        }
    }

    fun saveSetting(key: String, value: String) {
        viewModelScope.launch {
            repository.saveSetting(key, value)
        }
    }

    fun checkPermissions(context: Context) {
        _hasUsagePermission.value = UsageStatsHelper.hasUsageStatsPermission(context)
        _hasOverlayPermission.value = UsageStatsHelper.hasOverlayPermission(context)
        _hasNotificationPermission.value = UsageStatsHelper.hasNotificationPermission(context)
    }

    fun testLockScreen(context: Context, targetPackage: String = "com.google.android.youtube") {
        val app = rawApps.value.find { it.packageName == targetPackage }
        val intent = Intent(context, BlockedAppActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(BlockedAppActivity.EXTRA_PACKAGE_NAME, targetPackage)
            putExtra(BlockedAppActivity.EXTRA_APP_NAME, app?.appName ?: "Test App")
            putExtra(BlockedAppActivity.EXTRA_USED_MINUTES, app?.usedTodayMinutes ?: 30)
            putExtra(BlockedAppActivity.EXTRA_LIMIT_MINUTES, app?.dailyLimitMinutes ?: 30)
        }
        context.startActivity(intent)
    }
}
