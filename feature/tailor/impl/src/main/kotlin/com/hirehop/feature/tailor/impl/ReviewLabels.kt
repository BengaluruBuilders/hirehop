package com.hirehop.feature.tailor.impl

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.hirehop.core.model.BulletDecision
import com.hirehop.core.model.EditType
import com.hirehop.core.model.GuardrailViolation

@StringRes
internal fun EditType.labelRes(): Int = when (this) {
    EditType.REWORD -> R.string.feature_tailor_impl_edit_reworded
    EditType.REORDER -> R.string.feature_tailor_impl_edit_reordered
    EditType.SHORTEN -> R.string.feature_tailor_impl_edit_shortened
    EditType.EMPHASISE -> R.string.feature_tailor_impl_edit_emphasised
    EditType.MERGE -> R.string.feature_tailor_impl_edit_merged
}

@StringRes
internal fun BulletDecision.statusRes(): Int = when (this) {
    BulletDecision.ACCEPTED -> R.string.feature_tailor_impl_decision_accepted
    BulletDecision.REJECTED -> R.string.feature_tailor_impl_decision_rejected
    BulletDecision.PENDING -> R.string.feature_tailor_impl_decision_pending
}

@Composable
internal fun GuardrailViolation.description(): String = when (this) {
    GuardrailViolation.MissingSource -> stringResource(R.string.feature_tailor_impl_violation_missing_source)
    is GuardrailViolation.UnsupportedNumber -> stringResource(R.string.feature_tailor_impl_violation_number, value)
    is GuardrailViolation.UnsupportedTerm -> stringResource(R.string.feature_tailor_impl_violation_term, term)
    is GuardrailViolation.VerbEscalation -> stringResource(R.string.feature_tailor_impl_violation_verb, from, to)
    is GuardrailViolation.UnsupportedScaleClaim -> stringResource(R.string.feature_tailor_impl_violation_scale, phrase)
}
