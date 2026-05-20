package com.gymia.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.gymia.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class UserProfileRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val json: Json
) {
    private val profileKey = stringPreferencesKey("user_profile")

    fun getProfile(): Flow<UserProfile> = dataStore.data.map { prefs ->
        val raw = prefs[profileKey] ?: return@map UserProfile()
        runCatching { json.decodeFromString<UserProfile>(raw) }.getOrDefault(UserProfile())
    }

    suspend fun saveProfile(profile: UserProfile) {
        dataStore.edit { prefs -> prefs[profileKey] = json.encodeToString(profile) }
    }
}
