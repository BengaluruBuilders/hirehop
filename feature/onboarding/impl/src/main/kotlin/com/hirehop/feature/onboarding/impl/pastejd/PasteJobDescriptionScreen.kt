package com.hirehop.feature.onboarding.impl.pastejd

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhButtonSize
import com.hirehop.core.designsystem.component.HhDecoration
import com.hirehop.core.designsystem.component.HhDecorationKind
import com.hirehop.core.designsystem.component.HhErrorCallout
import com.hirehop.core.designsystem.component.HhIconActionBar
import com.hirehop.core.designsystem.component.HhIconButton
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhSecondaryButton
import com.hirehop.core.designsystem.component.HhTextField
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.illustration.HhCharacterIllustration
import com.hirehop.core.designsystem.illustration.HhIllustration
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.onboarding.impl.R
import com.hirehop.feature.onboarding.impl.common.DisclosureCard
import com.hirehop.feature.onboarding.impl.common.OnboardingNotice
import com.hirehop.feature.onboarding.impl.common.OnboardingStepBar
import com.hirehop.feature.onboarding.impl.common.ReasonText

private val PASTE_STEP = 1
private val PASTE_FIELD_HEIGHT_EMPTY = 330.dp
private val PASTE_FIELD_HEIGHT_PASTED = 282.dp
private val PASTE_FIELD_HEIGHT_TOO_SHORT = 168.dp
private val PASTE_FIELD_CORNER = 28.dp
private val PASTE_FIELD_BORDER = 1.5.dp
private val PASTE_FIELD_PADDING = 18.dp
private val PASTE_FOOTER_RESERVE = 76.dp
private val PASTE_RING_SIZE = 34.dp
private val PASTE_ERROR_ICON = 22.dp
private val PASTE_CHIP_HEIGHT = 34.dp
private val PASTE_CHIP_PADDING = 12.dp
private val PASTE_ILLUSTRATION_WIDTH = 84.dp
private val PASTE_ILLUSTRATION_HEIGHT = 112.dp
private val SOURCE_CHIP_ICON = 16.dp
private val PASTE_WORD_ICON = 18.dp

@Composable
internal fun PasteJobDescriptionScreen(
    uiState: PasteJobDescriptionUiState,
    actions: PasteJobDescriptionActions,
    modifier: Modifier = Modifier,
) {
    HhScreen(
        modifier = modifier,
        sheet = false,
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
    var revealsRoleAndCompany by remember(uiState.role, uiState.company) {
        mutableStateOf(uiState.role.isNotBlank() || uiState.company.isNotBlank())
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HhTheme.spacing.gutter),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        OnboardingStepBar(
            step = PASTE_STEP,
            onBack = actions.onBack,
            backContentDescription = stringResource(
                R.string.feature_onboarding_impl_paste_jd_navigation_back_description,
            ),
        )
        PasteJobDescriptionNotices(uiState = uiState, actions = actions)
        PasteJobDescriptionIntro()
        PasteJobDescriptionField(uiState = uiState, actions = actions)
        val problem = uiState.problem
        when (problem) {
            PasteJobDescriptionProblem.TOO_SHORT -> TooShortHelp()
            PasteJobDescriptionProblem.LINK_ONLY, PasteJobDescriptionProblem.TOO_LONG ->
                OnboardingNotice(
                    text = problemText(problem),
                    icon = HhIcons.Block,
                    tone = com.hirehop.feature.onboarding.impl.common.NoticeTone.Error,
                )

            null ->
                if (uiState.text.isEmpty()) {
                    PasteJobDescriptionHint()
                } else {
                    SpottedRow(uiState = uiState, onEdit = { revealsRoleAndCompany = true })
                }
        }
        if (revealsRoleAndCompany) {
            RoleAndCompanyFields(actions = actions, uiState = uiState)
        }
    }
}

@Composable
private fun PasteJobDescriptionHint() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = HhIcons.Info,
            contentDescription = null,
            tint = HhTheme.colors.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_paste_jd_hint, PASTE_JD_MIN_WORDS),
            modifier = Modifier.weight(1f),
            style = HhTheme.typography.bodyS,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun PasteJobDescriptionIntro() {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        Text(
            text = stringResource(R.string.feature_onboarding_impl_paste_jd_heading),
            style = HhTheme.typography.headlineL,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_paste_jd_intro),
            style = HhTheme.typography.bodyL,
            color = HhTheme.colors.onSurfaceVariant,
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
    if (uiState.arrival == PasteJobDescriptionArrival.SHARED_IN) {
        SharedSourceChip()
    }
}

@Composable
private fun PasteJobDescriptionField(
    uiState: PasteJobDescriptionUiState,
    actions: PasteJobDescriptionActions,
) {
    val colors = HhTheme.colors
    val tooShort = uiState.problem == PasteJobDescriptionProblem.TOO_SHORT
    val border = if (tooShort) colors.coral else colors.outlineVariant
    val fieldHeight = when {
        tooShort -> PASTE_FIELD_HEIGHT_TOO_SHORT
        uiState.text.isNotEmpty() -> PASTE_FIELD_HEIGHT_PASTED
        else -> PASTE_FIELD_HEIGHT_EMPTY
    }
    Column(
        modifier = Modifier.padding(top = HhTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        Text(
            text = stringResource(R.string.feature_onboarding_impl_paste_jd_field_label),
            style = HhTheme.typography.labelL.copy(fontWeight = FontWeight.Bold),
            color = colors.onSurface,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = fieldHeight)
                .clip(HhTheme.shapes.card)
                .background(if (tooShort) colors.surface else colors.card)
                .border(PASTE_FIELD_BORDER, border, HhTheme.shapes.card),
        ) {
            PasteJobDescriptionTextArea(uiState = uiState, onTextChange = actions.onTextChange)
            if (uiState.text.isEmpty()) {
                HhDecoration(
                    kind = HhDecorationKind.Ring,
                    color = colors.special,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = PASTE_FIELD_PADDING * 6, end = PASTE_FIELD_PADDING + HhTheme.spacing.d4)
                        .size(PASTE_RING_SIZE),
                )
            }
            PasteJobDescriptionFieldFooter(uiState = uiState, onPaste = actions.onPaste)
        }
    }
}

@Composable
private fun BoxScope.PasteJobDescriptionFieldFooter(
    uiState: PasteJobDescriptionUiState,
    onPaste: () -> Unit,
) {
    Row(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .fillMaxWidth()
            .padding(start = PASTE_CHIP_PADDING + HhTheme.spacing.d2, end = PASTE_FIELD_PADDING, bottom = PASTE_CHIP_PADDING + HhTheme.spacing.d2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HhSecondaryButton(
            label = stringResource(R.string.feature_onboarding_impl_paste_jd_action_paste),
            onClick = onPaste,
            size = HhButtonSize.Compact,
            leadingIcon = HhIcons.Description,
        )
        Spacer(modifier = Modifier.weight(1f))
        PasteJobDescriptionCount(uiState = uiState)
    }
}

@Composable
private fun PasteJobDescriptionCount(uiState: PasteJobDescriptionUiState) {
    when {
        uiState.problem == PasteJobDescriptionProblem.TOO_SHORT -> Text(
            text = stringResource(
                R.string.feature_onboarding_impl_paste_jd_short_progress,
                uiState.wordCount,
                PASTE_JD_MIN_WORDS,
            ),
            style = HhTheme.typography.labelM.copy(fontWeight = FontWeight.Bold),
            color = HhTheme.colors.coral,
        )

        uiState.text.isNotBlank() -> Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs + HhTheme.spacing.xxs),
        ) {
            Icon(
                imageVector = HhIcons.CheckCircle,
                contentDescription = null,
                tint = HhTheme.colors.brand,
                modifier = Modifier.size(PASTE_WORD_ICON),
            )
            Text(
                text = stringResource(R.string.feature_onboarding_impl_paste_jd_enough, uiState.wordCount),
                style = HhTheme.typography.labelM.copy(fontWeight = FontWeight.Bold),
                color = HhTheme.colors.brand,
            )
        }

        else -> PasteJobDescriptionWordCount(wordCount = uiState.wordCount)
    }
}

@Composable
private fun PasteJobDescriptionWordCount(wordCount: Int) {
    val unit = pluralStringResource(R.plurals.feature_onboarding_impl_paste_jd_word_unit, wordCount)
    val description = stringResource(R.string.feature_onboarding_impl_paste_jd_word_count_description) +
        ": " + wordCount + " " + unit
    Text(
        text = wordCount.toString() + " " + unit,
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
    val label = stringResource(R.string.feature_onboarding_impl_paste_jd_field_label)
    BasicTextField(
        value = uiState.text,
        onValueChange = onTextChange,
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = PASTE_FIELD_PADDING,
                end = PASTE_FIELD_PADDING,
                top = PASTE_FIELD_PADDING,
                bottom = PASTE_FOOTER_RESERVE,
            )
            .clearAndSetSemantics { contentDescription = label },
        textStyle = HhTheme.typography.bodyL.copy(color = HhTheme.colors.onSurface),
        cursorBrush = SolidColor(HhTheme.colors.primary),
        decorationBox = { inner ->
            Box(modifier = Modifier.fillMaxWidth()) {
                if (uiState.text.isEmpty()) {
                    Text(
                        text = stringResource(R.string.feature_onboarding_impl_paste_jd_field_placeholder),
                        style = HhTheme.typography.bodyL,
                        color = HhTheme.colors.onSurfaceVariant,
                    )
                }
                inner()
            }
        },
    )
}

@Composable
private fun SpottedRow(uiState: PasteJobDescriptionUiState, onEdit: () -> Unit) {
    if (uiState.role.isBlank() && uiState.company.isBlank()) return
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        Text(
            text = stringResource(R.string.feature_onboarding_impl_paste_jd_spotted),
            style = HhTheme.typography.labelL.copy(fontWeight = FontWeight.Bold),
            color = HhTheme.colors.onSurface,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs + HhTheme.spacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (uiState.role.isNotBlank()) {
                SpottedChip(label = uiState.role)
            }
            if (uiState.company.isNotBlank()) {
                SpottedChip(label = uiState.company)
            }
            HhIconButton(
                icon = HhIcons.Edit,
                contentDescription = stringResource(R.string.feature_onboarding_impl_paste_jd_edit_description),
                onClick = onEdit,
                tint = HhTheme.colors.brand,
                containerColor = Color.Transparent,
                borderColor = Color.Transparent,
            )
        }
    }
}

@Composable
private fun SpottedChip(label: String) {
    Box(
        modifier = Modifier
            .clip(HhTheme.shapes.pill)
            .background(HhTheme.colors.card)
            .border(PASTE_FIELD_BORDER / 2, HhTheme.colors.outlineVariant, HhTheme.shapes.pill)
            .padding(horizontal = PASTE_CHIP_PADDING, vertical = HhTheme.spacing.xs + HhTheme.spacing.xxs),
    ) {
        Text(
            text = label,
            style = HhTheme.typography.labelL.copy(fontWeight = FontWeight.Bold),
            color = HhTheme.colors.onSurface,
        )
    }
}

@Composable
private fun RoleAndCompanyFields(
    uiState: PasteJobDescriptionUiState,
    actions: PasteJobDescriptionActions,
) {
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

@Composable
private fun TooShortHelp() {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = HhIcons.Error,
                contentDescription = null,
                tint = HhTheme.colors.coral,
                modifier = Modifier
                    .padding(top = HhTheme.spacing.xxs)
                    .size(PASTE_ERROR_ICON),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs),
            ) {
                Text(
                    text = stringResource(R.string.feature_onboarding_impl_paste_jd_short_title),
                    style = HhTheme.typography.titleS.copy(fontWeight = FontWeight.Bold),
                    color = HhTheme.colors.coral,
                )
                Text(
                    text = stringResource(R.string.feature_onboarding_impl_paste_jd_short_body),
                    style = HhTheme.typography.bodyM,
                    color = HhTheme.colors.onSurfaceVariant,
                )
            }
        }
        HelpCard()
    }
}

@Composable
private fun HelpCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HhTheme.shapes.card)
            .background(HhTheme.colors.card)
            .border(PASTE_FIELD_BORDER / 2, HhTheme.colors.outlineVariant, HhTheme.shapes.card)
            .padding(PASTE_CHIP_PADDING + HhTheme.spacing.d2),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        verticalAlignment = Alignment.Bottom,
    ) {
        HhCharacterIllustration(
            illustration = HhIllustration.Empty,
            contentDescription = null,
            modifier = Modifier.size(width = PASTE_ILLUSTRATION_WIDTH, height = PASTE_ILLUSTRATION_HEIGHT),
        )
        Column(
            modifier = Modifier.weight(1f).padding(bottom = PASTE_CHIP_PADDING),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        ) {
            Text(
                text = stringResource(R.string.feature_onboarding_impl_paste_jd_help_title),
                style = HhTheme.typography.titleS.copy(fontWeight = FontWeight.Bold),
                color = HhTheme.colors.onSurface,
            )
            Text(
                text = stringResource(R.string.feature_onboarding_impl_paste_jd_help_body),
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.onSurfaceVariant,
            )
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
        stringResource(R.string.feature_onboarding_impl_paste_jd_action_check)
    }
    HhBottomActionBar {
        HhIconActionBar(
            secondaryIcon = HhIcons.Close,
            secondaryContentDescription = stringResource(R.string.feature_onboarding_impl_paste_jd_action_clear),
            onSecondaryClick = if (uiState.canClear) actions.onClear else actions.onBack,
            primaryLabel = label,
            onPrimaryClick = actions.onAnalyse,
            primaryEnabled = uiState.canAnalyse,
            primaryTrailingIcon = HhIcons.ArrowForward,
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
        AnnotatedString(
            pluralStringResource(
                R.plurals.feature_onboarding_impl_paste_jd_disclosure_limit,
                FREE_ANALYSES_PER_DAY,
                FREE_ANALYSES_PER_DAY,
            ),
        )
    } else {
        val count = stringResource(
            R.string.feature_onboarding_impl_paste_jd_free_left,
            uiState.freeAnalysesLeft,
            FREE_ANALYSES_PER_DAY,
        )
        val full = stringResource(R.string.feature_onboarding_impl_paste_jd_disclosure, count)
        androidx.compose.ui.text.buildAnnotatedString {
            append(full)
            addStyle(
                androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold),
                0,
                count.length,
            )
        }
    }
    DisclosureCard(text = text, icon = HhIcons.Lock)
}
