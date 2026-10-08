package com.tailormyresume.core.designsystem.component

enum class TmrProvenanceKind { Confirmed, UserStated, UserEdited, Scanned }

internal fun TmrProvenanceKind.defaultLabel(): String =
    name.replaceFirstChar { character -> character.lowercaseChar() }
