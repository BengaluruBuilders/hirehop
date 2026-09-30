package com.hirehop.app.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.feature.applications.api.navigation.ApplicationsNavKey
import com.hirehop.feature.profile.api.navigation.ProfileNavKey
import com.hirehop.feature.applications.api.R as applicationsR
import com.hirehop.feature.profile.api.R as profileR

data class TopLevelNavItem(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @param:StringRes val labelRes: Int,
)

val APPLICATIONS = TopLevelNavItem(
    selectedIcon = HhIcons.Applications,
    unselectedIcon = HhIcons.ApplicationsBorder,
    labelRes = applicationsR.string.feature_applications_api_title,
)

val PROFILE = TopLevelNavItem(
    selectedIcon = HhIcons.Profile,
    unselectedIcon = HhIcons.ProfileBorder,
    labelRes = profileR.string.feature_profile_api_title,
)

val START_NAV_KEY: NavKey = ApplicationsNavKey

val TOP_LEVEL_NAV_ITEMS: Map<NavKey, TopLevelNavItem> = linkedMapOf(
    ApplicationsNavKey to APPLICATIONS,
    ProfileNavKey to PROFILE,
)
