package covoyage.travel.cameroon.di

import covoyage.travel.cameroon.data.local.LocalStorageService
import covoyage.travel.cameroon.data.remote.JourneyApiService
import covoyage.travel.cameroon.data.remote.PaymentApiService
import covoyage.travel.cameroon.data.remote.RideRequestApiService
import covoyage.travel.cameroon.data.repository.AuthRepository
import covoyage.travel.cameroon.data.repository.BookingRepository
import covoyage.travel.cameroon.data.repository.JourneyRepository
import covoyage.travel.cameroon.data.repository.RideRequestRepository
import covoyage.travel.cameroon.data.repository.mock.MockAuthRepository
import covoyage.travel.cameroon.data.repository.mock.MockBookingRepository
import covoyage.travel.cameroon.data.repository.mock.MockJourneyRepository
import covoyage.travel.cameroon.data.repository.mock.MockRideRequestRepository
import covoyage.travel.cameroon.ui.driver.DriverScreenModel
import covoyage.travel.cameroon.ui.riderequest.RideRequestScreenModel
import org.koin.dsl.module

val appModule = module {
    // Local storage (offline persistence)
    single { LocalStorageService() }

    // Repositories — swap mock for real implementations later
    single<AuthRepository> { MockAuthRepository(get()) }
    single<JourneyRepository> { MockJourneyRepository(get()) }
    single<BookingRepository> { MockBookingRepository(get()) }
    single<RideRequestRepository> { MockRideRequestRepository(get()) }

    // API services (calls our Ktor server)
    single { PaymentApiService() }
    single { JourneyApiService() }
    single { RideRequestApiService() }

    // Screen models
    single { DriverScreenModel(get<JourneyRepository>()) }
    single { RideRequestScreenModel(get()) }
}
