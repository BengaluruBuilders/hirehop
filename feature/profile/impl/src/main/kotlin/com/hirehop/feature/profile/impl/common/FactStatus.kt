package com.hirehop.feature.profile.impl.common

import androidx.annotation.StringRes
import com.hirehop.core.designsystem.component.HhProvenanceKind
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.ProfileEntry
import com.hirehop.feature.profile.impl.R

enum class FactStatus { Confirmed, UserStated, UserEdited, Scanned, ToConfirm }

internal fun ProfileEntry.status(): FactStatus = when {
    !isConfirmed -> FactStatus.ToConfirm
    source == FactSource.USER_STATED -> FactStatus.UserStated
    source == FactSource.USER_EDITED -> FactStatus.UserEdited
    else -> FactStatus.Confirmed
}

internal fun FactStatus.provenanceKind(): HhProvenanceKind? = when (this) {
    FactStatus.Confirmed -> HhProvenanceKind.Confirmed
    FactStatus.UserStated -> HhProvenanceKind.UserStated
    FactStatus.UserEdited -> HhProvenanceKind.UserEdited
    FactStatus.Scanned -> HhProvenanceKind.Scanned
    FactStatus.ToConfirm -> null
}

@StringRes
internal fun FactStatus.labelRes(): Int = when (this) {
    FactStatus.Confirmed -> R.string.feature_profile_impl_status_confirmed
    FactStatus.UserStated -> R.string.feature_profile_impl_status_user_stated
    FactStatus.UserEdited -> R.string.feature_profile_impl_status_user_edited
    FactStatus.Scanned -> R.string.feature_profile_impl_status_scanned
    FactStatus.ToConfirm -> R.string.feature_profile_impl_status_to_confirm
}

internal fun FactSource.status(isConfirmed: Boolean): FactStatus = when {
    !isConfirmed -> FactStatus.ToConfirm
    this == FactSource.USER_STATED -> FactStatus.UserStated
    this == FactSource.USER_EDITED -> FactStatus.UserEdited
    else -> FactStatus.Confirmed
}
