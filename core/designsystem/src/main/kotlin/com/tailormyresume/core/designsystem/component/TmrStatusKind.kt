package com.tailormyresume.core.designsystem.component

enum class TmrStatusKind { Met, Partial, Gap }

internal fun TmrStatusKind.defaultLabel(): String = name
