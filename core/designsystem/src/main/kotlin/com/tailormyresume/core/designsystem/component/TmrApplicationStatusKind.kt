package com.tailormyresume.core.designsystem.component

enum class TmrApplicationStatusKind { Saved, Applied, Interview, Offer, Rejected, NoResponse }

internal fun TmrApplicationStatusKind.defaultLabel(): String =
    name.replaceFirstChar { character -> character.lowercaseChar() }
