package com.tailormyresume.feature.tailor.impl.tailoring

import com.tailormyresume.core.designsystem.component.content.TmrProgressState
import com.tailormyresume.core.model.QuickAnswer

internal enum class TailoringRowKind { Matching, Rewriting, AddingExample, CheckingMustHaves, Fitting }

internal data class TailoringRowUi(val kind: TailoringRowKind, val state: TmrProgressState)

internal data class TailoringUiState(
    val percent: Int,
    val rows: List<TailoringRowUi>,
    val keywordCount: Int,
    val stickers: List<String>,
)

internal fun tailoringRows(percent: Int, addsExample: Boolean): List<TailoringRowUi> = emptyList()

internal fun QuickAnswer?.addsExample(): Boolean = false
