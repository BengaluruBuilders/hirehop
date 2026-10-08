package com.tailormyresume.core.domain.offline

internal data class BulletLine(val text: String, val marked: Boolean) {
    companion object {
        private val marker = Regex("^\\s*(?:[•·●▪◦►➢✓✔]\\s*|[*>\\-–—]\\s+|\\d{1,2}[.)]\\s+)")

        fun parse(line: String): BulletLine {
            val match = marker.find(line) ?: return BulletLine(line.trim(), marked = false)
            return BulletLine(line.substring(match.range.last + 1).trim(), marked = true)
        }
    }
}
