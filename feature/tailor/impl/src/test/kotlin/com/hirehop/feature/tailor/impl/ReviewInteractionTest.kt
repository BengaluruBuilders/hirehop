package com.hirehop.feature.tailor.impl

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

class ReviewInteractionTest {

    @Test
    fun startEditOpensBulletWithProposedTextAndNoError() {
        val interaction = ReviewInteraction()

        interaction.startEdit("b1", "Text")

        assertThat(interaction.editBulletId).isEqualTo("b1")
        assertThat(interaction.editText).isEqualTo("Text")
        assertThat(interaction.editShowsError).isFalse()
    }

    @Test
    fun blankTextCannotBeSavedAndShowsError() {
        val interaction = ReviewInteraction()

        interaction.changeEditText("   ")

        assertThat(interaction.canSaveEdit()).isFalse()
        assertThat(interaction.editShowsError).isTrue()
    }

    @Test
    fun emptyTextCannotBeSavedAndTypingClearsTheError() {
        val interaction = ReviewInteraction()

        interaction.changeEditText("")
        assertThat(interaction.canSaveEdit()).isFalse()
        assertThat(interaction.editShowsError).isTrue()

        interaction.changeEditText("x")

        assertThat(interaction.editShowsError).isFalse()
    }

    @Test
    fun editedTextCanBeSavedAndCarriesNoError() {
        val interaction = ReviewInteraction()

        interaction.changeEditText("Better line")

        assertThat(interaction.canSaveEdit()).isTrue()
        assertThat(interaction.editShowsError).isFalse()
    }
}

@RunWith(AndroidJUnit4::class)
class PrepQuestionsCountLabelTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun labelMatchesTheQuestionCount() {
        assertThat(
            context.resources.getQuantityString(
                R.plurals.feature_tailor_impl_prep_questions_count_label,
                1,
            ),
        ).isEqualTo("question")
        assertThat(
            context.resources.getQuantityString(
                R.plurals.feature_tailor_impl_prep_questions_count_label,
                0,
            ),
        ).isEqualTo("questions")
        assertThat(
            context.resources.getQuantityString(
                R.plurals.feature_tailor_impl_prep_questions_count_label,
                2,
            ),
        ).isEqualTo("questions")
    }
}
