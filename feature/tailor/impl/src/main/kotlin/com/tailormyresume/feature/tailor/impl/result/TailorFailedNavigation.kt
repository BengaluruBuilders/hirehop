package com.tailormyresume.feature.tailor.impl.result

import com.tailormyresume.core.navigation.Navigator
import com.tailormyresume.feature.tailor.api.navigation.TailoringNavKey

internal class TailorFailedNavigation(
    private val navigator: Navigator,
    private val applicationId: String,
) {
    fun retry() = navigator.replace(TailoringNavKey(applicationId))

    fun goBack() {
        navigator.goBack()
    }
}
