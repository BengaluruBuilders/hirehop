package com.hirehop.app.ai

import com.hirehop.core.data.mock.MockStateStore
import com.hirehop.core.domain.IdGenerator
import com.hirehop.core.model.EntryCategory
import javax.inject.Inject

class PendingTailoringIds @Inject constructor(
    private val store: MockStateStore,
    private val ids: IdGenerator,
) {
    suspend fun idFor(applicationId: String, section: EntryCategory?): String {
        val key = keyOf(applicationId, section)
        return store.read(key) ?: ids.newId().also { store.write(key, it) }
    }

    suspend fun clear(applicationId: String, section: EntryCategory?) = store.remove(keyOf(applicationId, section))

    private fun keyOf(applicationId: String, section: EntryCategory?) =
        "tailoring.request.$applicationId.${section?.name ?: "all"}"
}
