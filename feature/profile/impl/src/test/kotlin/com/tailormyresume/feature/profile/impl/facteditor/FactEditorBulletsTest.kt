package com.tailormyresume.feature.profile.impl.facteditor

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.IdGenerator
import com.tailormyresume.core.domain.fact.FactDraftErrorReason
import com.tailormyresume.core.domain.fact.FactField
import com.tailormyresume.core.domain.fact.FactIdAllocator
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.core.model.ProfileLimits
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.data.sampleProfile
import com.tailormyresume.core.testing.data.sampleProjectEntry
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.feature.profile.api.navigation.FactEditorNavKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FactEditorBulletsTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestProfileRepository()
    private var nextBulletId = 0

    private val threeBullets = sampleProjectEntry.copy(
        id = "C-02",
        source = FactSource.IMPORTED,
        isConfirmed = true,
        bullets = listOf(
            EvidenceBullet("answer-1", "Built the first screen."),
            EvidenceBullet("answer-2", "Shipped it to 40 classmates."),
            EvidenceBullet("answer-3", "Cut load time by half."),
        ),
    )

    @Before
    fun setup() {
        repository.sendProfile(sampleProfile.copy(entries = listOf(threeBullets)))
    }

    private fun createViewModel(handle: SavedStateHandle = SavedStateHandle()) = FactEditorViewModel(
        profileRepository = repository,
        factIdAllocator = FactIdAllocator(),
        idGenerator = IdGenerator { "new-${nextBulletId++}" },
        connectivityMonitor = TestConnectivityMonitor(),
        key = FactEditorNavKey(entryId = threeBullets.id, entryType = "project", scenario = DebugScenario.DEFAULT),
        savedState = handle,
    )

    private suspend fun savedEntry(): ProfileEntry =
        checkNotNull(repository.observeProfile().first()).entries.single { it.id == threeBullets.id }

    @Test
    fun editingAFactWithThreeBulletsShowsThemSeparately() {
        val draft = createViewModel().uiState.value.draft

        assertThat(draft.detail).isEqualTo("Built the first screen.")
        assertThat(draft.moreBullets.map { it.text })
            .containsExactly("Shipped it to 40 classmates.", "Cut load time by half.").inOrder()
    }

    @Test
    fun savingAnUnchangedFactLeavesItIdentical() = runTest {
        val viewModel = createViewModel()

        viewModel.save()

        assertThat(savedEntry()).isEqualTo(threeBullets)
    }

    @Test
    fun savingAfterEditingOneBulletKeepsThreeBulletsWithTheirIds() = runTest {
        val viewModel = createViewModel()

        viewModel.onMoreBulletChange(0, "Shipped it to 45 classmates.")
        viewModel.save()

        val saved = savedEntry()
        assertThat(saved.bullets.map { it.id }).containsExactly("answer-1", "answer-2", "answer-3").inOrder()
        assertThat(saved.bullets.map { it.text }).containsExactly(
            "Built the first screen.",
            "Shipped it to 45 classmates.",
            "Cut load time by half.",
        ).inOrder()
        assertThat(saved.source).isEqualTo(FactSource.USER_EDITED)
    }

    @Test
    fun anExtraBulletOverTheBulletLimitBlocksSaving() = runTest {
        val viewModel = createViewModel()

        viewModel.onMoreBulletChange(1, "x".repeat(ProfileLimits.MAX_BULLET_LENGTH + 1))
        viewModel.save()

        assertThat(viewModel.uiState.value.fieldErrors[FactField.DETAIL]).isEqualTo(FactDraftErrorReason.TOO_LONG)
        assertThat(savedEntry()).isEqualTo(threeBullets)
    }

    @Test
    fun editedExtraBulletsSurviveProcessDeath() {
        val handle = SavedStateHandle()
        createViewModel(handle).onMoreBulletChange(1, "Cut load time by 60 percent.")

        val restarted = SavedStateHandle(handle.keys().associateWith { handle.get<Any>(it) })
        val draft = createViewModel(restarted).uiState.value.draft

        assertThat(draft.moreBullets.map { it.id }).containsExactly("answer-2", "answer-3").inOrder()
        assertThat(draft.moreBullets.map { it.text })
            .containsExactly("Shipped it to 40 classmates.", "Cut load time by 60 percent.").inOrder()
    }

    @Test
    fun aStoredBulletOverTheLimitShowsTheTooLongErrorAndBlocksSaving() {
        val tooLong = threeBullets.bullets[2].copy(text = "x".repeat(ProfileLimits.MAX_BULLET_LENGTH + 1))
        repository.sendProfile(sampleProfile.copy(entries = listOf(threeBullets.copy(bullets = threeBullets.bullets.dropLast(1) + tooLong))))

        val state = createViewModel().uiState.value

        assertThat(state.visibleReasonFor(FactField.DETAIL)).isEqualTo(FactDraftErrorReason.TOO_LONG)
        assertThat(state.isSaveEnabled).isFalse()
    }

    @Test
    fun savingAPendingImportedFactWithoutChangesConfirmsItAndKeepsItImported() = runTest {
        repository.sendProfile(sampleProfile.copy(entries = listOf(threeBullets.copy(isConfirmed = false))))
        val viewModel = createViewModel()

        viewModel.save()

        assertThat(savedEntry()).isEqualTo(threeBullets.copy(isConfirmed = true))
    }

    @Test
    fun aTrailingSpaceInAnExtraBulletLeavesTheFactUnchanged() = runTest {
        val viewModel = createViewModel()

        viewModel.onMoreBulletChange(1, "Cut load time by half. ")
        viewModel.save()

        assertThat(savedEntry()).isEqualTo(threeBullets)
    }

    @Test
    fun onlyTheOverLimitBulletShowsTheTooLongReason() {
        val tooLong = "x".repeat(ProfileLimits.MAX_BULLET_LENGTH + 1)
        val viewModel = createViewModel()
        viewModel.onMoreBulletChange(1, tooLong)
        viewModel.onDetailChange(viewModel.uiState.value.draft.detail)
        val state = viewModel.uiState.value

        assertThat(state.visibleBulletReason(tooLong)).isEqualTo(FactDraftErrorReason.TOO_LONG)
        assertThat(state.visibleBulletReason(state.draft.detail)).isNull()
        assertThat(state.visibleBulletReason(state.draft.moreBullets[0].text)).isNull()
    }
}
