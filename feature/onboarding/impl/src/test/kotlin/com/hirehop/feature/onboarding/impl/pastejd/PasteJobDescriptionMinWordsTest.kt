package com.hirehop.feature.onboarding.impl.pastejd

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PasteJobDescriptionMinWordsTest {

    @Test
    fun pasteJdProblem_withElevenWords_isTooShort() {
        assertThat(pasteJdProblem(ELEVEN_WORD_TEXT)).isEqualTo(PasteJobDescriptionProblem.TOO_SHORT)
    }

    @Test
    fun pasteJdProblem_withEighteenWords_isTooShort() {
        assertThat(pasteJdWordCount(EIGHTEEN_WORD_TEXT)).isEqualTo(18)
        assertThat(pasteJdProblem(EIGHTEEN_WORD_TEXT)).isEqualTo(PasteJobDescriptionProblem.TOO_SHORT)
    }

    @Test
    fun pasteJdProblem_withNineteenWords_isTooShort() {
        val text = List(PASTE_JD_MIN_WORDS - 1) { "word" }.joinToString(" ")

        assertThat(pasteJdWordCount(text)).isEqualTo(PASTE_JD_MIN_WORDS - 1)
        assertThat(pasteJdProblem(text)).isEqualTo(PasteJobDescriptionProblem.TOO_SHORT)
    }

    @Test
    fun pasteJdProblem_atTheWordFloor_hasNoProblem() {
        val text = List(PASTE_JD_MIN_WORDS) { "word" }.joinToString(" ")

        assertThat(pasteJdWordCount(text)).isEqualTo(PASTE_JD_MIN_WORDS)
        assertThat(pasteJdProblem(text)).isNull()
    }

    @Test
    fun canAnalyse_withElevenWords_isFalse() {
        assertThat(PasteJobDescriptionUiState(text = ELEVEN_WORD_TEXT).canAnalyse).isFalse()
    }

    private companion object {
        val ELEVEN_WORD_TEXT: String = "Hiring Associate Analyst BI, Bengaluru. SQL, Excel, Power BI. Apply now."

        val EIGHTEEN_WORD_TEXT: String = "We are hiring an Associate Analyst for our Bengaluru team to build weekly finance reports in SQL daily."
    }
}
