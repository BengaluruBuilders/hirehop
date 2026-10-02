package com.hirehop.core.data.repository

import com.hirehop.core.data.mock.MockStateStore
import com.hirehop.core.data.mock.observeValue
import com.hirehop.core.data.mock.readValue
import com.hirehop.core.data.mock.writeValue
import com.hirehop.core.model.PrepPlanItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class StoredPrepPlanRepository @Inject constructor(
    private val store: MockStateStore,
) : PrepPlanRepository {

    private val mutex = Mutex()
    private val serializer = ListSerializer(PrepItemDto.serializer())

    override fun observeItems(applicationId: String): Flow<List<PrepPlanItem>> =
        store.observeValue(keyOf(applicationId), serializer).map { dtos -> dtos.orEmpty().map(PrepItemDto::toModel) }

    override suspend fun add(applicationId: String, item: PrepPlanItem) =
        update(applicationId) { items -> if (items.any { it.id == item.id }) items else items + item.toDto() }

    override suspend fun remove(applicationId: String, itemId: String) =
        update(applicationId) { items -> items.filterNot { it.id == itemId } }

    override suspend fun setDone(applicationId: String, itemId: String, done: Boolean) =
        update(applicationId) { items -> items.map { if (it.id == itemId) it.copy(done = done) else it } }

    override suspend fun clearFor(applicationId: String) {
        mutex.withLock { store.remove(keyOf(applicationId)) }
    }

    private suspend fun update(applicationId: String, transform: (List<PrepItemDto>) -> List<PrepItemDto>) {
        mutex.withLock {
            val key = keyOf(applicationId)
            val next = transform(store.readValue(key, serializer).orEmpty())
            if (next.isEmpty()) store.remove(key) else store.writeValue(key, serializer, next)
        }
    }

    private fun keyOf(applicationId: String) = "prep.plan.$applicationId"
}

@Serializable
private data class PrepItemDto(val id: String, val text: String, val done: Boolean = false) {
    fun toModel() = PrepPlanItem(id = id, text = text, done = done)
}

private fun PrepPlanItem.toDto() = PrepItemDto(id = id, text = text, done = done)
