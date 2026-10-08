package com.tailormyresume.feature.tailor.impl.export

internal data class LineChunk(
    val firstLine: Int,
    val endLine: Int,
    val startsNewPage: Boolean,
)

internal data class PageSpace(
    val cursorY: Float,
    val pageTop: Float,
    val pageBottom: Float,
) {
    val remaining: Float get() = pageBottom - cursorY
    val contentHeight: Float get() = pageBottom - pageTop
    val isPageEmpty: Boolean get() = cursorY <= pageTop
}

internal object Pagination {

    fun shouldBreakBefore(space: PageSpace, blockHeight: Float, keepWithNext: Float): Boolean {
        val needed = blockHeight + keepWithNext
        val breakHelps = needed <= space.contentHeight
        return !space.isPageEmpty && needed > space.remaining && breakHelps
    }

    fun planChunks(
        lineCount: Int,
        space: PageSpace,
        lineTop: (Int) -> Float,
        lineBottom: (Int) -> Float,
    ): List<LineChunk> {
        val chunks = mutableListOf<LineChunk>()
        var firstLine = 0
        var available = space.remaining
        var atPageStart = space.isPageEmpty
        var startsNewPage = false
        while (firstLine < lineCount) {
            var count = linesThatFit(firstLine, lineCount, available, lineTop, lineBottom)
            if (count == 0 && !atPageStart) {
                startsNewPage = true
                available = space.contentHeight
                atPageStart = true
                continue
            }
            count = count.coerceAtLeast(1)
            chunks += LineChunk(firstLine, firstLine + count, startsNewPage)
            firstLine += count
            startsNewPage = true
            available = space.contentHeight
            atPageStart = true
        }
        return chunks
    }

    private fun linesThatFit(
        firstLine: Int,
        lineCount: Int,
        available: Float,
        lineTop: (Int) -> Float,
        lineBottom: (Int) -> Float,
    ): Int {
        val startTop = lineTop(firstLine)
        var count = 0
        while (firstLine + count < lineCount && lineBottom(firstLine + count) - startTop <= available) count++
        return count
    }
}
