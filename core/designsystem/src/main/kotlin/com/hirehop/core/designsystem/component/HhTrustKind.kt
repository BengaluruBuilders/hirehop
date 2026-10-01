package com.hirehop.core.designsystem.component

enum class HhTrustKind { NeverInvents, OnDevice, OfflineReady, NoScore }

internal fun HhTrustKind.defaultLabel(): String =
    name.replaceFirstChar { character -> character.lowercaseChar() }
