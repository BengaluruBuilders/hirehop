package com.tailormyresume.core.domain.offline

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.ProfileLimits
import com.tailormyresume.core.model.hasTooLongBullet
import kotlinx.coroutines.test.runTest
import org.junit.Test

class OfflineLongBulletImportTest {
    private val parser = OfflineResumeTextParser()

    private fun sentence(length: Int): String = "S" + "a".repeat(length - 2) + "."

    private fun resumeWithBullet(bullet: String): String =
        "Priya Deshmukh\n\nEXPERIENCE\nData Intern | Kiran Agro Exports | Jan 2024 - Mar 2024\n- $bullet\n"

    @Test
    fun overLimitBulletOfThreeSentencesImportsAsThreeBullets() = runTest {
        val original = listOf(sentence(299), sentence(299), sentence(300)).joinToString(" ")

        val entry = parser.parse(resumeWithBullet(original)).entries.single { it.category == EntryCategory.EXPERIENCE }

        assertThat(entry.bullets).hasSize(3)
        assertThat(entry.bullets.all { it.text.length <= ProfileLimits.MAX_BULLET_LENGTH }).isTrue()
        assertThat(entry.bullets.joinToString(" ") { it.text }).isEqualTo(original)
        assertThat(entry.bullets.map { it.id }.toSet()).hasSize(3)
        assertThat(entry.hasTooLongBullet).isFalse()
    }

    @Test
    fun overLimitSingleSentenceImportsWholeAndFlagged() = runTest {
        val original = sentence(450)

        val entry = parser.parse(resumeWithBullet(original)).entries.single { it.category == EntryCategory.EXPERIENCE }

        assertThat(entry.bullets.map { it.text }).containsExactly(original)
        assertThat(entry.hasTooLongBullet).isTrue()
    }
}
