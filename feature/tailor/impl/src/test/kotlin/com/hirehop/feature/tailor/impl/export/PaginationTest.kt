package com.hirehop.feature.tailor.impl.export

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PaginationTest {

    private val lineHeight = 10f

    private fun plan(lineCount: Int, cursorY: Float, pageTop: Float = 0f, pageBottom: Float = 100f): List<LineChunk> =
        Pagination.planChunks(
            lineCount = lineCount,
            space = PageSpace(cursorY, pageTop, pageBottom),
            lineTop = { it * lineHeight },
            lineBottom = { (it + 1) * lineHeight },
        )

    private fun linesIn(chunks: List<LineChunk>): List<Int> = chunks.flatMap { (it.firstLine until it.endLine).toList() }

    @Test
    fun blockThatFits_isOneChunkOnTheCurrentPage() {
        val chunks = plan(lineCount = 3, cursorY = 0f)

        assertThat(chunks).containsExactly(LineChunk(0, 3, startsNewPage = false))
    }

    @Test
    fun blockTallerThanThePage_isSplitWithoutLosingOrRepeatingLines() {
        val chunks = plan(lineCount = 25, cursorY = 0f)

        assertThat(linesIn(chunks)).isEqualTo((0 until 25).toList())
        assertThat(chunks.map { it.endLine - it.firstLine }).containsExactly(10, 10, 5).inOrder()
        assertThat(chunks.map { it.startsNewPage }).containsExactly(false, true, true).inOrder()
    }

    @Test
    fun blockThatStartsNearThePageEnd_fillsTheRestBeforeBreaking() {
        val chunks = plan(lineCount = 6, cursorY = 70f)

        assertThat(chunks.map { it.endLine - it.firstLine }).containsExactly(3, 3).inOrder()
        assertThat(linesIn(chunks)).isEqualTo((0 until 6).toList())
    }

    @Test
    fun noRoomOnTheCurrentPage_startsOnANewPage() {
        val chunks = plan(lineCount = 2, cursorY = 95f)

        assertThat(chunks).containsExactly(LineChunk(0, 2, startsNewPage = true))
    }

    @Test
    fun lineTallerThanThePage_stillMakesProgress() {
        val chunks = Pagination.planChunks(
            lineCount = 2,
            space = PageSpace(cursorY = 0f, pageTop = 0f, pageBottom = 50f),
            lineTop = { it * 80f },
            lineBottom = { (it + 1) * 80f },
        )

        assertThat(linesIn(chunks)).containsExactly(0, 1).inOrder()
    }

    @Test
    fun emptyBlock_hasNoChunks() {
        assertThat(plan(lineCount = 0, cursorY = 0f)).isEmpty()
    }

    @Test
    fun heading_breaksBeforeWhenTheFollowingContentDoesNotFit() {
        val space = PageSpace(cursorY = 80f, pageTop = 0f, pageBottom = 100f)

        assertThat(Pagination.shouldBreakBefore(space, blockHeight = 10f, keepWithNext = 30f)).isTrue()
    }

    @Test
    fun heading_staysWhenTheFollowingContentFits() {
        val space = PageSpace(cursorY = 40f, pageTop = 0f, pageBottom = 100f)

        assertThat(Pagination.shouldBreakBefore(space, blockHeight = 10f, keepWithNext = 30f)).isFalse()
    }

    @Test
    fun emptyPage_neverBreaksBefore() {
        val space = PageSpace(cursorY = 0f, pageTop = 0f, pageBottom = 100f)

        assertThat(Pagination.shouldBreakBefore(space, blockHeight = 200f, keepWithNext = 0f)).isFalse()
    }

    @Test
    fun blockTallerThanAPage_isSplitInsteadOfBreakingBefore() {
        val space = PageSpace(cursorY = 50f, pageTop = 0f, pageBottom = 100f)

        assertThat(Pagination.shouldBreakBefore(space, blockHeight = 150f, keepWithNext = 0f)).isFalse()
    }
}
