package com.hirehop.core.designsystem.component

enum class HhStatusKind { Met, Partial, Gap }

internal fun HhStatusKind.defaultLabel(): String = name
