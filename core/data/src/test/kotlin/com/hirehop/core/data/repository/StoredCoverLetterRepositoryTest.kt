package com.hirehop.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.model.WrittenCoverLetter
import com.hirehop.core.model.WrittenParagraph
import com.hirehop.core.testing.mock.TestMockStateStore
import com.hirehop.core.testing.repository.CoverLetterRepositoryContractTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Instant

class StoredCoverLetterRepositoryTest : CoverLetterRepositoryContractTest() {

    override fun createCoverLetterRepository(): CoverLetterRepository = StoredCoverLetterRepository(TestMockStateStore())

    @Test
    fun theLetterSurvivesARestartOfTheRepository() = runTest {
        val store = TestMockStateStore()
        val letter = WrittenCoverLetter(
            paragraphs = listOf(WrittenParagraph("Dear team,", isGreeting = true), WrittenParagraph("I wrote SQL.", isUserEdited = true)),
            writtenAt = Instant.fromEpochMilliseconds(1_700_000_000_000),
            generationId = "gen-letter",
        )
        StoredCoverLetterRepository(store).save("app-1", letter)

        assertThat(StoredCoverLetterRepository(store).observeLetter("app-1").first()).isEqualTo(letter)
    }

    @Test
    fun aLetterSavedBeforeGenerationIdsStillLoads() = runTest {
        val store = TestMockStateStore()
        store.write("coverletter.app-1", """{"paragraphs":[{"text":"Hi.","isGreeting":false,"isUserEdited":false}],"writtenAtMillis":1}""")

        val letter = StoredCoverLetterRepository(store).observeLetter("app-1").first()

        assertThat(letter?.generationId).isNull()
    }

    @Test
    fun theLetterSitsUnderTheKeyOfItsApplication() = runTest {
        val store = TestMockStateStore()
        StoredCoverLetterRepository(store).save("app-1", WrittenCoverLetter(listOf(WrittenParagraph("Hi.")), Instant.fromEpochMilliseconds(1)))

        assertThat(store.read("coverletter.app-1")).isNotNull()
    }
}
