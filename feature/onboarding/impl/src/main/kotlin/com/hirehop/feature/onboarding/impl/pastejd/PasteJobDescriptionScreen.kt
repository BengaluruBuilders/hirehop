package com.hirehop.feature.onboarding.impl.pastejd

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhErrorCallout
import com.hirehop.core.designsystem.component.HhHeroCard
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhSecondaryButton
import com.hirehop.core.designsystem.component.HhTextButton
import com.hirehop.core.designsystem.component.HhTextField
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.onboarding.impl.R
import com.hirehop.feature.onboarding.impl.common.DisclosureCard
import com.hirehop.feature.onboarding.impl.common.OnboardingNotice
import com.hirehop.feature.onboarding.impl.common.ReasonText

private val TEXT_AREA_MIN_HEIGHT = 132.dp
private val TEXT_AREA_MAX_HEIGHT = 220.dp
private val DASH_LENGTH = 6f
private val SOURCE_CHIP_ICON = 16.dp

@Composable
internal fun PasteJobDescriptionScreen(
    uiState: PasteJobDescriptionUiState,
    actions: PasteJobDescriptionActions,
    modifier: Modifier = Modifier,
) {
    HhScreen(
        modifier = modifier,
        sheet = false,
        header = {
            HhInnerHeader(
                title = stringResource(R.string.feature_onboarding_impl_paste_jd_title),
                subtitle = stringResource(R.string.feature_onboarding_impl_paste_jd_subtitle),
                onBack = actions.onBack,
                backContentDescription = stringResource(
                    R.string.feature_onboarding_impl_paste_jd_navigation_back_description,
                ),
            )
        },
        bottomBar = { PasteJobDescriptionBottomBar(uiState = uiState, actions = actions) },
        bottomBarNotice = { PasteJobDescriptionBarNotice(uiState = uiState) },
    ) { padding ->
        if (uiState.isLoading) {
            PasteJobDescriptionLoading(modifier = Modifier.padding(padding))
        } else {
            PasteJobDescriptionContent(uiState = uiState, actions = actions, modifier = Modifier.padding(padding))
        }
    }
}

@Composable
private fun PasteJobDescriptionLoading(modifier: Modifier = Modifier) {
    val message = stringResource(R.string.feature_onboarding_impl_paste_jd_loading)
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(HhTheme.spacing.gutter),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HhLoadingWheel(contentDesc = message)
        Text(text = message, style = HhTheme.typography.bodyM, color = HhTheme.colors.onSurfaceVariant)
    }
}

@Composable
private fun PasteJobDescriptionContent(
    uiState: PasteJobDescriptionUiState,
    actions: PasteJobDescriptionActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HhTheme.spacing.gutter),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        PasteJobDescriptionNotices(uiState = uiState, actions = actions)
        PasteJobDescriptionCard(uiState = uiState, actions = actions)
        HhTextField(
            value = uiState.company,
            onValueChange = actions.onCompanyChange,
            label = stringResource(R.string.feature_onboarding_impl_paste_jd_company_label),
            placeholder = stringResource(R.string.feature_onboarding_impl_paste_jd_company_placeholder),
        )
        HhTextField(
            value = uiState.role,
            onValueChange = actions.onRoleChange,
            label = stringResource(R.string.feature_onboarding_impl_paste_jd_role_label),
            placeholder = stringResource(R.string.feature_onboarding_impl_paste_jd_role_placeholder),
        )
    }
}

@Composable
private fun PasteJobDescriptionNotices(
    uiState: PasteJobDescriptionUiState,
    actions: PasteJobDescriptionActions,
) {
    HhOfflineBanner(
        message = stringResource(R.string.feature_onboarding_impl_paste_jd_offline_message),
        visible = uiState.isOffline,
    )
    when (uiState.message) {
        PasteJobDescriptionMessage.PASTE_FAILED -> HhErrorCallout(
            title = stringResource(R.string.feature_onboarding_impl_paste_jd_error_title),
            supportingText = stringResource(R.string.feature_onboarding_impl_paste_jd_error_body),
            actionLabel = stringResource(R.string.feature_onboarding_impl_paste_jd_try_again),
            onAction = actions.onRetry,
        )

        PasteJobDescriptionMessage.NOTHING_TO_READ -> OnboardingNotice(
            text = stringResource(R.string.feature_onboarding_impl_paste_jd_message_nothing_to_read),
            icon = HhIcons.Block,
        )

        PasteJobDescriptionMessage.PASTE_PARTIAL -> OnboardingNotice(
            text = stringResource(R.string.feature_onboarding_impl_paste_jd_message_partial),
            icon = HhIcons.Block,
        )

        null -> Unit
    }
}

@Composable
private fun PasteJobDescriptionCard(
    uiState: PasteJobDescriptionUiState,
    actions: PasteJobDescriptionActions,
) {
    HhHeroCard(contentPadding = PaddingValues(HhTheme.spacing.gutter)) {
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm + HhTheme.spacing.xxs)) {
            if (uiState.arrival == PasteJobDescriptionArrival.SHARED_IN) {
                SharedSourceChip()
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.feature_onboarding_impl_paste_jd_field_label),
                    modifier = Modifier.weight(1f),
                    style = HhTheme.typography.labelL,
                    color = HhTheme.colors.onSurface,
                )
                if (uiState.text.isNotBlank()) {
                    PasteJobDescriptionWordCount(wordCount = uiState.wordCount)
                }
            }
            PasteJobDescriptionTextArea(uiState = uiState, onTextChange = actions.onTextChange)
            uiState.problem?.let { problem ->
                OnboardingNotice(text = problemText(problem), icon = HhIcons.Block)
            }
            if (uiState.canClear) {
                HhTextButton(
                    label = stringResource(R.string.feature_onboarding_impl_paste_jd_action_clear),
                    onClick = actions.onClear,
                )
            } else {
                HhSecondaryButton(
                    label = stringResource(R.string.feature_onboarding_impl_paste_jd_action_paste),
                    onClick = actions.onPaste,
                )
            }
        }
    }
}

@Composable
private fun SharedSourceChip() {
    Row(
        modifier = Modifier
            .clip(HhTheme.shapes.pill)
            .background(HhTheme.colors.primaryContainer)
            .padding(horizontal = HhTheme.spacing.md, vertical = HhTheme.spacing.xs + HhTheme.spacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm - HhTheme.spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = HhIcons.Share,
            contentDescription = null,
            tint = HhTheme.colors.onPrimaryContainer,
            modifier = Modifier.size(SOURCE_CHIP_ICON),
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_paste_jd_arrival_shared_in),
            style = HhTheme.typography.labelM,
            color = HhTheme.colors.onPrimaryContainer,
        )
    }
}

@Composable
private fun PasteJobDescriptionWordCount(wordCount: Int) {
    val unit = pluralStringResource(R.plurals.feature_onboarding_impl_paste_jd_word_unit, wordCount)
    val description = stringResource(R.string.feature_onboarding_impl_paste_jd_word_count_description) +
        ": " + wordCount + " " + unit
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = HhTheme.colors.onSurface)) {
                append(wordCount.toString())
            }
            append(" ")
            append(unit)
        },
        modifier = Modifier.clearAndSetSemantics { contentDescription = description },
        style = HhTheme.typography.labelM,
        color = HhTheme.colors.onSurfaceVariant,
    )
}

@Composable
private fun PasteJobDescriptionTextArea(
    uiState: PasteJobDescriptionUiState,
    onTextChange: (String) -> Unit,
) {
    val outline = HhTheme.colors.outline
    val corner = HhTheme.spacing.sm
    val stroke = HhTheme.spacing.d2 * 3 / 4
    val label = stringResource(R.string.feature_onboarding_impl_paste_jd_field_label)
    BasicTextField(
        value = uiState.text,
        onValueChange = onTextChange,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = TEXT_AREA_MIN_HEIGHT, max = TEXT_AREA_MAX_HEIGHT)
            .drawBehind {
                drawRoundRect(
                    color = outline,
                    cornerRadius = CornerRadius(corner.toPx()),
                    style = Stroke(
                        width = stroke.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(DASH_LENGTH.dp.toPx(), DASH_LENGTH.dp.toPx())),
                    ),
                )
            }
            .padding(HhTheme.spacing.md)
            .clearAndSetSemantics { contentDescription = label },
        textStyle = HhTheme.typography.bodyM.copy(color = HhTheme.colors.body),
        cursorBrush = SolidColor(HhTheme.colors.primary),
        decorationBox = { inner ->
            Box(modifier = Modifier.fillMaxWidth()) {
                if (uiState.text.isEmpty()) {
                    Text(
                        text = stringResource(R.string.feature_onboarding_impl_paste_jd_field_placeholder),
                        style = HhTheme.typography.bodyM,
                        color = HhTheme.colors.onSurfaceVariant,
                    )
                }
                inner()
            }
        },
    )
}

@Composable
private fun problemText(problem: PasteJobDescriptionProblem): String = when (problem) {
    PasteJobDescriptionProblem.LINK_ONLY -> stringResource(
        R.string.feature_onboarding_impl_paste_jd_problem_link_only,
    )

    PasteJobDescriptionProblem.TOO_SHORT -> stringResource(
        R.string.feature_onboarding_impl_paste_jd_problem_too_short,
    )

    PasteJobDescriptionProblem.TOO_LONG -> stringResource(
        R.string.feature_onboarding_impl_paste_jd_problem_too_long,
    )
}

@Composable
private fun PasteJobDescriptionBottomBar(
    uiState: PasteJobDescriptionUiState,
    actions: PasteJobDescriptionActions,
) {
    val label = if (uiState.isOffline) {
        stringResource(R.string.feature_onboarding_impl_paste_jd_action_analyse_later)
    } else {
        stringResource(R.string.feature_onboarding_impl_paste_jd_action_analyse)
    }
    HhBottomActionBar {
        HhPrimaryButton(
            label = label,
            onClick = actions.onAnalyse,
            enabled = uiState.canAnalyse,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PasteJobDescriptionBarNotice(uiState: PasteJobDescriptionUiState) {
    val reason = when {
        uiState.isDailyLimitReached || uiState.canAnalyse -> null
        uiState.problem == PasteJobDescriptionProblem.TOO_SHORT ||
            uiState.problem == PasteJobDescriptionProblem.TOO_LONG ->
            stringResource(R.string.feature_onboarding_impl_paste_jd_reason_incomplete)
        else -> stringResource(R.string.feature_onboarding_impl_paste_jd_reason_empty)
    }
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        PasteJobDescriptionDisclosure(uiState = uiState)
        if (reason != null) {
            ReasonText(text = reason)
        }
    }
}

@Composable
private fun PasteJobDescriptionDisclosure(uiState: PasteJobDescriptionUiState) {
    val text = if (uiState.isDailyLimitReached) {
        buildAnnotatedString {
            append(
                pluralStringResource(
                    R.plurals.feature_onboarding_impl_paste_jd_disclosure_limit,
                    FREE_ANALYSES_PER_DAY,
                    FREE_ANALYSES_PER_DAY,
                ),
            )
        }
    } else {
        val count = stringResource(
            R.string.feature_onboarding_impl_paste_jd_free_left,
            uiState.freeAnalysesLeft,
            FREE_ANALYSES_PER_DAY,
        )
        val full = stringResource(R.string.feature_onboarding_impl_paste_jd_disclosure, count)
        buildAnnotatedString {
            append(full)
            addStyle(SpanStyle(fontWeight = FontWeight.Bold), 0, count.length)
        }
    }
    DisclosureCard(text = text, icon = HhIcons.Lock)
}
