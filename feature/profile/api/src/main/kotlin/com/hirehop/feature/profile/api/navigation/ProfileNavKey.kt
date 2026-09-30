package com.hirehop.feature.profile.api.navigation

import androidx.navigation3.runtime.NavKey
import com.hirehop.core.model.DebugScenario
import kotlinx.serialization.Serializable

@Serializable
data class ProfileNavKey(
    val scenario: DebugScenario = DebugScenario.defaultValue,
) : NavKey

val DefaultProfileNavKey = ProfileNavKey()
