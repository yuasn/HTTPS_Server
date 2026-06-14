package com.example.httpsserveryuasn.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    companion object {
        val PORT = intPreferencesKey("port")
        val WEB_DIR_PATH = stringPreferencesKey("web_dir_path")
        val USE_SELF_SIGNED = booleanPreferencesKey("use_self_signed")
        val CERT_FILE_PATH = stringPreferencesKey("cert_file_path")
    }

    val portFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PORT] ?: 8443
    }

    val webDirPathFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[WEB_DIR_PATH] ?: context.getExternalFilesDir(null)?.absolutePath ?: ""
    }

    val useSelfSignedFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_SELF_SIGNED] ?: true
    }

    val certFilePathFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CERT_FILE_PATH] ?: ""
    }

    suspend fun updatePort(port: Int) {
        context.dataStore.edit { preferences ->
            preferences[PORT] = port
        }
    }

    suspend fun updateWebDirPath(path: String) {
        context.dataStore.edit { preferences ->
            preferences[WEB_DIR_PATH] = path
        }
    }

    suspend fun updateUseSelfSigned(use: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_SELF_SIGNED] = use
        }
    }

    suspend fun updateCertFilePath(path: String) {
        context.dataStore.edit { preferences ->
            preferences[CERT_FILE_PATH] = path
        }
    }
}
