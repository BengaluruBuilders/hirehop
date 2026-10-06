package com.hirehop.feature.profile.impl

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.designsystem.component.HhAccent
import org.junit.Test

class ProfileSectionAccentTest {

    @Test
    fun sectionAccent_isFixedPerKind() {
        assertThat(sectionAccent(ProfileSectionKind.Education)).isEqualTo(HhAccent.Coral)
        assertThat(sectionAccent(ProfileSectionKind.Experience)).isEqualTo(HhAccent.Jade)
        assertThat(sectionAccent(ProfileSectionKind.Projects)).isEqualTo(HhAccent.Marigold)
        assertThat(sectionAccent(ProfileSectionKind.Skills)).isEqualTo(HhAccent.Coral)
        assertThat(sectionAccent(ProfileSectionKind.Certifications)).isEqualTo(HhAccent.Jade)
        assertThat(sectionAccent(ProfileSectionKind.Extras)).isEqualTo(HhAccent.Marigold)
    }

    @Test
    fun monogramRes_isDistinctPerKind() {
        val monograms = ProfileSectionKind.entries.map { it.monogramRes() }

        assertThat(monograms).containsNoDuplicates()
    }
}
