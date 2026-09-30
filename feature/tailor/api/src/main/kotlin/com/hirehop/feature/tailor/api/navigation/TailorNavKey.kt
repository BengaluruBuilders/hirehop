package com.hirehop.feature.tailor.api.navigation

import androidx.navigation3.runtime.NavKey
import com.hirehop.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class TailorNavKey(val applicationId: String) : NavKey

fun Navigator.navigateToTailor(applicationId: String) {
    navigate(TailorNavKey(applicationId))
}
