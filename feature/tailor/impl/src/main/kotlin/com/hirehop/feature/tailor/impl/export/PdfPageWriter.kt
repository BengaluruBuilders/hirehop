package com.hirehop.feature.tailor.impl.export

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.text.StaticLayout

internal class PdfPageWriter(private val pdf: PdfDocument) {

    private var pageNumber = 0
    private var cursorY = MARGIN
    private var page: PdfDocument.Page = startPage()

    val contentWidth: Int get() = (PAGE_WIDTH - 2 * MARGIN).toInt()

    fun drawBlock(layout: StaticLayout, keepWithNext: Float = 0f) {
        ensureRoom(layout.height + keepWithNext)
        drawLayoutAt(layout, MARGIN)
        cursorY += layout.height
    }

    fun drawHanging(marker: StaticLayout, body: StaticLayout, indent: Float) {
        ensureRoom(body.height.toFloat())
        drawLayoutAt(marker, MARGIN)
        drawLayoutAt(body, MARGIN + indent)
        cursorY += body.height
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

    private fun drawLayoutAt(layout: StaticLayout, x: Float) {
        val canvas = page.canvas
        canvas.save()
        canvas.translate(x, cursorY)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun ensureRoom(height: Float) {
        val fitsOnPage = cursorY + height <= PAGE_HEIGHT - MARGIN
        val pageIsEmpty = cursorY <= MARGIN
        if (!fitsOnPage && !pageIsEmpty) {
            pdf.finishPage(page)
            page = startPage()
        }
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
