package com.hoaxdetector.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.*
import androidx.navigation.compose.*
import com.hoaxdetector.app.data.local.UserSessionManager
import com.hoaxdetector.app.ui.screens.auth.AuthScreen
import com.hoaxdetector.app.ui.screens.check.CheckScreen
import com.hoaxdetector.app.ui.screens.education.EducationScreen
import com.hoaxdetector.app.ui.screens.history.HistoryScreen
import com.hoaxdetector.app.ui.screens.home.HomeScreen
import com.hoaxdetector.app.ui.screens.result.ResultScreen
import com.hoaxdetector.app.ui.screens.settings.SettingsScreen
import com.hoaxdetector.app.ui.theme.*

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Auth : Screen("auth", "Masuk", Icons.Default.Lock)

    // Sesuai desain Figma Image 1-5:
    // Tab 1: Beranda dengan ikon kaca pembesar (Search)
    object Home : Screen("home", "Beranda", Icons.Default.Search)
    // Tab 2: Periksa dengan ikon perisai verified (VerifiedUser)
    object Check : Screen("check", "Periksa", Icons.Default.VerifiedUser) {
        const val routePattern = "check?tab={tab}"
        fun createRoute(tab: Int = 0) = "check?tab=$tab"
    }
    // Tab 3: Riwayat dengan ikon history (History)
    object History : Screen("history", "Riwayat", Icons.Default.History)
    // Tab 4: Edukasi dengan ikon buku (MenuBook)
    object Education : Screen("education", "Edukasi", Icons.AutoMirrored.Filled.MenuBook)

    object Settings : Screen("settings", "Pengaturan", Icons.Default.Settings)
    object Result : Screen("result/{historyId}", "Hasil", Icons.AutoMirrored.Filled.Article) {
        fun createRoute(historyId: Long) = "result/$historyId"
    }
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Check,
    Screen.History,
    Screen.Education
)

@Composable
fun HoaxDetectorNavGraph() {
    val context = LocalContext.current
    val sessionManager = remember { UserSessionManager(context) }
    val startDestination = if (sessionManager.isSessionActive()) Screen.Home.route else Screen.Auth.route

    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination
    val currentRoute = currentDestination?.route

    val showBottomBar = currentRoute != Screen.Auth.route && (currentRoute in listOf(
        Screen.Home.route,
        Screen.Check.routePattern,
        Screen.Check.route,
        Screen.History.route,
        Screen.Education.route
    ) || currentRoute?.startsWith("check") == true)

    Scaffold(
        containerColor = NavyDeep,
        bottomBar = {
            if (showBottomBar) {
                HoaxDetectorBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues),
            enterTransition = {
                fadeIn(animationSpec = tween(200)) + slideInHorizontally(
                    animationSpec = tween(200),
                    initialOffsetX = { it / 3 }
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(200))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(200))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(200)) + slideOutHorizontally(
                    animationSpec = tween(200),
                    targetOffsetX = { it / 3 }
                )
            }
        ) {
            // Layar Auth (Login / Daftar / Gunakan Tanpa Akun)
            composable(Screen.Auth.route) {
                AuthScreen(
                    onAuthSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Auth.route) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToCheck = { tab ->
                        navController.navigate(Screen.Check.createRoute(tab)) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToHistory = {
                        navController.navigate(Screen.History.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToEducation = {
                        navController.navigate(Screen.Education.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onNavigateToResult = { historyId ->
                        navController.navigate(Screen.Result.createRoute(historyId))
                    }
                )
            }

            composable(
                route = Screen.Check.routePattern,
                arguments = listOf(navArgument("tab") {
                    type = NavType.IntType
                    defaultValue = 0
                })
            ) { backStackEntry ->
                val tab = backStackEntry.arguments?.getInt("tab") ?: 0
                CheckScreen(
                    initialTab = tab,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToResult = { historyId ->
                        navController.navigate(Screen.Result.createRoute(historyId))
                    }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    onNavigateToResult = { historyId ->
                        navController.navigate(Screen.Result.createRoute(historyId))
                    }
                )
            }

            composable(Screen.Education.route) {
                EducationScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCheck = {
                        navController.navigate(Screen.Check.createRoute(0)) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAuth = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(Screen.Home.route) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable(
                route = Screen.Result.route,
                arguments = listOf(navArgument("historyId") { type = NavType.LongType })
            ) { backStackEntry ->
                val historyId = backStackEntry.arguments?.getLong("historyId") ?: 0L
                ResultScreen(
                    historyId = historyId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun HoaxDetectorBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = NavyMedium,
        contentColor = TextPrimary,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { screen ->
            val selected = if (screen == Screen.Check) {
                currentRoute?.startsWith("check") == true
            } else {
                currentRoute == screen.route
            }

            val targetRoute = if (screen == Screen.Check) {
                Screen.Check.createRoute(0)
            } else {
                screen.route
            }

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.label
                    )
                },
                label = {
                    Text(
                        text = screen.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = selected,
                onClick = { onNavigate(targetRoute) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ElectricBlue,
                    selectedTextColor = ElectricBlue,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = ElectricBlueDim
                )
            )
        }
    }
}
