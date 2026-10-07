package com.hirehop.feature.tailor.impl

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.hirehop.core.designsystem.component.HhProvenanceKind
import com.hirehop.core.model.EditType
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.FactSource
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
internal fun EntryCategory.headingRes(): Int = when (this) {
    EntryCategory.EXPERIENCE -> R.string.feature_tailor_impl_section_experience
    EntryCategory.PROJECT -> R.string.feature_tailor_impl_section_projects
    EntryCategory.CERTIFICATION -> R.string.feature_tailor_impl_section_certifications
    EntryCategory.ACHIEVEMENT -> R.string.feature_tailor_impl_section_achievements
    EntryCategory.EDUCATION -> R.string.feature_tailor_impl_section_education
}

internal fun FactSource.provenanceKind(): HhProvenanceKind = when (this) {
    FactSource.IMPORTED -> HhProvenanceKind.Confirmed
    FactSource.USER_STATED -> HhProvenanceKind.UserStated
    FactSource.USER_EDITED -> HhProvenanceKind.UserEdited
}

@StringRes
internal fun FactSource.provenanceRes(): Int = when (this) {
    FactSource.IMPORTED -> R.string.feature_tailor_impl_provenance_confirmed
    FactSource.USER_STATED -> R.string.feature_tailor_impl_provenance_user_stated
    FactSource.USER_EDITED -> R.string.feature_tailor_impl_provenance_user_edited
}

@Composable
internal fun GuardrailViolation.flagNote(sourceId: String): String = when (this) {
    GuardrailViolation.MissingSource -> flagOther(stringResource(R.string.feature_tailor_impl_violation_missing_source))
    is GuardrailViolation.UnsupportedNumber ->
        flagOther(stringResource(R.string.feature_tailor_impl_violation_number, value))
    is GuardrailViolation.UnsupportedTerm ->
        flagOther(stringResource(R.string.feature_tailor_impl_violation_term, term))
    is GuardrailViolation.VerbEscalation -> stringResource(R.string.feature_tailor_impl_bullet_verb_flag, from, to)
    is GuardrailViolation.UnsupportedScaleClaim ->
        stringResource(R.string.feature_tailor_impl_bullet_scale_flag, phrase, sourceId)
}

@Composable
private fun flagOther(text: String): String = stringResource(R.string.feature_tailor_impl_bullet_flag_other, text)
