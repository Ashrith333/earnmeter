package com.earnmeter.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.earnmeter.app.presentation.auth.AuthViewModel
import com.earnmeter.app.presentation.auth.LoginScreen
import com.earnmeter.app.presentation.auth.OtpScreen
import com.earnmeter.app.presentation.home.HomeScreen
import com.earnmeter.app.presentation.onboarding.PermissionsScreen
import com.earnmeter.app.presentation.profile.ProfileScreen
import com.earnmeter.app.presentation.settings.SettingsScreen
import com.earnmeter.app.presentation.settings.OverlaySettingsScreen
import com.earnmeter.app.presentation.settings.TrackProfitsSettingsScreen
import com.earnmeter.app.presentation.rides.RideHistoryScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Otp : Screen("otp/{phoneNumber}") {
        fun createRoute(phoneNumber: String) = "otp/$phoneNumber"
    }
    object Permissions : Screen("permissions")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object OverlaySettings : Screen("overlay_settings")
    object TrackProfitsSettings : Screen("track_profits_settings")
    object RideHistory : Screen("ride_history")
}

@Composable
fun EarnMeterNavHost(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState(initial = false)
    val startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToOtp = { phoneNumber ->
                    navController.navigate(Screen.Otp.createRoute(phoneNumber))
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Permissions.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Otp.route) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            OtpScreen(
                phoneNumber = phoneNumber,
                onNavigateToHome = {
                    navController.navigate(Screen.Permissions.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Permissions.route) {
            PermissionsScreen(
                onPermissionsGranted = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Permissions.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToRideHistory = {
                    navController.navigate(Screen.RideHistory.route)
                }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToOverlaySettings = {
                    navController.navigate(Screen.OverlaySettings.route)
                },
                onNavigateToTrackProfitsSettings = {
                    navController.navigate(Screen.TrackProfitsSettings.route)
                }
            )
        }
        
        composable(Screen.OverlaySettings.route) {
            OverlaySettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.TrackProfitsSettings.route) {
            TrackProfitsSettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.RideHistory.route) {
            RideHistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

