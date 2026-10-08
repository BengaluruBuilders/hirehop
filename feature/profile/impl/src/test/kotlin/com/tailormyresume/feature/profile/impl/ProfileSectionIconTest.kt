package com.tailormyresume.feature.profile.impl

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ProfileSectionIconTest {

    @Test
    fun icon_isDistinctPerKind() {
        val icons = ProfileSectionKind.entries.map { it.icon() }

        assertThat(icons.toSet()).hasSize(ProfileSectionKind.entries.size)
    }

    @Test
    fun titleRes_isDistinctPerKind() {
        val titles = ProfileSectionKind.entries.map { it.titleRes() }

        assertThat(titles).containsNoDuplicates()
    }
}
