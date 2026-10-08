package com.tailormyresume.core.testing.repository

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.data.repository.CoverLetterRepository
import com.tailormyresume.core.model.WrittenCoverLetter
import com.tailormyresume.core.model.WrittenParagraph
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Instant

abstract class CoverLetterRepositoryContractTest {

    protected abstract fun createCoverLetterRepository(): CoverLetterRepository

    private val letter = WrittenCoverLetter(
        paragraphs = listOf(
            WrittenParagraph("Dear Hiring Manager,", isGreeting = true),
            WrittenParagraph("I built a reporting tool.", isUserEdited = true),
        ),
        writtenAt = Instant.fromEpochMilliseconds(1_700_000_000_000),
    )

    @Test
    fun anApplicationWithNoLetterReadsAsNull() = runTest {
        assertThat(createCoverLetterRepository().observeLetter("app-1").first()).isNull()
    }

    @Test
    fun aSavedLetterKeepsItsFields() = runTest {
        val letters = createCoverLetterRepository()

        letters.save("app-1", letter)

        assertThat(letters.observeLetter("app-1").first()).isEqualTo(letter)
        assertThat(letters.observeLetter("app-2").first()).isNull()
    }

    @Test
    fun savingAgainReplacesTheLetter() = runTest {
        val letters = createCoverLetterRepository()
        letters.save("app-1", letter)
        val rewritten = letter.copy(paragraphs = listOf(WrittenParagraph("Short.")))

        letters.save("app-1", rewritten)

        assertThat(letters.observeLetter("app-1").first()).isEqualTo(rewritten)
    }

    @Test
    fun clearForRemovesOnlyThatApplicationsLetter() = runTest {
        val letters = createCoverLetterRepository()
        letters.save("app-1", letter)
        letters.save("app-2", letter)

        letters.clearFor("app-1")

        assertThat(letters.observeLetter("app-1").first()).isNull()
        assertThat(letters.observeLetter("app-2").first()).isEqualTo(letter)
    }

    @Test
    fun wordCountSumsTheWordsOfEveryParagraph() {
        assertThat(letter.wordCount).isEqualTo(8)
    }
}
