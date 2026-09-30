package com.hirehop.feature.onboarding.impl.pastejd

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhErrorCallout
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.designsystem.component.HhScaffold
import com.hirehop.core.designsystem.component.HhSpotIllustration
import com.hirehop.core.designsystem.component.HhSpotKind
import com.hirehop.core.designsystem.component.HhTextField
import com.hirehop.core.designsystem.component.HhTopAppBar
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.onboarding.impl.R

private val HH_TOUCH_TARGET: Dp = 48.dp

@Composable
internal fun PasteJobDescriptionScreen(
    uiState: PasteJobDescriptionUiState,
    actions: PasteJobDescriptionActions,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
) {
    HhScaffold(
        modifier = modifier,
        topBar = {
            HhTopAppBar(
                title = stringResource(R.string.feature_onboarding_impl_paste_jd_title),
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                navigationIconContentDescription = stringResource(
                    R.string.feature_onboarding_impl_paste_jd_navigation_back_description,
                ),
                onNavigationClick = onBack,
            )
        },
        bottomBar = { PasteJobDescriptionBottomBar(uiState = uiState, actions = actions) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (uiState.isLoading) {
                PasteJobDescriptionLoading()
            } else {
                PasteJobDescriptionContent(uiState = uiState, actions = actions)
            }
        }
    }
}

@Composable
private fun PasteJobDescriptionLoading() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(HhTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HhSpotIllustration(kind = HhSpotKind.Scanned)
        Text(
            text = stringResource(R.string.feature_onboarding_impl_paste_jd_loading),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun PasteJobDescriptionContent(
    uiState: PasteJobDescriptionUiState,
    actions: PasteJobDescriptionActions,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(HhTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        HhOfflineBanner(
            message = stringResource(R.string.feature_onboarding_impl_paste_jd_offline_message),
            supportingText = stringResource(
                R.string.feature_onboarding_impl_paste_jd_offline_support,
            ),
            visible = uiState.isOffline,
        )
        if (uiState.message == PasteJobDescriptionMessage.PASTE_FAILED) {
            HhErrorCallout(
                title = stringResource(R.string.feature_onboarding_impl_paste_jd_error_title),
                supportingText = stringResource(
                    R.string.feature_onboarding_impl_paste_jd_error_body,
                ),
                actionLabel = stringResource(R.string.feature_onboarding_impl_paste_jd_try_again),
                onAction = actions.onRetry,
            )
        }
        if (uiState.arrival == PasteJobDescriptionArrival.SHARED_IN) {
            Text(
                text = stringResource(
                    R.string.feature_onboarding_impl_paste_jd_arrival_shared_in,
                ),
                style = HhTheme.typography.labelMedium,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
        PasteJobDescriptionField(uiState = uiState, actions = actions)
        PasteJobDescriptionWordCount(uiState = uiState)
        if (uiState.canClear) {
            PasteJobDescriptionClear(actions = actions)
        }
        PasteJobDescriptionCompanyField(uiState = uiState, actions = actions)
        PasteJobDescriptionRoleField(uiState = uiState, actions = actions)
        Text(
            text = stringResource(R.string.feature_onboarding_impl_paste_jd_not_read_yet),
            style = HhTheme.typography.bodySmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
        PasteJobDescriptionStateNote(message = uiState.message)
    }
}

@Composable
private fun PasteJobDescriptionField(
    uiState: PasteJobDescriptionUiState,
    actions: PasteJobDescriptionActions,
) {
    HhTextField(
        value = uiState.text,
        onValueChange = actions.onTextChange,
        label = stringResource(R.string.feature_onboarding_impl_paste_jd_field_label),
        placeholder = stringResource(
            R.string.feature_onboarding_impl_paste_jd_field_placeholder,
        ),
        errorText = uiState.problem?.let { problem -> problemText(problem) },
        singleLine = false,
        minLines = 6,
        modifier = Modifier.heightIn(min = HH_TOUCH_TARGET),
    )
}

@Composable
private fun PasteJobDescriptionWordCount(uiState: PasteJobDescriptionUiState) {
    val words = pluralStringResource(
        R.plurals.feature_onboarding_impl_paste_jd_word_count,
        uiState.wordCount,
        uiState.wordCount,
    )
    val description = stringResource(
        R.string.feature_onboarding_impl_paste_jd_word_count_description,
    ) + ": " + words
    Text(
        text = words,
        modifier = Modifier.clearAndSetSemantics { contentDescription = description },
        style = HhTheme.typography.monoSmall,
        color = HhTheme.colors.onSurfaceVariant,
    )
}

@Composable
private fun PasteJobDescriptionClear(actions: PasteJobDescriptionActions) {
    HhOutlinedButton(
        onClick = actions.onClear,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = HH_TOUCH_TARGET),
    ) {
        Text(
            text = stringResource(R.string.feature_onboarding_impl_paste_jd_action_clear),
            style = HhTheme.typography.labelLarge,
            color = HhTheme.colors.onSurface,
        )
    }
}

@Composable
private fun PasteJobDescriptionCompanyField(
    uiState: PasteJobDescriptionUiState,
    actions: PasteJobDescriptionActions,
) {
    HhTextField(
        value = uiState.company,
        onValueChange = actions.onCompanyChange,
        label = stringResource(R.string.feature_onboarding_impl_paste_jd_company_label),
        placeholder = stringResource(
            R.string.feature_onboarding_impl_paste_jd_company_placeholder,
        ),
        supportingText = { PasteJobDescriptionOptionalHint() },
        modifier = Modifier.heightIn(min = HH_TOUCH_TARGET),
    )
}

@Composable
private fun PasteJobDescriptionRoleField(
    uiState: PasteJobDescriptionUiState,
    actions: PasteJobDescriptionActions,
) {
    HhTextField(
        value = uiState.role,
        onValueChange = actions.onRoleChange,
        label = stringResource(R.string.feature_onboarding_impl_paste_jd_role_label),
        placeholder = stringResource(R.string.feature_onboarding_impl_paste_jd_role_placeholder),
        supportingText = { PasteJobDescriptionOptionalHint() },
        modifier = Modifier.heightIn(min = HH_TOUCH_TARGET),
    )
}

@Composable
private fun PasteJobDescriptionOptionalHint() {
    Text(
        text = stringResource(R.string.feature_onboarding_impl_paste_jd_optional_hint),
        style = HhTheme.typography.bodySmall,
        color = HhTheme.colors.onSurfaceVariant,
    )
}

@Composable
private fun PasteJobDescriptionStateNote(message: PasteJobDescriptionMessage?) {
    val note = when (message) {
        PasteJobDescriptionMessage.NOTHING_TO_READ -> stringResource(
            R.string.feature_onboarding_impl_paste_jd_message_nothing_to_read,
        )

        PasteJobDescriptionMessage.PASTE_PARTIAL -> stringResource(
            R.string.feature_onboarding_impl_paste_jd_message_partial,
        )

        PasteJobDescriptionMessage.PASTE_FAILED,
        null,
        -> null
    }
    if (note != null) {
        Text(
            text = note,
            style = HhTheme.typography.bodySmall,
            color = HhTheme.colors.onSurfaceVariant,
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
    HhBottomActionBar(
        contentPadding = PaddingValues(
            start = HhTheme.spacing.d20,
            end = HhTheme.spacing.d20,
            top = HhTheme.spacing.md,
            bottom = HhTheme.spacing.md,
        ),
        creditDisclosure = {
            Text(
                text = stringResource(
                    R.string.feature_onboarding_impl_paste_jd_disclosure_stays_on_phone,
                ),
                style = HhTheme.typography.bodySmall,
                color = HhTheme.colors.onSurfaceVariant,
            )
        },
        actions = {
            HhButton(
                onClick = actions.onAnalyse,
                enabled = uiState.canAnalyse,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = HH_TOUCH_TARGET),
            ) {
                Text(
                    text = stringResource(
                        R.string.feature_onboarding_impl_paste_jd_action_analyse,
                    ),
                    style = HhTheme.typography.labelLarge,
                )
            }
        },
    )
}
