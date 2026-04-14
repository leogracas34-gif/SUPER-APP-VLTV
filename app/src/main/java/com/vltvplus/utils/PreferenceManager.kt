package com.vltvplus.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "vltv_prefs")

@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val KEY_USERNAME = stringPreferencesKey("username")
        val KEY_PASSWORD = stringPreferencesKey("password")
        val KEY_ACTIVE_DNS = stringPreferencesKey("active_dns")
        val KEY_BASE_URL = stringPreferencesKey("base_url")
        val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val KEY_LAST_SYNC = longPreferencesKey("last_sync")
        val KEY_PLAYER_QUALITY = stringPreferencesKey("player_quality")
        val KEY_AUTO_NEXT_EPISODE = booleanPreferencesKey("auto_next_episode")
        val KEY_SKIP_INTRO_TIME = intPreferencesKey("skip_intro_time")
        val KEY_APP_LANGUAGE = stringPreferencesKey("app_language")
        val KEY_PARENTAL_PIN = stringPreferencesKey("parental_pin")
    }

    private val dataStore = context.dataStore

    // Blocking reads (for startup)
    fun getActiveDns(): String? = runBlocking {
        dataStore.data.catch { emit(emptyPreferences()) }.first()[KEY_ACTIVE_DNS]
    }

    fun getBaseUrl(): String? = runBlocking {
        dataStore.data.catch { emit(emptyPreferences()) }.first()[KEY_BASE_URL]
    }

    fun getUsername(): String? = runBlocking {
        dataStore.data.catch { emit(emptyPreferences()) }.first()[KEY_USERNAME]
    }

    fun getPassword(): String? = runBlocking {
        dataStore.data.catch { emit(emptyPreferences()) }.first()[KEY_PASSWORD]
    }

    fun isLoggedIn(): Boolean = runBlocking {
        dataStore.data.catch { emit(emptyPreferences()) }.first()[KEY_IS_LOGGED_IN] ?: false
    }

    // Suspend setters
    suspend fun setActiveDns(dns: String) {
        dataStore.edit { it[KEY_ACTIVE_DNS] = dns }
    }

    suspend fun setBaseUrl(url: String) {
        dataStore.edit { it[KEY_BASE_URL] = url }
    }

    suspend fun setCredentials(username: String, password: String) {
        dataStore.edit {
            it[KEY_USERNAME] = username
            it[KEY_PASSWORD] = password
            it[KEY_IS_LOGGED_IN] = true
        }
    }

    suspend fun setLoggedIn(value: Boolean) {
        dataStore.edit { it[KEY_IS_LOGGED_IN] = value }
    }

    suspend fun setLastSync(timestamp: Long) {
        dataStore.edit { it[KEY_LAST_SYNC] = timestamp }
    }

    suspend fun setAutoNextEpisode(enabled: Boolean) {
        dataStore.edit { it[KEY_AUTO_NEXT_EPISODE] = enabled }
    }

    suspend fun clearSession() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_USERNAME)
            prefs.remove(KEY_PASSWORD)
            prefs.remove(KEY_ACTIVE_DNS)
            prefs.remove(KEY_BASE_URL)
            prefs.remove(KEY_IS_LOGGED_IN)
            prefs.remove(KEY_LAST_SYNC)
        }
    }

    // Flow getters
    val isLoggedInFlow: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[KEY_IS_LOGGED_IN] ?: false }

    val autoNextEpisodeFlow: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[KEY_AUTO_NEXT_EPISODE] ?: true }

    val lastSyncFlow: Flow<Long> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[KEY_LAST_SYNC] ?: 0L }

    fun getLastSync(): Long = runBlocking {
        dataStore.data.catch { emit(emptyPreferences()) }.first()[KEY_LAST_SYNC] ?: 0L
    }

    fun getAutoNextEpisode(): Boolean = runBlocking {
        dataStore.data.catch { emit(emptyPreferences()) }.first()[KEY_AUTO_NEXT_EPISODE] ?: true
    }
}
