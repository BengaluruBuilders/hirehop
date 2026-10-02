package com.hirehop.feature.tailor.impl.export

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.text.StaticLayout

internal class PdfPageWriter(private val pdf: PdfDocument) {

    private var pageNumber = 0
    private var cursorY = MARGIN
    private var page: PdfDocument.Page = startPage()

    val pageCount: Int get() = pageNumber

    val contentWidth: Int get() = (PAGE_WIDTH - 2 * MARGIN).toInt()

    fun drawBlock(layout: StaticLayout, keepWithNext: Float = 0f) {
        drawLines(layout, MARGIN, marker = null, keepWithNext = keepWithNext)
    }

    fun drawHanging(marker: StaticLayout, body: StaticLayout, indent: Float) {
        drawLines(body, MARGIN + indent, marker = marker, keepWithNext = 0f)
    }

    fun drawRule(paint: Paint) {
        page.canvas.drawLine(MARGIN, cursorY, PAGE_WIDTH - MARGIN, cursorY, paint)
    }

    fun space(points: Float) {
        cursorY += points
    }

    fun finish() {
        pdf.finishPage(page)
    }

    private fun drawLines(layout: StaticLayout, x: Float, marker: StaticLayout?, keepWithNext: Float) {
        if (layout.lineCount == 0) return
        if (Pagination.shouldBreakBefore(pageSpace(), layout.height.toFloat(), keepWithNext)) nextPage()
        val chunks = Pagination.planChunks(
            lineCount = layout.lineCount,
            space = pageSpace(),
            lineTop = { layout.getLineTop(it).toFloat() },
            lineBottom = { layout.getLineBottom(it).toFloat() },
        )
        chunks.forEachIndexed { index, chunk ->
            if (chunk.startsNewPage) nextPage()
            drawChunk(layout, chunk, x, if (index == 0) marker else null)
        }
    }

    private fun drawChunk(layout: StaticLayout, chunk: LineChunk, x: Float, marker: StaticLayout?) {
        val top = layout.getLineTop(chunk.firstLine).toFloat()
        val bottom = layout.getLineBottom(chunk.endLine - 1).toFloat()
        val canvas = page.canvas
        marker?.let {
            canvas.save()
            canvas.translate(MARGIN, cursorY)
            it.draw(canvas)
            canvas.restore()
        }
        canvas.save()
        canvas.translate(x, cursorY - top)
        canvas.clipRect(0f, top, layout.width.toFloat(), bottom)
        layout.draw(canvas)
        canvas.restore()
        cursorY += bottom - top
    }

    private fun pageSpace(): PageSpace = PageSpace(
        cursorY = cursorY,
        pageTop = MARGIN,
        pageBottom = PAGE_HEIGHT - MARGIN,
    )

    private fun nextPage() {
        pdf.finishPage(page)
        page = startPage()
    }

    private fun startPage(): PdfDocument.Page {
        pageNumber += 1
        cursorY = MARGIN
        val info = PdfDocument.PageInfo.Builder(PAGE_WIDTH.toInt(), PAGE_HEIGHT.toInt(), pageNumber).create()
        return pdf.startPage(info)
    }

    companion object {
        const val PAGE_WIDTH = 595f
        const val PAGE_HEIGHT = 842f
        const val MARGIN = 48f
    }
}
