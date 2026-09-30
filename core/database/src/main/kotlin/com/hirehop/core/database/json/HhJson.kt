package com.hirehop.core.database.json

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

internal object HhJson {
    val format = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun <T> encode(serializer: KSerializer<T>, value: T): String =
        format.encodeToString(serializer, value)

    fun <T> decode(serializer: KSerializer<T>, text: String): T =
        format.decodeFromString(serializer, text)
}
