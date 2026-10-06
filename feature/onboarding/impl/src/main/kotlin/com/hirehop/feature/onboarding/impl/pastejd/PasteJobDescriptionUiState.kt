package com.hirehop.feature.onboarding.impl.pastejd

import com.hirehop.core.data.repository.UsageAllowance
import com.hirehop.core.domain.onboarding.OnboardingStep
import com.hirehop.core.model.DebugScenario

const val FREE_ANALYSES_PER_DAY: Int = UsageAllowance.DAILY_ANALYSES

const val PASTE_JD_MIN_WORDS: Int = 20

const val PASTE_JD_MAX_CHARACTERS: Int = 12000

enum class PasteJobDescriptionArrival { TYPED, SHARED_IN }

enum class PasteJobDescriptionProblem { LINK_ONLY, TOO_SHORT, TOO_LONG }

enum class PasteJobDescriptionMessage { PASTE_FAILED, NOTHING_TO_READ, PASTE_PARTIAL }

data class PasteJobDescriptionUiState(
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val text: String = "",
    val company: String = "",
    val role: String = "",
    val arrival: PasteJobDescriptionArrival = PasteJobDescriptionArrival.TYPED,
    val message: PasteJobDescriptionMessage? = null,
    val freeAnalysesLeft: Int = FREE_ANALYSES_PER_DAY,
    val nextStep: OnboardingStep? = null,
) {
    val isDailyLimitReached: Boolean get() = freeAnalysesLeft <= 0
    val wordCount: Int get() = pasteJdWordCount(text)
    val problem: PasteJobDescriptionProblem? get() = pasteJdProblem(text = text, wordCount = wordCount)
    val canClear: Boolean get() = text.isNotEmpty()
    val canAnalyse: Boolean
        get() = !isLoading && !isDailyLimitReached && text.isNotBlank() && problem == null
}

fun pasteJobDescriptionStateFor(
    scenario: DebugScenario,
    sharedText: String = "",
): PasteJobDescriptionUiState {
    val text = sharedText.trim()
    val arrival = if (text.isNotEmpty()) {
        PasteJobDescriptionArrival.SHARED_IN
    } else {
        PasteJobDescriptionArrival.TYPED
    }
    val base = PasteJobDescriptionUiState(text = text, arrival = arrival)
    return when (scenario) {
        DebugScenario.LOADING -> base.copy(isLoading = true)
        DebugScenario.EMPTY -> base.copy(text = "", message = PasteJobDescriptionMessage.NOTHING_TO_READ)
        DebugScenario.OFFLINE -> base.copy(isOffline = true)
        DebugScenario.ERROR -> base.copy(message = PasteJobDescriptionMessage.PASTE_FAILED)
        DebugScenario.PARTIAL -> base.copy(
            message = if (text.isEmpty()) PasteJobDescriptionMessage.PASTE_PARTIAL else null,
        )

        DebugScenario.PENDING -> base.copy(freeAnalysesLeft = 0)
        else -> base
    }
}

fun pasteJdWordCount(text: String): Int =
    text.split(PASTE_JD_WHITESPACE)
        .count { word -> word.isNotBlank() }

fun pasteJdProblem(
    text: String,
    wordCount: Int = pasteJdWordCount(text),
): PasteJobDescriptionProblem? = when {
    text.isBlank() -> null
    pasteJdLooksLikeLinkOnly(text) -> PasteJobDescriptionProblem.LINK_ONLY
    wordCount < PASTE_JD_MIN_WORDS -> PasteJobDescriptionProblem.TOO_SHORT
    text.length > PASTE_JD_MAX_CHARACTERS -> PasteJobDescriptionProblem.TOO_LONG
    else -> null
}

private fun pasteJdLooksLikeLinkOnly(text: String): Boolean {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return false
    val words = trimmed.split(PASTE_JD_WHITESPACE).count { word -> word.isNotBlank() }
    if (words > PASTE_JD_LINK_WORD_CEILING) return false
    return trimmed.contains("http") ||
        trimmed.contains("www.") ||
        trimmed.contains(".com") ||
        trimmed.contains(".in/")
}

private val PASTE_JD_WHITESPACE: Regex = Regex("\\s+")

private const val PASTE_JD_LINK_WORD_CEILING: Int = 4
