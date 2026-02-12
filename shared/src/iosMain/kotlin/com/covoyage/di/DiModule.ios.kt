package com.covoyage.di

import com.covoyage.data.local.TokenManager
import com.covoyage.data.local.TokenManagerImpl
import org.koin.dsl.module

actual fun platformModule() = module {
    single<TokenManager> { TokenManagerImpl() }
}
