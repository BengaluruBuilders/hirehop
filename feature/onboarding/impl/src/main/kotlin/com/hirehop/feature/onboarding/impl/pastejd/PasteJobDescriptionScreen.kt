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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.hirehop.core.designsystem.component.HhErrorCallout
import com.hirehop.core.designsystem.component.HhHeadline
import com.hirehop.core.designsystem.component.HhIconButton
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhSecondaryButton
import com.hirehop.core.designsystem.component.HhTextField
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.onboarding.impl.R
import com.hirehop.feature.onboarding.impl.common.DisclosureCard
import com.hirehop.feature.onboarding.impl.common.NoticeTone
import com.hirehop.feature.onboarding.impl.common.OnboardingNotice
import com.hirehop.feature.onboarding.impl.common.OnboardingStepBar
import com.hirehop.feature.onboarding.impl.common.ReasonText

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
            onBack = actions.onBack,
            backContentDescription = stringResource(
                R.string.feature_onboarding_impl_paste_jd_navigation_back_description,
            ),
        )
        PasteJobDescriptionIntro()
        PasteJobDescriptionNotices(uiState = uiState, actions = actions)
        PasteJobDescriptionField(uiState = uiState, actions = actions)
        val problem = uiState.problem
        if (problem != null) {
            OnboardingNotice(
                text = problemText(problem),
                icon = if (problem == PasteJobDescriptionProblem.TOO_SHORT) HhIcons.Error else HhIcons.Block,
                tone = if (problem == PasteJobDescriptionProblem.TOO_SHORT) NoticeTone.Warning else NoticeTone.Error,
            )
        } else if (uiState.text.isNotEmpty()) {
            SpottedRow(uiState = uiState, onEdit = { revealsRoleAndCompany = true })
        }
        if (uiState.isDailyLimitReached) {
            OnboardingNotice(
                text = pluralStringResource(
                    R.plurals.feature_onboarding_impl_paste_jd_disclosure_limit,
                    FREE_ANALYSES_PER_DAY,
                    FREE_ANALYSES_PER_DAY,
                ),
                icon = HhIcons.Error,
                tone = NoticeTone.Warning,
            )
        }
        if (revealsRoleAndCompany) {
            RoleAndCompanyFields(actions = actions, uiState = uiState)
        }
    }
}

@Composable
private fun PasteJobDescriptionIntro() {
    HhHeadline(
        text = stringResource(R.string.feature_onboarding_impl_paste_jd_heading),
        style = HhTheme.typography.headlineL,
        color = HhTheme.colors.onSurface,
    )
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
            tone = NoticeTone.Warning,
        )

        PasteJobDescriptionMessage.PASTE_PARTIAL -> OnboardingNotice(
            text = stringResource(R.string.feature_onboarding_impl_paste_jd_message_partial),
            icon = HhIcons.Block,
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
    val colors = HhTheme.colors
    val outline = when (uiState.problem) {
        PasteJobDescriptionProblem.TOO_SHORT -> colors.partial
        PasteJobDescriptionProblem.LINK_ONLY, PasteJobDescriptionProblem.TOO_LONG -> colors.error
        null -> null
    }
    val shape = RoundedCornerShape(PASTE_FIELD_CORNER)
    val fieldHeight = if (uiState.text.isEmpty()) PASTE_FIELD_HEIGHT_EMPTY else PASTE_FIELD_HEIGHT_PASTED
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = fieldHeight)
            .clip(shape)
            .background(colors.card)
            .then(if (outline != null) Modifier.border(PASTE_FIELD_BORDER, outline, shape) else Modifier),
    ) {
        PasteJobDescriptionTextArea(uiState = uiState, onTextChange = actions.onTextChange)
        PasteJobDescriptionFieldFooter(uiState = uiState, actions = actions)
    }
}

@Composable
private fun BoxScope.PasteJobDescriptionFieldFooter(
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
            HhSecondaryButton(
                label = stringResource(R.string.feature_onboarding_impl_paste_jd_action_paste),
                onClick = actions.onPaste,
                size = HhButtonSize.Compact,
                leadingIcon = HhIcons.Description,
            )
        } else {
            PasteJobDescriptionCount(uiState = uiState)
            Spacer(modifier = Modifier.weight(1f))
            HhOutlineButton(
                label = stringResource(R.string.feature_onboarding_impl_paste_jd_action_clear),
                onClick = actions.onClear,
                size = HhButtonSize.Compact,
                leadingIcon = HhIcons.Close,
            )
        }
    }
}

@Composable
private fun PasteJobDescriptionCount(uiState: PasteJobDescriptionUiState) {
    if (uiState.problem == PasteJobDescriptionProblem.LINK_ONLY) {
        Text(
            text = stringResource(R.string.feature_onboarding_impl_paste_jd_link_count),
            style = HhTheme.typography.labelM.copy(fontWeight = FontWeight.ExtraBold),
            color = HhTheme.colors.onSurfaceVariant,
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
        style = HhTheme.typography.labelM.copy(fontWeight = FontWeight.ExtraBold),
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
                tint = HhTheme.colors.primary,
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
    val label = when {
        uiState.isOffline -> stringResource(R.string.feature_onboarding_impl_paste_jd_action_analyse_later)
        uiState.canAnalyse -> stringResource(R.string.feature_onboarding_impl_paste_jd_action_check)
        else -> stringResource(R.string.feature_onboarding_impl_paste_jd_action_analyse_off)
    }
    HhBottomActionBar {
        HhPrimaryButton(
            label = label,
            onClick = actions.onAnalyse,
            enabled = uiState.canAnalyse,
            trailingIcon = if (uiState.canAnalyse) HhIcons.ArrowForward else null,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PasteJobDescriptionBarNotice(uiState: PasteJobDescriptionUiState) {
    val reason = when {
        uiState.isDailyLimitReached || uiState.canAnalyse || uiState.isOffline -> null
        uiState.problem == PasteJobDescriptionProblem.TOO_SHORT ||
            uiState.problem == PasteJobDescriptionProblem.TOO_LONG ->
            stringResource(R.string.feature_onboarding_impl_paste_jd_reason_incomplete)
        else -> stringResource(R.string.feature_onboarding_impl_paste_jd_reason_empty)
    }
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        if (reason != null) {
            ReasonText(text = reason)
        }
        DisclosureCard(
            text = AnnotatedString(stringResource(R.string.feature_onboarding_impl_paste_jd_disclosure)),
            icon = HhIcons.Lock,
        )
        Text(
            text = pluralStringResource(
                R.plurals.feature_onboarding_impl_paste_jd_free_left,
                uiState.freeAnalysesLeft,
                uiState.freeAnalysesLeft,
                FREE_ANALYSES_PER_DAY,
            ),
            style = HhTheme.typography.bodyM.copy(fontWeight = FontWeight.SemiBold),
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}
