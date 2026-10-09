package com.tailormyresume.core.domain.fact

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.core.model.continues
import org.junit.Test

class ProfileEntryContinuesTest {
    private val first = entry()

    private fun entry(
        id: String = "W-01",
        category: EntryCategory = EntryCategory.EXPERIENCE,
        title: String = "Intern",
        organization: String = "Acme",
        startDate: String = "2024",
        endDate: String = "2025",
    ) = ProfileEntry(id, category, title, organization, startDate, endDate, emptyList(), FactSource.IMPORTED, false)

    @Test
    fun sameHeaderContinuesEvenWithPaddingAndANewId() {
        assertThat(entry(id = "W-02", title = " Intern ").continues(first)).isTrue()
    }

    @Test
    fun anyDifferentHeaderPartIsAnotherRole() {
        assertThat(entry(title = "Analyst").continues(first)).isFalse()
        assertThat(entry(organization = "Beta").continues(first)).isFalse()
        assertThat(entry(startDate = "2025").continues(first)).isFalse()
        assertThat(entry(endDate = "2026").continues(first)).isFalse()
        assertThat(entry(category = EntryCategory.PROJECT).continues(first)).isFalse()
    }
}
