package com.hirehop.feature.analysis.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhBottomSheet
import com.hirehop.core.designsystem.component.HhEvidenceText
import com.hirehop.core.designsystem.component.HhFactId
import com.hirehop.core.designsystem.component.HhIconButton
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhProvenanceChip
import com.hirehop.core.designsystem.component.HhProvenanceKind
import com.hirehop.core.designsystem.component.HhStatusDisc
import com.hirehop.core.designsystem.component.HhStatusKind
import com.hirehop.core.designsystem.component.HhTextButton
import com.hirehop.core.designsystem.component.HhTextField
import com.hirehop.core.designsystem.component.evidenceMarkSpanStyle
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.designsystem.theme.hhShadow
import com.hirehop.core.domain.displayKeywords
import com.hirehop.core.domain.prep.RequirementPhrase
import com.hirehop.core.ui.FactSourceProvenance
import kotlin.math.roundToInt

@Composable
internal fun RowMenuOverlay(state: AnalysisUiState.Result, anchor: Rect, actions: AnalysisActions) {
    val menu = state.overlay as? AnalysisOverlay.Menu ?: return
    val item = state.itemOrNull(menu.requirementId) ?: return
    val colors = HhTheme.colors
    val density = LocalDensity.current
    val menuWidth = HhTheme.spacing.d64 * MENU_WIDTH_UNITS
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = actions.onDismissOverlay,
            ),
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
                .hhShadow(HhTheme.elevation.modal, HhTheme.shapes.banner)
                .background(colors.surface, HhTheme.shapes.banner)
                .padding(vertical = HhTheme.spacing.sm),
        ) {
            if (!item.isReported) {
                MenuItem(HhIcons.Flag, stringResource(R.string.feature_analysis_impl_menu_report)) {
                    actions.onReport(item.id)
                }
            }
            if (item.hasSource) {
                MenuItem(
                    HhIcons.Facts,
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
            .heightIn(min = HhTheme.spacing.touch)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = HhTheme.spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = HhTheme.colors.onSurfaceVariant, modifier = Modifier.size(HhTheme.spacing.xxl))
        Text(text = label, style = HhTheme.typography.bodyM, color = HhTheme.colors.onSurface)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AnalysisSheets(state: AnalysisUiState.Result, actions: AnalysisActions) {
    when (val overlay = state.overlay) {
        is AnalysisOverlay.Source -> state.itemOrNull(overlay.requirementId)?.let { item ->
            HhBottomSheet(onDismissRequest = actions.onDismissOverlay) {
                SourceSheetContent(item, actions)
            }
        }
        is AnalysisOverlay.Question -> state.itemOrNull(overlay.requirementId)?.let { item ->
            HhBottomSheet(onDismissRequest = actions.onDismissOverlay) {
                QuestionSheetContent(item, actions, notClosed = overlay.notClosed)
            }
        }
        AnalysisOverlay.ShareCard -> ShareFitScreen(state, actions)
        else -> Unit
    }
}

@Composable
internal fun SourceSheetContent(item: RequirementItem, actions: AnalysisActions) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
        Text(
            text = stringResource(R.string.feature_analysis_impl_source_title, item.requirement.text),
            style = HhTheme.typography.labelM,
            color = HhTheme.colors.onSurfaceVariant,
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
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.body,
            )
        }
        val single = item.factRefs.singleOrNull()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (single != null) Arrangement.SpaceBetween else Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (single != null) {
                HhTextButton(
                    label = stringResource(R.string.feature_analysis_impl_edit_fact),
                    onClick = { actions.onEditFact(single.factId) },
                    leadingIcon = HhIcons.Edit,
                )
            }
            if (!item.isReported) {
                HhTextButton(
                    label = stringResource(R.string.feature_analysis_impl_menu_report),
                    onClick = { actions.onReport(item.id) },
                    leadingIcon = HhIcons.Flag,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FactBlock(ref: RequirementFactRef, keywords: List<String>, onEdit: (() -> Unit)?) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            HhFactId(id = ref.displayId)
            HhProvenanceChip(kind = ref.provenanceKind(), label = stringResource(ref.provenanceKind().labelRes()))
            if (onEdit != null) {
                HhTextButton(label = stringResource(R.string.feature_analysis_impl_edit_fact), onClick = onEdit)
            }
        }
        if (ref.lines.isEmpty()) {
            HhEvidenceText(text = highlighted(ref.title, keywords), style = HhTheme.typography.bodyM)
        } else {
            Text(text = ref.headline(), style = HhTheme.typography.titleM, color = HhTheme.colors.onSurface)
            ref.lines.forEach { line ->
                HhEvidenceText(text = highlighted(line, keywords), color = HhTheme.colors.body)
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

private fun RequirementFactRef.provenanceKind(): HhProvenanceKind =
    FactSourceProvenance().confirmedKindOf(source, isConfirmed)

private fun HhProvenanceKind.labelRes(): Int = when (this) {
    HhProvenanceKind.Confirmed -> R.string.feature_analysis_impl_provenance_confirmed
    HhProvenanceKind.UserStated -> R.string.feature_analysis_impl_provenance_user_stated
    HhProvenanceKind.UserEdited -> R.string.feature_analysis_impl_provenance_user_edited
    HhProvenanceKind.Scanned -> R.string.feature_analysis_impl_provenance_scanned
}

@Composable
internal fun QuestionSheetContent(item: RequirementItem, actions: AnalysisActions, notClosed: Boolean = false) {
    var statement by rememberSaveable { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = stringResource(R.string.feature_analysis_impl_question_title),
            style = HhTheme.typography.headlineM,
            color = HhTheme.colors.onSurface,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .background(HhTheme.colors.card, HhTheme.shapes.pill)
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HhStatusDisc(kind = HhStatusKind.Gap, size = 22.dp)
            Text(
                text = item.requirement.text.headline(),
                style = HhTheme.typography.bodyL.copy(fontWeight = FontWeight.Bold),
                color = HhTheme.colors.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.feature_analysis_impl_status_gap),
                style = HhTheme.typography.labelM.copy(fontWeight = FontWeight.Bold),
                color = HhTheme.colors.body,
            )
        }
        Text(
            text = stringResource(R.string.feature_analysis_impl_question_body),
            style = HhTheme.typography.bodyM,
            color = HhTheme.colors.onSurfaceVariant,
        )
        if (notClosed) {
            val keywords = displayKeywords(item.requirement)
            Text(
                text = if (keywords.isNotEmpty()) {
                    stringResource(R.string.feature_analysis_impl_question_not_closed, keywords.joinToString(" or "))
                } else {
                    stringResource(R.string.feature_analysis_impl_question_not_closed_generic)
                },
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.onSurface,
            )
        }
        NewFactRow(
            id = stringResource(R.string.feature_analysis_impl_question_new_fact_id),
            text = stringResource(R.string.feature_analysis_impl_question_new_fact_text),
        )
        HhTextField(
            value = statement,
            onValueChange = { statement = it },
            label = stringResource(R.string.feature_analysis_impl_question_label),
            singleLine = false,
            minLines = QUESTION_MIN_LINES,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HhIconButton(
                icon = HhIcons.Close,
                contentDescription = stringResource(R.string.feature_analysis_impl_question_close),
                onClick = actions.onDismissOverlay,
                size = 60.dp,
                containerColor = HhTheme.colors.card,
                borderColor = HhTheme.colors.outlineVariant,
            )
            HhPrimaryButton(
                label = stringResource(R.string.feature_analysis_impl_question_write),
                onClick = { actions.onSubmitEvidence(item.id, statement) },
                modifier = Modifier.weight(1f),
                enabled = statement.isNotBlank(),
            )
        }
    }
}

@Composable
private fun NewFactRow(id: String, text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .background(HhTheme.colors.primaryContainer, HhTheme.shapes.pill)
            .border(2.dp, HhTheme.colors.brand, HhTheme.shapes.pill)
            .padding(start = 14.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = id,
            style = HhTheme.typography.labelM,
            color = HhTheme.colors.onSurfaceVariant,
        )
        Text(
            text = text,
            style = HhTheme.typography.labelL.copy(fontWeight = FontWeight.Bold),
            color = HhTheme.colors.onPrimaryContainer,
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
