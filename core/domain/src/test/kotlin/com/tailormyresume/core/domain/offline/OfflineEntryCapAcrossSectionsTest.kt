package com.tailormyresume.core.domain.offline

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.ProfileLimits
import com.tailormyresume.core.model.hasTooManyBullets
import kotlinx.coroutines.test.runTest
import org.junit.Test

class OfflineEntryCapAcrossSectionsTest {
    private val parser = OfflineResumeTextParser()

    private fun education(count: Int) = "EDUCATION\n" + (1..count).joinToString("\n") {
        "Degree $it | College $it | 2020 - 2024"
    }

    private fun experience(roles: Int, bullets: Int) = "EXPERIENCE\n" + (1..roles).joinToString("\n") { role ->
        "Role $role | Kiran Agro | Jan 2024 - Mar 2024\n" + (1..bullets).joinToString("\n") { "- Task $it done well." }
    }

    private fun resume(vararg sections: String) = "Priya Deshmukh\n\n" + sections.joinToString("\n\n")

    @Test
    fun entriesInLaterSectionsCountAgainstTheSplit() = runTest {
        val profile = parser.parse(resume(experience(1, 20), education(39)))

        assertThat(profile.entries).hasSize(ProfileLimits.MAX_ENTRIES)
        assertThat(profile.entries.single { it.category == EntryCategory.EXPERIENCE }.hasTooManyBullets).isTrue()
    }

    @Test
    fun entriesInEarlierSectionsCountAgainstTheSplit() = runTest {
        val profile = parser.parse(resume(education(39), experience(1, 20)))

        assertThat(profile.entries).hasSize(ProfileLimits.MAX_ENTRIES)
        assertThat(profile.entries.single { it.category == EntryCategory.EXPERIENCE }.hasTooManyBullets).isTrue()
    }

    @Test
    fun keptEntriesOfTheProfileCountAgainstTheSplit() = runTest {
        val text = resume(education(37), experience(1, 20))

        val withoutKept = parser.parse(text, keptEntries = 0)
        val withKept = parser.parse(text, keptEntries = 3)

        assertThat(withoutKept.entries).hasSize(39)
        assertThat(withKept.entries).hasSize(38)
        assertThat(withKept.entries.single { it.category == EntryCategory.EXPERIENCE }.hasTooManyBullets).isTrue()
    }
}
