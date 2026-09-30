package com.hirehop.app.debug

import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import com.hirehop.app.R
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.testing.data.canonicalApplication
import com.hirehop.feature.analysis.api.navigation.AnalysisNavKey
import com.hirehop.feature.applications.api.navigation.ApplicationDetailNavKey
import com.hirehop.feature.applications.api.navigation.ApplicationsNavKey
import com.hirehop.feature.profile.api.navigation.ProfileNavKey
import com.hirehop.feature.tailor.api.navigation.TailorNavKey

enum class DebugScenarioTarget(@param:StringRes val titleRes: Int) {
    Applications(R.string.debug_target_applications),
    ApplicationDetail(R.string.debug_target_application_detail),
    Profile(R.string.debug_target_profile),
    Analysis(R.string.debug_target_analysis),
    Tailor(R.string.debug_target_tailor),
    ;

    fun navKey(scenario: DebugScenario): NavKey = when (this) {
        Applications -> ApplicationsNavKey(scenario = scenario)
        ApplicationDetail -> ApplicationDetailNavKey(
            applicationId = canonicalApplication.id,
            scenario = scenario,
        )

        Profile -> ProfileNavKey(scenario = scenario)
        Analysis -> AnalysisNavKey(scenario = scenario)
        Tailor -> TailorNavKey(applicationId = canonicalApplication.id, scenario = scenario)
    }

    companion object {
        fun of(key: NavKey): DebugScenarioTarget? = when (key) {
            is ApplicationsNavKey -> Applications
            is ProfileNavKey -> Profile
            is AnalysisNavKey -> Analysis
            else -> null
        }
    }
}

fun NavKey.withScenario(scenario: DebugScenario): NavKey =
    DebugScenarioTarget.of(this)?.navKey(scenario) ?: this
