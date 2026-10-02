package com.hirehop.core.navigation

import androidx.navigation3.runtime.NavKey

object PendingNavigation {
    private var keys: List<NavKey> = emptyList()

    fun set(pending: List<NavKey>) {
        keys = pending
    }

    fun consume(): List<NavKey> = keys.also { keys = emptyList() }
}
