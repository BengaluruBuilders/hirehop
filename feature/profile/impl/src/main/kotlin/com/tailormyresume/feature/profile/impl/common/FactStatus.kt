package com.tailormyresume.feature.profile.impl.common

import androidx.annotation.StringRes
import com.tailormyresume.core.designsystem.component.TmrProvenanceKind
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.core.model.hasTooLongBullet
import com.tailormyresume.core.model.hasTooManyBullets
import com.tailormyresume.feature.profile.impl.R

enum class FactStatus { Confirmed, UserStated, UserEdited, Scanned, ToConfirm, TooLong, TooManyLines }

internal fun ProfileEntry.displayStatus(): FactStatus = when {
    hasTooLongBullet -> FactStatus.TooLong
    hasTooManyBullets -> FactStatus.TooManyLines
    else -> status()
}

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
    FactStatus.ToConfirm, FactStatus.TooLong, FactStatus.TooManyLines -> null
}

@StringRes
internal fun FactStatus.labelRes(): Int = when (this) {
    FactStatus.Confirmed -> R.string.feature_profile_impl_status_confirmed
    FactStatus.UserStated -> R.string.feature_profile_impl_status_user_stated
    FactStatus.UserEdited -> R.string.feature_profile_impl_status_user_edited
    FactStatus.Scanned -> R.string.feature_profile_impl_status_scanned
    FactStatus.ToConfirm -> R.string.feature_profile_impl_status_to_confirm
    FactStatus.TooLong -> R.string.feature_profile_impl_status_too_long
    FactStatus.TooManyLines -> R.string.feature_profile_impl_status_too_many_lines
}

internal fun FactSource.status(isConfirmed: Boolean): FactStatus = when {
    !isConfirmed -> FactStatus.ToConfirm
    this == FactSource.USER_STATED -> FactStatus.UserStated
    this == FactSource.USER_EDITED -> FactStatus.UserEdited
    else -> FactStatus.Confirmed
}
