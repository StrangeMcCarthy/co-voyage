package com.covoyage.data.local

import kotlinx.cinterop.*
import platform.CoreFoundation.*
import platform.Foundation.*
import platform.Security.*

actual class TokenManagerImpl : TokenManager {

    companion object {
        private const val SERVICE_NAME = "com.covoyage.app"
        private const val TOKEN_KEY = "auth_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
    }

    override suspend fun saveToken(token: String) {
        saveToKeychain(TOKEN_KEY, token)
    }

    override suspend fun getToken(): String? {
        return readFromKeychain(TOKEN_KEY)
    }

    override suspend fun saveRefreshToken(token: String) {
        saveToKeychain(REFRESH_TOKEN_KEY, token)
    }

    override suspend fun getRefreshToken(): String? {
        return readFromKeychain(REFRESH_TOKEN_KEY)
    }

    override suspend fun clearTokens() {
        deleteFromKeychain(TOKEN_KEY)
        deleteFromKeychain(REFRESH_TOKEN_KEY)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun saveToKeychain(key: String, value: String) {
        // Delete existing item first
        deleteFromKeychain(key)

        val data = (value as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return

        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to SERVICE_NAME,
            kSecAttrAccount to key,
            kSecValueData to data,
            kSecAttrAccessible to kSecAttrAccessibleWhenUnlockedThisDeviceOnly
        ) as CFDictionaryRef

        SecItemAdd(query, null)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun readFromKeychain(key: String): String? {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to SERVICE_NAME,
            kSecAttrAccount to key,
            kSecReturnData to kCFBooleanTrue,
            kSecMatchLimit to kSecMatchLimitOne
        ) as CFDictionaryRef

        memScoped {
            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query, result.ptr)

            if (status == errSecSuccess) {
                val data = result.value as? NSData ?: return null
                return NSString.create(data = data, encoding = NSUTF8StringEncoding) as? String
            }
        }
        return null
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun deleteFromKeychain(key: String) {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to SERVICE_NAME,
            kSecAttrAccount to key
        ) as CFDictionaryRef

        SecItemDelete(query)
    }
}
