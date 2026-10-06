package com.hirehop.feature.onboarding.impl.signin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhCheckbox
import com.hirehop.core.designsystem.component.HhDecoration
import com.hirehop.core.designsystem.component.HhDecorationKind
import com.hirehop.core.designsystem.component.HhHeroCard
import com.hirehop.core.designsystem.component.HhIconButton
import com.hirehop.core.designsystem.component.HhInkButton
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhTextButton
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.illustration.HhCharacterIllustration
import com.hirehop.core.designsystem.illustration.HhIllustration
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.onboarding.impl.R
import com.hirehop.feature.onboarding.impl.common.NoticeTone
import com.hirehop.feature.onboarding.impl.common.OnboardingNotice
import com.hirehop.feature.onboarding.impl.common.OnboardingStepBar
import com.hirehop.feature.onboarding.impl.common.ReasonText

private const val SIGN_IN_STEP = 2
private val GOOGLE_MARK_SIZE = 30.dp
private val BADGE_ICON_SIZE = 18.dp
private val BADGE_HEIGHT = 34.dp
private val NOTE_ICON_SIZE = 40.dp
private val NOTE_ICON_GLYPH = 22.dp
private val ART_DISC = 180.dp
private val ART_HEIGHT = 250.dp
private val RING_CLEARANCE = 56.dp
private val HEADER_RING_PADDING_END = 24.dp
private val HEADER_RING_PADDING_BOTTOM = 24.dp
private val LEGAL_LINE_CLEARANCE = 92.dp
private val PINNED_ACTION_BOTTOM = 24.dp

@Composable
internal fun SignInScreen(
    uiState: SignInUiState,
    actions: SignInActions,
    modifier: Modifier = Modifier,
) {
    if (uiState.stage == SignInStage.UNDER_18) {
        UnderEighteenScreen(actions = actions, modifier = modifier)
    } else {
        SignInFormScreen(uiState = uiState, actions = actions, modifier = modifier)
    }
}

@Composable
private fun SignInFormScreen(
    uiState: SignInUiState,
    actions: SignInActions,
    modifier: Modifier = Modifier,
) {
    HhScreen(
        modifier = modifier,
        sheet = false,
        header = { SignInHeader(actions = actions) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            SignInContent(uiState = uiState, actions = actions)
            HhTextButton(
                label = stringResource(R.string.feature_onboarding_impl_sign_in_action_under_18),
                onClick = actions.onUnderEighteen,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = PINNED_ACTION_BOTTOM),
            )
        }
    }
}

@Composable
private fun SignInHeader(actions: SignInActions, modifier: Modifier = Modifier) {
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(HhTheme.shapes.heroBottom)
            .background(HhTheme.colors.brand)
            .padding(horizontal = HhTheme.spacing.gutter)
            .padding(top = statusTop + HhTheme.spacing.xxl, bottom = HhTheme.spacing.xxl),
    ) {
        HhDecoration(
            kind = HhDecorationKind.Ring,
            color = HhTheme.colors.special,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = HEADER_RING_PADDING_END, bottom = HEADER_RING_PADDING_BOTTOM),
        )
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xl)) {
            OnboardingStepBar(
                step = SIGN_IN_STEP,
                onBack = actions.onBack,
                onBrand = true,
                backContentDescription = stringResource(
                    R.string.feature_onboarding_impl_sign_in_navigation_back_content_description,
                ),
            )
            Column(
                modifier = Modifier.padding(end = RING_CLEARANCE),
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            ) {
                Text(
                    text = stringResource(R.string.feature_onboarding_impl_sign_in_heading),
                    style = HhTheme.typography.displayM,
                    color = HhTheme.colors.onBrand,
                )
                Text(
                    text = stringResource(R.string.feature_onboarding_impl_sign_in_intro),
                    style = HhTheme.typography.bodyL,
                    color = HhTheme.colors.onHeaderVariant,
                )
            }
        }
    }
}

@Composable
private fun SignInContent(
    uiState: SignInUiState,
    actions: SignInActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HhTheme.spacing.gutter)
            .padding(top = HhTheme.spacing.sectionGap, bottom = LEGAL_LINE_CLEARANCE),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.lg),
    ) {
        SignInNotices(uiState = uiState)
        SignInAgeCard(uiState = uiState, onChange = actions.onAdultConfirmationChange)
        GoogleSignInButton(uiState = uiState, onContinue = actions.onContinue)
        SignInReason(uiState = uiState)
        SignInLegalLine()
    }
}

@Composable
private fun SignInNotices(uiState: SignInUiState) {
    HhOfflineBanner(
        message = stringResource(R.string.feature_onboarding_impl_sign_in_offline_message),
        visible = uiState.isOffline,
    )
    when (uiState.stage) {
        SignInStage.FAILED -> OnboardingNotice(
            text = stringResource(R.string.feature_onboarding_impl_sign_in_failure_message),
            icon = HhIcons.Error,
            tone = NoticeTone.Error,
        )

        SignInStage.CANCELLED -> OnboardingNotice(
            text = stringResource(R.string.feature_onboarding_impl_sign_in_cancelled_message),
            icon = HhIcons.Block,
        )

        else -> Unit
    }
}

@Composable
private fun SignInAgeCard(uiState: SignInUiState, onChange: (Boolean) -> Unit) {
    HhHeroCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(value = uiState.isAdultConfirmed, role = Role.Checkbox, onValueChange = onChange),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
            verticalAlignment = Alignment.Top,
        ) {
            HhCheckbox(
                checked = uiState.isAdultConfirmed,
                onCheckedChange = onChange,
                modifier = Modifier.clearAndSetSemantics {},
            )
            Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs)) {
                Text(
                    text = stringResource(R.string.feature_onboarding_impl_sign_in_age_label),
                    style = HhTheme.typography.titleM,
                    color = HhTheme.colors.onSurface,
                )
                Text(
                    text = stringResource(R.string.feature_onboarding_impl_sign_in_age_support),
                    style = HhTheme.typography.labelL,
                    color = HhTheme.colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun GoogleSignInButton(uiState: SignInUiState, onContinue: () -> Unit) {
    HhOutlinedButton(
        onClick = onContinue,
        enabled = uiState.canContinue && uiState.isAdultConfirmed,
        modifier = Modifier.fillMaxWidth(),
    ) {
        GoogleMark()
        Text(
            text = stringResource(R.string.feature_onboarding_impl_sign_in_action_continue),
            color = LocalContentColor.current,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun GoogleMark() {
    Box(
        modifier = Modifier
            .size(GOOGLE_MARK_SIZE)
            .clip(HhTheme.shapes.pill)
            .background(HhTheme.colors.surface)
            .border(HhTheme.spacing.d2 * 3 / 4, HhTheme.colors.outlineVariant, HhTheme.shapes.pill),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.feature_onboarding_impl_sign_in_google_mark),
            style = HhTheme.typography.titleM,
            color = HhTheme.colors.onSurface,
        )
    }
}

@Composable
private fun SignInReason(uiState: SignInUiState) {
    val reason = when {
        uiState.isBusy -> stringResource(R.string.feature_onboarding_impl_sign_in_reason_in_progress)
        uiState.isOffline -> stringResource(R.string.feature_onboarding_impl_sign_in_reason_offline)
        !uiState.isAdultConfirmed -> stringResource(R.string.feature_onboarding_impl_sign_in_age_nudge)
        else -> null
    }
    if (reason != null) {
        ReasonText(text = reason)
    }
}

@Composable
private fun SignInLegalLine() {
    val strong = SpanStyle(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)
    Text(
        text = buildAnnotatedString {
            append(stringResource(R.string.feature_onboarding_impl_sign_in_legal_prefix))
            withStyle(strong) { append(stringResource(R.string.feature_onboarding_impl_sign_in_legal_terms)) }
            append(stringResource(R.string.feature_onboarding_impl_sign_in_legal_and))
            withStyle(strong) { append(stringResource(R.string.feature_onboarding_impl_sign_in_legal_privacy)) }
            append(".")
        },
        modifier = Modifier.fillMaxWidth(),
        style = HhTheme.typography.bodyS,
        color = HhTheme.colors.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun UnderEighteenScreen(actions: SignInActions, modifier: Modifier = Modifier) {
    HhScreen(modifier = modifier, sheet = false) { padding ->
        UnderEighteenContent(actions = actions, modifier = Modifier.padding(padding))
    }
}

@Composable
private fun UnderEighteenContent(actions: SignInActions, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HhTheme.spacing.gutter)
            .padding(top = HhTheme.spacing.xxl, bottom = HhTheme.spacing.gutter),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xl),
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            HhIconButton(
                icon = HhIcons.Close,
                contentDescription = stringResource(
                    R.string.feature_onboarding_impl_sign_in_under_18_close_description,
                ),
                onClick = actions.onBackFromUnderEighteen,
                containerColor = HhTheme.colors.card,
                borderColor = HhTheme.colors.outlineVariant,
            )
        }
        UnderEighteenArt()
        UnderEighteenBadge()
        Text(
            text = stringResource(R.string.feature_onboarding_impl_sign_in_under_18_title),
            style = HhTheme.typography.headlineL,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_sign_in_under_18_body),
            style = HhTheme.typography.bodyL,
            color = HhTheme.colors.onSurfaceVariant,
        )
        NothingKeptCard()
        HhInkButton(
            label = stringResource(R.string.feature_onboarding_impl_sign_in_under_18_action_back),
            onClick = actions.onBackFromUnderEighteen,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun UnderEighteenArt(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth()
            .height(ART_HEIGHT),
        contentAlignment = Alignment.Center,
    ) {
        HhDecoration(
            kind = HhDecorationKind.Squiggle,
            color = HhTheme.colors.coral,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = HhTheme.spacing.lg),
        )
        HhDecoration(
            kind = HhDecorationKind.Ring,
            color = HhTheme.colors.special,
            modifier = Modifier.align(Alignment.TopEnd),
        )
        HhDecoration(
            kind = HhDecorationKind.Dots,
            color = HhTheme.colors.brand,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = HhTheme.spacing.md),
        )
        Box(
            modifier = Modifier
                .size(ART_DISC)
                .clip(HhTheme.shapes.pill)
                .background(HhTheme.colors.card)
                .border(1.dp, HhTheme.colors.outlineVariant, HhTheme.shapes.pill),
            contentAlignment = Alignment.BottomCenter,
        ) {
            HhCharacterIllustration(
                illustration = HhIllustration.Goodbye,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun UnderEighteenBadge(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = BADGE_HEIGHT)
                .clip(HhTheme.shapes.pill)
                .background(HhTheme.colors.coral)
                .padding(horizontal = HhTheme.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm - HhTheme.spacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = HhIcons.CancelCircle,
                contentDescription = null,
                tint = HhTheme.colors.onCoral,
                modifier = Modifier.size(BADGE_ICON_SIZE),
            )
            Text(
                text = stringResource(R.string.feature_onboarding_impl_sign_in_under_18_badge),
                style = HhTheme.typography.labelL.copy(fontWeight = FontWeight.Bold),
                color = HhTheme.colors.onCoral,
            )
        }
    }
}

@Composable
private fun NothingKeptCard(modifier: Modifier = Modifier) {
    HhHeroCard(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(NOTE_ICON_SIZE)
                    .clip(HhTheme.shapes.pill)
                    .background(HhTheme.colors.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = HhIcons.Verified,
                    contentDescription = null,
                    tint = HhTheme.colors.onPrimaryContainer,
                    modifier = Modifier.size(NOTE_ICON_GLYPH),
                )
            }
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(R.string.feature_onboarding_impl_sign_in_under_18_note_strong))
                    }
                    append(stringResource(R.string.feature_onboarding_impl_sign_in_under_18_note))
                },
                modifier = Modifier.weight(1f),
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.onSurface,
            )
        }
    }
}
