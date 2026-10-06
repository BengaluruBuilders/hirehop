package com.hirehop.feature.onboarding.impl.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhDecoration
import com.hirehop.core.designsystem.component.HhDecorationKind
import com.hirehop.core.designsystem.component.HhErrorCallout
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhTextButton
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.illustration.HhCharacterIllustration
import com.hirehop.core.designsystem.illustration.HhIllustration
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.CareerStage
import com.hirehop.feature.onboarding.impl.R

private val TOP_ROW_HEIGHT = 48.dp
private val LOGO_MARK = 32.dp
private val STAGE_HEIGHT = 286.dp
private val STAGE_CARD_WIDTH = 164.dp
private val STAGE_CARD_HEIGHT = 192.dp
private val STAGE_CARD_GAP = 10.dp
private val STAGE_ITEM_GAP = 6.dp
private val STAGE_MONOGRAM = 38.dp
private val STAGE_MARK = 14.dp
private val STAGE_CHECK = 9.dp
private val STAGE_RING_STROKE = 1.5.dp
private val HERO_WIDTH = 156.dp
private val HERO_HEIGHT = 208.dp
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
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
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        WelcomeNotices(uiState = uiState, actions = actions)
        WelcomeTopRow(actions = actions)
        WelcomeStage()
        WelcomeHeadline()
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
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = TOP_ROW_HEIGHT),
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
        modifier = modifier
            .size(LOGO_MARK)
            .background(HhTheme.colors.brand, HhTheme.shapes.pill),
        contentAlignment = Alignment.Center,
    ) {
        HhDecoration(kind = HhDecorationKind.Ring, color = HhTheme.colors.onBrand)
    }
}

@Composable
private fun WelcomeStage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(STAGE_HEIGHT),
    ) {
        Box(modifier = Modifier.fillMaxSize().clearAndSetSemantics {}) {
            HhDecoration(
                kind = HhDecorationKind.Squiggle,
                color = HhTheme.colors.coral,
                modifier = Modifier.align(Alignment.TopStart).padding(top = 10.dp),
            )
            HhDecoration(
                kind = HhDecorationKind.Ring,
                color = HhTheme.colors.special,
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 4.dp, end = 18.dp),
            )
            HhDecoration(
                kind = HhDecorationKind.Dots,
                color = HhTheme.colors.brand,
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 10.dp, bottom = 6.dp),
            )
            WelcomeStageCards()
        }
        HhCharacterIllustration(
            illustration = HhIllustration.Hero,
            contentDescription = stringResource(R.string.feature_onboarding_impl_welcome_illustration_description),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 8.dp)
                .size(width = HERO_WIDTH, height = HERO_HEIGHT),
        )
    }
}

@Composable
private fun BoxScope.WelcomeStageCards() {
    val colors = HhTheme.colors
    WelcomeStageCard(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(x = 2.dp, y = 58.dp)
            .graphicsLayer(rotationZ = -11f),
        monogram = stringResource(R.string.feature_onboarding_impl_welcome_card1_monogram),
        title = stringResource(R.string.feature_onboarding_impl_welcome_card1_title),
        items = listOf(
            stringResource(R.string.feature_onboarding_impl_welcome_card1_item1),
            stringResource(R.string.feature_onboarding_impl_welcome_card1_item2),
        ),
        fill = colors.coral,
        content = colors.onCoral,
        letter = colors.coral,
        disc = colors.onBrand,
        check = colors.coral,
    )
    WelcomeStageCard(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(x = 56.dp, y = 20.dp)
            .graphicsLayer(rotationZ = -3f),
        monogram = stringResource(R.string.feature_onboarding_impl_welcome_card2_monogram),
        title = stringResource(R.string.feature_onboarding_impl_welcome_card2_title),
        items = listOf(
            stringResource(R.string.feature_onboarding_impl_welcome_card2_item1),
            stringResource(R.string.feature_onboarding_impl_welcome_card2_item2),
            stringResource(R.string.feature_onboarding_impl_welcome_card2_item3),
        ),
        fill = colors.brand,
        content = colors.onBrand,
        letter = colors.brand,
        disc = colors.onBrand,
        check = colors.brand,
        lastItemOpen = true,
    )
    WelcomeStageCard(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(x = 104.dp, y = 70.dp)
            .graphicsLayer(rotationZ = 8f),
        monogram = stringResource(R.string.feature_onboarding_impl_welcome_card3_monogram),
        title = stringResource(R.string.feature_onboarding_impl_welcome_card3_title),
        items = listOf(
            stringResource(R.string.feature_onboarding_impl_welcome_card3_item1),
            stringResource(R.string.feature_onboarding_impl_welcome_card3_item2),
        ),
        fill = colors.special,
        content = colors.onSpecial,
        letter = colors.onSpecial,
        disc = colors.onSpecial,
        check = colors.special,
    )
}

@Composable
private fun WelcomeStageCard(
    modifier: Modifier = Modifier,
    monogram: String,
    title: String,
    items: List<String>,
    fill: Color,
    content: Color,
    letter: Color,
    disc: Color,
    check: Color,
    lastItemOpen: Boolean = false,
) {
    Box(
        modifier = modifier
            .size(STAGE_CARD_WIDTH, STAGE_CARD_HEIGHT)
            .clip(HhTheme.shapes.card)
            .background(fill, HhTheme.shapes.card),
    ) {
        Column(
            modifier = Modifier.padding(HhTheme.spacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(STAGE_CARD_GAP),
        ) {
            StageMonogram(monogram = monogram, letter = letter)
            Text(text = title, style = HhTheme.typography.titleS, color = content)
            Column(verticalArrangement = Arrangement.spacedBy(STAGE_ITEM_GAP)) {
                items.forEachIndexed { index, item ->
                    StageItem(
                        text = item,
                        content = content,
                        disc = disc,
                        check = check,
                        open = lastItemOpen && index == items.lastIndex,
                    )
                }
            }
        }
    }
}

@Composable
private fun StageMonogram(
    monogram: String,
    letter: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(STAGE_MONOGRAM)
            .background(HhTheme.colors.onBrand, HhTheme.shapes.pill)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = monogram,
            style = HhTheme.typography.titleS.copy(fontWeight = FontWeight.ExtraBold),
            color = letter,
        )
    }
}

@Composable
private fun StageItem(
    text: String,
    content: Color,
    disc: Color,
    check: Color,
    open: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(STAGE_ITEM_GAP),
    ) {
        StageItemMark(open = open, disc = disc, check = check)
        Text(
            text = text,
            style = HhTheme.typography.labelM.copy(fontWeight = FontWeight.Bold),
            color = content,
        )
    }
}

@Composable
private fun StageItemMark(
    open: Boolean,
    disc: Color,
    check: Color,
    modifier: Modifier = Modifier,
) {
    if (open) {
        Box(
            modifier = modifier
                .size(STAGE_MARK)
                .border(STAGE_RING_STROKE, disc, HhTheme.shapes.pill),
        )
        return
    }
    Box(
        modifier = modifier
            .size(STAGE_MARK)
            .background(disc, HhTheme.shapes.pill),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = HhIcons.Check,
            contentDescription = null,
            tint = check,
            modifier = Modifier.size(STAGE_CHECK),
        )
    }
}

@Composable
private fun WelcomeHeadline(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.feature_onboarding_impl_welcome_headline),
            style = HhTheme.typography.displayM,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_welcome_subline),
            style = HhTheme.typography.bodyL,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
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
    val container = if (isSelected) colors.inverseSurface else colors.card
    val content = if (isSelected) colors.inverseOnSurface else colors.onSurface
    val shape = HhTheme.shapes.pill
    Box(
        modifier = modifier
            .heightIn(min = CHOICE_HEIGHT)
            .clip(shape)
            .selectable(selected = isSelected, role = Role.RadioButton) { onSelect(stage) }
            .background(container, shape)
            .then(if (isSelected) Modifier else Modifier.border(CHOICE_BORDER, colors.outlineVariant, shape))
            .padding(horizontal = 8.dp, vertical = HhTheme.spacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        ) {
            if (isSelected) {
                CareerChoiceDisc()
            }
            Text(
                text = label,
                style = HhTheme.typography.titleS,
                color = content,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun CareerChoiceDisc(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(CHOICE_DISC)
            .background(HhTheme.colors.special, HhTheme.shapes.pill),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = HhIcons.Check,
            contentDescription = null,
            tint = HhTheme.colors.onSpecial,
            modifier = Modifier.size(CHOICE_CHECK),
        )
    }
}

@Composable
private fun WelcomeLoading(modifier: Modifier = Modifier) {
    val message = stringResource(R.string.feature_onboarding_impl_welcome_loading)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = HhTheme.spacing.lg),
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
