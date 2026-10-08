package com.tailormyresume.app.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.feature.applications.api.navigation.ApplicationsNavKey
import com.tailormyresume.feature.applications.api.navigation.DefaultApplicationsNavKey
import com.tailormyresume.feature.profile.api.navigation.DefaultProfileNavKey
import com.tailormyresume.feature.profile.api.navigation.ProfileNavKey
import com.tailormyresume.feature.settings.api.navigation.DefaultSettingsNavKey
import com.tailormyresume.feature.settings.api.navigation.SettingsNavKey
import com.tailormyresume.feature.applications.api.R as applicationsR
import com.tailormyresume.feature.profile.api.R as profileR
import com.tailormyresume.feature.settings.api.R as settingsR

data class TopLevelNavItem(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @param:StringRes val labelRes: Int,
)

val APPLICATIONS = TopLevelNavItem(
    selectedIcon = TmrIcons.Applications,
    unselectedIcon = TmrIcons.ApplicationsBorder,
    labelRes = applicationsR.string.feature_applications_api_title,
)

val PROFILE = TopLevelNavItem(
    selectedIcon = TmrIcons.Profile,
    unselectedIcon = TmrIcons.ProfileBorder,
    labelRes = profileR.string.feature_profile_api_title,
)

val SETTINGS = TopLevelNavItem(
    selectedIcon = TmrIcons.Settings,
    unselectedIcon = TmrIcons.SettingsBorder,
    labelRes = settingsR.string.feature_settings_api_title,
)

val START_NAV_KEY: NavKey = DefaultApplicationsNavKey

val TOP_LEVEL_NAV_ITEMS: Map<NavKey, TopLevelNavItem> = linkedMapOf(
    DefaultApplicationsNavKey to APPLICATIONS,
    DefaultProfileNavKey to PROFILE,
    DefaultSettingsNavKey to SETTINGS,
)

fun NavKey.isTopLevelDestination(): Boolean =
    this is ApplicationsNavKey || this is ProfileNavKey || this is SettingsNavKey
