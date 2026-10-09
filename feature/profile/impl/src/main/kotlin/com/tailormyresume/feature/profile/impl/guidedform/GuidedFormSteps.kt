package com.tailormyresume.feature.profile.impl.guidedform

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.component.TmrCard
import com.tailormyresume.core.designsystem.component.TmrHeadline
import com.tailormyresume.core.designsystem.component.TmrTextButton
import com.tailormyresume.core.designsystem.component.TmrTextField
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.domain.fact.FactLineRenderer
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.feature.profile.impl.R
import com.tailormyresume.feature.profile.impl.common.FactCard
import com.tailormyresume.feature.profile.impl.common.FactIdChip
import com.tailormyresume.feature.profile.impl.common.FactStatus
import com.tailormyresume.feature.profile.impl.common.Note
import com.tailormyresume.feature.profile.impl.common.NoteTone
import com.tailormyresume.feature.profile.impl.common.RemovableChip
import com.tailormyresume.feature.profile.impl.common.kindRes

private val DiscSize = 88.dp
private val SelectedWidth = 2.dp

@Composable
internal fun FiledEntries(
    uiState: GuidedFormUiState,
    onEditFact: (entryId: String, entryType: String) -> Unit,
) {
    if (uiState.filedEntries.isEmpty()) return
    val previous = guidedStepAt(uiState.stepIndex - 1)
    Note(
        text = stringResource(R.string.feature_profile_impl_guided_form_filed_caption, stringResource(stepTitleRes(previous))),
        tone = NoteTone.Positive,
        icon = TmrIcons.CheckCircle,
    )
    uiState.filedEntries.forEachIndexed { index, entry ->
        FiledCard(entry = entry, highlighted = index == 0, onEdit = { onEditFact(entry.id, entry.category.name) })
    }
}

@Composable
private fun FiledCard(entry: ProfileEntry, highlighted: Boolean, onEdit: () -> Unit) {
    FactCard(
        id = entry.id,
        status = FactStatus.UserStated,
        kind = stringResource(entry.category.kindRes()),
        summary = FactLineRenderer.render(entry),
        highlighted = highlighted,
    ) {
        TmrTextButton(label = stringResource(R.string.feature_profile_impl_fact_edit), onClick = onEdit)
    }
}

@Composable
internal fun StepContent(
    uiState: GuidedFormUiState,
    actions: GuidedFormActions,
) {
    when (uiState.step) {
        GuidedStep.CONTACT, GuidedStep.EDUCATION -> Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md)) {
            uiState.step.fields().forEach { field -> StepField(field = field, uiState = uiState, actions = actions) }
        }

        GuidedStep.SKILLS -> SkillsContent(uiState = uiState, actions = actions)

        GuidedStep.EXPERIENCE -> ExperienceContent(choice = uiState.experienceChoice, actions = actions)
    }
}

@Composable
private fun StepField(
    field: GuidedField,
    uiState: GuidedFormUiState,
    actions: GuidedFormActions,
) {
    val label = if (field == GuidedField.COURSEWORK) {
        stringResource(R.string.feature_profile_impl_guided_form_field_coursework_optional)
    } else {
        stringResource(fieldLabelRes(field))
    }
    TmrTextField(
        value = uiState.values[field].orEmpty(),
        onValueChange = { actions.onValueChange(field, it) },
        label = label,
        errorText = uiState.fieldProblems[field]?.let { problemText(it) },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SkillsContent(
    uiState: GuidedFormUiState,
    actions: GuidedFormActions,
) {
    Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md)) {
        TmrTextField(
            value = uiState.values[GuidedField.SKILL].orEmpty(),
            onValueChange = { actions.onValueChange(GuidedField.SKILL, it) },
            label = stringResource(fieldLabelRes(GuidedField.SKILL)),
            placeholder = stringResource(R.string.feature_profile_impl_guided_form_skill_placeholder),
            trailingSlot = {
                TmrTextButton(
                    label = stringResource(R.string.feature_profile_impl_add),
                    onClick = actions.onAddSkill,
                    enabled = uiState.values[GuidedField.SKILL].orEmpty().isNotBlank(),
                )
            },
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
        ) {
            uiState.skills.forEach { skill -> RemovableChip(label = skill, onRemove = { actions.onRemoveSkill(skill) }) }
        }
        Note(
            text = stringResource(R.string.feature_profile_impl_guided_form_skills_hint),
            tone = NoteTone.Plain,
            icon = TmrIcons.Info,
        )
    }
}

@Composable
private fun ExperienceContent(
    choice: ExperienceChoice?,
    actions: GuidedFormActions,
) {
    Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md)) {
        Text(
            text = stringResource(R.string.feature_profile_impl_guided_form_experience_question),
            style = TmrTheme.typography.titleL,
            color = TmrTheme.colors.onSurface,
        )
        ChoiceRow(
            labelRes = R.string.feature_profile_impl_guided_form_choice_yes,
            selected = choice == ExperienceChoice.YES,
            onClick = { actions.onChooseExperience(ExperienceChoice.YES) },
        )
        ChoiceRow(
            labelRes = R.string.feature_profile_impl_guided_form_choice_no,
            selected = choice == ExperienceChoice.NO,
            onClick = { actions.onChooseExperience(ExperienceChoice.NO) },
        )
        if (choice == ExperienceChoice.NO) {
            Note(text = stringResource(R.string.feature_profile_impl_guided_form_skips_reassurance), icon = TmrIcons.Info)
        }
    }
}

@Composable
private fun ChoiceRow(
    @StringRes labelRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val outline = if (selected) Modifier.border(SelectedWidth, TmrTheme.colors.primary, TmrTheme.shapes.card) else Modifier
    TmrCard(
        onClick = onClick,
        modifier = outline.semantics {
            this.selected = selected
            role = Role.RadioButton
        },
        contentPadding = PaddingValues(horizontal = TmrTheme.spacing.lg, vertical = TmrTheme.spacing.md),
    ) {
        Row(
            modifier = Modifier.padding(vertical = TmrTheme.spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(labelRes),
                modifier = Modifier.weight(1f),
                style = TmrTheme.typography.titleM,
                color = TmrTheme.colors.onSurface,
            )
            if (selected) Icon(TmrIcons.Check, contentDescription = null, tint = TmrTheme.colors.primary)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SavedBody(
    saved: GuidedSaved,
    padding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(horizontal = TmrTheme.spacing.gutter),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
    ) {
        Box(
            modifier = Modifier.size(DiscSize).background(TmrTheme.colors.metContainer, TmrTheme.shapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(TmrIcons.CheckCircle, contentDescription = null, tint = TmrTheme.colors.met, modifier = Modifier.size(40.dp))
        }
        TmrHeadline(
            text = stringResource(R.string.feature_profile_impl_guided_form_saved_title),
            style = TmrTheme.typography.headlineL,
        )
        Text(
            text = pluralStringResource(
                R.plurals.feature_profile_impl_guided_form_saved_summary,
                saved.completedSteps,
                saved.completedSteps,
                saved.totalSteps,
            ),
            style = TmrTheme.typography.bodyL.copy(fontWeight = FontWeight.SemiBold),
            color = TmrTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        StepProgress(
            number = saved.completedSteps,
            stepName = saved.lastDoneStep?.let { stringResource(stepTitleRes(it)) },
            filledBars = saved.completedSteps,
            doneSteps = saved.doneSteps,
            currentStep = null,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
        ) {
            saved.entryIds.forEach { id -> FactIdChip(id = id, status = FactStatus.UserStated) }
        }
    }
}
