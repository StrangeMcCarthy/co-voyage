# Implementation Guide — Co-Voyage KMP

## Overview

Co-Voyage is a Kotlin Multiplatform ride-sharing app. This guide covers the actual implementation architecture.

---

## Backend (Ktor + MongoDB)

### Server Entry Point — `Application.kt`

```kotlin
fun Application.module() {
    // DB init (synchronous at startup)
    runBlocking { DatabaseConfig.init() }

    // Repositories
    val userRepository = UserRepository()
    val rideRepository = RideRepository()
    val bookingRepository = BookingRepository()
    val paymentRepository = PaymentRepository()
    val ratingRepository = RatingRepository()

    // Services
    val flutterwaveService = FlutterwaveService()

    // Controllers
    val authController = AuthController(userRepository)
    val rideController = RideController(rideRepository, userRepository)
    val bookingController = BookingController(bookingRepository, rideRepository, userRepository)
    val paymentController = PaymentController(paymentRepository, bookingRepository, flutterwaveService)
    val ratingController = RatingController(ratingRepository, bookingRepository, userRepository)

    // Plugin & route setup
    configureSerialization()
    configureSecurity()     // JWT auth
    configureHTTP(dotenv)   // CORS
    configureMonitoring()   // Call logging
    configureRouting(authController, rideController, bookingController, paymentController, ratingController)
    configureStatusPages()  // Error handling
}
```

### Database — MongoDB Kotlin Driver

```kotlin
object DatabaseConfig {
    lateinit var database: MongoDatabase
    private lateinit var client: MongoClient

    suspend fun init() {
        val uri = dotenv["MONGODB_URI"] ?: "mongodb://localhost:27017"
        val dbName = dotenv["MONGODB_DATABASE"] ?: "covoyage_db"
        client = MongoClient.create(uri)
        database = client.getDatabase(dbName)
    }
}
```

### Authentication — JWT

- Tokens are signed with HMAC256
- Access tokens expire in 24h, refresh tokens in 7 days
- All protected routes use `authenticate("auth-jwt") { ... }`
- Passwords hashed with BCrypt

### Repositories

Each repository wraps a MongoDB collection:

| Repository | Collection | Operations |
|-----------|-----------|------------|
| `UserRepository` | `users` | CRUD, find by email, update rating |
| `RideRepository` | `rides` | CRUD, search with filters, update seats |
| `BookingRepository` | `bookings` | CRUD, find by passenger/driver, update status |
| `PaymentRepository` | `payments` | CRUD, find by transaction ref, update status |
| `RatingRepository` | `ratings` | create, find by reviewed user, duplicate check |

### Payments — Flutterwave

- Supports Orange Money, MTN Mobile Money, cards, bank transfer
- Platform fee is configurable via `PLATFORM_FEE_PERCENTAGE` env var (default 2%)
- Webhook endpoint at `/api/v1/payments/webhook` verified by `verif-hash` header
- Refund support via `FlutterwaveService.initiateRefund()`

---

## Shared Module (KMP)

### Domain Models

Located in `shared/src/commonMain/kotlin/com/covoyage/domain/model/`:
- `User.kt` — User, AuthResponse, LoginRequest, UserRegistrationRequest
- `Ride.kt` — Ride, CreateRideRequest, RideFilter, RideSearchResponse
- `Booking.kt` — Booking, CreateBookingRequest
- `Payment.kt` — Payment, InitiatePaymentRequest
- `Rating.kt` — Rating, CreateRatingRequest

### API Services

Located in `shared/src/commonMain/kotlin/com/covoyage/data/remote/api/`:
- `AuthApiService` — register, login, logout, getCurrentUser
- `RideApiService` — searchRides, getRide, createRide, getMyRides
- `BookingApiService` — createBooking, getBooking, getMyBookings
- `PaymentApiService` — initiatePayment, getPaymentStatus

### Token Management

- **Android**: Stores tokens using `EncryptedSharedPreferences` (AES-256)
- **iOS**: Stores tokens using iOS Keychain (`Security.framework`) with `kSecAttrAccessibleWhenUnlockedThisDeviceOnly`

### Dependency Injection — Koin

```kotlin
// shared/src/commonMain/kotlin/com/covoyage/di/KoinModules.kt
val commonModule = module {
    single<TokenManager> { TokenManagerImpl() }
    single { HttpClientFactory.create(get()) }
    single { AuthApiService(get()) }
    single { RideApiService(get()) }
    single { BookingApiService(get()) }
    single { PaymentApiService(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    factory { AuthViewModel(get()) }
}
```

Initialized in `CoVoyageApplication.kt`:
```kotlin
class CoVoyageApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CoVoyageApplication)
            modules(commonModule)
        }
    }
}
```

---

## Android App

### Navigation

Uses Jetpack Navigation Compose with routes:
- `onboarding` → `login` → `register` → `home`
- `home` → `search`, `profile`, `bookings`, `create_ride`

### Screens

| Screen | File | Description |
|--------|------|-------------|
| Onboarding | `OnboardingScreen.kt` | Welcome with CTA buttons |
| Login | `LoginScreen.kt` | Email/password login |
| Register | `RegisterScreen.kt` | Full registration with role selection |
| Home | `HomeScreen.kt` | Dashboard with quick actions |
| Ride Search | `RideSearchScreen.kt` | Search form + ride results |
| Profile | `ProfileScreen.kt` | User info, rating, logout |

### Theme

Material 3 with custom Indigo/Purple color scheme supporting light and dark modes.
