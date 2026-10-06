package com.hirehop.feature.tailor.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhBottomSheet
import com.hirehop.core.designsystem.component.HhConfirmDialog
import com.hirehop.core.designsystem.component.HhContentSwitch
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhSpotKind
import com.hirehop.core.designsystem.component.HhStepProgress
import com.hirehop.core.designsystem.component.HhToastHost
import com.hirehop.core.designsystem.component.HhToastResult
import com.hirehop.core.designsystem.component.rememberHhToastState
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.EntryCategory

internal data class TailorActions(
    val onBack: () -> Unit,
    val onPreviewExport: () -> Unit,
    val onAccept: (String) -> Unit,
    val onKeepOriginal: (String) -> Unit,
    val onUndo: (String) -> Unit,
    val onEditByHand: (String, String) -> Unit,
    val onRegenerate: (EntryCategory) -> Unit,
    val onRetry: () -> Unit,
    val onReportBullet: (String) -> Unit,
    val onReportSection: (String) -> Unit,
    val onEditFact: (entryId: String, entryType: String) -> Unit,
    val onBulletSheetClosed: () -> Unit = {},
)

internal sealed interface ReviewToastState {
    data class Accepted(val bulletId: String, val position: Int) : ReviewToastState

    data object Reported : ReviewToastState
}

@Stable
internal class ReviewInteraction(initialBulletId: String? = null, initialToast: ReviewToastState? = null) {
    var openBulletId by mutableStateOf(initialBulletId)
    var editBulletId by mutableStateOf<String?>(null)
    var editText by mutableStateOf("")
        private set
    var editShowsError by mutableStateOf(false)
        private set
    var sourceBulletId by mutableStateOf<String?>(null)
    var regenerateCategory by mutableStateOf<EntryCategory?>(null)
    var toast by mutableStateOf(initialToast)

    fun startEdit(bulletId: String, proposedText: String) {
        editText = proposedText
        editShowsError = false
        editBulletId = bulletId
    }

    fun changeEditText(text: String) {
        editText = text
        editShowsError = false
    }

    fun canSaveEdit(): Boolean {
        editShowsError = editText.isBlank()
        return !editShowsError
    }
}

@Composable
internal fun TailorScreen(
    uiState: TailorUiState,
    actions: TailorActions,
    modifier: Modifier = Modifier,
    initialBulletId: String? = null,
    interaction: ReviewInteraction = remember { ReviewInteraction(initialBulletId) },
) {
    val toastState = rememberHhToastState()
    val toast = interaction.toast
    val toastMessage = toast?.let { stringResource(it.messageRes(), *it.messageArgs()) }
    val undoLabel = stringResource(R.string.feature_tailor_impl_toast_undo)
    LaunchedEffect(toast) {
        if (toast == null || toastMessage == null) return@LaunchedEffect
        val result = toastState.show(
            message = toastMessage,
            actionLabel = undoLabel.takeIf { toast is ReviewToastState.Accepted },
        )
        if (result == HhToastResult.ActionPerformed && toast is ReviewToastState.Accepted) {
            actions.onUndo(toast.bulletId)
        }
        interaction.toast = null
    }
    HhScreen(
        modifier = modifier,
        sheet = false,
        header = {
            HhInnerHeader(
                title = stringResource(R.string.feature_tailor_impl_title_review),
                subtitle = uiState.jobSubtitle(),
                onBack = actions.onBack,
                backContentDescription = stringResource(R.string.feature_tailor_impl_back),
            )
        },
        bottomBar = { TailorBottomBar(uiState, actions, interaction) },
        bottomBarNotice = if (uiState is TailorUiState.Success && uiState.openCount > 0) {
            { DecideNote() }
        } else {
            null
        },
        snackbarHost = { HhToastHost(toastState) },
    ) { padding ->
        HhContentSwitch(targetState = uiState, contentKey = { it::class }) { state ->
            TailorBody(state, actions, interaction, padding)
        }
    }
    (uiState as? TailorUiState.Success)?.let { ReviewOverlays(it, actions, interaction) }
}

@Composable
private fun TailorUiState.jobSubtitle(): String? {
    val job = when (this) {
        is TailorUiState.Loading -> job
        is TailorUiState.Failed -> job
        is TailorUiState.Success -> job
        TailorUiState.NotFound -> null
    } ?: return null
    return jobLine(job.title, job.company)
}

private fun ReviewToastState.messageRes(): Int = when (this) {
    is ReviewToastState.Accepted -> R.string.feature_tailor_impl_toast_accepted
    ReviewToastState.Reported -> R.string.feature_tailor_impl_report_thanks
}

private fun ReviewToastState.messageArgs(): Array<Any> = when (this) {
    is ReviewToastState.Accepted -> arrayOf(position)
    ReviewToastState.Reported -> emptyArray()
}

@Composable
private fun TailorBody(
    uiState: TailorUiState,
    actions: TailorActions,
    interaction: ReviewInteraction,
    padding: PaddingValues,
) {
    when (uiState) {
        is TailorUiState.Loading -> LoadingContent(uiState, padding)
        is TailorUiState.Failed -> StatusContent(
            padding = padding,
            title = stringResource(R.string.feature_tailor_impl_failed_title),
            body = stringResource(R.string.feature_tailor_impl_failed_body),
        )
        TailorUiState.NotFound -> StatusContent(
            padding = padding,
            title = stringResource(R.string.feature_tailor_impl_not_found_title),
            body = stringResource(R.string.feature_tailor_impl_not_found),
        )
        is TailorUiState.Success -> ReviewContent(
            state = uiState,
            onOpenChange = { interaction.openBulletId = it },
            onOpenSource = { interaction.sourceBulletId = it },
            onRegenerate = { interaction.regenerateCategory = it },
            onReportSection = { key ->
                actions.onReportSection(key)
                interaction.toast = ReviewToastState.Reported
            },
            modifier = Modifier.fillMaxSize(),
            contentPadding = padding,
        )
    }
}

@Composable
private fun LoadingContent(state: TailorUiState.Loading, padding: PaddingValues) {
    val picked = state.factCount?.let { stringResource(R.string.feature_tailor_impl_loading_step_pick_detail, it) }
    val written = state.lineCount?.let { stringResource(R.string.feature_tailor_impl_loading_step_write_detail, it) }
    LazyColumn(
        contentPadding = PaddingValues(
            start = HhTheme.spacing.gutter,
            end = HhTheme.spacing.gutter,
            top = padding.calculateTopPadding() + HhTheme.spacing.sm,
            bottom = padding.calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        item {
            HhStepProgress(
                stepNames = listOf(
                    stringResource(R.string.feature_tailor_impl_loading_step_pick),
                    stringResource(R.string.feature_tailor_impl_loading_step_write),
                    stringResource(R.string.feature_tailor_impl_loading_step_check),
                ),
                currentStepIndex = 1,
                ordinalLabel = stringResource(R.string.feature_tailor_impl_loading_caption),
                stepDetails = listOf(picked, written, null),
                footnote = stringResource(R.string.feature_tailor_impl_loading_footnote),
            )
        }
    }
}

@Composable
private fun StatusContent(padding: PaddingValues, title: String, body: String) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = HhTheme.spacing.gutter,
            end = HhTheme.spacing.gutter,
            top = padding.calculateTopPadding() + HhTheme.spacing.sm,
            bottom = padding.calculateBottomPadding(),
        ),
    ) {
        item { StatusCard(kind = HhSpotKind.Error, title = title, body = body) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewOverlays(state: TailorUiState.Success, actions: TailorActions, interaction: ReviewInteraction) {
    val closeSheet = {
        interaction.openBulletId = null
        actions.onBulletSheetClosed()
    }
    val openItem = interaction.openBulletId?.let { id -> state.changes.firstOrNull { it.bullet.id == id } }
    val editItem = interaction.editBulletId?.let { id -> state.changes.firstOrNull { it.bullet.id == id } }
    val sourceItem = interaction.sourceBulletId?.let { id -> state.allBullets().firstOrNull { it.bullet.id == id } }
    if (openItem != null && editItem == null && sourceItem == null) {
        HhBottomSheet(onDismissRequest = closeSheet) {
            BulletReviewSheetContent(
                item = openItem,
                position = state.changeIndexOf(openItem.bullet.id) + 1,
                total = state.totalCount,
                actions = bulletSheetActions(state, openItem, actions, interaction, closeSheet),
                isReported = openItem.bullet.id in state.reportedIds,
            )
        }
    }
    if (editItem != null) {
        HhBottomSheet(onDismissRequest = { interaction.editBulletId = null }) {
            EditByHandContent(
                position = state.changeIndexOf(editItem.bullet.id) + 1,
                text = interaction.editText,
                onTextChange = interaction::changeEditText,
                showsError = interaction.editShowsError,
                onCancel = { interaction.editBulletId = null },
                onSave = {
                    if (interaction.canSaveEdit()) {
                        actions.onEditByHand(editItem.bullet.id, interaction.editText)
                        interaction.editBulletId = null
                    }
                },
            )
        }
    }
    if (sourceItem != null && sourceItem.sources.isNotEmpty()) {
        HhBottomSheet(onDismissRequest = { interaction.sourceBulletId = null }) {
            SourceFactSheetContent(
                sources = sourceItem.sources,
                onEditFact = { source ->
                    interaction.sourceBulletId = null
                    interaction.openBulletId = null
                    actions.onEditFact(source.entryId, source.category.name.lowercase())
                },
                onReport = {
                    actions.onReportBullet(sourceItem.bullet.id)
                    interaction.sourceBulletId = null
                    interaction.toast = ReviewToastState.Reported
                },
                isReported = sourceItem.bullet.id in state.reportedIds,
            )
        }
    }
    interaction.regenerateCategory?.let { category -> RegenerateDialog(state, category, actions, interaction) }
}

private fun TailorUiState.Success.allBullets(): List<TailorBulletUi> = sections
    .filterIsInstance<ReviewSection.Entries>()
    .flatMap { it.entries }
    .flatMap { it.bullets }

private fun bulletSheetActions(
    state: TailorUiState.Success,
    item: TailorBulletUi,
    actions: TailorActions,
    interaction: ReviewInteraction,
    closeSheet: () -> Unit,
): BulletSheetActions {
    val index = state.changeIndexOf(item.bullet.id)
    val id = item.bullet.id
    return BulletSheetActions(
        onPrevious = state.changes.getOrNull(index - 1)?.let { previous -> { interaction.openBulletId = previous.bullet.id } },
        onNext = state.changes.getOrNull(index + 1)?.let { next -> { interaction.openBulletId = next.bullet.id } },
        onAccept = {
            actions.onAccept(id)
            interaction.toast = ReviewToastState.Accepted(id, index + 1)
            closeSheet()
        },
        onKeepOriginal = {
            actions.onKeepOriginal(id)
            if (state.openCount <= 1) closeSheet()
        },
        onUndo = { actions.onUndo(id) },
        onEditByHand = {
            interaction.startEdit(id, item.bullet.proposedText)
        },
        onNextChange = {
            val next = state.nextOpenChange(id)
            if (next == null || next.bullet.id == id) closeSheet() else interaction.openBulletId = next.bullet.id
        },
        onOpenSource = { interaction.sourceBulletId = id },
        onReport = {
            actions.onReportBullet(id)
            interaction.toast = ReviewToastState.Reported
        },
    )
}

@Composable
private fun RegenerateDialog(
    state: TailorUiState.Success,
    category: EntryCategory,
    actions: TailorActions,
    interaction: ReviewInteraction,
) {
    val changeCount = state.sections.filterIsInstance<ReviewSection.Entries>()
        .firstOrNull { it.category == category }?.changeCount ?: 0
    val used = MAX_REGENERATIONS - state.regenerationsLeft + 1
    HhConfirmDialog(
        title = stringResource(R.string.feature_tailor_impl_regenerate_title, stringResource(category.headingRes())),
        message = pluralStringResource(
            R.plurals.feature_tailor_impl_regenerate_message,
            changeCount,
            changeCount,
            used,
            MAX_REGENERATIONS,
        ),
        confirmLabel = stringResource(R.string.feature_tailor_impl_regenerate_confirm, used, MAX_REGENERATIONS),
        cancelLabel = stringResource(R.string.feature_tailor_impl_regenerate_cancel),
        onConfirm = {
            actions.onRegenerate(category)
            interaction.regenerateCategory = null
        },
        onCancel = { interaction.regenerateCategory = null },
    )
}

@Composable
private fun TailorBottomBar(uiState: TailorUiState, actions: TailorActions, interaction: ReviewInteraction) {
    when (uiState) {
        is TailorUiState.Loading -> HhBottomActionBar {
            HhOutlineButton(
                label = stringResource(R.string.feature_tailor_impl_loading_leave),
                onClick = actions.onBack,
                modifier = Modifier.weight(1f),
            )
        }
        is TailorUiState.Failed -> HhBottomActionBar {
            HhOutlineButton(
                label = stringResource(R.string.feature_tailor_impl_failed_back),
                onClick = actions.onBack,
                modifier = Modifier.weight(1f),
            )
            HhPrimaryButton(
                label = stringResource(R.string.feature_tailor_impl_failed_retry),
                onClick = actions.onRetry,
                modifier = Modifier.weight(1f),
            )
        }
        TailorUiState.NotFound -> HhBottomActionBar {
            HhOutlineButton(
                label = stringResource(R.string.feature_tailor_impl_back),
                onClick = actions.onBack,
                modifier = Modifier.weight(1f),
            )
        }
        is TailorUiState.Success -> SuccessBottomBar(uiState, actions, interaction)
    }
}

@Composable
private fun SuccessBottomBar(state: TailorUiState.Success, actions: TailorActions, interaction: ReviewInteraction) {
    HhBottomActionBar {
        if (state.openCount > 0) {
            HhPrimaryButton(
                label = pluralStringResource(
                    R.plurals.feature_tailor_impl_changes_left,
                    state.openCount,
                    state.openCount,
                ),
                onClick = { state.nextOpenChange()?.let { interaction.openBulletId = it.bullet.id } },
                trailingIcon = HhIcons.ArrowForward,
                modifier = Modifier.weight(1f),
            )
        } else {
            HhPrimaryButton(
                label = stringResource(R.string.feature_tailor_impl_preview_export),
                onClick = actions.onPreviewExport,
                enabled = state.canPreviewExport,
                trailingIcon = HhIcons.ArrowForward,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
