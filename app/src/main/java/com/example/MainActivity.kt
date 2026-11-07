package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.navigation.Screen
import com.example.ui.screens.AppDetailScreen
import com.example.ui.screens.AppsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EntertainmentScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.PermissionsSetupScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.ForceStopTheme
import com.example.ui.theme.IndigoPrimary
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Start background usage enforcement monitor service
        (application as ForceStopApp).startMonitorServiceIfPermitted()

        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val settings by mainViewModel.allSettings.collectAsState()

            val themeMode = settings["theme_mode"] ?: "light"
            val isDark = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> false
            }

            LaunchedEffect(Unit) {
                mainViewModel.checkPermissions(this@MainActivity)
            }

            ForceStopTheme(darkTheme = isDark) {
                MainAppScaffold(viewModel = mainViewModel)
            }
        }
    }
}

@Composable
fun MainAppScaffold(viewModel: MainViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    val selectedAppPackage by viewModel.selectedAppPackage.collectAsState()

    val navScreens = listOf(
        Screen.Dashboard,
        Screen.Apps,
        Screen.Entertainment,
        Screen.History,
        Screen.Settings
    )

    // Handle system back navigation if inside detail screen or permissions guide screen
    if (selectedAppPackage != null) {
        BackHandler {
            viewModel.selectApp(null)
        }
    } else if (currentScreen == Screen.PermissionsSetup) {
        BackHandler {
            currentScreen = Screen.Dashboard
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (selectedAppPackage == null && currentScreen != Screen.PermissionsSetup) {
                NavigationBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("main_bottom_nav"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    navScreens.forEach { screen ->
                        val isSelected = currentScreen == screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                currentScreen = screen
                                viewModel.selectApp(null)
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = IndigoPrimary.copy(alpha = 0.15f),
                                selectedIconColor = IndigoPrimary,
                                selectedTextColor = IndigoPrimary
                            ),
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = selectedAppPackage to currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "screen_transition"
        ) { (selectedPkg, screen) ->
            if (selectedPkg != null) {
                AppDetailScreen(
                    packageName = selectedPkg,
                    viewModel = viewModel,
                    onBack = { viewModel.selectApp(null) }
                )
            } else {
                when (screen) {
                    Screen.Dashboard -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToApps = { currentScreen = Screen.Apps },
                        onNavigateToEntertainment = { currentScreen = Screen.Entertainment },
                        onNavigateToPermissionsSetup = { currentScreen = Screen.PermissionsSetup },
                        onSelectApp = { pkg -> viewModel.selectApp(pkg) }
                    )
                    Screen.Apps -> AppsScreen(
                        viewModel = viewModel,
                        onSelectApp = { pkg -> viewModel.selectApp(pkg) }
                    )
                    Screen.Entertainment -> EntertainmentScreen(
                        viewModel = viewModel,
                        onSelectApp = { pkg -> viewModel.selectApp(pkg) },
                        onNavigateToApps = { currentScreen = Screen.Apps }
                    )
                    Screen.History -> HistoryScreen(
                        viewModel = viewModel,
                        onSelectApp = { pkg -> viewModel.selectApp(pkg) }
                    )
                    Screen.Settings -> SettingsScreen(
                        viewModel = viewModel,
                        onNavigateToPermissionsSetup = { currentScreen = Screen.PermissionsSetup }
                    )
                    Screen.PermissionsSetup -> PermissionsSetupScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = Screen.Dashboard }
                    )
                    Screen.AppDetail -> Unit
                }
            }
        }
    }
}
