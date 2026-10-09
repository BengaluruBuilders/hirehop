package com.tailormyresume.feature.onboarding.impl.welcome

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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.component.TmrBottomActionBar
import com.tailormyresume.core.designsystem.component.TmrCard
import com.tailormyresume.core.designsystem.component.TmrDivider
import com.tailormyresume.core.designsystem.component.TmrErrorCallout
import com.tailormyresume.core.designsystem.component.TmrFactId
import com.tailormyresume.core.designsystem.component.TmrHeadline
import com.tailormyresume.core.designsystem.component.TmrLoadingWheel
import com.tailormyresume.core.designsystem.component.TmrMonogram
import com.tailormyresume.core.designsystem.component.TmrOfflineBanner
import com.tailormyresume.core.designsystem.component.TmrPrimaryButton
import com.tailormyresume.core.designsystem.component.TmrScreen
import com.tailormyresume.core.designsystem.component.TmrSectionLabel
import com.tailormyresume.core.designsystem.component.TmrStatusChip
import com.tailormyresume.core.designsystem.component.TmrStatusKind
import com.tailormyresume.core.designsystem.component.TmrTextButton
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.CareerStage
import com.tailormyresume.feature.onboarding.impl.R

private val TOP_ROW_HEIGHT = 48.dp
private val LOGO_MARK = 36.dp
private const val LINK_OWN_LINE_FONT_SCALE = 1.5f
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
    TmrScreen(
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
            .padding(horizontal = TmrTheme.spacing.gutter)
            .padding(bottom = TmrTheme.spacing.gutter),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.lg),
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
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
    ) {
        TmrOfflineBanner(
            message = stringResource(R.string.feature_onboarding_impl_welcome_offline_message),
            supportingText = stringResource(R.string.feature_onboarding_impl_welcome_offline_support),
            visible = uiState.isOffline,
        )
        if (uiState.message == WelcomeMessage.LOAD_FAILED) {
            TmrErrorCallout(
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
    val isLinkOnOwnLine = LocalDensity.current.fontScale >= LINK_OWN_LINE_FONT_SCALE
    val brand: @Composable () -> Unit = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
        ) {
            WelcomeLogoMark()
            Text(
                text = stringResource(R.string.feature_onboarding_impl_welcome_brand),
                style = TmrTheme.typography.titleL,
                color = TmrTheme.colors.onSurface,
            )
        }
    }
    val haveAccount: @Composable () -> Unit = {
        TmrTextButton(
            label = stringResource(R.string.feature_onboarding_impl_welcome_have_account),
            onClick = actions.onHaveAccount,
        )
    }
    if (isLinkOnOwnLine) {
        Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Box(modifier = Modifier.heightIn(min = TOP_ROW_HEIGHT), contentAlignment = Alignment.CenterStart) { brand() }
            haveAccount()
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth().heightIn(min = TOP_ROW_HEIGHT),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            brand()
            haveAccount()
        }
    }
}

@Composable
private fun WelcomeLogoMark(modifier: Modifier = Modifier) {
    TmrMonogram(
        text = stringResource(R.string.feature_onboarding_impl_welcome_brand),
        modifier = modifier,
        size = LOGO_MARK,
        shape = TmrTheme.shapes.logoTile,
    )
}

@Composable
private fun WelcomeHeadline(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
    ) {
        TmrHeadline(
            text = stringResource(R.string.feature_onboarding_impl_welcome_headline),
            style = TmrTheme.typography.displayM,
            color = TmrTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_welcome_subline),
            style = TmrTheme.typography.titleM,
            color = TmrTheme.colors.onSurface,
        )
    }
}

@Composable
private fun WelcomeSample(modifier: Modifier = Modifier) {
    TmrCard(modifier = modifier.fillMaxWidth()) {
        SampleLabel(R.string.feature_onboarding_impl_welcome_sample_original)
        Text(
            text = stringResource(R.string.feature_onboarding_impl_welcome_sample_original_text),
            style = TmrTheme.typography.bodyL,
            color = TmrTheme.colors.onSurfaceVariant,
        )
        SampleLabel(R.string.feature_onboarding_impl_welcome_sample_rewritten)
        Text(
            text = stringResource(R.string.feature_onboarding_impl_welcome_sample_rewritten_text),
            style = TmrTheme.typography.titleM,
            color = TmrTheme.colors.onSurface,
        )
        TmrDivider()
        Row(
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TmrFactId(id = stringResource(R.string.feature_onboarding_impl_welcome_sample_fact_id))
            TmrStatusChip(
                kind = TmrStatusKind.Met,
                label = stringResource(R.string.feature_onboarding_impl_welcome_sample_confirmed),
            )
        }
        Text(
            text = stringResource(R.string.feature_onboarding_impl_welcome_sample_source),
            style = TmrTheme.typography.bodyS,
            color = TmrTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun SampleLabel(@StringRes label: Int) {
    TmrSectionLabel(text = stringResource(label))
}

@Composable
private fun WelcomeCareerChoice(
    selected: CareerStage?,
    onSelect: (CareerStage) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
    ) {
        Text(
            text = stringResource(R.string.feature_onboarding_impl_welcome_career_question),
            style = TmrTheme.typography.titleM,
            color = TmrTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_welcome_career_hint),
            style = TmrTheme.typography.bodyS,
            color = TmrTheme.colors.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth().selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
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
    val colors = TmrTheme.colors
    val isSelected = selected == stage
    val container = if (isSelected) colors.brand else colors.card
    val content = if (isSelected) colors.onBrand else colors.onSurface
    val shape = TmrTheme.shapes.pill
    Box(
        modifier = modifier
            .heightIn(min = CHOICE_HEIGHT)
            .clip(shape)
            .selectable(selected = isSelected, role = Role.RadioButton) { onSelect(stage) }
            .background(container, shape)
            .then(if (isSelected) Modifier else Modifier.border(CHOICE_BORDER, colors.outlineVariant, shape))
            .padding(horizontal = TmrTheme.spacing.sm, vertical = TmrTheme.spacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
        ) {
            if (isSelected) CareerChoiceDisc()
            Text(text = label, style = TmrTheme.typography.titleS, color = content, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun CareerChoiceDisc(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(CHOICE_DISC).background(TmrTheme.colors.onBrand, TmrTheme.shapes.pill),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = TmrIcons.Check,
            contentDescription = null,
            tint = TmrTheme.colors.brand,
            modifier = Modifier.size(CHOICE_CHECK),
        )
    }
}

@Composable
private fun WelcomeLoading(modifier: Modifier = Modifier) {
    val message = stringResource(R.string.feature_onboarding_impl_welcome_loading)
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = TmrTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TmrLoadingWheel(contentDesc = message)
        Text(text = message, style = TmrTheme.typography.bodyM, color = TmrTheme.colors.onSurfaceVariant)
    }
}

@Composable
private fun WelcomeBottomBar(
    uiState: WelcomeUiState,
    actions: WelcomeActions,
) {
    TmrBottomActionBar {
        TmrPrimaryButton(
            label = stringResource(R.string.feature_onboarding_impl_welcome_action_continue),
            onClick = actions.onPasteJobDescription,
            enabled = uiState.isActionsEnabled,
            trailingIcon = TmrIcons.ArrowForward,
            modifier = Modifier.weight(1f),
        )
    }
}
