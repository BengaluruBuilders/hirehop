package com.hirehop.feature.profile.impl.guidedform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhErrorCallout
import com.hirehop.core.designsystem.component.HhHeroCard
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhSpotKind
import com.hirehop.core.designsystem.component.HhTextButton
import com.hirehop.core.designsystem.component.HhTextField
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.domain.fact.FactLineRenderer
import com.hirehop.core.model.ProfileEntry
import com.hirehop.feature.profile.impl.R
import com.hirehop.feature.profile.impl.common.FactCard
import com.hirehop.feature.profile.impl.common.FactIdChip
import com.hirehop.feature.profile.impl.common.FactStatus
import com.hirehop.feature.profile.impl.common.RemovableChip
import com.hirehop.feature.profile.impl.common.SpotCircle
import com.hirehop.feature.profile.impl.common.kindRes

private val SpotSize = 96.dp
private val SegmentHeight = 6.dp
private val SegmentGap = 4.dp
private val CardGap = 10.dp
private val ButtonHeight = 52.dp
private val CheckSize = 28.dp
private val ChipGap = 6.dp

@Composable
internal fun GuidedFormScreen(
    uiState: GuidedFormUiState,
    actions: GuidedFormActions,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val saved = uiState.saved
    val goBack = if (saved == null && !uiState.isFirstStep && !uiState.showIntro) actions.onBack else onBack
    HhScreen(
        modifier = modifier,
        sheet = false,
        header = {
            HhInnerHeader(
                title = stringResource(R.string.feature_profile_impl_guided_form_title),
                subtitle = stringResource(R.string.feature_profile_impl_guided_form_subtitle),
                onBack = goBack,
                backContentDescription = stringResource(R.string.feature_profile_impl_guided_form_back),
            )
        },
        bottomBar = if (uiState.isLoading) null else ({ GuidedActionBar(uiState = uiState, actions = actions) }),
    ) { padding ->
        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                HhLoadingWheel(contentDesc = stringResource(R.string.feature_profile_impl_guided_form_loading))
            }

            saved != null -> SavedBody(saved = saved, padding = padding)

            else -> FormBody(uiState = uiState, actions = actions, padding = padding)
        }
    }
}

@Composable
private fun FormBody(
    uiState: GuidedFormUiState,
    actions: GuidedFormActions,
    padding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(horizontal = HhTheme.spacing.gutter),
        verticalArrangement = Arrangement.spacedBy(CardGap),
    ) {
        if (uiState.message == GuidedMessage.LOAD_FAILED) {
            HhErrorCallout(title = stringResource(R.string.feature_profile_impl_guided_form_load_failed))
        }
        if (uiState.message == GuidedMessage.SAVE_FAILED) {
            HhErrorCallout(title = stringResource(R.string.feature_profile_impl_guided_form_save_failed))
        }
        if (uiState.showIntro) {
            ScannedCard()
        } else if (uiState.isOffline) {
            OfflineCard()
        }
        StepCounterCard(stepIndex = uiState.stepIndex)
        if (uiState.showIntro) {
            HhHeroCard(contentPadding = PaddingValues(HhTheme.spacing.cardPadding)) {
                Text(
                    text = stringResource(R.string.feature_profile_impl_guided_form_intro),
                    style = HhTheme.typography.bodyM,
                    color = HhTheme.colors.body,
                )
            }
        } else {
            FiledEntries(uiState = uiState)
            StepContent(uiState = uiState, actions = actions)
        }
    }
}

@Composable
private fun ScannedCard() {
    HhHeroCard(contentPadding = PaddingValues(HhTheme.spacing.cardPadding)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SpotCircle(kind = HhSpotKind.Scanned, size = SpotSize, contentDescription = null)
            Text(
                text = stringResource(R.string.feature_profile_impl_guided_form_scanned_arrival),
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.body,
            )
        }
    }
}

@Composable
private fun OfflineCard() {
    HhHeroCard(contentPadding = PaddingValues(HhTheme.spacing.cardPadding)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SpotCircle(kind = HhSpotKind.Offline, size = SpotSize, contentDescription = null)
            Text(
                text = stringResource(R.string.feature_profile_impl_guided_form_offline_message),
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.body,
            )
        }
    }
}

@Composable
private fun StepCounterCard(stepIndex: Int) {
    val step = guidedStepAt(stepIndex)
    val total = GUIDED_STEPS.size
    val stepName = stringResource(stepTitleRes(step))
    val description = stringResource(
        R.string.feature_profile_impl_guided_form_step_counter_description,
        stepIndex + 1,
        total,
        stepName,
    )
    HhHeroCard(contentPadding = PaddingValues(HhTheme.spacing.cardPadding)) {
        Column(
            modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = description },
            verticalArrangement = Arrangement.spacedBy(CardGap),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(CardGap),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = stringResource(R.string.feature_profile_impl_guided_form_step_of, stepIndex + 1, total),
                    style = HhTheme.typography.displayM,
                    color = HhTheme.colors.onSurface,
                )
                Text(
                    text = stepName,
                    style = HhTheme.typography.titleM,
                    color = HhTheme.colors.onSurface,
                    modifier = Modifier.padding(bottom = HhTheme.spacing.xs),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SegmentGap),
            ) {
                repeat(total) { position ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(SegmentHeight)
                            .clip(HhTheme.shapes.pill)
                            .background(
                                if (position <= stepIndex) HhTheme.colors.primary else HhTheme.colors.outlineVariant,
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun FiledEntries(uiState: GuidedFormUiState) {
    if (uiState.filedEntries.isEmpty()) return
    val previous = guidedStepAt(uiState.stepIndex - 1)
    Column(
        modifier = Modifier.padding(top = HhTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        Text(
            text = stringResource(
                R.string.feature_profile_impl_guided_form_filed_caption,
                stringResource(stepTitleRes(previous)),
            ),
            style = HhTheme.typography.labelM,
            color = HhTheme.colors.onSurfaceVariant,
        )
        uiState.filedEntries.forEachIndexed { index, entry ->
            FiledCard(entry = entry, highlighted = index == 0)
        }
    }
}

@Composable
private fun FiledCard(entry: ProfileEntry, highlighted: Boolean) {
    FactCard(
        id = entry.id,
        status = FactStatus.UserStated,
        kind = stringResource(entry.category.kindRes()),
        summary = FactLineRenderer.render(entry),
        highlighted = highlighted,
    )
}

@Composable
private fun StepContent(
    uiState: GuidedFormUiState,
    actions: GuidedFormActions,
) {
    when (uiState.step) {
        GuidedStep.CONTACT, GuidedStep.EDUCATION -> HhHeroCard(contentPadding = PaddingValues(HhTheme.spacing.lg)) {
            Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
                uiState.step.fields().forEach { field -> StepField(field = field, uiState = uiState, actions = actions) }
            }
        }

        GuidedStep.SKILLS -> SkillsCard(uiState = uiState, actions = actions)

        GuidedStep.EXPERIENCE -> ExperienceCard(actions = actions)
    }
}

@Composable
private fun StepField(
    field: GuidedField,
    uiState: GuidedFormUiState,
    actions: GuidedFormActions,
) {
    val label = stringResource(fieldLabelRes(field))
    HhTextField(
        value = uiState.values[field].orEmpty(),
        onValueChange = { actions.onValueChange(field, it) },
        label = if (field == GuidedField.COURSEWORK) {
            stringResource(R.string.feature_profile_impl_guided_form_field_coursework_optional)
        } else {
            label
        },
        errorText = uiState.fieldProblems[field]?.let { problemText(it) },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SkillsCard(
    uiState: GuidedFormUiState,
    actions: GuidedFormActions,
) {
    HhHeroCard(contentPadding = PaddingValues(HhTheme.spacing.lg)) {
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
            HhTextField(
                value = uiState.values[GuidedField.SKILL].orEmpty(),
                onValueChange = { actions.onValueChange(GuidedField.SKILL, it) },
                label = stringResource(fieldLabelRes(GuidedField.SKILL)),
                placeholder = stringResource(R.string.feature_profile_impl_guided_form_skill_placeholder),
                trailingSlot = {
                    HhTextButton(
                        label = stringResource(R.string.feature_profile_impl_add),
                        onClick = actions.onAddSkill,
                        enabled = uiState.values[GuidedField.SKILL].orEmpty().isNotBlank(),
                    )
                },
                supportingText = {
                    Text(text = stringResource(R.string.feature_profile_impl_guided_form_skills_hint))
                },
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(ChipGap),
                verticalArrangement = Arrangement.spacedBy(ChipGap),
            ) {
                uiState.skills.forEach { skill ->
                    RemovableChip(label = skill, onRemove = { actions.onRemoveSkill(skill) })
                }
            }
        }
    }
}

@Composable
private fun ExperienceCard(actions: GuidedFormActions) {
    HhHeroCard(contentPadding = PaddingValues(HhTheme.spacing.lg + HhTheme.spacing.xxs)) {
        Column(verticalArrangement = Arrangement.spacedBy(CardGap)) {
            Text(
                text = stringResource(R.string.feature_profile_impl_guided_form_experience_question),
                style = HhTheme.typography.titleL,
                color = HhTheme.colors.onSurface,
            )
            Text(
                text = stringResource(R.string.feature_profile_impl_guided_form_skips_reassurance),
                style = HhTheme.typography.bodyL,
                color = HhTheme.colors.body,
            )
            Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
                HhOutlineButton(
                    label = stringResource(R.string.feature_profile_impl_guided_form_add_job),
                    onClick = actions.onAddJob,
                    trailingIcon = HhIcons.Add,
                    modifier = Modifier.fillMaxWidth().height(ButtonHeight),
                )
                HhOutlineButton(
                    label = stringResource(R.string.feature_profile_impl_guided_form_go_to_projects),
                    onClick = actions.onGoToProjects,
                    trailingIcon = HhIcons.ArrowForward,
                    modifier = Modifier.fillMaxWidth().height(ButtonHeight),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SavedBody(
    saved: GuidedSaved,
    padding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(horizontal = HhTheme.spacing.gutter),
    ) {
        HhHeroCard(contentPadding = PaddingValues(HhTheme.spacing.lg + HhTheme.spacing.xxs)) {
            Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(CardGap),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = HhIcons.CheckCircle,
                        contentDescription = null,
                        tint = HhTheme.colors.met,
                        modifier = Modifier.size(CheckSize),
                    )
                    Text(
                        text = stringResource(R.string.feature_profile_impl_guided_form_saved_title),
                        style = HhTheme.typography.titleL,
                        color = HhTheme.colors.onSurface,
                    )
                }
                Text(
                    text = pluralStringResource(
                        R.plurals.feature_profile_impl_guided_form_saved_summary,
                        saved.completedSteps,
                        saved.completedSteps,
                        saved.totalSteps,
                    ),
                    style = HhTheme.typography.bodyM,
                    color = HhTheme.colors.body,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(ChipGap),
                    verticalArrangement = Arrangement.spacedBy(ChipGap),
                ) {
                    saved.entryIds.forEach { id -> FactIdChip(id = id, status = FactStatus.UserStated) }
                }
            }
        }
    }
}

@Composable
private fun GuidedActionBar(
    uiState: GuidedFormUiState,
    actions: GuidedFormActions,
) {
    HhBottomActionBar {
        if (uiState.saved != null) {
            HhPrimaryButton(
                label = stringResource(R.string.feature_profile_impl_guided_form_back_to_profile),
                onClick = actions.onFinishSaved,
                leadingIcon = HhIcons.Profile,
                modifier = Modifier.weight(1f),
            )
        } else {
            HhOutlineButton(
                label = stringResource(R.string.feature_profile_impl_guided_form_save_and_finish_later),
                onClick = actions.onSaveAndFinishLater,
                enabled = !uiState.isSaving,
                modifier = Modifier.weight(1f),
            )
            HhPrimaryButton(
                label = stringResource(primaryLabelRes(uiState)),
                onClick = if (uiState.showIntro) actions.onStartForm else actions.onNext,
                enabled = !uiState.isSaving,
                trailingIcon = HhIcons.ArrowForward,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private fun primaryLabelRes(uiState: GuidedFormUiState): Int = when {
    uiState.showIntro -> R.string.feature_profile_impl_guided_form_start_with_contact
    uiState.isLastStep -> R.string.feature_profile_impl_guided_form_next_evidence
    else -> R.string.feature_profile_impl_guided_form_next
}
