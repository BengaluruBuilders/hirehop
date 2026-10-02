package com.hirehop.feature.applications.impl

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HomeHeaderCollapseTest {

    @Test
    fun atTheTop_theHeaderIsExpanded() {
        assertThat(nextHeaderCompact(wasCompact = true, firstVisibleItemIndex = 0, firstVisibleItemScrollOffset = 0)).isFalse()
        assertThat(nextHeaderCompact(wasCompact = false, firstVisibleItemIndex = 0, firstVisibleItemScrollOffset = 0)).isFalse()
    }

    @Test
    fun whileTheFirstItemIsOnlyPartlyScrolledAway_theHeaderKeepsItsState() {
        assertThat(nextHeaderCompact(wasCompact = false, firstVisibleItemIndex = 0, firstVisibleItemScrollOffset = 40)).isFalse()
        assertThat(nextHeaderCompact(wasCompact = true, firstVisibleItemIndex = 0, firstVisibleItemScrollOffset = 40)).isTrue()
    }

    @Test
    fun onceTheFirstItemIsFullyScrolledAway_theHeaderCollapses() {
        assertThat(nextHeaderCompact(wasCompact = false, firstVisibleItemIndex = 1, firstVisibleItemScrollOffset = 0)).isTrue()
        assertThat(nextHeaderCompact(wasCompact = true, firstVisibleItemIndex = 3, firstVisibleItemScrollOffset = 12)).isTrue()
    }
}
