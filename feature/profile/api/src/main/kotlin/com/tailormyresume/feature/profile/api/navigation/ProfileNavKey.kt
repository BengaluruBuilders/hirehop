package com.tailormyresume.feature.profile.api.navigation

import androidx.navigation3.runtime.NavKey
import com.tailormyresume.core.model.DebugScenario
import kotlinx.serialization.Serializable

@Serializable
data class ProfileNavKey(
    val scenario: DebugScenario = DebugScenario.defaultValue,
) : NavKey

val DefaultProfileNavKey = ProfileNavKey()
