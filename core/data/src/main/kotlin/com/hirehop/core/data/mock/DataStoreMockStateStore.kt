package com.hirehop.core.data.mock

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DataStoreMockStateStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : MockStateStore {

    override fun observe(key: String): Flow<String?> =
        dataStore.data.map { preferences -> preferences[stringPreferencesKey(key)] }.distinctUntilChanged()

    override suspend fun read(key: String): String? = observe(key).first()

    override suspend fun write(key: String, value: String) {
        dataStore.edit { preferences -> preferences[stringPreferencesKey(key)] = value }
    }

    override suspend fun remove(key: String) {
        dataStore.edit { preferences -> preferences.remove(stringPreferencesKey(key)) }
    }

    override suspend fun removeWithPrefix(prefix: String) {
        dataStore.edit { preferences ->
            preferences.asMap().keys.filter { it.name.startsWith(prefix) }.forEach { preferences.remove(it) }
        }
    }

    override suspend fun clear() {
        dataStore.edit { preferences -> preferences.clear() }
    }
}
