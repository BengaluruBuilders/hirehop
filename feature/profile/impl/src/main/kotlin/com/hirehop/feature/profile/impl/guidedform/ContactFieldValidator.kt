package com.hirehop.feature.profile.impl.guidedform

internal object ContactFieldValidator {

    private val emailPattern = Regex("[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}")

    private val phonePattern = Regex("\\+?[0-9 ()-]+")

    fun isValidEmail(value: String): Boolean = emailPattern.matches(value.trim())

    fun isValidPhone(value: String): Boolean {
        val trimmed = value.trim()
        if (!phonePattern.matches(trimmed)) return false
        return trimmed.count { it.isDigit() } in 7..15
    }
}
