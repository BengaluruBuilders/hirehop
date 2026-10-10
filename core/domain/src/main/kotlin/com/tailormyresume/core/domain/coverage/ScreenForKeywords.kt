package com.tailormyresume.core.domain.coverage

import com.tailormyresume.core.model.RequirementMatch

data class KeywordScreen(val have: List<String>, val missing: List<String>) {
    val count: Int get() = 0
}

object ScreenForKeywords {
    operator fun invoke(matches: List<RequirementMatch>): KeywordScreen = KeywordScreen(emptyList(), emptyList())
}
