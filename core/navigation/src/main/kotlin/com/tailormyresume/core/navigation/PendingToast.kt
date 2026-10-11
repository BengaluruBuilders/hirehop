package com.tailormyresume.core.navigation

import androidx.annotation.StringRes

object PendingToast {
    val queued: Int? = null

    fun set(@StringRes message: Int) = Unit

    fun consume(): Int? = null
}
