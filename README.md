# Co-Voyage KMP 🚗

> **Cross-platform ride-sharing application for Cameroon** — built with Kotlin Multiplatform (KMP), Ktor backend, MongoDB, and Jetpack Compose.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Shared Logic** | Kotlin Multiplatform (KMP) |
| **Android UI** | Jetpack Compose + Material 3 |
| **iOS UI** | SwiftUI (planned) |
| **Backend** | Ktor (Kotlin) |
| **Database** | MongoDB (Kotlin Driver) |
| **Auth** | JWT (access + refresh tokens) |
| **Payments** | Flutterwave (Orange Money, MTN MoMo, cards) |
| **DI** | Koin |
| **Networking** | Ktor Client (shared), Ktor Server (backend) |

## Project Structure

```
covoyage-kmp/
├── shared/              # KMP shared module
│   └── src/
│       ├── commonMain/  # Shared business logic
│       │   ├── domain/model/     # Data models
│       │   ├── data/remote/      # API services & HTTP client
│       │   ├── data/repository/  # Repository layer
│       │   ├── data/local/       # Token storage
│       │   ├── presentation/     # ViewModels
│       │   └── di/               # Koin dependency injection
│       ├── androidMain/ # Android-specific implementations
│       └── iosMain/     # iOS-specific implementations (Keychain)
├── androidApp/          # Android application
│   └── src/main/
│       └── java/com/covoyage/android/
│           ├── CoVoyageApplication.kt  # Koin initialization
│           ├── MainActivity.kt
│           └── ui/
│               ├── navigation/   # Nav graph
│               ├── screens/      # Compose screens
│               └── theme/        # Material 3 theme
├── backend/             # Ktor backend server
│   └── src/main/kotlin/com/covoyage/backend/
│       ├── Application.kt        # Server entry point
│       ├── config/               # Database & JWT config
│       ├── controllers/          # Request handlers
│       ├── models/               # MongoDB data models
│       ├── routes/               # API route definitions
│       ├── services/             # Repositories & services
│       └── utils/                # Helpers (ApiResponse)
└── build.gradle.kts     # Root build config
```

## Getting Started

### Prerequisites
- JDK 17+
- Android Studio Hedgehog+
- MongoDB (local or Atlas)
- Kotlin 2.0+

### Backend Setup
```bash
cd backend
cp .env.example .env
# Edit .env with your MongoDB URI, JWT secret, Flutterwave keys
./gradlew run
```

The server starts at `http://localhost:5000`. Test with:
```bash
curl http://localhost:5000/health
```

### Android Setup
1. Open the project in Android Studio
2. Sync Gradle
3. Run on emulator or device

### Environment Variables
See `backend/.env.example` for all configuration options:
- `MONGODB_URI` — MongoDB connection string
- `JWT_SECRET` — JWT signing secret
- `FLUTTERWAVE_SECRET_KEY` — Flutterwave payment API key
- `PLATFORM_FEE_PERCENTAGE` — Platform commission (default: 2%)

## API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/v1/auth/register` | ✗ | Register user |
| POST | `/api/v1/auth/login` | ✗ | Login |
| GET | `/api/v1/auth/me` | ✓ | Current user |
| POST | `/api/v1/auth/logout` | ✓ | Logout |
| GET | `/api/v1/rides/search` | ✗ | Search rides |
| GET | `/api/v1/rides/{id}` | ✗ | Get ride details |
| POST | `/api/v1/rides` | ✓ | Create ride (drivers) |
| GET | `/api/v1/rides/my` | ✓ | My rides |
| PUT | `/api/v1/rides/{id}` | ✓ | Update ride |
| PUT | `/api/v1/rides/{id}/cancel` | ✓ | Cancel ride |
| POST | `/api/v1/bookings` | ✓ | Create booking |
| GET | `/api/v1/bookings/{id}` | ✓ | Get booking |
| GET | `/api/v1/bookings/my` | ✓ | My bookings |
| PUT | `/api/v1/bookings/{id}/accept` | ✓ | Accept booking (driver) |
| PUT | `/api/v1/bookings/{id}/reject` | ✓ | Reject booking (driver) |
| PUT | `/api/v1/bookings/{id}/cancel` | ✓ | Cancel booking |
| PUT | `/api/v1/bookings/{id}/confirm-arrival` | ✓ | Confirm arrival |
| POST | `/api/v1/payments/initiate` | ✓ | Start payment |
| GET | `/api/v1/payments/{id}/status` | ✓ | Payment status |
| POST | `/api/v1/payments/{id}/refund` | ✓ | Request refund |
| GET | `/api/v1/payments/history` | ✓ | Payment history |
| GET | `/api/v1/payments/{id}/verify` | ✓ | Verify payment |
| POST | `/api/v1/payments/webhook` | ✗ | Flutterwave webhook |
| POST | `/api/v1/ratings` | ✓ | Submit rating |
| GET | `/api/v1/ratings/user/{userId}` | ✗ | User ratings |

## Architecture

```
┌─────────────────────────────────────────────┐
│  Android / iOS UI                           │
├─────────────────────────────────────────────┤
│  ViewModels (shared/presentation)           │
├─────────────────────────────────────────────┤
│  Repositories (shared/data/repository)      │
├─────────────────────────────────────────────┤
│  API Services (shared/data/remote)          │
├─────────────────────────────────────────────┤
│  Ktor HTTP Client ←→ Ktor Server (backend)  │
├─────────────────────────────────────────────┤
│  MongoDB                                    │
└─────────────────────────────────────────────┘
```

## License

MIT
