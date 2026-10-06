package com.hirehop.feature.profile.impl

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.designsystem.component.HhAccent
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.ProfileEntry
import org.junit.Test

class ProfileSectionAccentTest {

    @Test
    fun sectionAccent_isFixedPerKind() {
        ProfileSectionKind.entries.forEach { kind ->
            assertThat(sectionAccent(kind)).isEqualTo(sectionAccent(kind))
        }

        assertThat(sectionAccent(ProfileSectionKind.Education)).isEqualTo(HhAccent.Coral)
        assertThat(sectionAccent(ProfileSectionKind.Experience)).isEqualTo(HhAccent.Jade)
        assertThat(sectionAccent(ProfileSectionKind.Projects)).isEqualTo(HhAccent.Marigold)
        assertThat(sectionAccent(ProfileSectionKind.Skills)).isEqualTo(HhAccent.Coral)
        assertThat(sectionAccent(ProfileSectionKind.Certifications)).isEqualTo(HhAccent.Jade)
        assertThat(sectionAccent(ProfileSectionKind.Extras)).isEqualTo(HhAccent.Marigold)
    }

    @Test
    fun sectionAccent_doesNotShiftWhenAnEarlierSectionAppears() {
        val experience = fact("I-01", EntryCategory.EXPERIENCE, "Data Operations Associate, Saffron Retail")
        val project = fact("C-01", EntryCategory.PROJECT, "Placement Stats Dashboard")
        val before = profile(listOf(experience, project))
        val accentBefore = projectsAccent(ProfileOverviewState.of(before))

        val education = fact("U-01", EntryCategory.EDUCATION, "B.Tech Computer Science")
        val after = profile(listOf(education, experience, project))
        val accentAfter = projectsAccent(ProfileOverviewState.of(after))

        assertThat(ProfileOverviewState.of(before).sections.map { it.kind })
            .containsExactly(ProfileSectionKind.Experience, ProfileSectionKind.Projects)
        assertThat(ProfileOverviewState.of(after).sections.map { it.kind })
            .containsExactly(
                ProfileSectionKind.Education,
                ProfileSectionKind.Experience,
                ProfileSectionKind.Projects,
            ).inOrder()
        assertThat(accentAfter).isEqualTo(accentBefore)
    }

    @Test
    fun monogramRes_isDistinctPerKind() {
        val monograms = ProfileSectionKind.entries.map { it.monogramRes() }

        assertThat(monograms).containsNoDuplicates()
    }

    private fun projectsAccent(state: ProfileOverviewState): HhAccent = sectionAccent(
        state.sections.single { it.kind == ProfileSectionKind.Projects }.kind,
    )

    private fun profile(entries: List<ProfileEntry>) = CandidateProfile(
        fullName = "Priya Deshmukh",
        email = "priya.d@example.com",
        phone = "+91 98220 41873",
        headline = "Data Operations Associate",
        skills = emptyList(),
        entries = entries,
    )

    private fun fact(
        id: String,
        category: EntryCategory,
        title: String,
    ) = ProfileEntry(
        id = id,
        category = category,
        title = title,
        organization = "",
        startDate = "",
        endDate = "",
        bullets = emptyList(),
        source = FactSource.IMPORTED,
        isConfirmed = true,
    )
}
