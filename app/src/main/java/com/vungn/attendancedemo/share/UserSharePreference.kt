package com.vungn.attendancedemo.share

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class UserSharePreference @Inject constructor(private val dataStore: DataStore<Preferences>) {
    private val _accessToken = dataStore.data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { pref: Preferences ->
        val token: String? = pref[PreferenceKeys.ACCESS_TOKEN]
        token
    }

    val accessToken = _accessToken

    suspend fun save(token: String) {
        dataStore.edit { pref ->
            pref[PreferenceKeys.ACCESS_TOKEN] = token
        }
    }
}