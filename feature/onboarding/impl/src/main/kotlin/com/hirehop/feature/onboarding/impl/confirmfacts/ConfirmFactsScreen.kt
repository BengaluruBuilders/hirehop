package com.hirehop.feature.onboarding.impl.confirmfacts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhConfirmDialog
import com.hirehop.core.designsystem.component.HhErrorCallout
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.designsystem.component.HhScaffold
import com.hirehop.core.designsystem.component.HhSpotIllustration
import com.hirehop.core.designsystem.component.HhSpotKind
import com.hirehop.core.designsystem.component.HhTopAppBar
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.FactSource
import com.hirehop.core.ui.FactIdTag
import com.hirehop.core.ui.FactProvenanceChip
import com.hirehop.feature.onboarding.impl.R

private val HH_TOUCH_TARGET: Dp = 48.dp
private val HH_ICON_SIZE: Dp = 18.dp

data class ConfirmFactsActions(
    val onBack: () -> Unit,
    val onConfirm: (String) -> Unit,
    val onEdit: (String?, EntryCategory) -> Unit,
    val onRequestDelete: (String) -> Unit,
    val onAddOne: (ConfirmFactsSection) -> Unit,
    val onSkip: (ConfirmFactsSection) -> Unit,
    val onContinue: () -> Unit,
    val onImportResume: () -> Unit,
    val onDismissRemovedNotice: () -> Unit,
)

@Composable
fun ConfirmFactsScreen(
    uiState: ConfirmFactsUiState,
    actions: ConfirmFactsActions,
    modifier: Modifier = Modifier,
) {
    HhScaffold(
        modifier = modifier,
        topBar = { ConfirmFactsTopBar(uiState = uiState, actions = actions) },
        bottomBar = { ConfirmFactsActionBar(uiState = uiState, actions = actions) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            ConfirmFactsBody(uiState = uiState, actions = actions)
        }
    }
}

@Composable
private fun ConfirmFactsTopBar(
    uiState: ConfirmFactsUiState,
    actions: ConfirmFactsActions,
) {
    HhTopAppBar(
        title = stringResource(
            R.string.feature_onboarding_impl_confirm_facts_app_bar,
            pluralStringResource(
                R.plurals.feature_onboarding_impl_confirm_facts_counter,
                uiState.confirmedCount,
                uiState.confirmedCount,
                uiState.facts.size,
            ),
        ),
        navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
        navigationIconContentDescription = stringResource(
            R.string.feature_onboarding_impl_confirm_facts_back_description,
        ),
        onNavigationClick = actions.onBack,
    )
}

@Composable
private fun ConfirmFactsBody(
    uiState: ConfirmFactsUiState,
    actions: ConfirmFactsActions,
) {
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            HhSpotIllustration(
                kind = HhSpotKind.Empty,
                contentDescription = stringResource(
                    R.string.feature_onboarding_impl_confirm_facts_spot_empty_description,
                ),
            )
        }
        return
    }
    if (uiState.isEmpty) {
        ConfirmFactsEmptyState(actions = actions)
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HhTheme.spacing.lg, vertical = HhTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.lg),
    ) {
        HhOfflineBanner(
            message = stringResource(R.string.feature_onboarding_impl_confirm_facts_offline_message),
            visible = uiState.isOffline,
        )
        if (uiState.hasSaveFailed) {
            HhErrorCallout(title = stringResource(R.string.feature_onboarding_impl_confirm_facts_error_title))
        }
        if (uiState.showRemovedNotice) {
            ConfirmRemovedNotice(onDismiss = actions.onDismissRemovedNotice)
        }
        if (!uiState.contact.isEmpty) {
            ConfirmContactSummary(contact = uiState.contact)
        }
        ConfirmSectionStrip(uiState = uiState)
        uiState.visibleSections.forEach { section ->
            ConfirmSectionBlock(section = section, actions = actions)
        }
    }
}

@Composable
private fun ConfirmRemovedNotice(onDismiss: () -> Unit) {
    val label = stringResource(R.string.feature_onboarding_impl_confirm_facts_removed_banner)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = HhTheme.colors.spotContainer,
                shape = RoundedCornerShape(HhTheme.shapes.sm),
            )
            .padding(start = HhTheme.spacing.md, top = HhTheme.spacing.md, end = HhTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = Icons.Rounded.Info,
            contentDescription = null,
            tint = HhTheme.colors.onSpotContainer,
            modifier = Modifier.sizeIn(maxWidth = HH_ICON_SIZE, maxHeight = HH_ICON_SIZE),
        )
        Text(
            text = label,
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSpotContainer,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_confirm_facts_removed_banner_ok),
            style = HhTheme.typography.labelLarge,
            color = HhTheme.colors.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .heightIn(min = HH_TOUCH_TARGET)
                .clickable(onClick = onDismiss),
        )
    }
}

@Composable
private fun ConfirmContactSummary(contact: ContactUi) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = HhTheme.colors.surface,
                shape = RoundedCornerShape(HhTheme.shapes.md),
            )
            .border(
                width = HhTheme.spacing.d2,
                color = HhTheme.colors.hairline,
                shape = RoundedCornerShape(HhTheme.shapes.md),
            )
            .padding(HhTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs),
    ) {
        Text(
            text = stringResource(R.string.feature_onboarding_impl_confirm_facts_contact_summary),
            style = HhTheme.typography.monoSmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
        val nameLabel = stringResource(R.string.feature_onboarding_impl_confirm_facts_contact_name)
        val emailLabel = stringResource(R.string.feature_onboarding_impl_confirm_facts_contact_email)
        val phoneLabel = stringResource(R.string.feature_onboarding_impl_confirm_facts_contact_phone)
        listOf(
            nameLabel to contact.fullName,
            emailLabel to contact.email,
            phoneLabel to contact.phone,
        ).forEach { (label, value) ->
            if (value.isNotBlank()) {
                Text(
                    text = value,
                    style = HhTheme.typography.bodyMedium,
                    color = HhTheme.colors.onSurface,
                    modifier = Modifier.clearAndSetSemantics { contentDescription = "$label: $value" },
                )
            }
        }
        FactProvenanceChip(source = FactSource.IMPORTED, isConfirmed = false)
    }
}

@Composable
private fun ConfirmSectionStrip(uiState: ConfirmFactsUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
    ) {
        uiState.visibleSections.forEach { section ->
            val label = stringResource(section.section.sectionRes())
            val description = stringResource(
                R.string.feature_onboarding_impl_confirm_facts_strip_description,
                label,
                section.count,
            )
            Text(
                text = stringResource(
                    R.string.feature_onboarding_impl_confirm_facts_section_header,
                    label,
                    section.count,
                ),
                style = HhTheme.typography.monoSmall,
                color = HhTheme.colors.onSurface,
                modifier = Modifier
                    .background(
                        color = HhTheme.colors.surfaceContainerHigh,
                        shape = RoundedCornerShape(HhTheme.shapes.xs),
                    )
                    .padding(horizontal = HhTheme.spacing.sm, vertical = HhTheme.spacing.xs)
                    .clearAndSetSemantics { contentDescription = description },
            )
        }
    }
}

@Composable
private fun ConfirmSectionBlock(
    section: ConfirmFactsSectionUi,
    actions: ConfirmFactsActions,
) {
    val label = stringResource(section.section.sectionRes())
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        Text(
            text = stringResource(
                R.string.feature_onboarding_impl_confirm_facts_section_header,
                label,
                section.count,
            ),
            style = HhTheme.typography.monoSmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
        if (section.isEmpty) {
            ConfirmEmptySection(section = section.section, actions = actions)
        }
        if (section.section == ConfirmFactsSection.Skills) {
            ConfirmSkillChips(skills = section.skills)
        }
        section.facts.forEach { fact ->
            ConfirmFactCard(fact = fact, actions = actions)
        }
    }
}

@Composable
private fun ConfirmEmptySection(
    section: ConfirmFactsSection,
    actions: ConfirmFactsActions,
) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
        Text(
            text = stringResource(section.emptyRes()),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = HhTheme.spacing.d2,
                    color = HhTheme.colors.hairlineStrong,
                    shape = RoundedCornerShape(HhTheme.shapes.md),
                )
                .padding(HhTheme.spacing.md),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
            HhOutlinedButton(
                onClick = { actions.onAddOne(section) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = HH_TOUCH_TARGET),
            ) {
                Text(text = stringResource(R.string.feature_onboarding_impl_confirm_facts_add_one))
            }
            HhOutlinedButton(
                onClick = { actions.onSkip(section) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = HH_TOUCH_TARGET),
            ) {
                Text(text = stringResource(R.string.feature_onboarding_impl_confirm_facts_skip))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConfirmSkillChips(skills: List<String>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
    ) {
        skills.forEach { skill ->
            Text(
                text = skill,
                style = HhTheme.typography.labelMedium,
                color = HhTheme.colors.onSurface,
                modifier = Modifier
                    .background(
                        color = HhTheme.colors.surfaceContainerHigh,
                        shape = RoundedCornerShape(HhTheme.shapes.xs),
                    )
                    .padding(horizontal = HhTheme.spacing.sm, vertical = HhTheme.spacing.xxs),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConfirmFactCard(
    fact: ConfirmFactUi,
    actions: ConfirmFactsActions,
) {
    val sectionLabel = stringResource(fact.section.sectionRes())
    val status = stringResource(
        if (fact.isConfirmed) {
            R.string.feature_onboarding_impl_confirm_facts_status_confirmed
        } else {
            R.string.feature_onboarding_impl_confirm_facts_status_open
        },
    )
    val availableActions = listOfNotNull(
        if (fact.isConfirmed) null else stringResource(R.string.feature_onboarding_impl_confirm_facts_confirm),
        stringResource(R.string.feature_onboarding_impl_confirm_facts_edit),
        stringResource(R.string.feature_onboarding_impl_confirm_facts_delete),
    ).joinToString(separator = ", ")
    val description = stringResource(
        R.string.feature_onboarding_impl_confirm_facts_card_description,
        sectionLabel,
        fact.title,
        status,
        fact.id,
        availableActions,
    )
    val shape = RoundedCornerShape(HhTheme.shapes.md)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = HhTheme.colors.surface, shape = shape)
            .border(width = HhTheme.spacing.d2, color = HhTheme.colors.hairline, shape = shape)
            .padding(HhTheme.spacing.md)
            .clearAndSetSemantics { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FactIdTag(factId = fact.id)
            ConfirmStatusLabel(isConfirmed = fact.isConfirmed)
            FactProvenanceChip(source = fact.source)
        }
        Text(
            text = fact.title,
            style = HhTheme.typography.titleMedium,
            color = HhTheme.colors.onSurface,
        )
        if (fact.detail.isNotBlank()) {
            Text(
                text = fact.detail,
                style = HhTheme.typography.bodySmall,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        ) {
            ConfirmTextAction(
                label = stringResource(R.string.feature_onboarding_impl_confirm_facts_edit),
                color = HhTheme.colors.primary,
                onClick = { actions.onEdit(fact.id, fact.section.categoryOf()) },
            )
            if (!fact.isConfirmed) {
                HhOutlinedButton(
                    onClick = { actions.onConfirm(fact.id) },
                    modifier = Modifier.heightIn(min = HH_TOUCH_TARGET),
                ) {
                    Text(
                        text = stringResource(R.string.feature_onboarding_impl_confirm_facts_confirm),
                        color = HhTheme.colors.onSurface,
                    )
                }
            }
            ConfirmTextAction(
                label = stringResource(R.string.feature_onboarding_impl_confirm_facts_delete),
                color = HhTheme.colors.error,
                onClick = { actions.onRequestDelete(fact.id) },
            )
        }
    }
}

@Composable
private fun ConfirmTextAction(
    label: String,
    color: Color,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        style = HhTheme.typography.labelLarge,
        color = color,
        modifier = Modifier
            .heightIn(min = HH_TOUCH_TARGET)
            .clickable(onClick = onClick)
            .padding(horizontal = HhTheme.spacing.sm, vertical = HhTheme.spacing.sm)
            .clearAndSetSemantics { contentDescription = label },
    )
}

@Composable
private fun ConfirmStatusLabel(isConfirmed: Boolean) {
    val color = if (isConfirmed) HhTheme.colors.success else HhTheme.colors.onSurfaceVariant
    Text(
        text = stringResource(
            if (isConfirmed) {
                R.string.feature_onboarding_impl_confirm_facts_confirmed_chip
            } else {
                R.string.feature_onboarding_impl_confirm_facts_open_chip
            },
        ),
        style = HhTheme.typography.monoSmall,
        color = color,
        modifier = Modifier
            .border(
                width = HhTheme.spacing.d2,
                color = color,
                shape = RoundedCornerShape(HhTheme.shapes.xs),
            )
            .padding(horizontal = HhTheme.spacing.xs, vertical = HhTheme.spacing.xxs),
    )
}

@Composable
private fun ConfirmFactsActionBar(
    uiState: ConfirmFactsUiState,
    actions: ConfirmFactsActions,
) {
    HhBottomActionBar(
        creditDisclosure = {
            if (uiState.openCount > 0) {
                Text(
                    text = pluralStringResource(
                        R.plurals.feature_onboarding_impl_confirm_facts_open_line,
                        uiState.openCount,
                        uiState.openCount,
                    ),
                    style = HhTheme.typography.labelMedium,
                    color = HhTheme.colors.onSurfaceVariant,
                )
            }
        },
    ) {
        HhButton(
            onClick = actions.onContinue,
            enabled = !uiState.isSaving,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = HH_TOUCH_TARGET),
        ) {
            Text(text = stringResource(R.string.feature_onboarding_impl_confirm_facts_continue))
        }
    }
}

@Composable
private fun ConfirmFactsEmptyState(actions: ConfirmFactsActions) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HhTheme.spacing.d20, vertical = HhTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.lg),
    ) {
        HhSpotIllustration(
            kind = HhSpotKind.Empty,
            contentDescription = stringResource(
                R.string.feature_onboarding_impl_confirm_facts_spot_empty_description,
            ),
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_confirm_facts_empty_title),
            style = HhTheme.typography.headlineSmall,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_confirm_facts_empty_body),
            style = HhTheme.typography.bodyLarge,
            color = HhTheme.colors.onSurfaceVariant,
        )
        HhOutlinedButton(
            onClick = actions.onImportResume,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HH_TOUCH_TARGET),
        ) {
            Text(
                text = stringResource(R.string.feature_onboarding_impl_confirm_facts_empty_import),
                color = HhTheme.colors.onSurface,
            )
        }
    }
}

@Composable
fun ConfirmFactsDeleteDialog(
    factId: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    HhConfirmDialog(
        title = stringResource(
            R.string.feature_onboarding_impl_confirm_facts_delete_dialog_title,
            factId,
        ),
        message = stringResource(R.string.feature_onboarding_impl_confirm_facts_delete_dialog_message),
        confirmLabel = stringResource(R.string.feature_onboarding_impl_confirm_facts_delete),
        cancelLabel = stringResource(R.string.feature_onboarding_impl_confirm_facts_skip),
        destructive = true,
        onConfirm = onConfirm,
        onCancel = onDismiss,
    )
}

internal fun ConfirmFactsSection.sectionRes(): Int = when (this) {
    ConfirmFactsSection.Education -> R.string.feature_onboarding_impl_confirm_facts_section_education
    ConfirmFactsSection.Experience -> R.string.feature_onboarding_impl_confirm_facts_section_experience
    ConfirmFactsSection.Projects -> R.string.feature_onboarding_impl_confirm_facts_section_projects
    ConfirmFactsSection.Skills -> R.string.feature_onboarding_impl_confirm_facts_section_skills
    ConfirmFactsSection.Certifications -> R.string.feature_onboarding_impl_confirm_facts_section_certifications
    ConfirmFactsSection.Extras -> R.string.feature_onboarding_impl_confirm_facts_section_extras
}

internal fun ConfirmFactsSection.emptyRes(): Int = when (this) {
    ConfirmFactsSection.Education -> R.string.feature_onboarding_impl_confirm_facts_empty_education
    ConfirmFactsSection.Experience -> R.string.feature_onboarding_impl_confirm_facts_empty_experience
    ConfirmFactsSection.Projects -> R.string.feature_onboarding_impl_confirm_facts_empty_projects
    ConfirmFactsSection.Skills -> R.string.feature_onboarding_impl_confirm_facts_empty_skills
    ConfirmFactsSection.Certifications -> R.string.feature_onboarding_impl_confirm_facts_empty_certifications
    ConfirmFactsSection.Extras -> R.string.feature_onboarding_impl_confirm_facts_empty_extras
}
