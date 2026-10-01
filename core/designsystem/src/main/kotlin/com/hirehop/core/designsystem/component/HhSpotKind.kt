package com.hirehop.core.designsystem.component

enum class HhSpotKind { Empty, Scanned, Offline, Error, Done, Review }

internal fun HhSpotKind.defaultLabel(): String = name
