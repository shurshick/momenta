package com.bghitech.momenta.feature.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bghitech.momenta.core.design.MomentaBottomBar
import com.bghitech.momenta.core.design.MomentaBackground
import com.bghitech.momenta.core.design.MomentaGreen
import com.bghitech.momenta.core.design.MomentaLargeShape
import com.bghitech.momenta.core.design.MomentaSurface
import com.bghitech.momenta.core.design.MomentaText
import com.bghitech.momenta.core.design.MomentaTextSecondary
import com.bghitech.momenta.core.navigation.NavRoutes
import com.bghitech.momenta.feature.circle.CircleScreen
import com.bghitech.momenta.feature.feed.FeedScreen
import com.bghitech.momenta.feature.profile.ProfileScreen
import com.bghitech.momenta.feature.search.SearchScreen
import com.bghitech.momenta.feature.today.TodayScreen
import com.bghitech.momenta.feature.updates.AppUpdateInfo
import com.bghitech.momenta.feature.updates.AppUpdateViewModel
import com.bghitech.momenta.feature.updates.downloadAndOpenAppApk
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    onNavigateToCamera: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    startRoute: String = NavRoutes.TODAY,
    feedRefreshKey: Int = 0,
    updateViewModel: AppUpdateViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var updateBanner by remember { mutableStateOf<AppUpdateInfo?>(null) }
    var isDownloadingUpdate by remember { mutableStateOf(false) }
    var feedFocusPostId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val update = updateViewModel.checkLatestAppRelease()
        if (update.hasUpdate && update.downloadUrl != null) {
            updateBanner = update
            delay(5_000)
            updateBanner = null
        }
    }

    val bottomBarRoutes = listOf(
        NavRoutes.TODAY,
        NavRoutes.FEED,
        NavRoutes.PROFILE
    )
    val showBottomBar = currentRoute in bottomBarRoutes

    Box {
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
                        onOpenFeed = {
                            feedFocusPostId = null
                            navController.navigate(NavRoutes.FEED) {
                                popUpTo(startRoute) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onOpenBestPost = { postId ->
                            feedFocusPostId = postId
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
                    FeedScreen(
                        publishRefreshKey = feedRefreshKey,
                        focusPostId = feedFocusPostId
                    )
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

        AnimatedVisibility(
            visible = updateBanner != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            updateBanner?.let { update ->
                UpdateAvailableBanner(
                    update = update,
                    isDownloading = isDownloadingUpdate,
                    onDownload = {
                        val url = update.downloadUrl ?: return@UpdateAvailableBanner
                        scope.launch {
                            isDownloadingUpdate = true
                            downloadAndOpenAppApk(context, url, update.apkSha256)
                            isDownloadingUpdate = false
                            updateBanner = null
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun UpdateAvailableBanner(
    update: AppUpdateInfo,
    isDownloading: Boolean,
    onDownload: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MomentaSurface.copy(alpha = 0.96f),
        shape = MomentaLargeShape,
        border = BorderStroke(1.dp, MomentaGreen.copy(alpha = 0.55f)),
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Download,
                contentDescription = null,
                tint = MomentaGreen,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = if (update.mandatory) {
                    "Важное обновление ${update.latestVersion ?: ""}".trim()
                } else {
                    "Доступна версия ${update.latestVersion ?: ""}".trim()
                },
                color = MomentaText,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                enabled = !isDownloading,
                onClick = onDownload
            ) {
                Text(
                    text = if (isDownloading) "Скачиваем..." else "Скачать",
                    color = if (isDownloading) MomentaTextSecondary else MomentaGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
