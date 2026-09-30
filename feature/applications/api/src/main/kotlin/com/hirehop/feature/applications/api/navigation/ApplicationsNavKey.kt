package com.hirehop.feature.applications.api.navigation

import androidx.navigation3.runtime.NavKey
import com.hirehop.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data object ApplicationsNavKey : NavKey

@Serializable
data class ApplicationDetailNavKey(val applicationId: String) : NavKey

fun Navigator.navigateToApplicationDetail(applicationId: String) {
    navigate(ApplicationDetailNavKey(applicationId))
}
