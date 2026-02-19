package covoyage.travel.cameroon

import androidx.compose.runtime.*
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import covoyage.travel.cameroon.data.remote.PaymentApiService
import covoyage.travel.cameroon.data.repository.AuthRepository
import covoyage.travel.cameroon.data.repository.BookingRepository
import covoyage.travel.cameroon.data.repository.JourneyRepository
import covoyage.travel.cameroon.data.repository.RideRequestRepository
import covoyage.travel.cameroon.di.appModule
import covoyage.travel.cameroon.i18n.Language
import covoyage.travel.cameroon.i18n.LocalLanguage
import covoyage.travel.cameroon.i18n.LocalStrings
import covoyage.travel.cameroon.i18n.stringsFor
import covoyage.travel.cameroon.ui.auth.AuthScreenModel
import covoyage.travel.cameroon.ui.auth.LoginScreen
import covoyage.travel.cameroon.ui.booking.BookingScreenModel
import covoyage.travel.cameroon.ui.driver.DriverScreenModel
import covoyage.travel.cameroon.ui.journey.JourneyScreenModel
import covoyage.travel.cameroon.ui.navigation.MainScreen
import covoyage.travel.cameroon.ui.riderequest.RideRequestScreenModel
import covoyage.travel.cameroon.ui.theme.CoVoyageTheme
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@Composable
fun App() {
    KoinApplication(application = { modules(appModule) }) {
        CoVoyageTheme {
            AppContent()
        }
    }
}

@Composable
private fun AppContent() {
    var language by remember { mutableStateOf(Language.EN) }
    val strings = remember(language) { stringsFor(language) }

    CompositionLocalProvider(
        LocalLanguage provides language,
        LocalStrings provides strings,
    ) {
        AppNavigation(
            onLanguageChange = { language = it },
        )
    }
}

@Composable
private fun AppNavigation(
    onLanguageChange: (Language) -> Unit,
) {
    val authRepository: AuthRepository = koinInject()
    val journeyRepository: JourneyRepository = koinInject()
    val bookingRepository: BookingRepository = koinInject()
    val rideRequestRepository: RideRequestRepository = koinInject()
    val paymentApiService: PaymentApiService = koinInject()
    val driverScreenModel: DriverScreenModel = koinInject()

    val authScreenModel = remember { AuthScreenModel(authRepository) }
    val journeyScreenModel = remember { JourneyScreenModel(journeyRepository) }
    val bookingScreenModel = remember { BookingScreenModel(bookingRepository, paymentApiService) }
    val rideRequestScreenModel = remember { RideRequestScreenModel(rideRequestRepository) }
    val authState by authScreenModel.uiState.collectAsState()

    if (authState.isLoggedIn && authState.currentUser != null) {
        Navigator(
            MainScreen(
                journeyScreenModel = journeyScreenModel,
                bookingScreenModel = bookingScreenModel,
                rideRequestScreenModel = rideRequestScreenModel,
                currentUser = authState.currentUser!!,
                driverScreenModel = driverScreenModel,
                onLogout = {
                    authScreenModel.logout()
                },
                onLanguageChange = onLanguageChange,
            )
        ) { navigator ->
            SlideTransition(navigator)
        }
    } else {
        Navigator(
            LoginScreen(
                authScreenModel = authScreenModel,
                onLoginSuccess = { /* State-driven navigation handles this */ },
            )
        ) { navigator ->
            SlideTransition(navigator)
        }
    }
}