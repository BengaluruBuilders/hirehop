package com.hirehop.feature.onboarding.impl.consent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhCheckbox
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhStatusChip
import com.hirehop.core.designsystem.component.HhStatusKind
import com.hirehop.core.designsystem.component.HhTextButton
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.ConsentPurpose
import com.hirehop.feature.onboarding.impl.R
import com.hirehop.feature.onboarding.impl.common.MessageCard
import com.hirehop.feature.onboarding.impl.common.NoticeTone
import com.hirehop.feature.onboarding.impl.common.OnboardingNotice
import com.hirehop.feature.onboarding.impl.common.OnboardingStepBar
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val TOP_PADDING = 36.dp
private val LEADING_DISC_SIZE = 36.dp
private val LEADING_ICON_SIZE = 18.dp
private val CARD_ROW_GAP = 10.dp
private val CARD_TOP_PADDING = 8.dp
private val CARD_BOTTOM_PADDING = 10.dp
private val STATUS_WORD_GAP = 6.dp
private val DASH = 6f
private val STROKE = 1.5f
private val DATE_PATTERN = "d MMM yyyy"
private const val CONSENT_STEP = 3

private val FRAME_PURPOSES = listOf(
    ConsentPurpose.READ_AND_BUILD,
    ConsentPurpose.KEEP_CONFIRMED_FACTS,
    ConsentPurpose.ANALYSE_ON_DEVICE,
)

@Composable
internal fun ConsentScreen(
    uiState: ConsentUiState,
    actions: ConsentActions,
    modifier: Modifier = Modifier,
) {
    HhScreen(
        modifier = modifier,
        sheet = false,
        header = {
            OnboardingStepBar(
                modifier = Modifier.padding(horizontal = HhTheme.spacing.gutter),
                step = CONSENT_STEP,
                onBack = actions.onBack,
                backContentDescription = stringResource(
                    R.string.feature_onboarding_impl_consent_navigation_back_content_description,
                ),
            )
        },
        bottomBar = if (uiState.isReadOnly) null else ({ ConsentBottomBar(uiState = uiState, actions = actions) }),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = HhTheme.spacing.gutter)
                .padding(top = TOP_PADDING),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        ) {
            ConsentHeading()
            if (uiState.isDeclined) {
                MessageCard(
                    title = stringResource(R.string.feature_onboarding_impl_consent_declined_title),
                    body = stringResource(R.string.feature_onboarding_impl_consent_declined_body),
                )
            } else {
                ConsentBody(uiState = uiState, actions = actions)
            }
        }
    }
}

@Composable
private fun ConsentHeading() {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        Text(
            text = stringResource(R.string.feature_onboarding_impl_consent_heading),
            style = HhTheme.typography.headlineL,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_consent_intro),
            style = HhTheme.typography.bodyM,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun ConsentBody(
    uiState: ConsentUiState,
    actions: ConsentActions,
) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
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
    HhCard(
        contentPadding = PaddingValues(
            start = HhTheme.spacing.cardPadding,
            top = CARD_TOP_PADDING,
            end = HhTheme.spacing.cardPadding,
            bottom = CARD_BOTTOM_PADDING,
        ),
    ) {
        PurposeTitleRow(copy = copy, checked = checked, readOnly = readOnly, onToggle = onToggle)
        Text(
            text = stringResource(copy.body),
            style = HhTheme.typography.bodyS,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun PurposeTitleRow(
    copy: PurposeCopy,
    checked: Boolean,
    readOnly: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CARD_ROW_GAP),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(LEADING_DISC_SIZE)
                .clip(HhTheme.shapes.pill)
                .background(HhTheme.colors.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = copy.icon,
                contentDescription = null,
                tint = HhTheme.colors.onPrimaryContainer,
                modifier = Modifier.size(LEADING_ICON_SIZE),
            )
        }
        Text(
            text = stringResource(copy.title),
            modifier = Modifier.weight(1f),
            style = HhTheme.typography.titleM,
            color = HhTheme.colors.onSurface,
        )
        PurposeControl(checked = checked, readOnly = readOnly, onToggle = onToggle)
    }
}

@Composable
private fun PurposeControl(
    checked: Boolean,
    readOnly: Boolean,
    onToggle: () -> Unit,
) {
    val word = stringResource(
        if (checked) {
            R.string.feature_onboarding_impl_consent_row_ticked
        } else {
            R.string.feature_onboarding_impl_consent_row_not_ticked
        },
    )
    if (readOnly) {
        HhStatusChip(
            kind = if (checked) HhStatusKind.Met else HhStatusKind.Partial,
            label = word,
        )
        return
    }
    Row(
        modifier = Modifier
            .defaultMinSize(minHeight = HhTheme.spacing.touch)
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = { onToggle() }),
        horizontalArrangement = Arrangement.spacedBy(STATUS_WORD_GAP),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = word,
            style = HhTheme.typography.labelM.copy(fontWeight = FontWeight.Bold),
            color = if (checked) HhTheme.colors.primary else HhTheme.colors.onSurfaceVariant,
        )
        HhCheckbox(
            checked = checked,
            onCheckedChange = { onToggle() },
            modifier = Modifier.clearAndSetSemantics {},
        )
    }
}

@Composable
private fun CommitmentNotes() {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        CommitmentNote(
            icon = HhIcons.Block,
            text = stringResource(R.string.feature_onboarding_impl_consent_commitment_never_asks),
        )
        CommitmentNote(
            icon = HhIcons.Edit,
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
            tint = HhTheme.colors.primary,
            modifier = Modifier.padding(top = HhTheme.spacing.xxs).size(LEADING_ICON_SIZE),
        )
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = HhTheme.typography.bodyS,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun ReadOnlyFacts(uiState: ConsentUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        val agreedAt = uiState.agreedAt
        if (agreedAt != null) {
            OnboardingNotice(
                text = stringResource(R.string.feature_onboarding_impl_consent_agreed_on, formatDate(agreedAt)),
                icon = HhIcons.CheckCircle,
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
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = HhTheme.typography.labelL, color = HhTheme.colors.onSurface)
        PendingChip(value = value)
    }
}

@Composable
private fun PendingChip(value: String) {
    val outline = HhTheme.colors.outline
    val corner = HhTheme.spacing.xs
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
                horizontal = HhTheme.spacing.d12 - HhTheme.spacing.xxs,
                vertical = HhTheme.spacing.sm - HhTheme.spacing.xxs,
            ),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs),
    ) {
        Text(text = value, style = HhTheme.typography.factId, color = HhTheme.colors.onSurfaceVariant)
        Text(
            text = stringResource(R.string.feature_onboarding_impl_consent_pending_tag),
            style = HhTheme.typography.labelM,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun ConsentBottomBar(
    uiState: ConsentUiState,
    actions: ConsentActions,
) {
    if (uiState.isDeclined) {
        HhBottomActionBar {
            HhOutlineButton(
                label = stringResource(R.string.feature_onboarding_impl_consent_declined_action_read_again),
                onClick = actions.onReadAgain,
                modifier = Modifier.weight(1f),
            )
        }
        return
    }
    HhBottomActionBar {
        HhTextButton(
            label = stringResource(R.string.feature_onboarding_impl_consent_action_not_now),
            onClick = actions.onNotNow,
            modifier = Modifier.weight(1f),
        )
        HhPrimaryButton(
            label = agreeLabel(uiState),
            onClick = actions.onAgree,
            enabled = uiState.canAgree,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun agreeLabel(uiState: ConsentUiState): String = when {
    uiState.isEveryPurposeAcknowledged -> stringResource(R.string.feature_onboarding_impl_consent_action_agree)
    else -> stringResource(
        R.string.feature_onboarding_impl_consent_waiting,
        uiState.entries.size - uiState.acknowledgedCount,
    )
}

private class PurposeCopy(
    val icon: ImageVector,
    val title: Int,
    val body: Int,
)

private fun purposeCopy(purpose: ConsentPurpose): PurposeCopy = when (purpose) {
    ConsentPurpose.READ_AND_BUILD -> PurposeCopy(
        icon = HhIcons.Description,
        title = R.string.feature_onboarding_impl_consent_read_title,
        body = R.string.feature_onboarding_impl_consent_read_body,
    )

    ConsentPurpose.KEEP_CONFIRMED_FACTS -> PurposeCopy(
        icon = HhIcons.Verified,
        title = R.string.feature_onboarding_impl_consent_keep_title,
        body = R.string.feature_onboarding_impl_consent_keep_body,
    )

    ConsentPurpose.ANALYSE_ON_DEVICE -> PurposeCopy(
        icon = HhIcons.Link,
        title = R.string.feature_onboarding_impl_consent_match_title,
        body = R.string.feature_onboarding_impl_consent_match_body,
    )
}

private fun formatDate(instant: kotlin.time.Instant): String =
    DateTimeFormatter.ofPattern(DATE_PATTERN, Locale.getDefault())
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(instant.toEpochMilliseconds()))
