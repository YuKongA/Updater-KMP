package data.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.prefGet
import platform.prefRemove
import platform.prefSet

class PreferencesStorage {
    suspend fun get(key: String): String? = withContext(Dispatchers.Default) {
        prefGet(key)
    }

    suspend fun set(key: String, value: String) = withContext(Dispatchers.Default) {
        prefSet(key, value)
    }

    suspend fun remove(key: String) = withContext(Dispatchers.Default) {
        prefRemove(key)
    }
}
