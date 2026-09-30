package com.hirehop.feature.settings.impl.deleteaccount

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhDivider
import com.hirehop.core.designsystem.component.HhErrorCallout
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.designsystem.component.HhScaffold
import com.hirehop.core.designsystem.component.HhSpotIllustration
import com.hirehop.core.designsystem.component.HhSpotKind
import com.hirehop.core.designsystem.component.HhTopAppBar
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.domain.account.AccountDeletionStep
import com.hirehop.core.ui.component.DestructiveActionButton
import com.hirehop.feature.settings.impl.R

@Composable
internal fun DeleteAccountScreen(
    uiState: DeleteAccountUiState,
    actions: DeleteAccountActions,
    modifier: Modifier = Modifier,
) {
    HhScaffold(
        modifier = modifier,
        topBar = {
            if (uiState.stage != DeleteAccountStage.DONE) {
                HhTopAppBar(
                    title = stringResource(R.string.feature_settings_impl_delete_account_screen_title),
                    navigationIcon = if (uiState.isBackEnabled) HhIcons.ArrowBack else null,
                    navigationIconContentDescription = stringResource(
                        R.string.feature_settings_impl_delete_account_back_content_description,
                    ),
                    onNavigationClick = actions.onBack,
                )
            }
        },
        bottomBar = {
            if (uiState.showsMainStage) {
                DeleteAccountBottomBar(uiState = uiState, actions = actions)
            }
        },
    ) { padding ->
        when {
            uiState.showsMainStage -> DeleteAccountMain(uiState = uiState, actions = actions, padding = padding)
            uiState.stage == DeleteAccountStage.DELETING -> DeleteAccountDeleting(uiState = uiState, padding = padding)
            else -> DeleteAccountDone(actions = actions, padding = padding)
        }
    }
}

@Composable
private fun DeleteAccountMain(
    uiState: DeleteAccountUiState,
    actions: DeleteAccountActions,
    padding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HhTheme.spacing.d16)
            .padding(top = HhTheme.spacing.d16, bottom = HhTheme.spacing.d16),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        if (uiState.isOffline) {
            HhOfflineBanner(
                message = stringResource(R.string.feature_settings_impl_delete_account_offline_banner),
            )
        }
        if (uiState.stage == DeleteAccountStage.ERROR) {
            HhErrorCallout(
                title = stringResource(R.string.feature_settings_impl_delete_account_error_title),
                supportingText = stringResource(
                    if (uiState.isDataIntact) {
                        R.string.feature_settings_impl_delete_account_error_intact
                    } else {
                        R.string.feature_settings_impl_delete_account_error_partial
                    },
                ),
            )
        }
        Text(
            text = stringResource(R.string.feature_settings_impl_delete_account_headline),
            style = HhTheme.typography.displayLarge,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_settings_impl_delete_account_deletes_lead),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
        DeleteAccountCountCard(uiState = uiState)
        Text(
            text = stringResource(R.string.feature_settings_impl_delete_account_not_undoable),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurface,
        )
        DeleteAccountDownloadLink(onClick = actions.onDownloadData)
        DeleteAccountWebSlot()
    }
}

@Composable
private fun DeleteAccountCountCard(uiState: DeleteAccountUiState) {
    val shape = RoundedCornerShape(HhTheme.shapes.md)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = HhTheme.colors.surface, shape = shape)
            .border(width = 1.dp, color = HhTheme.colors.hairline, shape = shape),
    ) {
        DeleteAccountCountRow(
            count = uiState.counts.profileFacts,
            label = pluralStringResource(
                R.plurals.feature_settings_impl_delete_account_row_facts,
                uiState.counts.profileFacts,
            ),
        )
        HhDivider()
        DeleteAccountCountRow(
            count = uiState.counts.applications,
            label = pluralStringResource(
                R.plurals.feature_settings_impl_delete_account_row_applications,
                uiState.counts.applications,
            ),
        )
        HhDivider()
        DeleteAccountCountRow(
            count = uiState.counts.unusedCredits,
            label = pluralStringResource(
                R.plurals.feature_settings_impl_delete_account_row_credits,
                uiState.counts.unusedCredits,
            ),
        )
    }
}

@Composable
private fun DeleteAccountCountRow(
    count: Int,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HhTheme.spacing.md, vertical = HhTheme.spacing.d8),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        Text(
            text = count.toString(),
            style = HhTheme.typography.displaySmall,
            color = HhTheme.colors.onSurface,
            modifier = Modifier.widthIn(min = 34.dp),
        )
        Text(
            text = label,
            style = HhTheme.typography.bodyLarge,
            color = HhTheme.colors.onSurface,
        )
    }
}

@Composable
private fun DeleteAccountDownloadLink(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = HhTheme.spacing.d48)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = stringResource(R.string.feature_settings_impl_delete_account_download_first),
            style = HhTheme.typography.titleMedium,
            color = HhTheme.colors.primary,
        )
    }
}

@Composable
private fun DeleteAccountWebSlot(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
    ) {
        Text(
            text = stringResource(R.string.feature_settings_impl_delete_account_web_lead),
            style = HhTheme.typography.labelMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .background(
                        color = HhTheme.colors.surfaceContainer,
                        shape = RoundedCornerShape(HhTheme.shapes.xs),
                    )
                    .border(
                        width = 1.dp,
                        color = HhTheme.colors.onSurfaceVariant,
                        shape = RoundedCornerShape(HhTheme.shapes.xs),
                    )
                    .padding(horizontal = HhTheme.spacing.d8, vertical = HhTheme.spacing.d2),
            ) {
                Text(
                    text = stringResource(R.string.feature_settings_impl_delete_account_web_address),
                    style = HhTheme.typography.monoSmall,
                    color = HhTheme.colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DeleteAccountDeleting(
    uiState: DeleteAccountUiState,
    padding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = HhTheme.spacing.d24, vertical = HhTheme.spacing.d16),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.d16),
    ) {
        Text(
            text = stringResource(R.string.feature_settings_impl_delete_account_deleting_title),
            style = HhTheme.typography.displayLarge,
            color = HhTheme.colors.onSurface,
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs),
        ) {
            uiState.steps.forEach { step ->
                DeleteAccountStepRow(step = step)
            }
        }
        Text(
            text = stringResource(R.string.feature_settings_impl_delete_account_deleting_note),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun DeleteAccountStepRow(
    step: DeleteAccountStepState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = DELETE_ACCOUNT_STEP_MIN_HEIGHT),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DeleteAccountStepMarker(step = step)
        Text(
            text = stringResource(step.step.labelRes()),
            style = if (step.isCurrent) {
                HhTheme.typography.titleSmall
            } else {
                HhTheme.typography.bodyMedium
            },
            color = if (step.isPending) {
                HhTheme.colors.onSurfaceVariant
            } else {
                HhTheme.colors.onSurface
            },
        )
    }
}

@Composable
private fun DeleteAccountStepMarker(
    step: DeleteAccountStepState,
    modifier: Modifier = Modifier,
) {
    val color = HhTheme.colors
    Canvas(
        modifier = modifier.size(DELETE_ACCOUNT_MARKER_SIZE),
    ) {
        val radius = size.minDimension / 4f
        val center = Offset(size.width / 2f, size.height / 2f)
        when {
            step.isDone -> {
                val tick = Path().apply {
                    moveTo(size.width * 0.28f, size.height * 0.52f)
                    lineTo(size.width * 0.44f, size.height * 0.68f)
                    lineTo(size.width * 0.74f, size.height * 0.32f)
                }
                drawPath(
                    path = tick,
                    color = color.primary,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
                )
            }

            step.isCurrent -> {
                drawCircle(color = color.primaryContainer, radius = radius + MARKER_RING.toPx())
                drawCircle(color = color.primary, radius = radius)
            }

            else -> drawCircle(
                color = color.onSurfaceVariant,
                radius = radius,
                style = Stroke(width = 1.5.dp.toPx()),
            )
        }
    }
}

@Composable
private fun DeleteAccountDone(
    actions: DeleteAccountActions,
    padding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(start = HhTheme.spacing.d24, end = HhTheme.spacing.d24, top = HhTheme.spacing.d40),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.d16),
    ) {
        HhSpotIllustration(
            kind = HhSpotKind.Done,
            contentDescription = stringResource(
                R.string.feature_settings_impl_delete_account_done_illustration,
            ),
        )
        Text(
            text = stringResource(R.string.feature_settings_impl_delete_account_done_title),
            style = HhTheme.typography.displayLarge,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_settings_impl_delete_account_done_body),
            style = HhTheme.typography.bodyLarge,
            color = HhTheme.colors.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.weight(1f))
        HhButton(
            onClick = actions.onBackToWelcome,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HhTheme.spacing.d48),
        ) {
            Text(
                text = stringResource(R.string.feature_settings_impl_delete_account_back_to_welcome),
                style = HhTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun DeleteAccountBottomBar(
    uiState: DeleteAccountUiState,
    actions: DeleteAccountActions,
) {
    HhBottomActionBar(
        creditDisclosure = {
            if (uiState.isOffline) {
                Text(
                    text = stringResource(R.string.feature_settings_impl_delete_account_offline_button_note),
                    style = HhTheme.typography.labelMedium,
                    color = HhTheme.colors.onSurface,
                )
            }
        },
    ) {
        HhOutlinedButton(
            onClick = actions.onKeepAccount,
            enabled = uiState.isKeepEnabled,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = HhTheme.spacing.d48),
        ) {
            Text(
                text = stringResource(R.string.feature_settings_impl_delete_account_keep),
                style = HhTheme.typography.titleMedium,
                color = HhTheme.colors.onSurface,
            )
        }
        DestructiveActionButton(
            label = stringResource(R.string.feature_settings_impl_delete_account_confirm),
            onClick = actions.onDeleteAccount,
            enabled = uiState.isDeleteEnabled,
            modifier = Modifier.weight(1f),
        )
    }
}

private fun AccountDeletionStep.labelRes(): Int = when (this) {
    AccountDeletionStep.DELETING_APPLICATIONS ->
        R.string.feature_settings_impl_delete_account_step_applications

    AccountDeletionStep.DELETING_PROFILE_FACTS ->
        R.string.feature_settings_impl_delete_account_step_facts

    AccountDeletionStep.CLOSING_ACCOUNT ->
        R.string.feature_settings_impl_delete_account_step_closing
}

private val DELETE_ACCOUNT_STEP_MIN_HEIGHT = 36.dp

private val DELETE_ACCOUNT_MARKER_SIZE = 18.dp

private val MARKER_RING = 4.dp
