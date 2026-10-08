package com.tailormyresume.core.designsystem.component

import com.tailormyresume.core.designsystem.illustration.TmrIllustration

enum class TmrSpotKind { Empty, Scanned, Offline, Error, Done, Review }

internal fun TmrSpotKind.defaultLabel(): String = name

internal fun TmrSpotKind.illustration(): TmrIllustration? = when (this) {
    TmrSpotKind.Empty -> TmrIllustration.Empty
    TmrSpotKind.Scanned -> TmrIllustration.Scanned
    TmrSpotKind.Offline -> TmrIllustration.Offline
    TmrSpotKind.Error -> TmrIllustration.Error
    TmrSpotKind.Done -> TmrIllustration.Exported
    TmrSpotKind.Review -> null
}
