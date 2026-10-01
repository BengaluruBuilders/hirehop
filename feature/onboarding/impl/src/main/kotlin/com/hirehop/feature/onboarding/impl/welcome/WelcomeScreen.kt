package com.hirehop.feature.onboarding.impl.welcome

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhDivider
import com.hirehop.core.designsystem.component.HhErrorCallout
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.designsystem.component.HhScaffold
import com.hirehop.core.designsystem.component.HhSpotIllustration
import com.hirehop.core.designsystem.component.HhSpotKind
import com.hirehop.core.designsystem.component.HhTrustChip
import com.hirehop.core.designsystem.component.HhTrustKind
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.FactSource
import com.hirehop.core.ui.FactIdTag
import com.hirehop.core.ui.FactProvenanceChip
import com.hirehop.feature.onboarding.impl.R

private val HH_TOUCH_TARGET: Dp = 48.dp

private const val HH_FACT_SEPARATOR = " · "

@Composable
internal fun WelcomeScreen(
    uiState: WelcomeUiState,
    actions: WelcomeActions,
    modifier: Modifier = Modifier,
) {
    HhScaffold(
        modifier = modifier,
        bottomBar = { WelcomeBottomBar(uiState = uiState, actions = actions) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (uiState.isLoading) {
                WelcomeLoading()
            } else {
                WelcomeContent(uiState = uiState, actions = actions)
            }
        }
    }
}

@Composable
private fun WelcomeLoading() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(HhTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HhSpotIllustration(kind = HhSpotKind.Empty)
        Text(
            text = stringResource(R.string.feature_onboarding_impl_welcome_loading),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun WelcomeContent(
    uiState: WelcomeUiState,
    actions: WelcomeActions,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(HhTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        HhOfflineBanner(
            message = stringResource(R.string.feature_onboarding_impl_welcome_offline_message),
            supportingText = stringResource(
                R.string.feature_onboarding_impl_welcome_offline_support,
            ),
            visible = uiState.isOffline,
        )
        if (uiState.message == WelcomeMessage.LOAD_FAILED) {
            HhErrorCallout(
                title = stringResource(R.string.feature_onboarding_impl_welcome_load_failed_title),
                supportingText = stringResource(
                    R.string.feature_onboarding_impl_welcome_load_failed_body,
                ),
                actionLabel = stringResource(R.string.feature_onboarding_impl_welcome_try_again),
                onAction = actions.onRetry,
            )
        }
        WelcomeHeadline()
        WelcomeHero(uiState = uiState)
        WelcomeTrustRow()
        WelcomeBuildStepByStep(actions = actions)
        WelcomeFooterNote()
    }
}

@Composable
private fun WelcomeHeadline() {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        HhSpotIllustration(kind = HhSpotKind.Empty)
        Text(
            text = stringResource(R.string.feature_onboarding_impl_welcome_headline),
            style = HhTheme.typography.headlineSmall,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_welcome_subline),
            style = HhTheme.typography.bodyLarge,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun WelcomeHero(uiState: WelcomeUiState) {
    val originalLabel = stringResource(
        R.string.feature_onboarding_impl_welcome_hero_original_label,
    )
    val originalLine = stringResource(
        R.string.feature_onboarding_impl_welcome_hero_original_line,
    )
    val rewrittenLabel = stringResource(
        R.string.feature_onboarding_impl_welcome_hero_rewritten_label,
    )
    val rewrittenLine = stringResource(
        R.string.feature_onboarding_impl_welcome_hero_rewritten_line,
    )
    val factId = stringResource(R.string.feature_onboarding_impl_welcome_hero_fact_id)
    val factTitle = stringResource(R.string.feature_onboarding_impl_welcome_hero_fact_title)
    val waitingNote = stringResource(R.string.feature_onboarding_impl_welcome_hero_waiting)
    val staticNote = stringResource(R.string.feature_onboarding_impl_welcome_hero_static)
    HhCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = originalLabel,
            style = HhTheme.typography.labelMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
        Text(
            text = originalLine,
            style = HhTheme.typography.bodyLarge,
            color = HhTheme.colors.onSurfaceVariant,
        )
        if (uiState.showsRewrittenLine) {
            HhDivider()
            Text(
                text = rewrittenLabel,
                style = HhTheme.typography.labelMedium,
                color = HhTheme.colors.primary,
            )
            Text(
                text = rewrittenLine,
                style = HhTheme.typography.bodyLarge,
                color = HhTheme.colors.onSurface,
            )
        } else {
            Text(
                text = waitingNote,
                style = HhTheme.typography.bodySmall,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
        if (uiState.showsProvenanceThread) {
            WelcomeHeroSource(factId = factId, factTitle = factTitle)
        }
        if (uiState.reduceMotion) {
            Text(
                text = staticNote,
                style = HhTheme.typography.bodySmall,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WelcomeHeroSource(
    factId: String,
    factTitle: String,
) {
    val description = factId + HH_FACT_SEPARATOR + factTitle
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FactIdTag(factId = factId)
        Text(
            text = factTitle,
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurface,
        )
        FactProvenanceChip(source = FactSource.IMPORTED, isConfirmed = true)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WelcomeTrustRow() {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
    ) {
        HhTrustChip(
            kind = HhTrustKind.NeverInvents,
            label = stringResource(
                R.string.feature_onboarding_impl_welcome_trust_never_invents,
            ),
        )
        HhTrustChip(
            kind = HhTrustKind.OnDevice,
            label = stringResource(R.string.feature_onboarding_impl_welcome_trust_on_device),
        )
        HhTrustChip(
            kind = HhTrustKind.OfflineReady,
            label = stringResource(R.string.feature_onboarding_impl_welcome_trust_offline),
        )
        HhTrustChip(
            kind = HhTrustKind.NoScore,
            label = stringResource(R.string.feature_onboarding_impl_welcome_trust_no_score),
        )
    }
}

@Composable
private fun WelcomeBuildStepByStep(actions: WelcomeActions) {
    Text(
        text = stringResource(
            R.string.feature_onboarding_impl_welcome_action_build_step_by_step,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = HH_TOUCH_TARGET)
            .clickable(onClick = actions.onBuildProfileStepByStep)
            .padding(vertical = HhTheme.spacing.sm)
            .semantics { role = Role.Button },
        style = HhTheme.typography.labelLarge,
        color = HhTheme.colors.primary,
        textDecoration = TextDecoration.Underline,
    )
}

@Composable
private fun WelcomeFooterNote() {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs)) {
        Text(
            text = stringResource(R.string.feature_onboarding_impl_welcome_never_asks),
            style = HhTheme.typography.bodySmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_welcome_nothing_to_delete),
            style = HhTheme.typography.bodySmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun WelcomeBottomBar(
    uiState: WelcomeUiState,
    actions: WelcomeActions,
) {
    HhBottomActionBar(
        contentPadding = PaddingValues(
            start = HhTheme.spacing.d20,
            end = HhTheme.spacing.d20,
            top = HhTheme.spacing.md,
            bottom = HhTheme.spacing.md,
        ),
        actions = {
            HhButton(
                onClick = actions.onPasteJobDescription,
                enabled = uiState.isActionsEnabled,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = HH_TOUCH_TARGET),
            ) {
                Text(
                    text = stringResource(
                        R.string.feature_onboarding_impl_welcome_action_paste_jd,
                    ),
                    style = HhTheme.typography.labelLarge,
                )
            }
            HhOutlinedButton(
                onClick = actions.onImportResume,
                enabled = uiState.isActionsEnabled,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = HH_TOUCH_TARGET),
            ) {
                Text(
                    text = stringResource(
                        R.string.feature_onboarding_impl_welcome_action_import_resume,
                    ),
                    style = HhTheme.typography.labelLarge,
                    color = HhTheme.colors.onSurface,
                )
            }
        },
    )
}
