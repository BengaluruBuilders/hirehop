package com.hirehop.feature.tailor.impl.document

internal enum class ExportTemplate(val textScale: Float, val spaceScale: Float) {
    PLAIN(textScale = 1f, spaceScale = 1f),
    COMPACT(textScale = 0.92f, spaceScale = 0.6f),
    SPACIOUS(textScale = 1.04f, spaceScale = 1.5f),
}
