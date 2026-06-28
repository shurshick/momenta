package com.bghitech.momenta

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bghitech.momenta.core.design.MomentaBackground
import com.bghitech.momenta.core.design.MomentaTheme
import com.bghitech.momenta.core.navigation.NavRoutes
import com.bghitech.momenta.feature.auth.AuthScreen
import com.bghitech.momenta.feature.auth.AppAuthStateViewModel
import com.bghitech.momenta.feature.camera.CameraScreen
import com.bghitech.momenta.feature.feed.FeedScreen
import com.bghitech.momenta.feature.main.MainScreen
import com.bghitech.momenta.feature.onboarding.OnboardingScreen
import com.bghitech.momenta.feature.profile.ProfileScreen
import com.bghitech.momenta.feature.publish.PublishScreen
import com.bghitech.momenta.feature.settings.SettingsScreen
import com.bghitech.momenta.feature.splash.SplashScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MomentaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MomentaBackground
                ) {
                    MomentaNavGraph()
                }
            }
        }
    }
}

@Composable
fun MomentaNavGraph() {
    val navController = rememberNavController()
    val startDestination = NavRoutes.SPLASH
    val appAuthStateViewModel: AppAuthStateViewModel = hiltViewModel()
    val isLoggedIn by appAuthStateViewModel.isLoggedIn.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val context = LocalContext.current
    var feedRefreshKey by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(isLoggedIn, backStackEntry?.destination?.route) {
        val route = backStackEntry?.destination?.route
        val publicRoute = route in setOf(NavRoutes.SPLASH, NavRoutes.ONBOARDING, NavRoutes.AUTH)
        if (!isLoggedIn && !publicRoute) {
            Toast.makeText(context, "Сессия истекла. Войдите снова.", Toast.LENGTH_LONG).show()
            navController.navigate(NavRoutes.AUTH) {
                popUpTo(0)
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavRoutes.SPLASH) {
            SplashScreen(
                onNavigateToMain = {
                    navController.navigate(NavRoutes.MAIN) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(NavRoutes.ONBOARDING) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.ONBOARDING) {
            OnboardingScreen(
                onGetStarted = {
                    navController.navigate(NavRoutes.AUTH) {
                        popUpTo(NavRoutes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.AUTH) {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(NavRoutes.MAIN) {
                        popUpTo(NavRoutes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.MAIN) {
            MainScreen(
                onNavigateToCamera = {
                    navController.navigate(NavRoutes.CAMERA)
                },
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.SETTINGS)
                },
                onLogout = {
                    navController.navigate(NavRoutes.AUTH) {
                        popUpTo(NavRoutes.MAIN) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.MAIN_FEED) {
            MainScreen(
                startRoute = NavRoutes.FEED,
                feedRefreshKey = feedRefreshKey,
                onNavigateToCamera = {
                    navController.navigate(NavRoutes.CAMERA)
                },
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.SETTINGS)
                },
                onLogout = {
                    navController.navigate(NavRoutes.AUTH) {
                        popUpTo(NavRoutes.MAIN_FEED) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(NavRoutes.AUTH) {
                        popUpTo(NavRoutes.MAIN) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.CAMERA) {
            CameraScreen(
                onBack = { navController.popBackStack() },
                onImageCaptured = { imagePath ->
                    navController.navigate(NavRoutes.preview(imagePath))
                }
            )
        }

        composable(
            route = NavRoutes.PREVIEW,
            arguments = listOf(navArgument("imagePath") { type = NavType.StringType })
        ) { backStackEntry ->
            val imagePath = backStackEntry.arguments?.getString("imagePath") ?: ""
            PublishScreen(
                imagePath = imagePath,
                onBack = { navController.popBackStack() },
                onUploadSuccess = {
                    feedRefreshKey += 1
                    navController.navigate(NavRoutes.MAIN_FEED) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(NavRoutes.UPLOAD_SUCCESS) {
            com.bghitech.momenta.feature.publish.UploadSuccessScreen(
                onGoToToday = {
                    feedRefreshKey += 1
                    navController.navigate(NavRoutes.MAIN_FEED) {
                        popUpTo(NavRoutes.UPLOAD_SUCCESS) { inclusive = true }
                    }
                }
            )
        }
    }
}
