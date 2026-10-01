package com.hirehop.core.screenshot

data class HhTestDevice(
    val name: String,
    val qualifiers: String,
    val fontScale: Float,
)

object HhTestDevices {
    const val BOARD_QUALIFIERS = "w393dp-h852dp-normal-long-notround-any-440dpi-keyshidden-nonav"
    const val SMALL_PHONE_QUALIFIERS = "w320dp-h568dp-normal-long-notround-any-xhdpi-keyshidden-nonav"
    const val DEFAULT_FONT_SCALE = 1.0f
    const val LARGE_FONT_SCALE = 2.0f

    val board = HhTestDevice("board", BOARD_QUALIFIERS, DEFAULT_FONT_SCALE)
    val boardLargeFont = HhTestDevice("board-font-200", BOARD_QUALIFIERS, LARGE_FONT_SCALE)
    val smallPhone = HhTestDevice("small-phone", SMALL_PHONE_QUALIFIERS, DEFAULT_FONT_SCALE)

    val all: List<HhTestDevice> = listOf(board, boardLargeFont, smallPhone)
}
