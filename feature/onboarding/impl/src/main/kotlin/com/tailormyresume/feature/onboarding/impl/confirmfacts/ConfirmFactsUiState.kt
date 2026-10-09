package com.tailormyresume.feature.onboarding.impl.confirmfacts

import com.tailormyresume.core.domain.fact.FactDisplayIds
import com.tailormyresume.core.domain.fact.FactLineRenderer
import com.tailormyresume.core.domain.onboarding.OnboardingStep
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry

enum class ConfirmFactsSection { Education, Experience, Projects, Skills, Certifications, Extras }

data class ContactUi(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
) {
    val isEmpty: Boolean get() = fullName.isBlank() && email.isBlank() && phone.isBlank()
}

data class ConfirmFactUi(
    val id: String,
    val section: ConfirmFactsSection,
    val title: String,
    val detail: String,
    val source: FactSource,
    val isConfirmed: Boolean,
    val displayId: String = id,
)

data class ConfirmFactsSectionUi(
    val section: ConfirmFactsSection,
    val facts: List<ConfirmFactUi> = emptyList(),
    val skills: List<String> = emptyList(),
) {
    val count: Int get() = if (section == ConfirmFactsSection.Skills) skills.size else facts.size
    val isEmpty: Boolean get() = count == 0
}

data class PendingEdit(
    val factId: String?,
    val category: EntryCategory,
)

data class ConfirmFactsUiState(
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val isSaving: Boolean = false,
    val hasSaveFailed: Boolean = false,
    val contact: ContactUi = ContactUi(),
    val sections: List<ConfirmFactsSectionUi> = emptyList(),
    val skippedSections: Set<ConfirmFactsSection> = emptySet(),
    val pendingEdit: PendingEdit? = null,
    val nextStep: OnboardingStep? = null,
    val showsRemovedBanner: Boolean = true,
) {
    val facts: List<ConfirmFactUi> get() = sections.flatMap(ConfirmFactsSectionUi::facts)
    val skills: List<String>
        get() = sections.firstOrNull { it.section == ConfirmFactsSection.Skills }?.skills.orEmpty()
    val visibleSections: List<ConfirmFactsSectionUi>
        get() = sections.filter { it.section !in skippedSections || !it.isEmpty }
    val totalCount: Int get() = facts.size
    val confirmedCount: Int get() = facts.count(ConfirmFactUi::isConfirmed)
    val openCount: Int get() = totalCount - confirmedCount
    val canContinue: Boolean get() = confirmedCount > 0
    val isEmpty: Boolean get() = facts.isEmpty() && skills.isEmpty() && contact.isEmpty
    val isFullyConfirmed: Boolean get() = facts.isNotEmpty() && openCount == 0
}

object ConfirmFactsScenarioMapper {

    fun seed(scenario: DebugScenario): ConfirmFactsUiState = when (scenario) {
        DebugScenario.LOADING -> ConfirmFactsUiState(isLoading = true)
        DebugScenario.OFFLINE -> ConfirmFactsUiState(isOffline = true)
        DebugScenario.ERROR -> ConfirmFactsUiState(hasSaveFailed = true)
        else -> ConfirmFactsUiState()
    }

    fun withProfile(
        state: ConfirmFactsUiState,
        profile: CandidateProfile?,
        scenario: DebugScenario,
    ): ConfirmFactsUiState {
        if (scenario == DebugScenario.EMPTY || profile == null) return state
        val total = profile.entries.size
        val entries = profile.entries.mapIndexed { index, entry ->
            entry.toFactUi(
                isConfirmed = confirmedFor(entry, index, total, scenario),
                scenario = scenario,
                displayId = FactDisplayIds.of(entry, profile.entries),
            )
        }
        return state.copy(
            isLoading = false,
            contact = ContactUi(fullName = profile.fullName, email = profile.email, phone = profile.phone),
            sections = sectionsOf(entries = entries, skills = profile.skills),
            hasSaveFailed = scenario == DebugScenario.ERROR,
        )
    }

    private fun confirmedFor(
        entry: ProfileEntry,
        index: Int,
        total: Int,
        scenario: DebugScenario,
    ): Boolean = when (scenario) {
        DebugScenario.FULLY_CONFIRMED, DebugScenario.USER_STATED -> true
        DebugScenario.PARTLY_CONFIRMED, DebugScenario.PARTIAL, DebugScenario.SUCCESS -> index < total / 2
        else -> entry.isConfirmed
    }

    private fun sectionsOf(
        entries: List<ConfirmFactUi>,
        skills: List<String>,
    ): List<ConfirmFactsSectionUi> = CONFIRM_FACTS_SECTIONS.map { section ->
        ConfirmFactsSectionUi(
            section = section,
            facts = entries.filter { it.section == section },
            skills = if (section == ConfirmFactsSection.Skills) skills else emptyList(),
        )
    }

    private fun ProfileEntry.toFactUi(
        isConfirmed: Boolean,
        scenario: DebugScenario,
        displayId: String,
    ): ConfirmFactUi = ConfirmFactUi(
        id = id,
        section = category.sectionOf(),
        title = title,
        detail = detailOf(this),
        source = if (scenario == DebugScenario.USER_STATED) FactSource.USER_STATED else source,
        isConfirmed = isConfirmed,
        displayId = displayId,
    )
}

val CONFIRM_FACTS_SECTIONS: List<ConfirmFactsSection> = listOf(
    ConfirmFactsSection.Education,
    ConfirmFactsSection.Experience,
    ConfirmFactsSection.Projects,
    ConfirmFactsSection.Skills,
    ConfirmFactsSection.Certifications,
    ConfirmFactsSection.Extras,
)

fun EntryCategory.sectionOf(): ConfirmFactsSection = when (this) {
    EntryCategory.EDUCATION -> ConfirmFactsSection.Education
    EntryCategory.EXPERIENCE -> ConfirmFactsSection.Experience
    EntryCategory.PROJECT -> ConfirmFactsSection.Projects
    EntryCategory.CERTIFICATION -> ConfirmFactsSection.Certifications
    EntryCategory.ACHIEVEMENT -> ConfirmFactsSection.Extras
}

private fun detailOf(entry: ProfileEntry): String =
    FactLineRenderer.render(entry).removePrefix(entry.title).trimStart(' ', '·')
