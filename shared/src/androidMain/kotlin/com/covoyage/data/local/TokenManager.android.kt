package com.covoyage.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

actual class TokenManagerImpl(context: Context) : TokenManager {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "covoyage_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    override suspend fun saveToken(token: String) {
        sharedPreferences.edit().putString(TOKEN_KEY, token).apply()
    }
    
    override suspend fun getToken(): String? {
        return sharedPreferences.getString(TOKEN_KEY, null)
    }
    
    override suspend fun saveRefreshToken(token: String) {
        sharedPreferences.edit().putString(REFRESH_TOKEN_KEY, token).apply()
    }
    
    override suspend fun getRefreshToken(): String? {
        return sharedPreferences.getString(REFRESH_TOKEN_KEY, null)
    }
    
    override suspend fun clearTokens() {
        sharedPreferences.edit()
            .remove(TOKEN_KEY)
            .remove(REFRESH_TOKEN_KEY)
            .apply()
    }
    
    companion object {
        private const val TOKEN_KEY = "auth_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
    }
}
