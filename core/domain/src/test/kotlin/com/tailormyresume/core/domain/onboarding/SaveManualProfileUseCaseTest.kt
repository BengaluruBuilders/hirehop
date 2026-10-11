package com.tailormyresume.core.domain.onboarding

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.fact.FactIdAllocator
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.core.testing.repository.TestProfileRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SaveManualProfileUseCaseTest {

    private val repository = TestProfileRepository()
    private val useCase = SaveManualProfileUseCase(repository, FactIdAllocator())

    private val contact = ManualContact(
        fullName = "  Priya Deshmukh ",
        email = "priya@example.com",
        phone = " +91 98000 00000",
        city = "Pune ",
        jobTitle = " Business Analyst",
        company = "Infosys ",
    )

    @Test
    fun savesContactFieldsAndOneUserStatedEntryWithAllocatedId() = runTest {
        val returned = useCase(contact)

        val saved = checkNotNull(repository.observeProfile().first())
        assertThat(returned).isEqualTo(saved)
        assertThat(saved.fullName).isEqualTo("Priya Deshmukh")
        assertThat(saved.email).isEqualTo("priya@example.com")
        assertThat(saved.phone).isEqualTo("+91 98000 00000")
        assertThat(saved.city).isEqualTo("Pune")
        val entry = saved.entries.single()
        assertThat(entry.id).isEqualTo("W-01")
        assertThat(entry.category).isEqualTo(EntryCategory.EXPERIENCE)
        assertThat(entry.title).isEqualTo("Business Analyst")
        assertThat(entry.organization).isEqualTo("Infosys")
        assertThat(entry.source).isEqualTo(FactSource.USER_STATED)
        assertThat(saved.reviewedAt).isNull()
    }

    @Test
    fun blankRoleSavesNoEntry() = runTest {
        useCase(contact.copy(jobTitle = "  ", company = ""))

        val saved = checkNotNull(repository.observeProfile().first())
        assertThat(saved.entries).isEmpty()
        assertThat(saved.fullName).isEqualTo("Priya Deshmukh")
    }

    @Test
    fun keepsExistingUserFacts() = runTest {
        val kept = ProfileEntry(
            "W-01", EntryCategory.EXPERIENCE, "Kept role", "Acme", "2022", "2024", emptyList(),
            FactSource.USER_STATED, isConfirmed = true,
        )
        repository.sendProfile(
            CandidateProfile("Old", "", "", "", listOf("Kotlin"), listOf(kept), userStatedSkills = listOf("Kotlin")),
        )

        useCase(contact)

        val saved = checkNotNull(repository.observeProfile().first())
        assertThat(saved.entries.map { it.id }).containsExactly("W-01", "W-02").inOrder()
        assertThat(saved.entries.first()).isEqualTo(kept)
        assertThat(saved.skills).containsExactly("Kotlin")
        assertThat(saved.userStatedSkills).containsExactly("Kotlin")
    }
}
