package covoyage.travel.cameroon.data.local

import com.russhwolf.settings.Settings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Provides local persistence using multiplatform-settings.
 * Stores @Serializable objects as JSON strings in platform-native key-value storage
 * (SharedPreferences on Android, NSUserDefaults on iOS, localStorage on JS/WasmJS).
 */
class LocalStorageService(
    @PublishedApi internal val settings: Settings = Settings(),
    @PublishedApi internal val json: Json = Json { ignoreUnknownKeys = true },
) {

    /**
     * Save a list of serializable items under [key].
     */
    inline fun <reified T> saveList(key: String, list: List<T>) {
        settings.putString(key, json.encodeToString(list))
    }

    /**
     * Load a list previously saved under [key]. Returns empty list if nothing stored.
     */
    inline fun <reified T> loadList(key: String): List<T> {
        val raw = settings.getStringOrNull(key) ?: return emptyList()
        return try {
            json.decodeFromString<List<T>>(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Save a single serializable object under [key].
     */
    inline fun <reified T> saveObject(key: String, obj: T) {
        settings.putString(key, json.encodeToString(obj))
    }

    /**
     * Load a single object previously saved under [key]. Returns null if nothing stored.
     */
    inline fun <reified T> loadObject(key: String): T? {
        val raw = settings.getStringOrNull(key) ?: return null
        return try {
            json.decodeFromString<T>(raw)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Save a plain string value.
     */
    fun saveString(key: String, value: String) {
        settings.putString(key, value)
    }

    /**
     * Load a plain string value.
     */
    fun loadString(key: String): String? {
        return settings.getStringOrNull(key)
    }

    /**
     * Remove a specific key from storage.
     */
    fun remove(key: String) {
        settings.remove(key)
    }

    /**
     * Clear all stored data.
     */
    fun clear() {
        settings.clear()
    }
}
