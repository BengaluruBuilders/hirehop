package com.tailormyresume.app.ai

import com.tailormyresume.core.data.mock.MockStateStore
import com.tailormyresume.core.domain.IdGenerator
import javax.inject.Inject

class PendingTailoringIds @Inject constructor(
    private val store: MockStateStore,
    private val ids: IdGenerator,
) {
    suspend fun idFor(runId: String): String {
        val key = keyOf(runId)
        return store.read(key) ?: ids.newId().also { store.write(key, it) }
    }

    suspend fun clear(runId: String) = store.remove(keyOf(runId))

    private fun keyOf(runId: String) = "tailoring.request.$runId"
}
