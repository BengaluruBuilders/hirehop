package com.hirehop.core.designsystem.component

enum class HhProvenanceKind { Confirmed, UserStated, UserEdited, Scanned }

internal fun HhProvenanceKind.defaultLabel(): String =
    name.replaceFirstChar { character -> character.lowercaseChar() }
