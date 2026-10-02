package com.hirehop.feature.onboarding.impl.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhErrorCallout
import com.hirehop.core.designsystem.component.HhEvidenceText
import com.hirehop.core.designsystem.component.HhFactId
import com.hirehop.core.designsystem.component.HhHeroCard
import com.hirehop.core.designsystem.component.HhHomeHeader
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhProvenanceChip
import com.hirehop.core.designsystem.component.HhProvenanceKind
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhTextButton
import com.hirehop.core.designsystem.component.HhTrustChip
import com.hirehop.core.designsystem.component.HhTrustKind
import com.hirehop.core.designsystem.component.evidenceMarkSpanStyle
import com.hirehop.core.designsystem.illustration.HhCharacterIllustration
import com.hirehop.core.designsystem.illustration.HhIllustration
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.onboarding.impl.R

private val THREAD_WIDTH = 2.dp

@Composable
internal fun WelcomeScreen(
    uiState: WelcomeUiState,
    actions: WelcomeActions,
    modifier: Modifier = Modifier,
) {
    HhScreen(
        modifier = modifier,
        header = {
            HhHomeHeader(
                greeting = stringResource(R.string.feature_onboarding_impl_welcome_brand),
                headline = stringResource(R.string.feature_onboarding_impl_welcome_headline),
                illustration = {
                    HhCharacterIllustration(
                        illustration = HhIllustration.Hero,
                        contentDescription = stringResource(
                            R.string.feature_onboarding_impl_welcome_illustration_description,
                        ),
                        modifier = Modifier.fillMaxSize(),
                    )
                },
            )
        },
        bottomBar = { WelcomeBottomBar(uiState = uiState, actions = actions) },
    ) { padding ->
        if (uiState.isLoading) {
            WelcomeLoading(modifier = Modifier.padding(padding))
        } else {
            WelcomeContent(uiState = uiState, actions = actions, modifier = Modifier.padding(padding))
        }
    }
}

@Composable
private fun WelcomeLoading(modifier: Modifier = Modifier) {
    val message = stringResource(R.string.feature_onboarding_impl_welcome_loading)
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
private fun WelcomeContent(
    uiState: WelcomeUiState,
    actions: WelcomeActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HhTheme.spacing.gutter),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        HhOfflineBanner(
            message = stringResource(R.string.feature_onboarding_impl_welcome_offline_message),
            supportingText = stringResource(R.string.feature_onboarding_impl_welcome_offline_support),
            visible = uiState.isOffline,
        )
        if (uiState.message == WelcomeMessage.LOAD_FAILED) {
            HhErrorCallout(
                title = stringResource(R.string.feature_onboarding_impl_welcome_load_failed_title),
                supportingText = stringResource(R.string.feature_onboarding_impl_welcome_load_failed_body),
                actionLabel = stringResource(R.string.feature_onboarding_impl_welcome_try_again),
                onAction = actions.onRetry,
            )
        }
        Text(
            text = stringResource(R.string.feature_onboarding_impl_welcome_subline),
            style = HhTheme.typography.bodyM,
            color = HhTheme.colors.body,
        )
        WelcomeProofCard(uiState = uiState)
        if (uiState.showsNeverInventsChip) {
            HhTrustChip(
                kind = HhTrustKind.NeverInvents,
                label = stringResource(R.string.feature_onboarding_impl_welcome_trust_never_invents),
            )
        }
        HhTextButton(
            label = stringResource(R.string.feature_onboarding_impl_welcome_action_build_step_by_step),
            onClick = actions.onBuildProfileStepByStep,
            enabled = uiState.isActionsEnabled,
        )
    }
}

@Composable
private fun WelcomeProofCard(uiState: WelcomeUiState) {
    val colors = HhTheme.colors
    HhHeroCard(contentPadding = PaddingValues(HhTheme.spacing.gutter)) {
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm + HhTheme.spacing.xxs)) {
            Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs)) {
                Text(
                    text = stringResource(R.string.feature_onboarding_impl_welcome_hero_original_label),
                    style = HhTheme.typography.labelM,
                    color = colors.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.feature_onboarding_impl_welcome_hero_original_line),
                    style = HhTheme.typography.bodyM,
                    color = if (uiState.showsRewrittenLine) colors.onSurfaceVariant else colors.onSurface,
                )
            }
            if (uiState.showsRewrittenLine) {
                WelcomeRewrittenLine()
            }
            if (uiState.showsProvenanceThread) {
                WelcomeProvenanceThread()
            }
        }
    }
}

@Composable
private fun WelcomeRewrittenLine() {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs)) {
        Text(
            text = stringResource(R.string.feature_onboarding_impl_welcome_hero_rewritten_label),
            style = HhTheme.typography.labelM,
            color = HhTheme.colors.primary,
        )
        HhEvidenceText(
            text = markedLine(
                line = stringResource(R.string.feature_onboarding_impl_welcome_hero_rewritten_line),
                marks = stringArrayResource(R.array.feature_onboarding_impl_welcome_hero_rewritten_marks).toList(),
            ),
            style = HhTheme.typography.bodyL.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun markedLine(line: String, marks: List<String>): AnnotatedString {
    val markStyle = evidenceMarkSpanStyle()
    return buildAnnotatedString {
        append(line)
        marks.forEach { mark ->
            val start = line.indexOf(mark)
            if (start >= 0) {
                addStyle(markStyle, start, start + mark.length)
            }
        }
    }
}

@Composable
private fun WelcomeProvenanceThread() {
    val colors = HhTheme.colors
    val factId = stringResource(R.string.feature_onboarding_impl_welcome_hero_fact_id)
    val factTitle = stringResource(R.string.feature_onboarding_impl_welcome_hero_fact_title)
    val shape = HhTheme.shapes.banner
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .semantics(mergeDescendants = true) { contentDescription = "$factId. $factTitle" },
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.d12 - HhTheme.spacing.xxs),
    ) {
        Box(
            modifier = Modifier
                .padding(start = HhTheme.spacing.d8 + HhTheme.spacing.d2 / 2)
                .width(THREAD_WIDTH)
                .fillMaxSize()
                .background(colors.primary, HhTheme.shapes.tag),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(shape)
                .background(colors.card, shape)
                .border(HhTheme.spacing.d2 / 2, colors.outlineVariant, shape)
                .padding(horizontal = HhTheme.spacing.md, vertical = HhTheme.spacing.d12 - HhTheme.spacing.xxs),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.d4 + HhTheme.spacing.xxs),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HhFactId(id = factId)
                HhProvenanceChip(
                    kind = HhProvenanceKind.Confirmed,
                    label = stringResource(R.string.feature_onboarding_impl_welcome_hero_confirmed),
                )
            }
            Text(text = factTitle, style = HhTheme.typography.bodyM, color = HhTheme.colors.body)
        }
    }
}

@Composable
private fun WelcomeBottomBar(
    uiState: WelcomeUiState,
    actions: WelcomeActions,
) {
    HhBottomActionBar(primaryLast = false) {
        HhPrimaryButton(
            label = stringResource(R.string.feature_onboarding_impl_welcome_action_paste_jd),
            onClick = actions.onPasteJobDescription,
            enabled = uiState.isActionsEnabled,
            modifier = Modifier.weight(1f),
        )
        HhPrimaryButton(
            label = stringResource(R.string.feature_onboarding_impl_welcome_action_import_resume),
            onClick = actions.onImportResume,
            enabled = uiState.isActionsEnabled,
            modifier = Modifier.weight(1f),
        )
    }
}
