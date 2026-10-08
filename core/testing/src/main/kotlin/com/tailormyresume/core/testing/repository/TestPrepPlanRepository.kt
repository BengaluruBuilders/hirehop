package com.tailormyresume.core.testing.repository

import com.tailormyresume.core.data.repository.PrepPlanRepository
import com.tailormyresume.core.model.PrepPlanItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class TestPrepPlanRepository : PrepPlanRepository {

    private val plans = MutableStateFlow<Map<String, List<PrepPlanItem>>>(emptyMap())

    override fun observeItems(applicationId: String): Flow<List<PrepPlanItem>> =
        plans.map { it[applicationId].orEmpty() }

    override suspend fun add(applicationId: String, item: PrepPlanItem) = change(applicationId) { items ->
        if (items.any { it.id == item.id }) items else items + item
    }

    override suspend fun remove(applicationId: String, itemId: String) =
        change(applicationId) { items -> items.filterNot { it.id == itemId } }

    override suspend fun setDone(applicationId: String, itemId: String, done: Boolean) =
        change(applicationId) { items -> items.map { if (it.id == itemId) it.copy(done = done) else it } }

    override suspend fun clearFor(applicationId: String) {
        plans.update { it - applicationId }
    }

    private fun change(applicationId: String, transform: (List<PrepPlanItem>) -> List<PrepPlanItem>) {
        plans.update { it + (applicationId to transform(it[applicationId].orEmpty())) }
    }
}
