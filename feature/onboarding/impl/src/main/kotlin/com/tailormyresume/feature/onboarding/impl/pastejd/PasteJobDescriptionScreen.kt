package com.tailormyresume.feature.onboarding.impl.pastejd

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.component.TmrBottomActionBar
import com.tailormyresume.core.designsystem.component.TmrButtonSize
import com.tailormyresume.core.designsystem.component.TmrConfirmDialog
import com.tailormyresume.core.designsystem.component.TmrErrorCallout
import com.tailormyresume.core.designsystem.component.TmrHeadline
import com.tailormyresume.core.designsystem.component.TmrIconButton
import com.tailormyresume.core.designsystem.component.TmrLoadingWheel
import com.tailormyresume.core.designsystem.component.TmrOfflineBanner
import com.tailormyresume.core.designsystem.component.TmrOutlineButton
import com.tailormyresume.core.designsystem.component.TmrPrimaryButton
import com.tailormyresume.core.designsystem.component.TmrScreen
import com.tailormyresume.core.designsystem.component.TmrSecondaryButton
import com.tailormyresume.core.designsystem.component.TmrTextButton
import com.tailormyresume.core.designsystem.component.TmrTextField
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.feature.onboarding.impl.R
import com.tailormyresume.feature.onboarding.impl.common.DisclosureCard
import com.tailormyresume.feature.onboarding.impl.common.NoticeTone
import com.tailormyresume.feature.onboarding.impl.common.OnboardingNotice
import com.tailormyresume.feature.onboarding.impl.common.OnboardingStepBar
import com.tailormyresume.feature.onboarding.impl.common.ReasonText
import com.tailormyresume.feature.onboarding.impl.common.rememberIsStacked
import com.tailormyresume.feature.onboarding.impl.common.stackedHyphens

private val PASTE_FIELD_HEIGHT_EMPTY = 300.dp
private val PASTE_FIELD_HEIGHT_PASTED = 230.dp
private val PASTE_FIELD_CORNER = 22.dp
private val PASTE_FIELD_BORDER = 2.dp
private val PASTE_FIELD_PADDING = 18.dp
private val PASTE_FOOTER_RESERVE = 82.dp
private val PASTE_ERROR_ICON = 22.dp
private val PASTE_CHIP_HEIGHT = 34.dp
private val PASTE_CHIP_PADDING = 12.dp
private val SOURCE_CHIP_ICON = 16.dp
private val PASTE_WORD_ICON = 18.dp

@Composable
internal fun PasteJobDescriptionScreen(
    uiState: PasteJobDescriptionUiState,
    actions: PasteJobDescriptionActions,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val isDisclosureStacked = rememberIsStacked()
    var isDiscardRequested by remember { mutableStateOf(false) }
    val onBackRequest = {
        if (uiState.hasUnanalysedText) {
            isDiscardRequested = true
        } else {
            actions.onBack()
        }
    }
    val focusClearingActions = actions.copy(
        onBack = onBackRequest,
        onPaste = {
            focusManager.clearFocus()
            actions.onPaste()
        },
        onAnalyse = {
            focusManager.clearFocus()
            actions.onAnalyse()
        },
    )
    BackHandler(enabled = uiState.hasUnanalysedText) { onBackRequest() }
    TmrScreen(
        modifier = modifier,
        sheet = false,
        bottomBar = { PasteJobDescriptionBottomBar(uiState = uiState, actions = focusClearingActions) },
        bottomBarNotice = if (isDisclosureStacked && uiState.barReason() == null) {
            null
        } else {
            { PasteJobDescriptionBarNotice(uiState = uiState, isDisclosureStacked = isDisclosureStacked) }
        },
    ) { padding ->
        if (uiState.isLoading) {
            PasteJobDescriptionLoading(modifier = Modifier.padding(padding))
        } else {
            PasteJobDescriptionContent(
                uiState = uiState,
                actions = focusClearingActions,
                isDisclosureStacked = isDisclosureStacked,
                modifier = Modifier.padding(padding),
            )
        }
    }
    if (isDiscardRequested) {
        TmrConfirmDialog(
            title = stringResource(R.string.feature_onboarding_impl_paste_jd_discard_title),
            message = stringResource(R.string.feature_onboarding_impl_paste_jd_discard_message),
            confirmLabel = stringResource(R.string.feature_onboarding_impl_paste_jd_discard_confirm),
            cancelLabel = stringResource(R.string.feature_onboarding_impl_paste_jd_discard_cancel),
            onConfirm = {
                isDiscardRequested = false
                actions.onBack()
            },
            onCancel = { isDiscardRequested = false },
            destructive = true,
        )
    }
}

@Composable
private fun PasteJobDescriptionLoading(modifier: Modifier = Modifier) {
    val message = stringResource(R.string.feature_onboarding_impl_paste_jd_loading)
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(TmrTheme.spacing.gutter),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TmrLoadingWheel(contentDesc = message)
        Text(text = message, style = TmrTheme.typography.bodyM, color = TmrTheme.colors.onSurfaceVariant)
    }
}

@Composable
private fun PasteJobDescriptionContent(
    uiState: PasteJobDescriptionUiState,
    actions: PasteJobDescriptionActions,
    isDisclosureStacked: Boolean,
    modifier: Modifier = Modifier,
) {
    var isRoleAndCompanyRevealed by remember { mutableStateOf(false) }
    var focusCompanyRequested by remember { mutableStateOf(false) }
    val companyFocus = remember { FocusRequester() }
    val areFieldsShown = isRoleAndCompanyRevealed || uiState.role.isNotBlank() || uiState.company.isNotBlank()
    val contentActions = actions.copy(
        onClear = {
            isRoleAndCompanyRevealed = false
            actions.onClear()
        },
    )
    LaunchedEffect(areFieldsShown) {
        if (areFieldsShown) isRoleAndCompanyRevealed = true
    }
    LaunchedEffect(focusCompanyRequested) {
        if (focusCompanyRequested) {
            companyFocus.requestFocus()
            focusCompanyRequested = false
        }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = TmrTheme.spacing.gutter),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
    ) {
        OnboardingStepBar(
            onBack = contentActions.onBack,
            backContentDescription = stringResource(
                R.string.feature_onboarding_impl_paste_jd_navigation_back_description,
            ),
        )
        PasteJobDescriptionIntro()
        PasteJobDescriptionNotices(uiState = uiState, actions = contentActions)
        PasteJobDescriptionField(uiState = uiState, actions = contentActions)
        if (uiState.isDailyLimitReached) {
            OnboardingNotice(
                text = pluralStringResource(
                    R.plurals.feature_onboarding_impl_paste_jd_disclosure_limit,
                    FREE_ANALYSES_PER_DAY,
                    FREE_ANALYSES_PER_DAY,
                ),
                icon = TmrIcons.Error,
                tone = NoticeTone.Warning,
            )
        }
        val problem = uiState.problem
        if (problem != null) {
            OnboardingNotice(
                text = problemText(problem),
                icon = if (problem == PasteJobDescriptionProblem.TOO_SHORT) TmrIcons.Error else TmrIcons.Block,
                tone = if (problem == PasteJobDescriptionProblem.TOO_SHORT) NoticeTone.Warning else NoticeTone.Error,
            )
        } else if (uiState.text.isNotEmpty()) {
            SpottedRow(
                uiState = uiState,
                isRevealed = isRoleAndCompanyRevealed,
                onEdit = {
                    isRoleAndCompanyRevealed = true
                    focusCompanyRequested = true
                },
            )
        }
        if (areFieldsShown) {
            RoleAndCompanyFields(actions = contentActions, uiState = uiState, companyFocus = companyFocus)
        }
        if (isDisclosureStacked) {
            PasteJobDescriptionDisclosure(uiState = uiState)
        }
    }
}

@Composable
private fun PasteJobDescriptionIntro() {
    TmrHeadline(
        text = stringResource(R.string.feature_onboarding_impl_paste_jd_heading),
        style = TmrTheme.typography.headlineL.copy(hyphens = stackedHyphens()),
        color = TmrTheme.colors.onSurface,
    )
}

@Composable
private fun PasteJobDescriptionNotices(
    uiState: PasteJobDescriptionUiState,
    actions: PasteJobDescriptionActions,
) {
    TmrOfflineBanner(
        message = stringResource(R.string.feature_onboarding_impl_paste_jd_offline_message),
        visible = uiState.isOffline,
    )
    when (uiState.message) {
        PasteJobDescriptionMessage.PASTE_FAILED -> TmrErrorCallout(
            title = stringResource(R.string.feature_onboarding_impl_paste_jd_error_title),
            supportingText = stringResource(R.string.feature_onboarding_impl_paste_jd_error_body),
            actionLabel = stringResource(R.string.feature_onboarding_impl_paste_jd_try_again),
            onAction = actions.onRetry,
        )

        PasteJobDescriptionMessage.NOTHING_TO_READ -> OnboardingNotice(
            text = stringResource(R.string.feature_onboarding_impl_paste_jd_message_nothing_to_read),
            icon = TmrIcons.Block,
            tone = NoticeTone.Warning,
        )

        PasteJobDescriptionMessage.PASTE_PARTIAL -> OnboardingNotice(
            text = stringResource(R.string.feature_onboarding_impl_paste_jd_message_partial),
            icon = TmrIcons.Block,
            tone = NoticeTone.Warning,
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
    val colors = TmrTheme.colors
    val outline = when (uiState.problem) {
        PasteJobDescriptionProblem.TOO_SHORT -> colors.partial
        PasteJobDescriptionProblem.LINK_ONLY, PasteJobDescriptionProblem.TOO_LONG -> colors.error
        null -> null
    }
    val shape = RoundedCornerShape(PASTE_FIELD_CORNER)
    val fieldHeight = if (uiState.text.isEmpty()) PASTE_FIELD_HEIGHT_EMPTY else PASTE_FIELD_HEIGHT_PASTED
    if (!rememberIsStacked()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(fieldHeight)
                .clip(shape)
                .background(colors.card)
                .then(if (outline != null) Modifier.border(PASTE_FIELD_BORDER, outline, shape) else Modifier),
        ) {
            PasteJobDescriptionTextArea(
                uiState = uiState,
                onTextChange = actions.onTextChange,
                modifier = Modifier.fillMaxSize(),
                bottomPadding = PASTE_FOOTER_RESERVE,
            )
            PasteJobDescriptionInlineFooter(uiState = uiState, actions = actions)
        }
        return
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.card)
            .then(if (outline != null) Modifier.border(PASTE_FIELD_BORDER, outline, shape) else Modifier),
    ) {
        PasteJobDescriptionTextArea(
            uiState = uiState,
            onTextChange = actions.onTextChange,
            modifier = Modifier.height(fieldHeight - PASTE_FOOTER_RESERVE),
        )
        PasteJobDescriptionFieldFooter(uiState = uiState, actions = actions)
    }
}

@Composable
private fun BoxScope.PasteJobDescriptionInlineFooter(
    uiState: PasteJobDescriptionUiState,
    actions: PasteJobDescriptionActions,
) {
    Row(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .fillMaxWidth()
            .padding(start = PASTE_FIELD_PADDING, end = PASTE_FIELD_PADDING, bottom = PASTE_FIELD_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (uiState.text.isEmpty()) {
            TmrSecondaryButton(
                label = stringResource(R.string.feature_onboarding_impl_paste_jd_action_paste),
                onClick = actions.onPaste,
                size = TmrButtonSize.Compact,
                leadingIcon = TmrIcons.Description,
            )
        } else {
            PasteJobDescriptionCount(uiState = uiState)
            Spacer(modifier = Modifier.weight(1f))
            TmrOutlineButton(
                label = stringResource(R.string.feature_onboarding_impl_paste_jd_action_clear),
                onClick = actions.onClear,
                size = TmrButtonSize.Compact,
                leadingIcon = TmrIcons.Close,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PasteJobDescriptionFieldFooter(
    uiState: PasteJobDescriptionUiState,
    actions: PasteJobDescriptionActions,
) {
    Box(
        modifier = Modifier.fillMaxWidth().heightIn(min = PASTE_FOOTER_RESERVE),
        contentAlignment = Alignment.BottomStart,
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = PASTE_FIELD_PADDING, end = PASTE_FIELD_PADDING, bottom = PASTE_FIELD_PADDING),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            if (uiState.text.isEmpty()) {
                TmrSecondaryButton(
                    label = stringResource(R.string.feature_onboarding_impl_paste_jd_action_paste),
                    onClick = actions.onPaste,
                    size = TmrButtonSize.Compact,
                    leadingIcon = TmrIcons.Description,
                )
            } else {
                PasteJobDescriptionCount(uiState = uiState)
                TmrOutlineButton(
                    label = stringResource(R.string.feature_onboarding_impl_paste_jd_action_clear),
                    onClick = actions.onClear,
                    size = TmrButtonSize.Compact,
                    leadingIcon = TmrIcons.Close,
                )
            }
        }
    }
}

@Composable
private fun PasteJobDescriptionCount(uiState: PasteJobDescriptionUiState) {
    if (uiState.problem == PasteJobDescriptionProblem.LINK_ONLY) {
        Text(
            text = stringResource(R.string.feature_onboarding_impl_paste_jd_link_count),
            style = TmrTheme.typography.labelM.copy(fontWeight = FontWeight.ExtraBold),
            color = TmrTheme.colors.onSurfaceVariant,
        )
    } else {
        PasteJobDescriptionWordCount(wordCount = uiState.wordCount)
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
        style = TmrTheme.typography.labelM.copy(fontWeight = FontWeight.ExtraBold),
        color = TmrTheme.colors.onSurfaceVariant,
    )
}

@Composable
private fun PasteJobDescriptionTextArea(
    uiState: PasteJobDescriptionUiState,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 0.dp,
) {
    val label = stringResource(R.string.feature_onboarding_impl_paste_jd_field_label)
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = PASTE_FIELD_PADDING,
                end = PASTE_FIELD_PADDING,
                top = PASTE_FIELD_PADDING,
                bottom = bottomPadding,
            ),
    ) {
        val fieldMinHeight = maxHeight
        Box(modifier = Modifier.verticalScroll(rememberScrollState())) {
            BasicTextField(
                value = uiState.text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = fieldMinHeight)
                    .clearAndSetSemantics { contentDescription = label },
                textStyle = TmrTheme.typography.bodyL.copy(color = TmrTheme.colors.onSurface),
                cursorBrush = SolidColor(TmrTheme.colors.primary),
                decorationBox = { inner ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (uiState.text.isEmpty()) {
                            Text(
                                text = stringResource(R.string.feature_onboarding_impl_paste_jd_field_placeholder),
                                style = TmrTheme.typography.bodyL,
                                color = TmrTheme.colors.onSurfaceVariant,
                            )
                        }
                        inner()
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpottedRow(
    uiState: PasteJobDescriptionUiState,
    isRevealed: Boolean,
    onEdit: () -> Unit,
) {
    if (uiState.role.isBlank() && uiState.company.isBlank()) {
        if (!isRevealed) {
            TmrTextButton(
                label = stringResource(R.string.feature_onboarding_impl_paste_jd_add_labels),
                onClick = onEdit,
            )
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm)) {
            Text(
                text = stringResource(R.string.feature_onboarding_impl_paste_jd_spotted),
                style = TmrTheme.typography.labelL.copy(fontWeight = FontWeight.Bold),
                color = TmrTheme.colors.onSurface,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xs + TmrTheme.spacing.xxs),
                verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xs),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                if (uiState.role.isNotBlank()) {
                    SpottedChip(label = uiState.role)
                }
                if (uiState.company.isNotBlank()) {
                    SpottedChip(label = uiState.company)
                }
                TmrIconButton(
                    icon = TmrIcons.Edit,
                    contentDescription = stringResource(R.string.feature_onboarding_impl_paste_jd_edit_description),
                    onClick = onEdit,
                    tint = TmrTheme.colors.primary,
                    containerColor = Color.Transparent,
                    borderColor = Color.Transparent,
                )
            }
        }
    }
}

@Composable
private fun SpottedChip(label: String) {
    Box(
        modifier = Modifier
            .clip(TmrTheme.shapes.pill)
            .background(TmrTheme.colors.card)
            .border(PASTE_FIELD_BORDER / 2, TmrTheme.colors.outlineVariant, TmrTheme.shapes.pill)
            .padding(horizontal = PASTE_CHIP_PADDING, vertical = TmrTheme.spacing.xs + TmrTheme.spacing.xxs),
    ) {
        Text(
            text = label,
            style = TmrTheme.typography.labelL.copy(fontWeight = FontWeight.Bold),
            color = TmrTheme.colors.onSurface,
        )
    }
}

@Composable
private fun RoleAndCompanyFields(
    uiState: PasteJobDescriptionUiState,
    actions: PasteJobDescriptionActions,
    companyFocus: FocusRequester,
) {
    TmrTextField(
        value = uiState.company,
        onValueChange = actions.onCompanyChange,
        modifier = Modifier.focusRequester(companyFocus),
        label = stringResource(R.string.feature_onboarding_impl_paste_jd_company_label),
        placeholder = stringResource(R.string.feature_onboarding_impl_paste_jd_company_placeholder),
    )
    TmrTextField(
        value = uiState.role,
        onValueChange = actions.onRoleChange,
        label = stringResource(R.string.feature_onboarding_impl_paste_jd_role_label),
        placeholder = stringResource(R.string.feature_onboarding_impl_paste_jd_role_placeholder),
    )
}

@Composable
private fun SharedSourceChip() {
    Row(
        modifier = Modifier
            .clip(TmrTheme.shapes.pill)
            .background(TmrTheme.colors.primaryContainer)
            .padding(horizontal = TmrTheme.spacing.md, vertical = TmrTheme.spacing.xs + TmrTheme.spacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm - TmrTheme.spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = TmrIcons.Share,
            contentDescription = null,
            tint = TmrTheme.colors.onPrimaryContainer,
            modifier = Modifier.size(SOURCE_CHIP_ICON),
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_paste_jd_arrival_shared_in),
            style = TmrTheme.typography.labelM,
            color = TmrTheme.colors.onPrimaryContainer,
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
    val label = when {
        uiState.isOffline -> stringResource(R.string.feature_onboarding_impl_paste_jd_action_analyse_later)
        uiState.canAnalyse || uiState.isDailyLimitReached && uiState.text.isNotBlank() ->
            stringResource(R.string.feature_onboarding_impl_paste_jd_action_check)
        else -> stringResource(R.string.feature_onboarding_impl_paste_jd_action_analyse_off)
    }
    TmrBottomActionBar {
        TmrPrimaryButton(
            label = label,
            onClick = actions.onAnalyse,
            enabled = uiState.canAnalyse,
            trailingIcon = if (uiState.canAnalyse) TmrIcons.ArrowForward else null,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PasteJobDescriptionUiState.barReason(): String? = when {
    isDailyLimitReached || canAnalyse || isOffline -> null
    problem == PasteJobDescriptionProblem.TOO_SHORT || problem == PasteJobDescriptionProblem.TOO_LONG ->
        stringResource(R.string.feature_onboarding_impl_paste_jd_reason_incomplete)
    else -> stringResource(R.string.feature_onboarding_impl_paste_jd_reason_empty)
}

@Composable
private fun PasteJobDescriptionBarNotice(
    uiState: PasteJobDescriptionUiState,
    isDisclosureStacked: Boolean,
) {
    val reason = uiState.barReason()
    Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm)) {
        if (reason != null) {
            ReasonText(text = reason)
        }
        if (!isDisclosureStacked) {
            PasteJobDescriptionDisclosure(uiState = uiState)
        }
    }
}

@Composable
private fun PasteJobDescriptionDisclosure(uiState: PasteJobDescriptionUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm)) {
        DisclosureCard(
            text = AnnotatedString(stringResource(R.string.feature_onboarding_impl_paste_jd_disclosure)),
            icon = TmrIcons.Lock,
        )
        Text(
            text = pluralStringResource(
                R.plurals.feature_onboarding_impl_paste_jd_free_left,
                uiState.freeAnalysesLeft,
                uiState.freeAnalysesLeft,
                FREE_ANALYSES_PER_DAY,
            ),
            style = TmrTheme.typography.bodyM.copy(fontWeight = FontWeight.SemiBold),
            color = TmrTheme.colors.onSurfaceVariant,
        )
    }
}
