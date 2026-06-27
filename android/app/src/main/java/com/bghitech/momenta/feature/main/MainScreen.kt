package com.bghitech.momenta.feature.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bghitech.momenta.core.design.MomentaBottomBar
import com.bghitech.momenta.core.design.MomentaBackground
import com.bghitech.momenta.core.navigation.NavRoutes
import com.bghitech.momenta.feature.circle.CircleScreen
import com.bghitech.momenta.feature.feed.FeedScreen
import com.bghitech.momenta.feature.profile.ProfileScreen
import com.bghitech.momenta.feature.search.SearchScreen
import com.bghitech.momenta.feature.today.TodayScreen

@Composable
fun MainScreen(
    onNavigateToCamera: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    startRoute: String = NavRoutes.TODAY,
    forceRefreshFeed: Boolean = false
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf(
        NavRoutes.TODAY,
        NavRoutes.FEED,
        NavRoutes.PROFILE
    )
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        containerColor = MomentaBackground,
        bottomBar = {
            if (showBottomBar) {
                MomentaBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (route == NavRoutes.CAMERA) {
                            onNavigateToCamera()
                        } else {
                            navController.navigate(route) {
                                popUpTo(startRoute) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startRoute,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(NavRoutes.TODAY) {
                TodayScreen(
                    onCaptureClick = onNavigateToCamera,
                    onSettingsClick = onNavigateToSettings,
                    onOpenFeed = {
                        navController.navigate(NavRoutes.FEED) {
                            popUpTo(startRoute) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(NavRoutes.SEARCH) {
                SearchScreen()
            }
            composable(NavRoutes.FEED) {
                FeedScreen(forceRefreshOnOpen = forceRefreshFeed)
            }
            composable(NavRoutes.CIRCLE) {
                CircleScreen()
            }
            composable(NavRoutes.PROFILE) {
                ProfileScreen(
                    onSettingsClick = onNavigateToSettings,
                    onLogout = onLogout
                )
            }
        }
    }
}
