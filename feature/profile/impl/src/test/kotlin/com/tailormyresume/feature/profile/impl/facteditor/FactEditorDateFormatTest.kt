package com.tailormyresume.feature.profile.impl.facteditor

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.domain.IdGenerator
import com.tailormyresume.core.domain.fact.FactDraftErrorReason
import com.tailormyresume.core.domain.fact.FactField
import com.tailormyresume.core.domain.fact.FactIdAllocator
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.data.sampleProfile
import com.tailormyresume.core.testing.data.sampleProjectEntry
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.feature.profile.api.navigation.FactEditorNavKey
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FactEditorDateFormatTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestProfileRepository()
    private val connectivity = TestConnectivityMonitor()
    private val allocator = FactIdAllocator()
    private var nextBulletId = 0
    private val idGenerator = IdGenerator { "bullet-${nextBulletId++}" }

    @Before
    fun setup() {
        repository.sendProfile(sampleProfile.copy(entries = listOf(sampleProjectEntry)))
    }

    private fun createViewModel(
        entryId: String? = null,
        entryType: String = "project",
        scenario: DebugScenario = DebugScenario.DEFAULT,
        profileRepository: ProfileRepository = repository,
    ): FactEditorViewModel {
        return FactEditorViewModel(
            profileRepository = profileRepository,
            factIdAllocator = allocator,
            idGenerator = idGenerator,
            connectivityMonitor = connectivity,
            key = FactEditorNavKey(entryId = entryId, entryType = entryType, scenario = scenario),
        )
    }

    @Test
    fun unreadableEndDateShowsFormatErrorAndBlocksSave() {
        val viewModel = createViewModel()

        viewModel.onTitleChange("Dashboard")
        viewModel.onEndDateChange("Aug 20242023")

        val state = viewModel.uiState.value
        assertThat(state.fieldErrors[FactField.END_DATE]).isEqualTo(FactDraftErrorReason.INVALID_DATE)
        assertThat(state.isSaveEnabled).isFalse()
        assertThat(state.visibleReasonFor(FactField.END_DATE)).isEqualTo(FactDraftErrorReason.INVALID_DATE)
    }

    @Test
    fun unreadableStartDateBlocksSave() {
        val viewModel = createViewModel()

        viewModel.onTitleChange("Dashboard")
        viewModel.onStartDateChange("Aug 20242")

        val state = viewModel.uiState.value
        assertThat(state.fieldErrors[FactField.START_DATE]).isEqualTo(FactDraftErrorReason.INVALID_DATE)
        assertThat(state.isSaveEnabled).isFalse()
    }

    @Test
    fun readableDatesLeaveSaveEnabled() {
        val viewModel = createViewModel()

        viewModel.onTitleChange("Dashboard")
        viewModel.onStartDateChange("Aug 2023")
        viewModel.onEndDateChange("Present")

        val state = viewModel.uiState.value
        assertThat(state.fieldErrors).isEmpty()
        assertThat(state.isSaveEnabled).isTrue()
    }

    @Test
    fun fixingTheDateClearsTheError() {
        val viewModel = createViewModel()

        viewModel.onTitleChange("Dashboard")
        viewModel.onEndDateChange("Aug 20242023")

        viewModel.onEndDateChange("Aug 2024")

        assertThat(viewModel.uiState.value.fieldErrors).isEmpty()
    }
}
