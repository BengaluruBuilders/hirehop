package com.tailormyresume.feature.profile.impl.facteditor

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.IdGenerator
import com.tailormyresume.core.domain.fact.FactIdAllocator
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.data.sampleProfile
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.feature.profile.api.navigation.FactEditorNavKey
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FactEditorSavedStateTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestProfileRepository()

    @Before
    fun setup() {
        repository.sendProfile(sampleProfile)
    }

    private fun viewModel(handle: SavedStateHandle, entryId: String? = null) = FactEditorViewModel(
        profileRepository = repository,
        factIdAllocator = FactIdAllocator(),
        idGenerator = IdGenerator { "bullet-1" },
        connectivityMonitor = TestConnectivityMonitor(),
        key = FactEditorNavKey(entryId = entryId, entryType = "project", scenario = DebugScenario.DEFAULT),
        savedState = handle,
    )

    private fun restarted(handle: SavedStateHandle) =
        SavedStateHandle(handle.keys().associateWith { handle.get<Any>(it) })

    @Test
    fun theTypedDraftSurvivesProcessDeath() {
        val handle = SavedStateHandle()
        val before = viewModel(handle)
        before.onTitleChange("Placement dashboard")
        before.onDetailChange("Cut report time by 40 percent")
        before.onToolsChange("Power BI")
        before.onStartDateChange("Jan 2024")
        before.onEndDateChange("Apr 2024")

        val draft = viewModel(restarted(handle)).uiState.value.draft

        assertThat(draft.title).isEqualTo("Placement dashboard")
        assertThat(draft.detail).isEqualTo("Cut report time by 40 percent")
        assertThat(draft.organization).isEqualTo("Power BI")
        assertThat(draft.startDate).isEqualTo("Jan 2024")
        assertThat(draft.endDate).isEqualTo("Apr 2024")
    }

    @Test
    fun theTypedDraftWinsOverTheStoredEntryItEdits() {
        val entry = sampleProfile.entries.first()
        val handle = SavedStateHandle()
        viewModel(handle, entry.id).onTitleChange("Edited title")

        val state = viewModel(restarted(handle), entry.id).uiState.value

        assertThat(state.draft.title).isEqualTo("Edited title")
        assertThat(state.factId).isEqualTo(entry.id)
    }

    @Test
    fun aFreshEditorStartsFromTheStoredEntry() {
        val entry = sampleProfile.entries.first()

        val state = viewModel(SavedStateHandle(), entry.id).uiState.value

        assertThat(state.draft.title).isEqualTo(entry.title)
    }
}
