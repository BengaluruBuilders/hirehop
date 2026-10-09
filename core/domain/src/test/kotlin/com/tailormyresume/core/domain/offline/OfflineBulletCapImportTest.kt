package com.tailormyresume.core.domain.offline

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.core.model.ProfileLimits
import com.tailormyresume.core.model.hasTooManyBullets
import kotlinx.coroutines.test.runTest
import org.junit.Test

class OfflineBulletCapImportTest {
    private val parser = OfflineResumeTextParser()

    private fun bulletLines(count: Int, prefix: String = "Task") =
        (1..count).joinToString("\n") { "- $prefix number $it done well." }

    private fun resume(vararg blocks: String) = "Priya Deshmukh\n\nEXPERIENCE\n" + blocks.joinToString("\n")

    private fun block(title: String, bullets: Int, prefix: String = "Task") =
        "$title | Kiran Agro Exports | Jan 2024 - Mar 2024\n${bulletLines(bullets, prefix)}\n"

    private suspend fun experience(text: String): List<ProfileEntry> =
        parser.parse(text).entries.filter { it.category == EntryCategory.EXPERIENCE }

    @Test
    fun twentyBulletsImportAsFifteenAndFiveInOrder() = runTest {
        val entries = experience(resume(block("Data Intern", 20)))

        assertThat(entries.map { it.bullets.size }).containsExactly(15, 5).inOrder()
        assertThat(entries.flatMap { it.bullets }.map { it.text })
            .containsExactlyElementsIn((1..20).map { "Task number $it done well." }).inOrder()
        assertThat(entries.map { it.title }.toSet()).containsExactly("Data Intern")
        assertThat(entries.map { it.organization }.toSet()).containsExactly("Kiran Agro Exports")
        assertThat(entries.map { it.startDate }.toSet()).hasSize(1)
        assertThat(entries.map { it.id }.toSet()).hasSize(2)
        assertThat(entries.none { it.hasTooManyBullets }).isTrue()
    }

    @Test
    fun continuationEntriesStayWithinTheFortyEntryCap() = runTest {
        val blocks = (1..38).map { block("Role $it", 1, "Solo") } + block("Data Intern", 20)

        val entries = experience(resume(*blocks.toTypedArray()))

        assertThat(entries).hasSize(ProfileLimits.MAX_ENTRIES)
        assertThat(entries.last().bullets).hasSize(5)
    }

    @Test
    fun entryStaysWholeAndFlaggedWhenContinuationWouldExceedTheCap() = runTest {
        val blocks = (1..39).map { block("Role $it", 1, "Solo") } + block("Data Intern", 20)

        val entries = experience(resume(*blocks.toTypedArray()))

        assertThat(entries).hasSize(ProfileLimits.MAX_ENTRIES)
        assertThat(entries.last().bullets).hasSize(20)
        assertThat(entries.last().hasTooManyBullets).isTrue()
    }
}
