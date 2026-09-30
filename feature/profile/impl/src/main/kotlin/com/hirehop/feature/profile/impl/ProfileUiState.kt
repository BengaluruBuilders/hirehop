package com.hirehop.feature.profile.impl

import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.FactSource

enum class ProfileSectionKind {
    Education,
    Experience,
    Projects,
    Skills,
    Certifications,
    Extras,
}

data class ProfileFactRef(
    val id: String,
    val source: FactSource,
    val isConfirmed: Boolean,
)

data class ProfileSection(
    val kind: ProfileSectionKind,
    val count: Int,
    val facts: List<ProfileFactRef>,
) {
    val confirmedCount: Int get() = facts.count { it.isConfirmed }

    val userStatedCount: Int get() = count - confirmedCount
}

data class ProfileOverviewState(
    val headlineLine: String,
    val factCount: Int,
    val confirmedCount: Int,
    val userStatedCount: Int,
    val unconfirmedCount: Int,
    val firstUnconfirmedId: String?,
    val sections: List<ProfileSection>,
) {
    val isFullyConfirmed: Boolean get() = unconfirmedCount == 0

    companion object {
        fun of(
            profile: CandidateProfile,
            unconfirmedCount: Int = profile.unconfirmedCount(),
        ): ProfileOverviewState = ProfileOverviewState(
            headlineLine = listOf(profile.fullName, profile.headline)
                .filter { it.isNotBlank() }
                .joinToString(separator = " · "),
            factCount = profile.skills.size + profile.entries.size,
            confirmedCount = profile.skills.size + profile.entries.count {
                it.isConfirmed && it.source != FactSource.USER_STATED
            },
            userStatedCount = profile.entries.count {
                it.isConfirmed && it.source == FactSource.USER_STATED
            },
            unconfirmedCount = unconfirmedCount,
            firstUnconfirmedId = profile.entries.firstOrNull { !it.isConfirmed }?.id,
            sections = profile.sections(),
        )

        private fun CandidateProfile.sections(): List<ProfileSection> = listOf(
            ProfileSectionKind.Education to section(EntryCategory.EDUCATION),
            ProfileSectionKind.Experience to section(EntryCategory.EXPERIENCE),
            ProfileSectionKind.Projects to section(EntryCategory.PROJECT),
            ProfileSectionKind.Skills to ProfileSection(
                kind = ProfileSectionKind.Skills,
                count = skills.size,
                facts = skills.mapIndexed { index, skill ->
                    ProfileFactRef(
                        id = skill,
                        source = FactSource.USER_STATED,
                        isConfirmed = true,
                    )
                },
            ),
            ProfileSectionKind.Certifications to section(EntryCategory.CERTIFICATION),
            ProfileSectionKind.Extras to section(EntryCategory.ACHIEVEMENT),
        ).mapNotNull { (kind, section) -> section.takeIf { it.count > 0 } }

        private fun CandidateProfile.section(category: EntryCategory): ProfileSection {
            val matching = entries.filter { it.category == category }
            return ProfileSection(
                kind = category.sectionKind(),
                count = matching.size,
                facts = matching.map { entry ->
                    ProfileFactRef(
                        id = entry.id,
                        source = entry.source,
                        isConfirmed = entry.isConfirmed,
                    )
                },
            )
        }

        private fun EntryCategory.sectionKind(): ProfileSectionKind = when (this) {
            EntryCategory.EDUCATION -> ProfileSectionKind.Education
            EntryCategory.EXPERIENCE -> ProfileSectionKind.Experience
            EntryCategory.PROJECT -> ProfileSectionKind.Projects
            EntryCategory.CERTIFICATION -> ProfileSectionKind.Certifications
            EntryCategory.ACHIEVEMENT -> ProfileSectionKind.Extras
        }
    }
}

sealed interface ProfileUiState {
    data object Loading : ProfileUiState

    data object Empty : ProfileUiState

    data object Failure : ProfileUiState

    data class Success(
        val profile: CandidateProfile,
        val unconfirmedCount: Int,
        val isOffline: Boolean = false,
        val overview: ProfileOverviewState = ProfileOverviewState.of(profile, unconfirmedCount),
    ) : ProfileUiState
}
