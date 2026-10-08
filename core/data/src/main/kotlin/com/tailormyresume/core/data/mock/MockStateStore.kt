package com.tailormyresume.core.data.mock

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

interface MockStateStore {
    fun observe(key: String): Flow<String?>

    suspend fun read(key: String): String?

    suspend fun write(key: String, value: String)

    suspend fun remove(key: String)

    suspend fun removeWithPrefix(prefix: String)

    suspend fun clear()
}

private val mockStateJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

suspend fun <T> MockStateStore.readValue(key: String, serializer: KSerializer<T>): T? =
    read(key)?.let { text -> decodeOrNull(serializer, text) }

fun <T> MockStateStore.observeValue(key: String, serializer: KSerializer<T>): Flow<T?> =
    observe(key).map { text -> text?.let { decodeOrNull(serializer, it) } }

suspend fun <T> MockStateStore.writeValue(key: String, serializer: KSerializer<T>, value: T) {
    write(key, mockStateJson.encodeToString(serializer, value))
}

private fun <T> decodeOrNull(serializer: KSerializer<T>, text: String): T? =
    try {
        mockStateJson.decodeFromString(serializer, text)
    } catch (failure: SerializationException) {
        null
    } catch (failure: IllegalArgumentException) {
        null
    }
