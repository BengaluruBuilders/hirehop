package com.hirehop.feature.onboarding.impl.welcome

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhDivider
import com.hirehop.core.designsystem.component.HhErrorCallout
import com.hirehop.core.designsystem.component.HhFactId
import com.hirehop.core.designsystem.component.HhHeadline
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhSectionLabel
import com.hirehop.core.designsystem.component.HhStatusChip
import com.hirehop.core.designsystem.component.HhStatusKind
import com.hirehop.core.designsystem.component.HhTextButton
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.CareerStage
import com.hirehop.feature.onboarding.impl.R

private val TOP_ROW_HEIGHT = 48.dp
private val LOGO_MARK = 32.dp
private val CHOICE_HEIGHT = 56.dp
private val CHOICE_DISC = 22.dp
private val CHOICE_CHECK = 13.dp
private val CHOICE_BORDER = 1.5.dp

@Composable
internal fun WelcomeScreen(
    uiState: WelcomeUiState,
    actions: WelcomeActions,
    modifier: Modifier = Modifier,
) {
    HhScreen(
        modifier = modifier,
        bottomBar = { WelcomeBottomBar(uiState = uiState, actions = actions) },
    ) { padding ->
        WelcomeContent(
            uiState = uiState,
            actions = actions,
            modifier = Modifier.fillMaxSize().padding(padding),
        )
    }
}

@Composable
private fun WelcomeContent(
    uiState: WelcomeUiState,
    actions: WelcomeActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HhTheme.spacing.gutter)
            .padding(bottom = HhTheme.spacing.gutter),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.lg),
    ) {
        WelcomeNotices(uiState = uiState, actions = actions)
        WelcomeTopRow(actions = actions)
        WelcomeHeadline()
        WelcomeSample()
        if (uiState.isLoading) {
            WelcomeLoading()
        } else {
            WelcomeCareerChoice(selected = uiState.careerStage, onSelect = actions.onSelectCareerStage)
        }
    }
}

@Composable
private fun WelcomeNotices(
    uiState: WelcomeUiState,
    actions: WelcomeActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        HhOfflineBanner(
            message = stringResource(R.string.feature_onboarding_impl_welcome_offline_message),
            supportingText = stringResource(R.string.feature_onboarding_impl_welcome_offline_support),
            visible = uiState.isOffline,
        )
        if (uiState.message == WelcomeMessage.LOAD_FAILED) {
            HhErrorCallout(
                title = stringResource(R.string.feature_onboarding_impl_welcome_load_failed_title),
                supportingText = stringResource(R.string.feature_onboarding_impl_welcome_load_failed_body),
                actionLabel = stringResource(R.string.feature_onboarding_impl_welcome_try_again),
                onAction = actions.onRetry,
            )
        }
    }
}

@Composable
private fun WelcomeTopRow(
    actions: WelcomeActions,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = TOP_ROW_HEIGHT),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        ) {
            WelcomeLogoMark()
            Text(
                text = stringResource(R.string.feature_onboarding_impl_welcome_brand),
                style = HhTheme.typography.titleL,
                color = HhTheme.colors.onSurface,
            )
        }
        HhTextButton(
            label = stringResource(R.string.feature_onboarding_impl_welcome_have_account),
            onClick = actions.onHaveAccount,
        )
    }
}

@Composable
private fun WelcomeLogoMark(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(LOGO_MARK).background(HhTheme.colors.brand, HhTheme.shapes.pill),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = HhIcons.ArrowForward,
            contentDescription = null,
            tint = HhTheme.colors.onBrand,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun WelcomeHeadline(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        HhHeadline(
            text = stringResource(R.string.feature_onboarding_impl_welcome_headline),
            style = HhTheme.typography.displayM,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_welcome_subline),
            style = HhTheme.typography.titleM,
            color = HhTheme.colors.onSurface,
        )
    }
}

@Composable
private fun WelcomeSample(modifier: Modifier = Modifier) {
    HhCard(modifier = modifier.fillMaxWidth()) {
        SampleLabel(R.string.feature_onboarding_impl_welcome_sample_original)
        Text(
            text = stringResource(R.string.feature_onboarding_impl_welcome_sample_original_text),
            style = HhTheme.typography.bodyL,
            color = HhTheme.colors.onSurfaceVariant,
        )
        SampleLabel(R.string.feature_onboarding_impl_welcome_sample_rewritten)
        Text(
            text = stringResource(R.string.feature_onboarding_impl_welcome_sample_rewritten_text),
            style = HhTheme.typography.titleM,
            color = HhTheme.colors.onSurface,
        )
        HhDivider()
        Row(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HhFactId(id = stringResource(R.string.feature_onboarding_impl_welcome_sample_fact_id))
            HhStatusChip(
                kind = HhStatusKind.Met,
                label = stringResource(R.string.feature_onboarding_impl_welcome_sample_confirmed),
            )
        }
        Text(
            text = stringResource(R.string.feature_onboarding_impl_welcome_sample_source),
            style = HhTheme.typography.bodyS,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun SampleLabel(@StringRes label: Int) {
    HhSectionLabel(text = stringResource(label))
}

@Composable
private fun WelcomeCareerChoice(
    selected: CareerStage?,
    onSelect: (CareerStage) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        Text(
            text = stringResource(R.string.feature_onboarding_impl_welcome_career_question),
            style = HhTheme.typography.titleM,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_welcome_career_hint),
            style = HhTheme.typography.bodyS,
            color = HhTheme.colors.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth().selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        ) {
            CareerChoicePill(
                label = stringResource(R.string.feature_onboarding_impl_welcome_career_starting),
                stage = CareerStage.JUST_STARTING_OUT,
                selected = selected,
                onSelect = onSelect,
                modifier = Modifier.weight(1f),
            )
            CareerChoicePill(
                label = stringResource(R.string.feature_onboarding_impl_welcome_career_one_two),
                stage = CareerStage.ONE_TO_TWO_YEARS_IN,
                selected = selected,
                onSelect = onSelect,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CareerChoicePill(
    label: String,
    stage: CareerStage,
    selected: CareerStage?,
    onSelect: (CareerStage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HhTheme.colors
    val isSelected = selected == stage
    val container = if (isSelected) colors.brand else colors.card
    val content = if (isSelected) colors.onBrand else colors.onSurface
    val shape = HhTheme.shapes.pill
    Box(
        modifier = modifier
            .heightIn(min = CHOICE_HEIGHT)
            .clip(shape)
            .selectable(selected = isSelected, role = Role.RadioButton) { onSelect(stage) }
            .background(container, shape)
            .then(if (isSelected) Modifier else Modifier.border(CHOICE_BORDER, colors.outlineVariant, shape))
            .padding(horizontal = HhTheme.spacing.sm, vertical = HhTheme.spacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        ) {
            if (isSelected) CareerChoiceDisc()
            Text(text = label, style = HhTheme.typography.titleS, color = content, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun CareerChoiceDisc(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(CHOICE_DISC).background(HhTheme.colors.onBrand, HhTheme.shapes.pill),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = HhIcons.Check,
            contentDescription = null,
            tint = HhTheme.colors.brand,
            modifier = Modifier.size(CHOICE_CHECK),
        )
    }
}

@Composable
private fun WelcomeLoading(modifier: Modifier = Modifier) {
    val message = stringResource(R.string.feature_onboarding_impl_welcome_loading)
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = HhTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HhLoadingWheel(contentDesc = message)
        Text(text = message, style = HhTheme.typography.bodyM, color = HhTheme.colors.onSurfaceVariant)
    }
}

@Composable
private fun WelcomeBottomBar(
    uiState: WelcomeUiState,
    actions: WelcomeActions,
) {
    HhBottomActionBar {
        HhPrimaryButton(
            label = stringResource(R.string.feature_onboarding_impl_welcome_action_continue),
            onClick = actions.onPasteJobDescription,
            enabled = uiState.isActionsEnabled,
            trailingIcon = HhIcons.ArrowForward,
            modifier = Modifier.weight(1f),
        )
    }
}
