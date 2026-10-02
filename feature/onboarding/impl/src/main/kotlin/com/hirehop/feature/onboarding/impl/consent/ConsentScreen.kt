package com.hirehop.feature.onboarding.impl.consent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhCheckbox
import com.hirehop.core.designsystem.component.HhDivider
import com.hirehop.core.designsystem.component.HhHeroCard
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.ConsentPurpose
import com.hirehop.feature.onboarding.impl.R
import com.hirehop.feature.onboarding.impl.common.MessageCard
import com.hirehop.feature.onboarding.impl.common.NoticeTone
import com.hirehop.feature.onboarding.impl.common.OnboardingNotice
import com.hirehop.feature.onboarding.impl.common.ReasonText
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val LABEL_WIDTH = 64.dp
private val COMMITMENT_ICON = 18.dp
private val DASH = 6f
private val STROKE = 1.5f
private val DATE_PATTERN = "d MMM yyyy"

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
            HhInnerHeader(
                title = stringResource(R.string.feature_onboarding_impl_consent_title),
                subtitle = stringResource(
                    if (uiState.isReadOnly) {
                        R.string.feature_onboarding_impl_consent_subtitle_read_only
                    } else {
                        R.string.feature_onboarding_impl_consent_subtitle
                    },
                ),
                onBack = actions.onBack,
                backContentDescription = stringResource(
                    R.string.feature_onboarding_impl_consent_navigation_back_content_description,
                ),
            )
        },
        bottomBar = if (uiState.isReadOnly) null else ({ ConsentBottomBar(uiState = uiState, actions = actions) }),
        bottomBarNotice = if (uiState.isReadOnly || uiState.isDeclined || uiState.isEveryPurposeAcknowledged) {
            null
        } else {
            ({ ConsentReason(uiState = uiState) })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = HhTheme.spacing.gutter)
                .padding(top = HhTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        ) {
            if (uiState.isDeclined) {
                MessageCard(
                    title = stringResource(R.string.feature_onboarding_impl_consent_declined_title),
                    body = stringResource(R.string.feature_onboarding_impl_consent_declined_body),
                )
            } else {
                ConsentNoticeCard(uiState = uiState, actions = actions)
            }
        }
    }
}

@Composable
private fun ConsentNoticeCard(
    uiState: ConsentUiState,
    actions: ConsentActions,
) {
    HhHeroCard(contentPadding = PaddingValues(HhTheme.spacing.gutter)) {
        Column {
            val agreedAt = uiState.agreedAt
            if (uiState.isReadOnly && agreedAt != null) {
                OnboardingNotice(
                    text = stringResource(R.string.feature_onboarding_impl_consent_agreed_on, formatDate(agreedAt)),
                    icon = HhIcons.CheckCircle,
                    tone = NoticeTone.Success,
                )
            }
            uiState.entries.forEach { entry ->
                PurposeBlock(
                    entry = entry,
                    enabled = !uiState.isReadOnly,
                    onToggle = { actions.onPurposeToggle(entry.purpose) },
                )
                HhDivider()
            }
            CommitmentRow(
                icon = HhIcons.Block,
                text = stringResource(R.string.feature_onboarding_impl_consent_commitment_never_asks),
            )
            HhDivider()
            CommitmentRow(
                icon = HhIcons.Edit,
                text = stringResource(R.string.feature_onboarding_impl_consent_commitment_your_data),
            )
            HhDivider()
            Column(
                modifier = Modifier.padding(top = HhTheme.spacing.d12 - HhTheme.spacing.xxs),
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            ) {
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
    }
}

@Composable
private fun PurposeBlock(
    entry: ConsentPurposeState,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    val copy = purposeCopy(entry.purpose)
    Column(
        modifier = Modifier.padding(vertical = HhTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        LabelledLine(label = stringResource(R.string.feature_onboarding_impl_consent_we_do)) {
            Text(text = stringResource(copy.doText), style = HhTheme.typography.bodyM, color = HhTheme.colors.onSurface)
        }
        LabelledLine(label = stringResource(R.string.feature_onboarding_impl_consent_we_keep)) {
            if (copy.keepText != null) {
                Text(
                    text = stringResource(copy.keepText),
                    style = HhTheme.typography.bodyM,
                    color = HhTheme.colors.onSurface,
                )
            } else {
                PendingChip(value = stringResource(R.string.feature_onboarding_impl_consent_purpose_analyse_keep_pending))
            }
        }
        AcknowledgeRow(checked = entry.isAcknowledged, enabled = enabled, onToggle = onToggle)
    }
}

@Composable
private fun LabelledLine(
    label: String,
    content: @Composable () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        Text(
            text = label,
            modifier = Modifier.width(LABEL_WIDTH),
            style = HhTheme.typography.labelM,
            color = HhTheme.colors.onSurfaceVariant,
        )
        Column(modifier = Modifier.weight(1f)) { content() }
    }
}

@Composable
private fun AcknowledgeRow(
    checked: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .offset(x = -HhTheme.spacing.md)
            .defaultMinSize(minHeight = HhTheme.spacing.touch)
            .toggleable(value = checked, enabled = enabled, role = Role.Checkbox, onValueChange = { onToggle() }),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HhCheckbox(
            checked = checked,
            onCheckedChange = { onToggle() },
            enabled = enabled,
            modifier = Modifier.clearAndSetSemantics {},
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_consent_understand),
            style = HhTheme.typography.bodyM,
            color = HhTheme.colors.onSurface,
        )
    }
}

@Composable
private fun CommitmentRow(
    icon: ImageVector,
    text: String,
) {
    Row(
        modifier = Modifier.padding(vertical = HhTheme.spacing.d12 - HhTheme.spacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.d12 - HhTheme.spacing.xxs),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = HhTheme.colors.primary,
            modifier = Modifier.padding(top = HhTheme.spacing.xxs).size(COMMITMENT_ICON),
        )
        Text(text = text, style = HhTheme.typography.bodyM, color = HhTheme.colors.onSurface, modifier = Modifier.weight(1f))
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
            .padding(horizontal = HhTheme.spacing.d12 - HhTheme.spacing.xxs, vertical = HhTheme.spacing.sm - HhTheme.spacing.xxs),
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
        HhOutlineButton(
            label = stringResource(R.string.feature_onboarding_impl_consent_action_not_now),
            onClick = actions.onNotNow,
            modifier = Modifier.weight(1f),
        )
        HhPrimaryButton(
            label = stringResource(R.string.feature_onboarding_impl_consent_action_agree),
            onClick = actions.onAgree,
            enabled = uiState.canAgree,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ConsentReason(uiState: ConsentUiState) {
    val count = stringResource(
        R.string.feature_onboarding_impl_consent_reason_count,
        uiState.acknowledgedCount,
        uiState.entries.size,
    )
    val full = stringResource(
        R.string.feature_onboarding_impl_consent_reason_incomplete,
        uiState.entries.size,
        count,
    )
    val start = full.indexOf(count)
    ReasonText(
        text = full,
        emphasis = if (start >= 0) start until start + count.length else null,
    )
}

private class PurposeCopy(val doText: Int, val keepText: Int?)

private fun purposeCopy(purpose: ConsentPurpose): PurposeCopy = when (purpose) {
    ConsentPurpose.READ_AND_BUILD -> PurposeCopy(
        doText = R.string.feature_onboarding_impl_consent_purpose_read_do,
        keepText = R.string.feature_onboarding_impl_consent_purpose_read_keep,
    )

    ConsentPurpose.ANALYSE_ON_DEVICE -> PurposeCopy(
        doText = R.string.feature_onboarding_impl_consent_purpose_analyse_do,
        keepText = null,
    )

    ConsentPurpose.KEEP_CONFIRMED_FACTS -> PurposeCopy(
        doText = R.string.feature_onboarding_impl_consent_purpose_facts_do,
        keepText = R.string.feature_onboarding_impl_consent_purpose_facts_keep,
    )
}

private fun formatDate(instant: kotlin.time.Instant): String =
    DateTimeFormatter.ofPattern(DATE_PATTERN, Locale.getDefault())
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(instant.toEpochMilliseconds()))
