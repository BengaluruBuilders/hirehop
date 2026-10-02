package com.hirehop.feature.onboarding.impl.confirmfacts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhErrorCallout
import com.hirehop.core.designsystem.component.HhFactId
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhProvenanceChip
import com.hirehop.core.designsystem.component.HhProvenanceKind
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhSecondaryButton
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.illustration.HhIllustration
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.EntryCategory
import com.hirehop.feature.onboarding.impl.R
import com.hirehop.feature.onboarding.impl.common.DisclosureCard
import com.hirehop.feature.onboarding.impl.common.NoticeTone
import com.hirehop.feature.onboarding.impl.common.OnboardingNotice
import com.hirehop.feature.onboarding.impl.common.StateCard

data class ConfirmFactsActions(
    val onBack: () -> Unit,
    val onConfirm: (String) -> Unit,
    val onEdit: (String?, EntryCategory) -> Unit,
    val onAddOne: (ConfirmFactsSection) -> Unit,
    val onSkip: (ConfirmFactsSection) -> Unit,
    val onContinue: () -> Unit,
    val onImportResume: () -> Unit,
)

@Composable
fun ConfirmFactsScreen(
    uiState: ConfirmFactsUiState,
    actions: ConfirmFactsActions,
    modifier: Modifier = Modifier,
) {
    HhScreen(
        modifier = modifier,
        sheet = false,
        header = {
            HhInnerHeader(
                title = stringResource(R.string.feature_onboarding_impl_confirm_facts_title),
                subtitle = pluralStringResource(
                    R.plurals.feature_onboarding_impl_confirm_facts_counter,
                    uiState.facts.size,
                    uiState.confirmedCount,
                    uiState.facts.size,
                ),
                onBack = actions.onBack,
                backContentDescription = stringResource(R.string.feature_onboarding_impl_confirm_facts_back_description),
            )
        },
        bottomBar = if (uiState.isLoading || uiState.isEmpty) null else ({ ConfirmFactsBottomBar(actions) }),
        bottomBarNotice = if (uiState.isLoading || uiState.isEmpty || uiState.openCount == 0) {
            null
        } else {
            ({ OpenFactsDisclosure(uiState.openCount) })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = HhTheme.spacing.gutter),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.d12 - HhTheme.spacing.xxs),
        ) {
            when {
                uiState.isLoading -> ConfirmFactsLoading()
                uiState.isEmpty -> ConfirmFactsEmpty(actions)
                else -> ConfirmFactsContent(uiState = uiState, actions = actions)
            }
        }
    }
}

@Composable
private fun ConfirmFactsLoading() {
    val message = stringResource(R.string.feature_onboarding_impl_confirm_facts_loading)
    Column(
        modifier = Modifier.fillMaxWidth().padding(HhTheme.spacing.gutter),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        HhLoadingWheel(contentDesc = message)
        Text(text = message, style = HhTheme.typography.bodyM, color = HhTheme.colors.onSurfaceVariant)
    }
}

@Composable
private fun ConfirmFactsEmpty(actions: ConfirmFactsActions) {
    StateCard(
        illustration = HhIllustration.Empty,
        illustrationDescription = stringResource(R.string.feature_onboarding_impl_confirm_facts_spot_empty_description),
        title = stringResource(R.string.feature_onboarding_impl_confirm_facts_empty_title),
        body = stringResource(R.string.feature_onboarding_impl_confirm_facts_empty_body),
        extra = {
            HhOutlineButton(
                label = stringResource(R.string.feature_onboarding_impl_confirm_facts_empty_import),
                onClick = actions.onImportResume,
            )
        },
    )
}

@Composable
private fun ConfirmFactsContent(
    uiState: ConfirmFactsUiState,
    actions: ConfirmFactsActions,
) {
    HhOfflineBanner(
        message = stringResource(R.string.feature_onboarding_impl_confirm_facts_offline_message),
        visible = uiState.isOffline,
    )
    if (uiState.hasSaveFailed) {
        HhErrorCallout(title = stringResource(R.string.feature_onboarding_impl_confirm_facts_error_title))
    }
    if (uiState.isFullyConfirmed) {
        OnboardingNotice(
            text = pluralStringResource(
                R.plurals.feature_onboarding_impl_confirm_facts_all_confirmed,
                uiState.facts.size,
                uiState.facts.size,
            ),
            icon = HhIcons.CheckCircle,
            tone = NoticeTone.Success,
        )
    } else {
        OnboardingNotice(
            text = stringResource(R.string.feature_onboarding_impl_confirm_facts_removed_banner),
            icon = HhIcons.CheckCircle,
            tone = NoticeTone.Success,
        )
    }
    uiState.visibleSections.forEach { section ->
        SectionHeading(section.section)
        when {
            section.section == ConfirmFactsSection.Skills && section.skills.isNotEmpty() -> SkillsCard(section.skills)
            section.isEmpty -> EmptySectionCard(section = section.section, actions = actions)
            else -> section.facts.forEach { fact ->
                FactCard(fact = fact, category = section.section.categoryOf(), actions = actions)
            }
        }
    }
}

@Composable
private fun SectionHeading(section: ConfirmFactsSection) {
    Text(
        text = stringResource(sectionTitle(section)),
        modifier = Modifier.padding(top = HhTheme.spacing.xs),
        style = HhTheme.typography.titleS,
        color = HhTheme.colors.onSurfaceVariant,
    )
}

@Composable
private fun FactCard(
    fact: ConfirmFactUi,
    category: EntryCategory,
    actions: ConfirmFactsActions,
) {
    val state = stringResource(
        if (fact.isConfirmed) {
            R.string.feature_onboarding_impl_confirm_facts_state_confirmed
        } else {
            R.string.feature_onboarding_impl_confirm_facts_state_open
        },
    )
    val description = stringResource(
        R.string.feature_onboarding_impl_confirm_facts_card_description,
        fact.title,
        fact.detail,
        fact.displayId,
        state,
    )
    HhCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(HhTheme.spacing.cardPadding),
    ) {
        Column(
            modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = description },
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HhFactId(id = fact.displayId)
                if (fact.isConfirmed) {
                    HhProvenanceChip(
                        kind = HhProvenanceKind.Confirmed,
                        label = stringResource(R.string.feature_onboarding_impl_confirm_facts_status_confirmed),
                    )
                }
            }
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = HhTheme.colors.onSurface)) {
                        append(fact.title)
                    }
                    if (fact.detail.isNotBlank()) {
                        append(" · ")
                        append(fact.detail)
                    }
                },
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.body,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
            HhOutlineButton(
                label = stringResource(R.string.feature_onboarding_impl_confirm_facts_edit),
                onClick = { actions.onEdit(fact.id, category) },
                leadingIcon = HhIcons.Edit,
            )
            if (!fact.isConfirmed) {
                HhSecondaryButton(
                    label = stringResource(R.string.feature_onboarding_impl_confirm_facts_confirm),
                    onClick = { actions.onConfirm(fact.id) },
                    leadingIcon = HhIcons.Check,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SkillsCard(skills: List<String>) {
    HhCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(HhTheme.spacing.cardPadding),
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        ) {
            skills.forEach { skill -> SkillChip(skill) }
        }
    }
}

@Composable
private fun SkillChip(skill: String) {
    Text(
        text = skill,
        modifier = Modifier
            .clip(HhTheme.shapes.pill)
            .background(HhTheme.colors.neutralContainer)
            .padding(horizontal = HhTheme.spacing.md, vertical = HhTheme.spacing.xs + HhTheme.spacing.xxs),
        style = HhTheme.typography.labelM,
        color = HhTheme.colors.onNeutralContainer,
    )
}

@Composable
private fun EmptySectionCard(
    section: ConfirmFactsSection,
    actions: ConfirmFactsActions,
) {
    HhCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(HhTheme.spacing.cardPadding),
    ) {
        Text(
            text = stringResource(sectionEmpty(section)),
            style = HhTheme.typography.bodyM,
            color = HhTheme.colors.onSurface,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
            HhOutlineButton(
                label = stringResource(R.string.feature_onboarding_impl_confirm_facts_skip),
                onClick = { actions.onSkip(section) },
                modifier = Modifier.weight(1f),
            )
            HhOutlineButton(
                label = stringResource(R.string.feature_onboarding_impl_confirm_facts_add_one),
                onClick = { actions.onAddOne(section) },
                leadingIcon = HhIcons.Add,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ConfirmFactsBottomBar(actions: ConfirmFactsActions) {
    HhBottomActionBar {
        HhPrimaryButton(
            label = stringResource(R.string.feature_onboarding_impl_confirm_facts_continue),
            onClick = actions.onContinue,
            trailingIcon = HhIcons.ArrowForward,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun OpenFactsDisclosure(openCount: Int) {
    val unit = pluralStringResource(R.plurals.feature_onboarding_impl_confirm_facts_open_count_unit, openCount)
    val lead = stringResource(R.string.feature_onboarding_impl_confirm_facts_open_count, openCount, unit)
    val line = pluralStringResource(R.plurals.feature_onboarding_impl_confirm_facts_open_line, openCount, lead)
    DisclosureCard(
        text = buildAnnotatedString {
            append(line)
            val start = line.indexOf(lead)
            if (start >= 0) {
                addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, start + lead.length)
            }
        },
        icon = HhIcons.Flag,
    )
}

private fun sectionTitle(section: ConfirmFactsSection): Int = when (section) {
    ConfirmFactsSection.Education -> R.string.feature_onboarding_impl_confirm_facts_section_education
    ConfirmFactsSection.Experience -> R.string.feature_onboarding_impl_confirm_facts_section_experience
    ConfirmFactsSection.Projects -> R.string.feature_onboarding_impl_confirm_facts_section_projects
    ConfirmFactsSection.Skills -> R.string.feature_onboarding_impl_confirm_facts_section_skills
    ConfirmFactsSection.Certifications -> R.string.feature_onboarding_impl_confirm_facts_section_certifications
    ConfirmFactsSection.Extras -> R.string.feature_onboarding_impl_confirm_facts_section_extras
}

private fun sectionEmpty(section: ConfirmFactsSection): Int = when (section) {
    ConfirmFactsSection.Education -> R.string.feature_onboarding_impl_confirm_facts_empty_education
    ConfirmFactsSection.Experience -> R.string.feature_onboarding_impl_confirm_facts_empty_experience
    ConfirmFactsSection.Projects -> R.string.feature_onboarding_impl_confirm_facts_empty_projects
    ConfirmFactsSection.Skills -> R.string.feature_onboarding_impl_confirm_facts_empty_skills
    ConfirmFactsSection.Certifications -> R.string.feature_onboarding_impl_confirm_facts_empty_certifications
    ConfirmFactsSection.Extras -> R.string.feature_onboarding_impl_confirm_facts_empty_extras
}
