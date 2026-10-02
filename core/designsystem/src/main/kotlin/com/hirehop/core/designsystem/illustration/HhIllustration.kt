package com.hirehop.core.designsystem.illustration

import androidx.annotation.DrawableRes
import com.hirehop.core.designsystem.R

enum class HhIllustration(@DrawableRes internal val drawableRes: Int) {
    Hero(R.drawable.core_designsystem_illustration_hero),
    Empty(R.drawable.core_designsystem_illustration_empty),
    Error(R.drawable.core_designsystem_illustration_error),
    Offline(R.drawable.core_designsystem_illustration_offline),
    Scanned(R.drawable.core_designsystem_illustration_scanned),
    Exported(R.drawable.core_designsystem_illustration_exported),
    Goodbye(R.drawable.core_designsystem_illustration_goodbye),
}
