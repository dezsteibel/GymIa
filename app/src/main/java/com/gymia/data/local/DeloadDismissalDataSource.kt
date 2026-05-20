package com.gymia.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val SEVEN_DAYS_MS = 7L * 24 * 60 * 60 * 1000

class DeloadDismissalDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val dismissedUntilKey = longPreferencesKey("deload_dismissed_until_ms")

    fun isDismissed(): Flow<Boolean> = dataStore.data.map { prefs ->
        System.currentTimeMillis() < (prefs[dismissedUntilKey] ?: 0L)
    }

    suspend fun dismissFor7Days() {
        dataStore.edit { prefs ->
            prefs[dismissedUntilKey] = System.currentTimeMillis() + SEVEN_DAYS_MS
        }
    }
}
