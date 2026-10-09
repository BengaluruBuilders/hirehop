package com.tailormyresume.core.domain.fact

object FactDateFormat {
    private val openEnded = setOf("present", "current", "now", "ongoing")

    fun isReadable(text: String): Boolean {
        val value = text.trim()
        if (value.isEmpty()) return true
        if (value.lowercase() in openEnded) return true
        return FreeFormDate.parse(value) != null
    }
}
