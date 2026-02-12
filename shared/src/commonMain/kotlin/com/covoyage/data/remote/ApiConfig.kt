package com.covoyage.data.remote

object ApiConfig {
    // Update this with your actual backend URL on Render
    const val BASE_URL = "https://covoyage-api.onrender.com/api/v1"
    
    // Flutterwave keys should be configured via environment/BuildConfig, not hardcoded
    // On Android: set via BuildConfig fields in build.gradle.kts
    // On iOS: set via Info.plist or environment variables
    
    // API Endpoints
    object Endpoints {
        // Auth
        const val REGISTER = "/auth/register"
        const val LOGIN = "/auth/login"
        const val REFRESH_TOKEN = "/auth/refresh"
        const val LOGOUT = "/auth/logout"
        
        // Users
        const val USERS = "/users"
        const val USER_PROFILE = "/users/{id}"
        const val UPDATE_PROFILE = "/users/{id}"
        const val VERIFY_DRIVER = "/users/{id}/verify"
        
        // Rides
        const val RIDES = "/rides"
        const val RIDE_DETAILS = "/rides/{id}"
        const val MY_RIDES = "/rides/my-rides"
        const val SEARCH_RIDES = "/rides/search"
        const val CREATE_RIDE = "/rides"
        const val UPDATE_RIDE = "/rides/{id}"
        const val CANCEL_RIDE = "/rides/{id}/cancel"
        
        // Passenger Requests
        const val PASSENGER_REQUESTS = "/passenger-requests"
        const val MY_REQUESTS = "/passenger-requests/my-requests"
        
        // Bookings
        const val BOOKINGS = "/bookings"
        const val BOOKING_DETAILS = "/bookings/{id}"
        const val MY_BOOKINGS = "/bookings/my-bookings"
        const val CREATE_BOOKING = "/bookings"
        const val CONFIRM_ARRIVAL = "/bookings/{id}/confirm-arrival"
        const val CANCEL_BOOKING = "/bookings/{id}/cancel"
        
        // Payments
        const val INITIATE_PAYMENT = "/payments/initiate"
        const val PAYMENT_WEBHOOK = "/payments/webhook"
        const val PAYMENT_STATUS = "/payments/{id}/status"
        const val REQUEST_REFUND = "/payments/{id}/refund"
        const val PAYMENTS = "/payments"
        
        // Ratings
        const val RATINGS = "/ratings"
        const val USER_RATINGS = "/ratings/user/{userId}"
        const val CREATE_RATING = "/ratings"
        
        // Notifications
        const val NOTIFICATIONS = "/notifications"
        const val MARK_READ = "/notifications/{id}/read"
        
        // Dashboard (Admin)
        const val DASHBOARD_STATS = "/dashboard/stats"
        const val DASHBOARD_USERS = "/dashboard/users"
        const val DASHBOARD_TRANSACTIONS = "/dashboard/transactions"
    }
    
    // Request Headers
    object Headers {
        const val AUTHORIZATION = "Authorization"
        const val CONTENT_TYPE = "Content-Type"
        const val ACCEPT = "Accept"
    }
    
    // Timeouts
    const val CONNECT_TIMEOUT = 30_000L
    const val READ_TIMEOUT = 30_000L
    const val WRITE_TIMEOUT = 30_000L
}
