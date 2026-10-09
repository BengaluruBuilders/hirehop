package com.tailormyresume.feature.profile.impl

import com.tailormyresume.core.domain.fact.FactDisplayIds
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.factCounts
import com.tailormyresume.core.model.isSkillUserStated
import com.tailormyresume.feature.profile.impl.common.FactStatus
import com.tailormyresume.feature.profile.impl.common.status

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
    val status: FactStatus,
    val displayId: String = id,
)

data class ProfileSection(
    val kind: ProfileSectionKind,
    val facts: List<ProfileFactRef>,
) {
    val count: Int get() = facts.size

    val confirmedCount: Int get() = facts.count { it.status != FactStatus.ToConfirm && it.status != FactStatus.UserStated }

    val userStatedCount: Int get() = facts.count { it.status == FactStatus.UserStated }

    val toConfirmCount: Int get() = facts.count { it.status == FactStatus.ToConfirm }
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
    companion object {
        fun of(profile: CandidateProfile): ProfileOverviewState {
            val counts = profile.factCounts()
            return ProfileOverviewState(
                headlineLine = listOf(profile.fullName, profile.headline)
                    .filter { it.isNotBlank() }
                    .joinToString(separator = HEADLINE_SEPARATOR),
                factCount = counts.total,
                confirmedCount = counts.confirmed,
                userStatedCount = counts.userStated,
                unconfirmedCount = profile.unconfirmedCount(),
                firstUnconfirmedId = profile.entries.firstOrNull { !it.isConfirmed }?.id,
                sections = profile.sections(),
            )
        }

        private fun CandidateProfile.sections(): List<ProfileSection> = listOf(
            entrySection(ProfileSectionKind.Education, EntryCategory.EDUCATION),
            entrySection(ProfileSectionKind.Experience, EntryCategory.EXPERIENCE),
            entrySection(ProfileSectionKind.Projects, EntryCategory.PROJECT),
            ProfileSection(
                kind = ProfileSectionKind.Skills,
                facts = skills.mapIndexed { index, skill -> ProfileFactRef(id = skillId(index), status = skillStatus(skill)) },
            ),
            entrySection(ProfileSectionKind.Certifications, EntryCategory.CERTIFICATION),
            entrySection(ProfileSectionKind.Extras, EntryCategory.ACHIEVEMENT),
        ).filter { it.count > 0 }

        private fun CandidateProfile.entrySection(
            kind: ProfileSectionKind,
            category: EntryCategory,
        ): ProfileSection = ProfileSection(
            kind = kind,
            facts = entries.filter { it.category == category }.map {
                ProfileFactRef(it.id, it.status(), FactDisplayIds.of(it, entries))
            },
        )

        private const val HEADLINE_SEPARATOR = " · "
    }
}

internal fun CandidateProfile.skillStatus(skill: String): FactStatus =
    if (isSkillUserStated(skill)) FactStatus.UserStated else FactStatus.Confirmed

fun skillId(index: Int): String = "S-" + (index + 1).toString().padStart(2, '0')

fun ProfileSectionKind.entryCategory(): EntryCategory? = when (this) {
    ProfileSectionKind.Education -> EntryCategory.EDUCATION
    ProfileSectionKind.Experience -> EntryCategory.EXPERIENCE
    ProfileSectionKind.Projects -> EntryCategory.PROJECT
    ProfileSectionKind.Skills -> null
    ProfileSectionKind.Certifications -> EntryCategory.CERTIFICATION
    ProfileSectionKind.Extras -> EntryCategory.ACHIEVEMENT
}

sealed interface ProfileUiState {
    data object Loading : ProfileUiState

    data class Empty(val headerLine: String = "") : ProfileUiState

    data object Failure : ProfileUiState

    data class Success(
        val profile: CandidateProfile,
        val isOffline: Boolean = false,
        val overview: ProfileOverviewState = ProfileOverviewState.of(profile),
    ) : ProfileUiState
}
