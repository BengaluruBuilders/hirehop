package com.tailormyresume.feature.profile.impl

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.AddUserStatedFactsUseCase
import com.tailormyresume.core.domain.fact.FactDraft
import com.tailormyresume.core.domain.fact.FactDraftErrorReason
import com.tailormyresume.core.domain.fact.FactField
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.testing.data.sampleProfile
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.util.TestIdGenerator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class UserFactWriterTest {

    private val repository = TestProfileRepository()
    private val writer = UserFactWriter(repository, AddUserStatedFactsUseCase(repository, TestIdGenerator()))

    private fun draft(
        title: String,
        category: EntryCategory = EntryCategory.PROJECT,
        detail: String = "",
        endDate: String = "",
        startDate: String = "",
    ) = FactDraft(
        category = category,
        title = title,
        organization = "",
        startDate = startDate,
        endDate = endDate,
        detail = detail,
    )

    @Test
    fun write_createsTheProfileWhenNoneExistsAndStampsUserStatedFacts() = runTest {
        val result = writer.write(
            drafts = listOf(draft("Placement Stats Dashboard", detail = "Power BI and Excel.")),
            contact = ContactInput(fullName = " Priya Deshmukh ", email = "priya.d@example.com"),
            skills = listOf("SQL", "sql", " Excel "),
        )

        val entry = (result as FactWriteResult.Written).entries.single()
        assertThat(entry.source).isEqualTo(FactSource.USER_STATED)
        assertThat(entry.isConfirmed).isTrue()
        assertThat(entry.bullets.single().text).isEqualTo("Power BI and Excel.")
        val saved = repository.observeProfile().first().let(::checkNotNull)
        assertThat(saved.fullName).isEqualTo("Priya Deshmukh")
        assertThat(saved.email).isEqualTo("priya.d@example.com")
        assertThat(saved.skills).containsExactly("SQL", "Excel").inOrder()
        assertThat(saved.entries).containsExactly(entry)
    }

    @Test
    fun write_allocatesDistinctIdsAcrossDraftsInOneCall() = runTest {
        repository.sendProfile(sampleProfile.copy(entries = emptyList()))

        val result = writer.write(listOf(draft("One"), draft("Two")))

        val ids = (result as FactWriteResult.Written).entries.map { it.id }
        assertThat(ids.toSet()).hasSize(2)
    }

    @Test
    fun write_replacingRemovesTheOldEntriesFirst() = runTest {
        repository.sendProfile(sampleProfile.copy(entries = emptyList()))
        val first = (writer.write(listOf(draft("Old title"))) as FactWriteResult.Written).entries.single()

        val second = (writer.write(listOf(draft("New title")), replacing = setOf(first.id)) as FactWriteResult.Written)
            .entries.single()

        assertThat(second.id).isEqualTo(first.id)
        assertThat(repository.observeProfile().first().let(::checkNotNull).entries.map { it.title }).containsExactly("New title")
    }

    @Test
    fun write_rejectsInvalidDraftsAndSavesNothing() = runTest {
        repository.sendProfile(sampleProfile)

        val result = writer.write(
            listOf(draft("Dashboard", startDate = "Apr 2024", endDate = "Jan 2024"), draft("")),
        )

        val errors = (result as FactWriteResult.Rejected).errors
        assertThat(errors.map { it.field to it.reason })
            .contains(FactField.END_DATE to FactDraftErrorReason.END_BEFORE_START)
        assertThat(repository.observeProfile().first()).isEqualTo(sampleProfile)
    }

    @Test
    fun write_withNothingFilledReportsNothingToWrite() = runTest {
        val result = writer.write(listOf(draft("", detail = "")))

        assertThat(result).isEqualTo(FactWriteResult.NothingToWrite)
        assertThat(repository.observeProfile().first()).isNull()
    }

    @Test
    fun write_withBlankContactKeepsTheExistingContact() = runTest {
        repository.sendProfile(sampleProfile)

        writer.write(emptyList(), contact = ContactInput(fullName = "  ", email = "new@example.com"))

        val saved = repository.observeProfile().first().let(::checkNotNull)
        assertThat(saved.fullName).isEqualTo(sampleProfile.fullName)
        assertThat(saved.email).isEqualTo("new@example.com")
    }
}
