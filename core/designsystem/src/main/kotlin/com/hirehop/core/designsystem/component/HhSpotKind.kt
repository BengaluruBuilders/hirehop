package com.hirehop.core.designsystem.component

import com.hirehop.core.designsystem.illustration.HhIllustration

enum class HhSpotKind { Empty, Scanned, Offline, Error, Done, Review }

internal fun HhSpotKind.defaultLabel(): String = name

internal fun HhSpotKind.illustration(): HhIllustration? = when (this) {
    HhSpotKind.Empty -> HhIllustration.Empty
    HhSpotKind.Scanned -> HhIllustration.Scanned
    HhSpotKind.Offline -> HhIllustration.Offline
    HhSpotKind.Error -> HhIllustration.Error
    HhSpotKind.Done -> HhIllustration.Exported
    HhSpotKind.Review -> null
}
