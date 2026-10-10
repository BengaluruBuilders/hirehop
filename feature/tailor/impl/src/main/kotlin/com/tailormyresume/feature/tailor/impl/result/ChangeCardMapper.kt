package com.tailormyresume.feature.tailor.impl.result

import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.TailoredResume

internal enum class ChangeKind { New, Rewritten, Added, Reordered }

internal enum class ChangeSource { YourResume, YourAnswer }

internal sealed interface ChangeArea {
    data object Summary : ChangeArea

    data object Skills : ChangeArea

    data class Entry(val label: String) : ChangeArea
}

internal data class ChangeCard(
    val id: String,
    val area: ChangeArea,
    val kind: ChangeKind,
    val source: ChangeSource,
    val before: String?,
    val after: String,
    val undone: Boolean,
)

internal fun TailoredResume.toChangeCards(profile: CandidateProfile): List<ChangeCard> = emptyList()
