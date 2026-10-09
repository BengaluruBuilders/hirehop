package com.tailormyresume.feature.analysis.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import com.tailormyresume.core.designsystem.component.TmrBottomSheet
import com.tailormyresume.core.designsystem.component.TmrEvidenceText
import com.tailormyresume.core.designsystem.component.TmrFactId
import com.tailormyresume.core.designsystem.component.TmrPrimaryButton
import com.tailormyresume.core.designsystem.component.TmrProvenanceChip
import com.tailormyresume.core.designsystem.component.TmrProvenanceKind
import com.tailormyresume.core.designsystem.component.TmrSecondaryButton
import com.tailormyresume.core.designsystem.component.TmrStatusChip
import com.tailormyresume.core.designsystem.component.TmrStatusKind
import com.tailormyresume.core.designsystem.component.TmrTextButton
import com.tailormyresume.core.designsystem.component.TmrTextField
import com.tailormyresume.core.designsystem.component.evidenceMarkSpanStyle
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.designsystem.theme.tmrShadow
import com.tailormyresume.core.domain.displayKeywords
import com.tailormyresume.core.domain.prep.RequirementPhrase
import com.tailormyresume.core.ui.FactSourceProvenance
import kotlin.math.roundToInt

@Composable
internal fun RowMenuOverlay(state: AnalysisUiState.Result, anchor: Rect, actions: AnalysisActions) {
    val menu = state.overlay as? AnalysisOverlay.Menu ?: return
    val item = state.itemOrNull(menu.requirementId) ?: return
    val colors = TmrTheme.colors
    val density = LocalDensity.current
    val menuWidth = TmrTheme.spacing.d64 * MENU_WIDTH_UNITS
    val dismissLabel = stringResource(R.string.feature_analysis_impl_menu_dismiss)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClickLabel = dismissLabel,
                onClick = actions.onDismissOverlay,
            )
            .semantics { contentDescription = dismissLabel },
    ) {
        Column(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (anchor.right - with(density) { menuWidth.toPx() }).roundToInt(),
                        y = anchor.bottom.roundToInt(),
                    )
                }
                .width(menuWidth)
                .tmrShadow(TmrTheme.elevation.modal, TmrTheme.shapes.banner)
                .background(colors.surface, TmrTheme.shapes.banner)
                .padding(vertical = TmrTheme.spacing.sm),
        ) {
            if (!item.isReported) {
                MenuItem(TmrIcons.Flag, stringResource(R.string.feature_analysis_impl_menu_report)) {
                    actions.onReport(item.id)
                }
            }
            if (item.hasSource) {
                MenuItem(
                    TmrIcons.Facts,
                    stringResource(R.string.feature_analysis_impl_menu_source),
                ) { actions.onSeeSource(item.id) }
            }
        }
    }
}

@Composable
private fun MenuItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = TmrTheme.spacing.touch)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = TmrTheme.spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = TmrTheme.colors.onSurfaceVariant, modifier = Modifier.size(TmrTheme.spacing.xxl))
        Text(text = label, style = TmrTheme.typography.bodyM, color = TmrTheme.colors.onSurface)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AnalysisSheets(state: AnalysisUiState.Result, actions: AnalysisActions) {
    when (val overlay = state.overlay) {
        is AnalysisOverlay.Source -> state.itemOrNull(overlay.requirementId)?.let { item ->
            TmrBottomSheet(onDismissRequest = actions.onDismissOverlay) {
                SourceSheetContent(item, actions)
            }
        }
        is AnalysisOverlay.Question -> state.itemOrNull(overlay.requirementId)?.let { item ->
            TmrBottomSheet(onDismissRequest = actions.onDismissOverlay) {
                QuestionSheetContent(item, actions, notClosed = overlay.notClosed)
            }
        }
        AnalysisOverlay.ShareCard -> ShareFitScreen(state, actions)
        else -> Unit
    }
}

@Composable
internal fun SourceSheetContent(item: RequirementItem, actions: AnalysisActions) {
    Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md)) {
        Text(
            text = stringResource(R.string.feature_analysis_impl_source_title, item.requirement.text),
            style = TmrTheme.typography.labelM,
            color = TmrTheme.colors.onSurfaceVariant,
        )
        item.factRefs.forEach { ref ->
            FactBlock(
                ref = ref,
                keywords = item.requirement.keywords,
                onEdit = { actions.onEditFact(ref.factId) }.takeIf { item.factRefs.size > 1 },
            )
        }
        if (item.skills.isNotEmpty()) {
            Text(
                text = stringResource(R.string.feature_analysis_impl_source_skills, item.skills.joinToString()),
                style = TmrTheme.typography.bodyM,
                color = TmrTheme.colors.body,
            )
        }
        val single = item.factRefs.singleOrNull()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (single != null) Arrangement.SpaceBetween else Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (single != null) {
                TmrTextButton(
                    label = stringResource(R.string.feature_analysis_impl_edit_fact),
                    onClick = { actions.onEditFact(single.factId) },
                    leadingIcon = TmrIcons.Edit,
                )
            }
            if (!item.isReported) {
                TmrTextButton(
                    label = stringResource(R.string.feature_analysis_impl_menu_report),
                    onClick = { actions.onReport(item.id) },
                    leadingIcon = TmrIcons.Flag,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FactBlock(ref: RequirementFactRef, keywords: List<String>, onEdit: (() -> Unit)?) {
    Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            TmrFactId(id = ref.displayId)
            TmrProvenanceChip(kind = ref.provenanceKind(), label = stringResource(ref.provenanceKind().labelRes()))
            if (onEdit != null) {
                TmrTextButton(label = stringResource(R.string.feature_analysis_impl_edit_fact), onClick = onEdit)
            }
        }
        if (ref.lines.isEmpty()) {
            TmrEvidenceText(text = highlighted(ref.title, keywords), style = TmrTheme.typography.bodyM)
        } else {
            Text(text = ref.headline(), style = TmrTheme.typography.titleM, color = TmrTheme.colors.onSurface)
            ref.lines.forEach { line ->
                TmrEvidenceText(text = highlighted(line, keywords), color = TmrTheme.colors.body)
            }
        }
    }
}

@Composable
private fun RequirementFactRef.headline(): String {
    val place = listOf(title, organization).filter { it.isNotBlank() }.joinToString(", ")
    return when {
        startDate.isBlank() -> place
        endDate.isBlank() -> stringResource(R.string.feature_analysis_impl_fact_until_now, place, startDate)
        else -> stringResource(R.string.feature_analysis_impl_fact_dates, place, startDate, endDate)
    }
}

@Composable
private fun highlighted(text: String, keywords: List<String>): AnnotatedString {
    val style = evidenceMarkSpanStyle()
    return buildAnnotatedString {
        append(text)
        keywords.filter { it.isNotBlank() }.forEach { keyword ->
            var from = text.indexOf(keyword, ignoreCase = true)
            while (from >= 0) {
                addStyle(style, from, from + keyword.length)
                from = text.indexOf(keyword, from + keyword.length, ignoreCase = true)
            }
        }
    }
}

private fun RequirementFactRef.provenanceKind(): TmrProvenanceKind =
    FactSourceProvenance().confirmedKindOf(source, isConfirmed)

private fun TmrProvenanceKind.labelRes(): Int = when (this) {
    TmrProvenanceKind.Confirmed -> R.string.feature_analysis_impl_provenance_confirmed
    TmrProvenanceKind.UserStated -> R.string.feature_analysis_impl_provenance_user_stated
    TmrProvenanceKind.UserEdited -> R.string.feature_analysis_impl_provenance_user_edited
    TmrProvenanceKind.Scanned -> R.string.feature_analysis_impl_provenance_scanned
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun QuestionSheetContent(item: RequirementItem, actions: AnalysisActions, notClosed: Boolean = false) {
    var statement by rememberSaveable { mutableStateOf("") }
    val name = item.requirement.text.headline()
    Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xs),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            TmrStatusChip(kind = TmrStatusKind.Gap, label = stringResource(R.string.feature_analysis_impl_status_gap))
            Text(
                text = stringResource(
                    R.string.feature_analysis_impl_question_priority,
                    stringResource(item.priorityLabelRes()),
                    name,
                ),
                style = TmrTheme.typography.bodyS.copy(fontWeight = FontWeight.Bold),
                color = TmrTheme.colors.onSurfaceVariant,
            )
        }
        Text(
            text = stringResource(R.string.feature_analysis_impl_question_title, name),
            style = TmrTheme.typography.headlineM,
            color = TmrTheme.colors.onSurface,
            modifier = Modifier.semantics { heading() },
        )
        if (notClosed) {
            val keywords = displayKeywords(item.requirement)
            Text(
                text = if (keywords.isNotEmpty()) {
                    stringResource(R.string.feature_analysis_impl_question_not_closed, keywords.joinToString(" or "))
                } else {
                    stringResource(R.string.feature_analysis_impl_question_not_closed_generic)
                },
                style = TmrTheme.typography.bodyM,
                color = TmrTheme.colors.onSurface,
            )
        }
        TmrTextField(
            value = statement,
            onValueChange = { statement = it },
            label = stringResource(R.string.feature_analysis_impl_question_label),
            singleLine = false,
            minLines = QUESTION_MIN_LINES,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm), verticalAlignment = Alignment.CenterVertically) {
            TmrFactId(id = stringResource(R.string.feature_analysis_impl_question_new_fact_id))
            Text(
                text = stringResource(R.string.feature_analysis_impl_question_note),
                style = TmrTheme.typography.bodyS,
                color = TmrTheme.colors.onSurfaceVariant,
            )
        }
        TmrPrimaryButton(
            label = stringResource(R.string.feature_analysis_impl_question_write),
            onClick = { actions.onSubmitEvidence(item.id, statement) },
            modifier = Modifier.fillMaxWidth(),
            enabled = statement.isNotBlank(),
        )
        TmrSecondaryButton(
            label = stringResource(R.string.feature_analysis_impl_question_cancel),
            onClick = actions.onDismissOverlay,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

internal fun String.splitDetail(): Pair<String, String?> {
    val match = DETAIL_PATTERN.matchEntire(trim()) ?: return trim() to null
    return match.groupValues[1].trim() to match.groupValues[2].trim()
}

internal fun String.headline(): String = RequirementPhrase.of(splitDetail().first)

private val DETAIL_PATTERN = Regex("""^(.*?)\s*\((.+)\)$""")
private const val MENU_WIDTH_UNITS = 4
private const val QUESTION_MIN_LINES = 3
