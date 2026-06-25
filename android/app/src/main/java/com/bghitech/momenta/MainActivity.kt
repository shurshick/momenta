package com.bghitech.momenta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavRoutes.SPLASH) {
            SplashScreen(
                onNavigateToAuth = {
                    navController.navigate(NavRoutes.AUTH) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                },
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
                    navController.navigate(NavRoutes.UPLOAD_SUCCESS) {
                        popUpTo(NavRoutes.CAMERA) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.UPLOAD_SUCCESS) {
            com.bghitech.momenta.feature.publish.UploadSuccessScreen(
                onGoToToday = {
                    navController.navigate(NavRoutes.MAIN) {
                        popUpTo(NavRoutes.UPLOAD_SUCCESS) { inclusive = true }
                    }
                }
            )
        }
    }
}
