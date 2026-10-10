package com.tailormyresume.feature.tailor.impl.result

import com.tailormyresume.core.navigation.Navigator

internal class TailorFailedNavigation(
    private val navigator: Navigator,
    private val applicationId: String,
) {
    fun retry() = Unit

    fun goBack() = Unit
}
