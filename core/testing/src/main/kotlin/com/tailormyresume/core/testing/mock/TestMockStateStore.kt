package com.tailormyresume.core.testing.mock

import com.tailormyresume.core.data.mock.MockStateStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class TestMockStateStore : MockStateStore {

    private val values = MutableStateFlow<Map<String, String>>(emptyMap())

    override fun observe(key: String): Flow<String?> = values.map { it[key] }.distinctUntilChanged()

    override suspend fun read(key: String): String? = values.value[key]

    override suspend fun write(key: String, value: String) {
        values.update { it + (key to value) }
    }

    override suspend fun remove(key: String) {
        values.update { it - key }
    }

    override suspend fun removeWithPrefix(prefix: String) {
        values.update { current -> current.filterKeys { !it.startsWith(prefix) } }
    }

    override suspend fun clear() {
        values.value = emptyMap()
    }
}
