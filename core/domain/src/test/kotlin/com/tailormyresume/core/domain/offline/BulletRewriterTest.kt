package com.tailormyresume.core.domain.offline

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BulletRewriterTest {
    private val rewriter = BulletRewriter(setOf("kotlin"))

    @Test
    fun whitespaceOnlyDifference_isNotAnEdit_andKeepsTheOriginalText() {
        val source = "Built  the  app   in Kotlin "

        val rewrite = rewriter.rewrite(source)

        assertThat(rewrite.editTypes).isEmpty()
        assertThat(rewrite.text).isEqualTo(source)
    }

    @Test
    fun removedFiller_isAShortenEdit() {
        val rewrite = rewriter.rewrite("Was responsible for building the Kotlin app")

        assertThat(rewrite.text.trim()).isNotEqualTo("Was responsible for building the Kotlin app")
        assertThat(rewrite.editTypes).isNotEmpty()
    }
}
