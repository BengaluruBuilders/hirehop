package com.tailormyresume.feature.profile.impl.common

import androidx.annotation.StringRes
import com.tailormyresume.core.designsystem.component.TmrProvenanceKind
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.feature.profile.impl.R

enum class FactStatus { Confirmed, UserStated, UserEdited, Scanned, ToConfirm }

internal fun ProfileEntry.status(): FactStatus = when {
    !isConfirmed -> FactStatus.ToConfirm
    source == FactSource.USER_STATED -> FactStatus.UserStated
    source == FactSource.USER_EDITED -> FactStatus.UserEdited
    else -> FactStatus.Confirmed
}

internal fun FactStatus.provenanceKind(): TmrProvenanceKind? = when (this) {
    FactStatus.Confirmed -> TmrProvenanceKind.Confirmed
    FactStatus.UserStated -> TmrProvenanceKind.UserStated
    FactStatus.UserEdited -> TmrProvenanceKind.UserEdited
    FactStatus.Scanned -> TmrProvenanceKind.Scanned
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
