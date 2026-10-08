package com.tailormyresume.feature.onboarding.impl.consent

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.component.TmrBottomActionBar
import com.tailormyresume.core.designsystem.component.TmrCard
import com.tailormyresume.core.designsystem.component.TmrCheckbox
import com.tailormyresume.core.designsystem.component.TmrDivider
import com.tailormyresume.core.designsystem.component.TmrHeadline
import com.tailormyresume.core.designsystem.component.TmrPrimaryButton
import com.tailormyresume.core.designsystem.component.TmrScreen
import com.tailormyresume.core.designsystem.component.TmrSectionLabel
import com.tailormyresume.core.designsystem.component.TmrTextButton
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.ConsentPurpose
import com.tailormyresume.feature.onboarding.impl.R
import com.tailormyresume.feature.onboarding.impl.common.NoticeTone
import com.tailormyresume.feature.onboarding.impl.common.OnboardingNotice
import com.tailormyresume.feature.onboarding.impl.common.OnboardingStepBar
import com.tailormyresume.feature.onboarding.impl.common.StateCard
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val LEADING_DISC_SIZE = 44.dp
private val LEADING_ICON_SIZE = 22.dp
private val CARD_ROW_GAP = 10.dp
private val STATUS_WORD_GAP = 6.dp
private val DASH = 6f
private val STROKE = 1.5f
private val DATE_PATTERN = "d MMM yyyy"

private val FRAME_PURPOSES = listOf(
    ConsentPurpose.READ_AND_BUILD,
    ConsentPurpose.KEEP_CONFIRMED_FACTS,
    ConsentPurpose.AI_PROCESSING,
    ConsentPurpose.AGE_18_PLUS,
)

@Composable
internal fun ConsentScreen(
    uiState: ConsentUiState,
    actions: ConsentActions,
    modifier: Modifier = Modifier,
) {
    TmrScreen(
        modifier = modifier,
        sheet = false,
        lightTop = true,
        header = {
            OnboardingStepBar(
                modifier = Modifier.statusBarsPadding().padding(horizontal = TmrTheme.spacing.gutter),
                onBack = actions.onBack,
                backContentDescription = stringResource(
                    R.string.feature_onboarding_impl_consent_navigation_back_content_description,
                ),
                title = if (uiState.isReadOnly) stringResource(R.string.feature_onboarding_impl_consent_title) else null,
            )
        },
        bottomBar = if (uiState.isReadOnly) null else ({ ConsentBottomBar(uiState = uiState, actions = actions) }),
        bottomBarNotice = if (uiState.showAgreementActions) ({ ConsentCounter(uiState = uiState) }) else null,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = TmrTheme.spacing.gutter),
            verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
        ) {
            if (uiState.isDeclined) {
                StateCard(
                    icon = TmrIcons.Lock,
                    title = stringResource(R.string.feature_onboarding_impl_consent_declined_title),
                    body = stringResource(R.string.feature_onboarding_impl_consent_declined_body),
                )
            } else {
                if (!uiState.isReadOnly) ConsentHeading()
                ConsentBody(uiState = uiState, actions = actions)
            }
        }
    }
}

@Composable
private fun ConsentCounter(uiState: ConsentUiState) {
    Text(
        text = pluralStringResource(
            R.plurals.feature_onboarding_impl_consent_understood_count,
            uiState.acknowledgedCount,
            uiState.acknowledgedCount,
            uiState.entries.size,
        ),
        modifier = Modifier.fillMaxWidth().padding(horizontal = TmrTheme.spacing.gutter),
        style = TmrTheme.typography.bodyM.copy(fontWeight = FontWeight.Bold),
        color = TmrTheme.colors.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun ConsentHeading() {
    Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm)) {
        TmrHeadline(
            text = stringResource(R.string.feature_onboarding_impl_consent_heading),
            style = TmrTheme.typography.headlineL,
            color = TmrTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_consent_intro),
            style = TmrTheme.typography.bodyL.copy(fontWeight = FontWeight.SemiBold),
            color = TmrTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun ConsentBody(
    uiState: ConsentUiState,
    actions: ConsentActions,
) {
    Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md)) {
        if (uiState.uploadFailed) {
            Text(
                text = stringResource(R.string.feature_onboarding_impl_consent_upload_failed),
                style = TmrTheme.typography.bodyM.copy(fontWeight = FontWeight.Bold),
                color = TmrTheme.colors.error,
            )
        }
        FRAME_PURPOSES.forEach { purpose ->
            PurposeCard(
                purpose = purpose,
                checked = uiState.isAcknowledged(purpose),
                readOnly = uiState.isReadOnly,
                onToggle = { actions.onPurposeToggle(purpose) },
            )
        }
        CommitmentNotes()
        if (uiState.isReadOnly) {
            ReadOnlyFacts(uiState = uiState)
        }
    }
}

@Composable
private fun PurposeCard(
    purpose: ConsentPurpose,
    checked: Boolean,
    readOnly: Boolean,
    onToggle: () -> Unit,
) {
    val copy = purposeCopy(purpose)
    TmrCard {
        PurposeTitleRow(copy = copy)
        PurposeLine(label = R.string.feature_onboarding_impl_consent_we_do, text = copy.body)
        PurposeLine(label = R.string.feature_onboarding_impl_consent_we_keep, text = copy.keep)
        TmrDivider()
        PurposeControl(checked = checked, readOnly = readOnly, onToggle = onToggle)
    }
}

@Composable
private fun PurposeTitleRow(copy: PurposeCopy) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CARD_ROW_GAP),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(LEADING_DISC_SIZE)
                .clip(TmrTheme.shapes.tag)
                .background(TmrTheme.colors.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = copy.icon,
                contentDescription = null,
                tint = TmrTheme.colors.onSurface,
                modifier = Modifier.size(LEADING_ICON_SIZE),
            )
        }
        Text(
            text = stringResource(copy.title),
            modifier = Modifier.weight(1f),
            style = TmrTheme.typography.titleM,
            color = TmrTheme.colors.onSurface,
        )
    }
}

@Composable
private fun PurposeLine(@StringRes label: Int, @StringRes text: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xxs)) {
        TmrSectionLabel(text = stringResource(label))
        Text(text = stringResource(text), style = TmrTheme.typography.bodyM, color = TmrTheme.colors.onSurface)
    }
}

@Composable
private fun PurposeControl(
    checked: Boolean,
    readOnly: Boolean,
    onToggle: () -> Unit,
) {
    if (readOnly) {
        Row(
            modifier = Modifier.defaultMinSize(minHeight = TmrTheme.spacing.touch),
            horizontalArrangement = Arrangement.spacedBy(STATUS_WORD_GAP),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (checked) {
                Icon(
                    imageVector = TmrIcons.Check,
                    contentDescription = null,
                    tint = TmrTheme.colors.primary,
                    modifier = Modifier.size(LEADING_ICON_SIZE),
                )
            }
            Text(
                text = stringResource(
                    if (checked) {
                        R.string.feature_onboarding_impl_consent_row_ticked
                    } else {
                        R.string.feature_onboarding_impl_consent_row_not_ticked
                    },
                ),
                style = TmrTheme.typography.titleS.copy(fontWeight = FontWeight.ExtraBold),
                color = if (checked) TmrTheme.colors.primary else TmrTheme.colors.onSurfaceVariant,
            )
        }
        return
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = TmrTheme.spacing.touch)
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = { onToggle() }),
        horizontalArrangement = Arrangement.spacedBy(STATUS_WORD_GAP),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TmrCheckbox(
            checked = checked,
            onCheckedChange = { onToggle() },
            modifier = Modifier.clearAndSetSemantics {},
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_consent_row_label),
            style = TmrTheme.typography.titleS,
            color = TmrTheme.colors.onSurface,
        )
    }
}

@Composable
private fun CommitmentNotes() {
    Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm)) {
        CommitmentNote(
            icon = TmrIcons.Block,
            text = stringResource(R.string.feature_onboarding_impl_consent_commitment_never_asks),
        )
        CommitmentNote(
            icon = TmrIcons.Edit,
            text = stringResource(R.string.feature_onboarding_impl_consent_commitment_your_data),
        )
    }
}

@Composable
private fun CommitmentNote(
    icon: ImageVector,
    text: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(STATUS_WORD_GAP),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TmrTheme.colors.primary,
            modifier = Modifier.padding(top = TmrTheme.spacing.xxs).size(LEADING_ICON_SIZE),
        )
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = TmrTheme.typography.bodyS,
            color = TmrTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun ReadOnlyFacts(uiState: ConsentUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm)) {
        val agreedAt = uiState.agreedAt
        if (agreedAt != null) {
            OnboardingNotice(
                text = stringResource(R.string.feature_onboarding_impl_consent_agreed_on, formatDate(agreedAt)),
                icon = TmrIcons.CheckCircle,
                tone = NoticeTone.Success,
            )
        }
        PendingFactRow(
            label = stringResource(R.string.feature_onboarding_impl_consent_policy_label),
            value = stringResource(R.string.feature_onboarding_impl_consent_policy_value),
        )
        PendingFactRow(
            label = stringResource(R.string.feature_onboarding_impl_consent_grievance_label),
            value = stringResource(R.string.feature_onboarding_impl_consent_grievance_value),
        )
    }
}

@Composable
private fun PendingFactRow(
    label: String,
    value: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = TmrTheme.typography.labelL, color = TmrTheme.colors.onSurface)
        PendingChip(value = value)
    }
}

@Composable
private fun PendingChip(value: String) {
    val outline = TmrTheme.colors.outline
    val corner = TmrTheme.spacing.xs
    Column(
        modifier = Modifier
            .drawBehind {
                drawRoundRect(
                    color = outline,
                    cornerRadius = CornerRadius(corner.toPx()),
                    style = Stroke(
                        width = STROKE.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(DASH.dp.toPx(), DASH.dp.toPx())),
                    ),
                )
            }
            .padding(
                horizontal = TmrTheme.spacing.d12 - TmrTheme.spacing.xxs,
                vertical = TmrTheme.spacing.sm - TmrTheme.spacing.xxs,
            ),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xxs),
    ) {
        Text(text = value, style = TmrTheme.typography.factId, color = TmrTheme.colors.onSurfaceVariant)
        Text(
            text = stringResource(R.string.feature_onboarding_impl_consent_pending_tag),
            style = TmrTheme.typography.labelM,
            color = TmrTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun ConsentBottomBar(
    uiState: ConsentUiState,
    actions: ConsentActions,
) {
    TmrBottomActionBar {
        if (uiState.isDeclined) {
            TmrPrimaryButton(
                label = stringResource(R.string.feature_onboarding_impl_consent_declined_action_read_again),
                onClick = actions.onReadAgain,
                modifier = Modifier.weight(1f),
            )
        } else {
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                TmrPrimaryButton(
                    label = stringResource(R.string.feature_onboarding_impl_consent_action_agree),
                    onClick = actions.onAgree,
                    enabled = uiState.canAgree,
                    modifier = Modifier.fillMaxWidth(),
                )
                TmrTextButton(
                    label = stringResource(R.string.feature_onboarding_impl_consent_action_not_now),
                    onClick = actions.onNotNow,
                )
            }
        }
    }
}

private class PurposeCopy(
    val icon: ImageVector,
    val title: Int,
    val body: Int,
    val keep: Int,
)

private fun purposeCopy(purpose: ConsentPurpose): PurposeCopy = when (purpose) {
    ConsentPurpose.READ_AND_BUILD -> PurposeCopy(
        icon = TmrIcons.Description,
        title = R.string.feature_onboarding_impl_consent_read_title,
        body = R.string.feature_onboarding_impl_consent_read_body,
        keep = R.string.feature_onboarding_impl_consent_read_keep,
    )

    ConsentPurpose.KEEP_CONFIRMED_FACTS -> PurposeCopy(
        icon = TmrIcons.Verified,
        title = R.string.feature_onboarding_impl_consent_keep_title,
        body = R.string.feature_onboarding_impl_consent_keep_body,
        keep = R.string.feature_onboarding_impl_consent_keep_keep,
    )

    ConsentPurpose.AI_PROCESSING -> PurposeCopy(
        icon = TmrIcons.Link,
        title = R.string.feature_onboarding_impl_consent_ai_title,
        body = R.string.feature_onboarding_impl_consent_ai_body,
        keep = R.string.feature_onboarding_impl_consent_ai_keep,
    )

    ConsentPurpose.AGE_18_PLUS -> PurposeCopy(
        icon = TmrIcons.Lock,
        title = R.string.feature_onboarding_impl_consent_age_title,
        body = R.string.feature_onboarding_impl_consent_age_body,
        keep = R.string.feature_onboarding_impl_consent_age_keep,
    )
}

private fun formatDate(instant: kotlin.time.Instant): String =
    DateTimeFormatter.ofPattern(DATE_PATTERN, Locale.getDefault())
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(instant.toEpochMilliseconds()))
