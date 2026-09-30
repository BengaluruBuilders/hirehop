package com.hirehop.core.designsystem.component

enum class HhApplicationStatusKind { Saved, Applied, Interview, Offer, Rejected, NoResponse }

internal fun HhApplicationStatusKind.defaultLabel(): String =
    name.replaceFirstChar { character -> character.lowercaseChar() }
