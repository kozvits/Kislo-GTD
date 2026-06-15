package com.kozvits.kislogtd.sync

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.syncStore: androidx.datastore.core.DataStore<Preferences>
        by preferencesDataStore(name = "sync_settings")

data class SyncSettings(
    val isEnabled: Boolean = false,
    val accessToken: String = "",
    val refreshToken: String = "",
    val lastSyncTimestamp: Long = 0L,
    val autoSync: Boolean = true,
    val syncIntervalHours: Int = 24,
    val lastSyncStatus: String = "never"
)

@Singleton
class SyncStateRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_ENABLED = booleanPreferencesKey("sync_enabled")
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val KEY_LAST_SYNC = longPreferencesKey("last_sync_timestamp")
        private val KEY_AUTO_SYNC = booleanPreferencesKey("auto_sync")
        private val KEY_SYNC_INTERVAL = intPreferencesKey("sync_interval_hours")
        private val KEY_LAST_STATUS = stringPreferencesKey("last_sync_status")
    }

    val settings: Flow<SyncSettings> = context.syncStore.data.map { prefs ->
        SyncSettings(
            isEnabled = prefs[KEY_ENABLED] ?: false,
            accessToken = prefs[KEY_ACCESS_TOKEN] ?: "",
            refreshToken = prefs[KEY_REFRESH_TOKEN] ?: "",
            lastSyncTimestamp = prefs[KEY_LAST_SYNC] ?: 0L,
            autoSync = prefs[KEY_AUTO_SYNC] ?: true,
            syncIntervalHours = prefs[KEY_SYNC_INTERVAL] ?: 24,
            lastSyncStatus = prefs[KEY_LAST_STATUS] ?: "never"
        )
    }

    suspend fun setAccessToken(token: String, refreshToken: String) {
        context.syncStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = token
            prefs[KEY_REFRESH_TOKEN] = refreshToken
            prefs[KEY_ENABLED] = true
        }
    }

    suspend fun setLastSyncTimestamp(timestamp: Long, status: String) {
        context.syncStore.edit { prefs ->
            prefs[KEY_LAST_SYNC] = timestamp
            prefs[KEY_LAST_STATUS] = status
        }
    }

    suspend fun clearAuth() {
        context.syncStore.edit { prefs ->
            prefs.remove(KEY_ACCESS_TOKEN)
            prefs.remove(KEY_REFRESH_TOKEN)
            prefs[KEY_ENABLED] = false
        }
    }

    suspend fun setAutoSync(enabled: Boolean, intervalHours: Int) {
        context.syncStore.edit { prefs ->
            prefs[KEY_AUTO_SYNC] = enabled
            prefs[KEY_SYNC_INTERVAL] = intervalHours
        }
    }
}
