package com.tailormyresume.core.domain.fact

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.core.model.hasTooManyBullets
import com.tailormyresume.core.model.splitBulletsForEntries
import org.junit.Test

class BulletSplitTest {
    private fun texts(count: Int) = (1..count).map { "Line $it." }

    private fun entryWith(count: Int) = ProfileEntry(
        "E1", EntryCategory.EXPERIENCE, "T", "O", "", "",
        (1..count).map { EvidenceBullet("E1-b$it", "Line $it.") }, FactSource.IMPORTED, isConfirmed = false,
    )

    @Test
    fun fifteenBulletsStayInOneEntry() {
        assertThat(splitBulletsForEntries(texts(15), 0, 0)).hasSize(1)
    }

    @Test
    fun thirtyOneBulletsSplitFifteenFifteenOneInOrder() {
        val chunks = splitBulletsForEntries(texts(31), 0, 0)

        assertThat(chunks.map { it.size }).containsExactly(15, 15, 1).inOrder()
        assertThat(chunks.flatten()).containsExactlyElementsIn(texts(31)).inOrder()
    }

    @Test
    fun noSplitWhenTheEntryCapWouldBeExceeded() {
        assertThat(splitBulletsForEntries(texts(20), 39, 0)).hasSize(1)
        assertThat(splitBulletsForEntries(texts(20), 38, 1)).hasSize(1)
        assertThat(splitBulletsForEntries(texts(20), 38, 0)).hasSize(2)
    }

    @Test
    fun entryOverFifteenBulletsIsFlagged() {
        assertThat(entryWith(15).hasTooManyBullets).isFalse()
        assertThat(entryWith(16).hasTooManyBullets).isTrue()
    }
}
