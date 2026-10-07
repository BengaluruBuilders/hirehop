package com.hirehop.feature.onboarding.impl.consent

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
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhCheckbox
import com.hirehop.core.designsystem.component.HhDivider
import com.hirehop.core.designsystem.component.HhHeadline
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhSectionLabel
import com.hirehop.core.designsystem.component.HhTextButton
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.ConsentPurpose
import com.hirehop.feature.onboarding.impl.R
import com.hirehop.feature.onboarding.impl.common.NoticeTone
import com.hirehop.feature.onboarding.impl.common.OnboardingNotice
import com.hirehop.feature.onboarding.impl.common.OnboardingStepBar
import com.hirehop.feature.onboarding.impl.common.StateCard
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
        lightTop = true,
        header = {
            OnboardingStepBar(
                modifier = Modifier.statusBarsPadding().padding(horizontal = HhTheme.spacing.gutter),
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
                .padding(horizontal = HhTheme.spacing.gutter),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        ) {
            if (uiState.isDeclined) {
                StateCard(
                    icon = HhIcons.Lock,
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
        modifier = Modifier.fillMaxWidth().padding(horizontal = HhTheme.spacing.gutter),
        style = HhTheme.typography.bodyM.copy(fontWeight = FontWeight.Bold),
        color = HhTheme.colors.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun ConsentHeading() {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        HhHeadline(
            text = stringResource(R.string.feature_onboarding_impl_consent_heading),
            style = HhTheme.typography.headlineL,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_consent_intro),
            style = HhTheme.typography.bodyL.copy(fontWeight = FontWeight.SemiBold),
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun ConsentBody(
    uiState: ConsentUiState,
    actions: ConsentActions,
) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
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
    HhCard {
        PurposeTitleRow(copy = copy)
        PurposeLine(label = R.string.feature_onboarding_impl_consent_we_do, text = copy.body)
        PurposeLine(label = R.string.feature_onboarding_impl_consent_we_keep, text = copy.keep)
        HhDivider()
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
                .clip(HhTheme.shapes.tag)
                .background(HhTheme.colors.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = copy.icon,
                contentDescription = null,
                tint = HhTheme.colors.onSurface,
                modifier = Modifier.size(LEADING_ICON_SIZE),
            )
        }
        Text(
            text = stringResource(copy.title),
            modifier = Modifier.weight(1f),
            style = HhTheme.typography.titleM,
            color = HhTheme.colors.onSurface,
        )
    }
}

@Composable
private fun PurposeLine(@StringRes label: Int, @StringRes text: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs)) {
        HhSectionLabel(text = stringResource(label))
        Text(text = stringResource(text), style = HhTheme.typography.bodyM, color = HhTheme.colors.onSurface)
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
            modifier = Modifier.defaultMinSize(minHeight = HhTheme.spacing.touch),
            horizontalArrangement = Arrangement.spacedBy(STATUS_WORD_GAP),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (checked) {
                Icon(
                    imageVector = HhIcons.Check,
                    contentDescription = null,
                    tint = HhTheme.colors.primary,
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
                style = HhTheme.typography.titleS.copy(fontWeight = FontWeight.ExtraBold),
                color = if (checked) HhTheme.colors.primary else HhTheme.colors.onSurfaceVariant,
            )
        }
        return
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = HhTheme.spacing.touch)
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = { onToggle() }),
        horizontalArrangement = Arrangement.spacedBy(STATUS_WORD_GAP),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HhCheckbox(
            checked = checked,
            onCheckedChange = { onToggle() },
            modifier = Modifier.clearAndSetSemantics {},
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_consent_row_label),
            style = HhTheme.typography.titleS,
            color = HhTheme.colors.onSurface,
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
    HhBottomActionBar {
        if (uiState.isDeclined) {
            HhPrimaryButton(
                label = stringResource(R.string.feature_onboarding_impl_consent_declined_action_read_again),
                onClick = actions.onReadAgain,
                modifier = Modifier.weight(1f),
            )
        } else {
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                HhPrimaryButton(
                    label = stringResource(R.string.feature_onboarding_impl_consent_action_agree),
                    onClick = actions.onAgree,
                    enabled = uiState.canAgree,
                    modifier = Modifier.fillMaxWidth(),
                )
                HhTextButton(
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
        icon = HhIcons.Description,
        title = R.string.feature_onboarding_impl_consent_read_title,
        body = R.string.feature_onboarding_impl_consent_read_body,
        keep = R.string.feature_onboarding_impl_consent_read_keep,
    )

    ConsentPurpose.KEEP_CONFIRMED_FACTS -> PurposeCopy(
        icon = HhIcons.Verified,
        title = R.string.feature_onboarding_impl_consent_keep_title,
        body = R.string.feature_onboarding_impl_consent_keep_body,
        keep = R.string.feature_onboarding_impl_consent_keep_keep,
    )

    ConsentPurpose.ANALYSE_ON_DEVICE -> PurposeCopy(
        icon = HhIcons.Link,
        title = R.string.feature_onboarding_impl_consent_match_title,
        body = R.string.feature_onboarding_impl_consent_match_body,
        keep = R.string.feature_onboarding_impl_consent_match_keep,
    )
}

private fun formatDate(instant: kotlin.time.Instant): String =
    DateTimeFormatter.ofPattern(DATE_PATTERN, Locale.getDefault())
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(instant.toEpochMilliseconds()))
