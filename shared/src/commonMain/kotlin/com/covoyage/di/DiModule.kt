package com.covoyage.di

import org.koin.core.module.Module
import com.covoyage.data.local.TokenManager
import com.covoyage.data.remote.HttpClientFactory
import com.covoyage.data.remote.api.AuthApiService
import com.covoyage.data.remote.api.BookingApiService
import com.covoyage.data.remote.api.PaymentApiService
import com.covoyage.data.remote.api.RideApiService
import com.covoyage.data.repository.AuthRepository
import com.covoyage.data.repository.AuthRepositoryImpl
import com.covoyage.presentation.auth.AuthViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val commonModule = module {
    // HTTP Client
    single { HttpClientFactory(get<TokenManager>()).create() }

    // API Services
    single { AuthApiService(get()) }
    single { RideApiService(get()) }
    single { BookingApiService(get()) }
    single { PaymentApiService(get()) }

    // Repositories
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }

    // ViewModels
    factory { AuthViewModel(get()) }
}

expect fun platformModule(): Module
