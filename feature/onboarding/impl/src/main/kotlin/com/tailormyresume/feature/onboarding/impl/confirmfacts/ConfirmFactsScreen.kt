package com.tailormyresume.feature.onboarding.impl.confirmfacts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.component.TmrButtonSize
import com.tailormyresume.core.designsystem.component.TmrCard
import com.tailormyresume.core.designsystem.component.TmrErrorCallout
import com.tailormyresume.core.designsystem.component.TmrExpandable
import com.tailormyresume.core.designsystem.component.TmrFactId
import com.tailormyresume.core.designsystem.component.TmrIconActionBar
import com.tailormyresume.core.designsystem.component.TmrLoadingWheel
import com.tailormyresume.core.designsystem.component.TmrOfflineBanner
import com.tailormyresume.core.designsystem.component.TmrOutlineButton
import com.tailormyresume.core.designsystem.component.TmrPillRow
import com.tailormyresume.core.designsystem.component.TmrPillRowStyle
import com.tailormyresume.core.designsystem.component.TmrPrimaryButton
import com.tailormyresume.core.designsystem.component.TmrScreen
import com.tailormyresume.core.designsystem.component.TmrStatusChip
import com.tailormyresume.core.designsystem.component.TmrStatusKind
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.feature.onboarding.impl.R
import com.tailormyresume.feature.onboarding.impl.common.DisclosureCard
import com.tailormyresume.feature.onboarding.impl.common.NoticeTone
import com.tailormyresume.feature.onboarding.impl.common.OnboardingNotice
import com.tailormyresume.feature.onboarding.impl.common.OnboardingStepBar
import com.tailormyresume.feature.onboarding.impl.common.ReasonText
import com.tailormyresume.feature.onboarding.impl.common.StateCard

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
    TmrScreen(
        modifier = modifier,
        sheet = false,
        bottomBar = if (uiState.isLoading || uiState.isEmpty) {
            null
        } else {
            ({ ConfirmFactsBottomBar(uiState = uiState, actions = actions) })
        },
        bottomBarNotice = if (uiState.isLoading || uiState.isEmpty || uiState.isFullyConfirmed) {
            null
        } else {
            ({ ConfirmFactsNotice(uiState) })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = TmrTheme.spacing.gutter),
            verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.d12 + TmrTheme.spacing.xxs),
        ) {
            OnboardingStepBar(
                onBack = actions.onBack,
                backContentDescription = stringResource(R.string.feature_onboarding_impl_confirm_facts_back_description),
                title = stringResource(R.string.feature_onboarding_impl_confirm_facts_title),
            )
            when {
                uiState.isLoading -> ConfirmFactsLoading()
                uiState.isEmpty -> ConfirmFactsEmpty(actions)
                uiState.isFullyConfirmed -> ConfirmedFactsBody(uiState = uiState, actions = actions)
                else -> FactsToReviewBody(uiState = uiState, actions = actions)
            }
        }
    }
}

@Composable
private fun FactsToReviewBody(
    uiState: ConfirmFactsUiState,
    actions: ConfirmFactsActions,
) {
    FactsProgress(uiState = uiState)
    ConfirmFactsStatus(uiState)
    FlaggedConfirmedFacts(uiState = uiState, actions = actions)
    if (uiState.facts.any(ConfirmFactUi::isConfirmedWithinLimits)) {
        ConfirmedFactsGroup(uiState = uiState, actions = actions)
    }
    val pending = uiState.facts.filterNot(ConfirmFactUi::isConfirmed)
    if (pending.isNotEmpty()) {
        ToReviewHeading(openCount = uiState.openCount)
        pending.forEach { fact ->
            FactCard(
                fact = fact,
                category = fact.section.categoryOf(),
                page = pageOf(uiState = uiState, fact = fact),
                actions = actions,
            )
        }
    }
    uiState.visibleSections.forEach { section ->
        if (section.section == ConfirmFactsSection.Skills && section.skills.isNotEmpty()) {
            SectionHeading(section.section)
            SkillsCard(section.skills)
        } else if (section.isEmpty) {
            SectionHeading(section.section)
            EmptySectionCard(section = section.section, actions = actions)
        }
    }
}

@Composable
private fun FactsProgress(uiState: ConfirmFactsUiState) {
    val counter = pluralStringResource(
        R.plurals.feature_onboarding_impl_confirm_facts_counter,
        uiState.confirmedCount,
        uiState.confirmedCount,
        uiState.totalCount,
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
    ) {
        Text(
            text = counter,
            style = TmrTheme.typography.headlineM,
            color = TmrTheme.colors.onSurface,
        )
        ProgressBar(confirmed = uiState.confirmedCount, total = uiState.totalCount)
    }
}

@Composable
internal fun ProgressBar(
    confirmed: Int,
    total: Int,
) {
    val fraction = if (total == 0) 0f else confirmed.toFloat() / total
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(PROGRESS_HEIGHT)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(fraction.coerceIn(0f, 1f), 0f..1f) }
            .clip(TmrTheme.shapes.pill)
            .background(TmrTheme.colors.primaryContainer),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction)
                .clip(TmrTheme.shapes.pill)
                .background(TmrTheme.colors.brand),
        )
    }
}

@Composable
private fun ConfirmFactsStatus(uiState: ConfirmFactsUiState) {
    TmrOfflineBanner(
        message = stringResource(R.string.feature_onboarding_impl_confirm_facts_offline_message),
        visible = uiState.isOffline,
    )
    if (uiState.hasSaveFailed) {
        TmrErrorCallout(title = stringResource(R.string.feature_onboarding_impl_confirm_facts_error_title))
    }
    if (uiState.showsRemovedBanner) {
        OnboardingNotice(
            text = stringResource(R.string.feature_onboarding_impl_confirm_facts_removed_banner),
            icon = TmrIcons.Info,
        )
    }
}

@Composable
private fun FlaggedConfirmedFacts(
    uiState: ConfirmFactsUiState,
    actions: ConfirmFactsActions,
) {
    uiState.facts.filter { it.isConfirmed && it.hasTooLongBullet }.forEach { fact ->
        FactCard(
            fact = fact,
            category = fact.section.categoryOf(),
            page = pageOf(uiState = uiState, fact = fact),
            actions = actions,
        )
    }
}

@Composable
private fun ConfirmedFactsGroup(
    uiState: ConfirmFactsUiState,
    actions: ConfirmFactsActions,
) {
    val confirmed = uiState.facts.filter(ConfirmFactUi::isConfirmedWithinLimits)
    var expanded by remember { mutableStateOf(false) }
    TmrPillRow(
        title = pluralStringResource(
            R.plurals.feature_onboarding_impl_confirm_facts_confirmed_group_title,
            confirmed.size,
            confirmed.size,
        ),
        onClick = { expanded = !expanded },
        style = TmrPillRowStyle.Jade,
        subtitle = confirmed.take(CONFIRMED_IDS_SHOWN).joinToString(" ") { it.displayId },
        icon = TmrIcons.CheckCircle,
        trailingIcon = if (expanded) TmrIcons.ExpandLess else TmrIcons.ExpandMore,
    )
    TmrExpandable(expanded = expanded) {
        Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm)) {
            confirmed.forEach { fact ->
                FactCard(
                    fact = fact,
                    category = fact.section.categoryOf(),
                    page = pageOf(uiState = uiState, fact = fact),
                    actions = actions,
                )
            }
        }
    }
}

@Composable
private fun ToReviewHeading(openCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.feature_onboarding_impl_confirm_facts_to_review),
            modifier = Modifier.weight(1f),
            style = TmrTheme.typography.titleM.copy(fontWeight = FontWeight.ExtraBold),
            color = TmrTheme.colors.onSurface,
        )
        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = BADGE_SIZE, minHeight = BADGE_SIZE)
                .clip(TmrTheme.shapes.pill)
                .background(TmrTheme.colors.special)
                .padding(horizontal = TmrTheme.spacing.sm),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = openCount.toString(),
                style = TmrTheme.typography.labelM.copy(fontWeight = FontWeight.ExtraBold),
                color = TmrTheme.colors.onSpecial,
            )
        }
    }
}

@Composable
private fun ConfirmedFactsBody(
    uiState: ConfirmFactsUiState,
    actions: ConfirmFactsActions,
) {
    FactsProgress(uiState = uiState)
    OnboardingNotice(
        text = pluralStringResource(
            if (uiState.skills.isEmpty()) {
                R.plurals.feature_onboarding_impl_confirm_facts_all_confirmed
            } else {
                R.plurals.feature_onboarding_impl_confirm_facts_all_confirmed_with_skills
            },
            uiState.totalCount,
            uiState.totalCount,
        ),
        icon = TmrIcons.CheckCircle,
        tone = NoticeTone.Success,
    )
    FlaggedConfirmedFacts(uiState = uiState, actions = actions)
    ReviewTheList(uiState = uiState, actions = actions)
}

@Composable
private fun ReviewTheList(
    uiState: ConfirmFactsUiState,
    actions: ConfirmFactsActions,
) {
    var expanded by remember { mutableStateOf(false) }
    val filled = uiState.visibleSections.filter { !it.isEmpty }.map { stringResource(sectionTitle(it.section)) }
    TmrPillRow(
        title = stringResource(R.string.feature_onboarding_impl_confirm_facts_review_list),
        onClick = { expanded = !expanded },
        style = TmrPillRowStyle.Neutral,
        subtitle = filled.joinToString(LIST_SEPARATOR),
        icon = TmrIcons.Description,
        trailingIcon = if (expanded) TmrIcons.ExpandLess else TmrIcons.ExpandMore,
    )
    TmrExpandable(expanded = expanded) {
        Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm)) {
            uiState.facts.filterNot { it.isConfirmed && it.hasTooLongBullet }.forEach { fact ->
                FactCard(
                    fact = fact,
                    category = fact.section.categoryOf(),
                    page = pageOf(uiState = uiState, fact = fact),
                    actions = actions,
                )
            }
            if (uiState.skills.isNotEmpty()) {
                SkillsCard(uiState.skills)
            }
        }
    }
}

@Composable
private fun SectionHeading(section: ConfirmFactsSection) {
    Text(
        text = stringResource(sectionTitle(section)),
        modifier = Modifier.padding(top = TmrTheme.spacing.xs),
        style = TmrTheme.typography.titleS,
        color = TmrTheme.colors.onSurfaceVariant,
    )
}

@Composable
private fun FactCard(
    fact: ConfirmFactUi,
    category: EntryCategory,
    page: Int,
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
    TmrCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(TmrTheme.spacing.cardPadding),
    ) {
        Column(
            modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = description },
            verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
        ) {
            FactHeader(fact = fact, page = page)
            Text(
                text = factLine(fact),
                style = TmrTheme.typography.bodyL.copy(fontWeight = FontWeight.SemiBold),
                color = TmrTheme.colors.onSurface,
            )
        }
        FactActions(fact = fact, category = category, actions = actions)
    }
}

@Composable
private fun FactHeader(
    fact: ConfirmFactUi,
    page: Int,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TmrFactId(id = fact.displayId)
        Text(
            text = stringResource(
                R.string.feature_onboarding_impl_confirm_facts_source_page,
                stringResource(sectionTitle(fact.section)),
                page,
            ),
            modifier = Modifier.weight(1f),
            style = TmrTheme.typography.labelM,
            color = TmrTheme.colors.onSurfaceVariant,
        )
        TmrStatusChip(
            kind = if (fact.isConfirmed && !fact.hasTooLongBullet) TmrStatusKind.Met else TmrStatusKind.Gap,
            label = stringResource(
                when {
                    fact.hasTooLongBullet -> R.string.feature_onboarding_impl_confirm_facts_too_long_status
                    fact.isConfirmed -> R.string.feature_onboarding_impl_confirm_facts_status_confirmed
                    else -> R.string.feature_onboarding_impl_confirm_facts_pending
                },
            ),
        )
    }
}

@Composable
private fun FactActions(
    fact: ConfirmFactUi,
    category: EntryCategory,
    actions: ConfirmFactsActions,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm)) {
        if (fact.hasTooLongBullet) {
            Text(
                text = stringResource(R.string.feature_onboarding_impl_confirm_facts_too_long_note),
                modifier = Modifier.weight(1f),
                style = TmrTheme.typography.labelM,
                color = TmrTheme.colors.onSurfaceVariant,
            )
        } else if (!fact.isConfirmed) {
            Box(
                modifier = Modifier.weight(1f).heightIn(min = TmrTheme.spacing.touch),
                contentAlignment = Alignment.Center,
            ) {
                TmrPrimaryButton(
                    label = stringResource(R.string.feature_onboarding_impl_confirm_facts_confirm),
                    onClick = { actions.onConfirm(fact.id) },
                    modifier = Modifier.fillMaxWidth(),
                    size = TmrButtonSize.Compact,
                )
            }
        }
        Box(
            modifier = Modifier.heightIn(min = TmrTheme.spacing.touch),
            contentAlignment = Alignment.Center,
        ) {
            TmrOutlineButton(
                label = stringResource(R.string.feature_onboarding_impl_confirm_facts_edit),
                onClick = { actions.onEdit(fact.id, category) },
                size = TmrButtonSize.Compact,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SkillsCard(skills: List<String>) {
    TmrCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(TmrTheme.spacing.cardPadding),
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
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
            .clip(TmrTheme.shapes.pill)
            .background(TmrTheme.colors.neutralContainer)
            .padding(horizontal = TmrTheme.spacing.md, vertical = TmrTheme.spacing.sm),
        style = TmrTheme.typography.labelM,
        color = TmrTheme.colors.onNeutralContainer,
    )
}

@Composable
private fun EmptySectionCard(
    section: ConfirmFactsSection,
    actions: ConfirmFactsActions,
) {
    TmrCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(TmrTheme.spacing.cardPadding),
    ) {
        Text(
            text = stringResource(sectionEmpty(section)),
            style = TmrTheme.typography.bodyM,
            color = TmrTheme.colors.onSurface,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm)) {
            TmrOutlineButton(
                label = stringResource(R.string.feature_onboarding_impl_confirm_facts_skip),
                onClick = { actions.onSkip(section) },
                modifier = Modifier.weight(1f),
            )
            TmrOutlineButton(
                label = stringResource(R.string.feature_onboarding_impl_confirm_facts_add_one),
                onClick = { actions.onAddOne(section) },
                leadingIcon = TmrIcons.Add,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ConfirmFactsLoading() {
    val message = stringResource(R.string.feature_onboarding_impl_confirm_facts_loading)
    Column(
        modifier = Modifier.fillMaxWidth().padding(TmrTheme.spacing.gutter),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
    ) {
        TmrLoadingWheel(contentDesc = message)
        Text(text = message, style = TmrTheme.typography.bodyM, color = TmrTheme.colors.onSurfaceVariant)
    }
}

@Composable
private fun ConfirmFactsEmpty(actions: ConfirmFactsActions) {
    StateCard(
        icon = TmrIcons.Description,
        title = stringResource(R.string.feature_onboarding_impl_confirm_facts_empty_title),
        body = stringResource(R.string.feature_onboarding_impl_confirm_facts_empty_body),
        extra = {
            TmrOutlineButton(
                label = stringResource(R.string.feature_onboarding_impl_confirm_facts_empty_import),
                onClick = actions.onImportResume,
            )
        },
    )
}

@Composable
private fun ConfirmFactsBottomBar(
    uiState: ConfirmFactsUiState,
    actions: ConfirmFactsActions,
) {
    val fullyConfirmed = uiState.isFullyConfirmed
    val label = when {
        fullyConfirmed -> stringResource(R.string.feature_onboarding_impl_confirm_facts_check_fit)
        uiState.canContinue -> stringResource(R.string.feature_onboarding_impl_confirm_facts_continue)
        else -> stringResource(R.string.feature_onboarding_impl_confirm_facts_left_to_review, uiState.openCount)
    }
    TmrIconActionBar(
        secondaryIcon = TmrIcons.Add,
        secondaryContentDescription = stringResource(R.string.feature_onboarding_impl_confirm_facts_add_description),
        onSecondaryClick = { actions.onAddOne(ConfirmFactsSection.Experience) },
        primaryLabel = label,
        onPrimaryClick = actions.onContinue,
        primaryEnabled = uiState.canContinue,
        primaryTrailingIcon = TmrIcons.ArrowForward.takeIf { uiState.canContinue },
    )
}

@Composable
private fun ConfirmFactsNotice(uiState: ConfirmFactsUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm)) {
        if (uiState.openCount > 0) {
            OpenFactsDisclosure(uiState.openCount)
        }
        if (!uiState.canContinue) {
            ReasonText(text = stringResource(R.string.feature_onboarding_impl_confirm_facts_reason_needs_one))
        }
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
        icon = TmrIcons.Flag,
    )
}

private fun pageOf(
    uiState: ConfirmFactsUiState,
    fact: ConfirmFactUi,
): Int = uiState.facts.indexOf(fact) / FACTS_PER_PAGE + 1

private fun factLine(fact: ConfirmFactUi): String =
    if (fact.detail.isBlank()) fact.title else fact.title + DETAIL_SEPARATOR + fact.detail

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

private const val FACTS_PER_PAGE = 4
private const val CONFIRMED_IDS_SHOWN = 6
private val PROGRESS_HEIGHT = 8.dp
private val BADGE_SIZE = 26.dp
private const val DETAIL_SEPARATOR = " · "
private const val LIST_SEPARATOR = " · "
