# Project Setup — Co-Voyage KMP

## Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| JDK | 17+ | Kotlin compilation |
| Android Studio | Hedgehog+ | Android development |
| MongoDB | 6.0+ | Database (local or Atlas) |
| Kotlin | 2.0+ | Language |
| Gradle | 8.2+ | Build system |

## 1. Clone & Open

```bash
git clone <repo-url> covoyage-kmp
cd covoyage-kmp
```

Open in Android Studio → File → Open → select `covoyage-kmp`.

## 2. Backend Setup

### Install MongoDB

**macOS:**
```bash
brew install mongodb-community
brew services start mongodb-community
```

**Ubuntu/Debian:**
```bash
sudo apt install mongodb-org
sudo systemctl start mongod
```

**Or use MongoDB Atlas** (cloud) — get your connection string from the Atlas dashboard.

### Configure Environment

```bash
cd backend
cp .env.example .env
```

Edit `.env` with your values:
```env
MONGODB_URI=mongodb://localhost:27017/covoyage_db
MONGODB_DATABASE=covoyage_db
JWT_SECRET=<generate-a-long-random-string>
FLUTTERWAVE_SECRET_KEY=<your-flutterwave-secret>
FLUTTERWAVE_PUBLIC_KEY=<your-flutterwave-public>
PLATFORM_FEE_PERCENTAGE=2.0
```

### Run Backend

```bash
./gradlew :backend:run
```

Verify: `curl http://localhost:5000/health`

Expected response:
```json
{"status": "OK", "timestamp": 1234567890, "version": "1.0.0"}
```

## 3. Android App Setup

1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Select `androidApp` run configuration
4. Run on emulator or device (API 26+)

### API Base URL

The shared module's `ApiConfig.kt` defines the base URL. For local development:
- Emulator: `http://10.0.2.2:5000/api/v1`
- Physical device: Use your machine's local IP

## 4. Project Dependencies

Managed via `libs.versions.toml` and `build.gradle.kts`:

| Dependency | Purpose |
|-----------|---------|
| Ktor Client | HTTP networking (shared) |
| Ktor Server + Netty | Backend HTTP server |
| MongoDB Kotlin Driver | Database access |
| Koin | Dependency injection |
| Kotlinx Serialization | JSON serialization |
| BCrypt | Password hashing |
| Jetpack Compose | Android UI |
| Material 3 | Design system |

## 5. Development Workflow

1. **Backend first**: Start the Ktor server
2. **Shared module**: Business logic changes compile for both platforms
3. **Android**: Run and test UI on emulator
4. **iOS**: Open `iosApp/iosApp.xcodeproj` in Xcode (when ready)

## 6. Useful Commands

```bash
# Run backend
./gradlew :backend:run

# Build shared module
./gradlew :shared:build

# Run all checks
./gradlew check

# Clean build
./gradlew clean
```
