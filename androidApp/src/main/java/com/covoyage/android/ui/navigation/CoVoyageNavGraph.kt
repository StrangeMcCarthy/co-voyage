package com.covoyage.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.covoyage.android.ui.screens.auth.LoginScreen
import com.covoyage.android.ui.screens.auth.RegisterScreen
import com.covoyage.android.ui.screens.home.HomeScreen
import com.covoyage.android.ui.screens.onboarding.OnboardingScreen
import com.covoyage.android.ui.screens.rides.RideSearchScreen
import com.covoyage.android.ui.screens.profile.ProfileScreen
import com.covoyage.presentation.auth.AuthViewModel
import org.koin.androidx.compose.koinViewModel

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object RideSearch : Screen("ride_search")
    object RideDetails : Screen("ride_details/{rideId}") {
        fun createRoute(rideId: String) = "ride_details/$rideId"
    }
    object CreateRide : Screen("create_ride")
    object Profile : Screen("profile")
    object Bookings : Screen("bookings")
    object BookingDetails : Screen("booking_details/{bookingId}") {
        fun createRoute(bookingId: String) = "booking_details/$bookingId"
    }
    object Payment : Screen("payment/{bookingId}") {
        fun createRoute(bookingId: String) = "payment/$bookingId"
    }
}

@Composable
fun CoVoyageNavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = koinViewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    
    val startDestination = if (isLoggedIn) {
        Screen.Home.route
    } else {
        Screen.Onboarding.route
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSearch = { navController.navigate(Screen.RideSearch.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToBookings = { navController.navigate(Screen.Bookings.route) },
                onNavigateToCreateRide = { navController.navigate(Screen.CreateRide.route) }
            )
        }
        
        composable(Screen.RideSearch.route) {
            RideSearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onRideClick = { rideId ->
                    navController.navigate(Screen.RideDetails.createRoute(rideId))
                }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        // TODO: Add more screens
    }
}
