package com.hirehop.core.testing.repository

import com.hirehop.core.data.repository.TailoringReviewStateRepository
import com.hirehop.core.model.TailoringReviewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class TestTailoringReviewStateRepository : TailoringReviewStateRepository {

    private val states = MutableStateFlow<Map<String, TailoringReviewState>>(emptyMap())

    override fun observe(applicationId: String): Flow<TailoringReviewState> =
        states.map { it[applicationId] ?: TailoringReviewState() }

    override suspend fun recordRegeneration(applicationId: String, section: String) = change(applicationId) { state ->
        state.copy(regenerationsBySection = state.regenerationsBySection + (section to state.regenerationsUsedIn(section) + 1))
    }

    override suspend fun markEdited(applicationId: String, bulletId: String) =
        change(applicationId) { state -> state.copy(editedBulletIds = state.editedBulletIds + bulletId) }

    override suspend fun clearEdited(applicationId: String, bulletIds: Collection<String>) =
        change(applicationId) { state -> state.copy(editedBulletIds = state.editedBulletIds - bulletIds.toSet()) }

    override suspend fun clearFor(applicationId: String) {
        states.update { it - applicationId }
    }

    private fun change(applicationId: String, transform: (TailoringReviewState) -> TailoringReviewState) {
        states.update { it + (applicationId to transform(it[applicationId] ?: TailoringReviewState())) }
    }
}
