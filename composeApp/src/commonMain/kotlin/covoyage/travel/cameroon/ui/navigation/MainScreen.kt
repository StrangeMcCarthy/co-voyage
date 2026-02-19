package covoyage.travel.cameroon.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PostAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import covoyage.travel.cameroon.data.model.UserProfile
import covoyage.travel.cameroon.data.model.UserType
import covoyage.travel.cameroon.i18n.Language
import covoyage.travel.cameroon.i18n.LocalStrings
import covoyage.travel.cameroon.ui.booking.BookingScreenModel
import covoyage.travel.cameroon.ui.driver.DriverDashboardScreen
import covoyage.travel.cameroon.ui.driver.DriverScreenModel
import covoyage.travel.cameroon.ui.journey.JourneyFeedScreen
import covoyage.travel.cameroon.ui.journey.JourneyScreenModel
import covoyage.travel.cameroon.ui.profile.ProfileScreen
import covoyage.travel.cameroon.ui.riderequest.RideRequestFeedScreen
import covoyage.travel.cameroon.ui.riderequest.RideRequestScreenModel

enum class BottomTab(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    RIDES(Icons.Filled.Explore, Icons.Outlined.Explore),
    REQUESTS(Icons.Filled.PostAdd, Icons.Outlined.PostAdd),
    MY_TRIPS(Icons.Filled.DirectionsCar, Icons.Outlined.DirectionsCar),
    PROFILE(Icons.Filled.Person, Icons.Outlined.Person),
}

class MainScreen(
    private val journeyScreenModel: JourneyScreenModel,
    private val bookingScreenModel: BookingScreenModel,
    private val rideRequestScreenModel: RideRequestScreenModel,
    private val currentUser: UserProfile,
    private val driverScreenModel: DriverScreenModel,
    private val onLogout: () -> Unit,
    private val onLanguageChange: (Language) -> Unit,
) : Screen {

    @Composable
    override fun Content() {
        val strings = LocalStrings.current
        var selectedTab by remember { mutableStateOf(BottomTab.RIDES) }
        val isDriver = currentUser.userType == UserType.DRIVER
        val visibleTabs = if (isDriver) {
            listOf(BottomTab.RIDES, BottomTab.REQUESTS, BottomTab.MY_TRIPS, BottomTab.PROFILE)
        } else {
            listOf(BottomTab.RIDES, BottomTab.REQUESTS, BottomTab.PROFILE)
        }

        val tabLabels = mapOf(
            BottomTab.RIDES to strings.tabRides,
            BottomTab.REQUESTS to strings.tabRequests,
            BottomTab.MY_TRIPS to strings.tabMyTrips,
            BottomTab.PROFILE to strings.tabProfile,
        )

        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = androidx.compose.ui.unit.dp.times(0),
                ) {
                    visibleTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == tab) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tabLabels[tab],
                                )
                            },
                            label = { Text(tabLabels[tab] ?: "") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        )
                    }
                }
            },
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when (selectedTab) {
                    BottomTab.RIDES -> {
                        Navigator(
                            JourneyFeedScreen(
                                journeyScreenModel = journeyScreenModel,
                                currentUser = currentUser,
                                bookingScreenModel = bookingScreenModel,
                            )
                        ) { navigator ->
                            SlideTransition(navigator)
                        }
                    }
                    BottomTab.REQUESTS -> {
                        Navigator(
                            RideRequestFeedScreen(
                                rideRequestScreenModel = rideRequestScreenModel,
                                currentUser = currentUser,
                            )
                        ) { navigator ->
                            SlideTransition(navigator)
                        }
                    }
                    BottomTab.MY_TRIPS -> {
                        Navigator(
                            DriverDashboardScreen(
                                currentUser = currentUser,
                                driverScreenModel = driverScreenModel,
                                journeyScreenModel = journeyScreenModel,
                            )
                        ) { navigator ->
                            SlideTransition(navigator)
                        }
                    }
                    BottomTab.PROFILE -> {
                        Navigator(
                            ProfileScreen(
                                currentUser = currentUser,
                                onLogout = onLogout,
                                onLanguageChange = onLanguageChange,
                            )
                        ) { navigator ->
                            SlideTransition(navigator)
                        }
                    }
                }
            }
        }
    }
}
