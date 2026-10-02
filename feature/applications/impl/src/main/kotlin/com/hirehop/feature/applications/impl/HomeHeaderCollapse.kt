package com.hirehop.feature.applications.impl

internal fun nextHeaderCompact(
    wasCompact: Boolean,
    firstVisibleItemIndex: Int,
    firstVisibleItemScrollOffset: Int,
): Boolean = when {
    firstVisibleItemIndex > 0 -> true
    firstVisibleItemScrollOffset == 0 -> false
    else -> wasCompact
}
