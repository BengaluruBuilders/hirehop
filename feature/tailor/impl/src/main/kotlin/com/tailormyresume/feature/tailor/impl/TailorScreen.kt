package com.tailormyresume.feature.tailor.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
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
import androidx.compose.ui.semantics.contentDescription
import com.tailormyresume.core.designsystem.component.TmrBottomActionBar
import com.tailormyresume.core.designsystem.component.TmrBottomSheet
import com.tailormyresume.core.designsystem.component.TmrContentSwitch
import com.tailormyresume.core.designsystem.component.TmrIconButton
import com.tailormyresume.core.designsystem.component.TmrInnerHeader
import com.tailormyresume.core.designsystem.component.TmrPrimaryButton
import com.tailormyresume.core.designsystem.component.TmrScreen
import com.tailormyresume.core.designsystem.component.TmrSecondaryButton
import com.tailormyresume.core.designsystem.component.TmrSpotKind
import com.tailormyresume.core.designsystem.component.TmrToastHost
import com.tailormyresume.core.designsystem.component.TmrToastResult
import com.tailormyresume.core.designsystem.component.rememberTmrToastState
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.EntryCategory

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

    data object RegenerateNoCredit : ReviewToastState

    data object RegenerateFailed : ReviewToastState
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
    var limitSheetOpen by mutableStateOf(false)
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
    val toastState = rememberTmrToastState()
    val toast = interaction.toast
    val toastMessage = toast?.let { stringResource(it.messageRes(), *it.messageArgs()) }
    val undoLabel = stringResource(R.string.feature_tailor_impl_toast_undo)
    LaunchedEffect(toast) {
        if (toast == null || toastMessage == null) return@LaunchedEffect
        val result = toastState.show(
            message = toastMessage,
            actionLabel = undoLabel.takeIf { toast is ReviewToastState.Accepted },
        )
        if (result == TmrToastResult.ActionPerformed && toast is ReviewToastState.Accepted) {
            actions.onUndo(toast.bulletId)
        }
        interaction.toast = null
    }
    TmrScreen(
        modifier = modifier,
        sheet = false,
        header = {
            TmrInnerHeader(
                title = headerTitle(uiState),
                subtitle = headerSubtitle(uiState),
                onBack = actions.onBack,
                backContentDescription = stringResource(R.string.feature_tailor_impl_back),
                trailing = (uiState as? TailorUiState.Success)?.let { { RegenerateButton(it, interaction) } },
            )
        },
        bottomBar = { TailorBottomBar(uiState, actions, interaction) },
        bottomBarNotice = if (uiState is TailorUiState.Success && uiState.openCount > 0) {
            { DecideNote() }
        } else {
            null
        },
        snackbarHost = { TmrToastHost(toastState) },
    ) { padding ->
        TmrContentSwitch(targetState = uiState, contentKey = { it::class }) { state ->
            TailorBody(state, actions, interaction, padding)
        }
    }
    (uiState as? TailorUiState.Success)?.let { ReviewOverlays(it, actions, interaction) }
}

private fun TailorUiState.job(): JobHeader? = when (this) {
    is TailorUiState.Loading -> job
    is TailorUiState.Failed -> job
    is TailorUiState.Success -> job
    TailorUiState.NotFound -> null
}

@Composable
private fun headerSubtitle(state: TailorUiState): String? {
    if (state is TailorUiState.Failed) return null
    val job = state.job() ?: return null
    return stringResource(
        R.string.feature_tailor_impl_job_subtitle,
        job.title.trim().ifEmpty { stringResource(R.string.feature_tailor_impl_role_not_set) },
        job.company.trim().ifEmpty { stringResource(R.string.feature_tailor_impl_company_not_set) },
    )
}

@Composable
private fun headerTitle(state: TailorUiState): String {
    val used = when (state) {
        is TailorUiState.Success -> MAX_REGENERATIONS - state.regenerationsLeft
        else -> 0
    }
    return stringResource(
        when (used) {
            1 -> R.string.feature_tailor_impl_title_review_draft_2nd
            2 -> R.string.feature_tailor_impl_title_review_draft_3rd
            else -> R.string.feature_tailor_impl_title_review
        },
    )
}

@Composable
private fun RegenerateButton(state: TailorUiState.Success, interaction: ReviewInteraction) {
    TmrIconButton(
        icon = TmrIcons.Refresh,
        contentDescription = if (state.regenerationsLeft == 0) {
            stringResource(R.string.feature_tailor_impl_menu_regenerate_none)
        } else {
            pluralStringResource(
                R.plurals.feature_tailor_impl_regen_open_description,
                state.regenerationsLeft,
                state.regenerationsLeft,
            )
        },
        onClick = {
            if (state.regenerationsLeft > 0) {
                state.sections.filterIsInstance<ReviewSection.Entries>()
                    .firstOrNull { it.changeCount > 0 }
                    ?.let { section -> interaction.regenerateCategory = section.category }
            } else {
                interaction.limitSheetOpen = true
            }
        },
    )
}

private fun ReviewToastState.messageRes(): Int = when (this) {
    is ReviewToastState.Accepted -> R.string.feature_tailor_impl_toast_accepted
    ReviewToastState.Reported -> R.string.feature_tailor_impl_report_thanks
    ReviewToastState.RegenerateNoCredit -> R.string.feature_tailor_impl_regenerate_no_credit
    ReviewToastState.RegenerateFailed -> R.string.feature_tailor_impl_regenerate_failed
}

private fun ReviewToastState.messageArgs(): Array<Any> = when (this) {
    is ReviewToastState.Accepted -> arrayOf(position)
    ReviewToastState.Reported, ReviewToastState.RegenerateNoCredit, ReviewToastState.RegenerateFailed -> emptyArray()
}

@Composable
private fun TailorBody(
    uiState: TailorUiState,
    actions: TailorActions,
    interaction: ReviewInteraction,
    padding: PaddingValues,
) {
    when (uiState) {
        is TailorUiState.Loading -> LoadingContent(padding, uiState)
        is TailorUiState.Failed -> FailedContent(
            padding = padding,
            title = stringResource(R.string.feature_tailor_impl_failed_title),
            body = stringResource(R.string.feature_tailor_impl_failed_body),
        )
        TailorUiState.NotFound -> FailedContent(
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
private fun LoadingContent(padding: PaddingValues, state: TailorUiState.Loading) {
    val steps = listOf(
        GenerationStep(
            title = stringResource(R.string.feature_tailor_impl_loading_step_pick),
            detail = state.factCount?.let {
                pluralStringResource(R.plurals.feature_tailor_impl_loading_step_pick_detail, it, it)
            },
            status = stringResource(R.string.feature_tailor_impl_status_done),
            mark = StepMark.Done,
        ),
        GenerationStep(
            title = stringResource(R.string.feature_tailor_impl_loading_step_write),
            detail = state.lineCount?.let {
                pluralStringResource(R.plurals.feature_tailor_impl_loading_step_write_detail, it, it)
            },
            status = stringResource(R.string.feature_tailor_impl_status_in_progress),
            mark = StepMark.Now,
        ),
        GenerationStep(
            title = stringResource(R.string.feature_tailor_impl_loading_step_check),
            detail = null,
            status = stringResource(R.string.feature_tailor_impl_status_up_next),
            mark = StepMark.Waiting,
        ),
    )
    LazyColumn(
        contentPadding = PaddingValues(
            start = TmrTheme.spacing.gutter,
            end = TmrTheme.spacing.gutter,
            top = padding.calculateTopPadding() + TmrTheme.spacing.md,
            bottom = padding.calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
    ) {
        item { GenerationSteps(steps) }
        item { NoteLine(text = stringResource(R.string.feature_tailor_impl_loading_caption), icon = TmrIcons.Clock) }
    }
}

@Composable
private fun FailedContent(padding: PaddingValues, title: String, body: String) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = TmrTheme.spacing.gutter,
            end = TmrTheme.spacing.gutter,
            top = padding.calculateTopPadding(),
            bottom = padding.calculateBottomPadding(),
        ),
    ) {
        item { StatusCard(kind = TmrSpotKind.Error, title = title, body = body) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReviewOverlays(state: TailorUiState.Success, actions: TailorActions, interaction: ReviewInteraction) {
    val closeSheet = {
        interaction.openBulletId = null
        actions.onBulletSheetClosed()
    }
    val openItem = interaction.openBulletId?.let { id -> state.changes.firstOrNull { it.bullet.id == id } }
    val editItem = interaction.editBulletId?.let { id -> state.changes.firstOrNull { it.bullet.id == id } }
    val sourceItem = interaction.sourceBulletId?.let { id -> state.allBullets().firstOrNull { it.bullet.id == id } }
    if (openItem != null && editItem == null && sourceItem == null) {
        TmrBottomSheet(onDismissRequest = closeSheet) {
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
        TmrBottomSheet(onDismissRequest = { interaction.editBulletId = null }) {
            EditByHandContent(
                text = interaction.editText,
                linkedFactIds = editItem.sources.map { it.displayId }.distinct(),
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
        TmrBottomSheet(onDismissRequest = { interaction.sourceBulletId = null }) {
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
                onReviewChange = {
                    interaction.sourceBulletId = null
                    interaction.openBulletId = sourceItem.bullet.id
                },
                onClose = { interaction.sourceBulletId = null },
            )
        }
    }
    interaction.regenerateCategory?.let { category -> RegenerateDialog(state, category, actions, interaction) }
    if (interaction.limitSheetOpen) RegenerateLimitSheet(interaction)
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

@OptIn(ExperimentalMaterial3Api::class)
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
    TmrBottomSheet(onDismissRequest = { interaction.regenerateCategory = null }) {
        RegenerateSheet(
            title = stringResource(R.string.feature_tailor_impl_regenerate_title, stringResource(category.headingRes())),
            message = pluralStringResource(
                R.plurals.feature_tailor_impl_regenerate_message,
                changeCount,
                changeCount,
                used,
                MAX_REGENERATIONS,
            ),
            regenerationsLeft = state.regenerationsLeft,
            confirmLabel = stringResource(R.string.feature_tailor_impl_regenerate_confirm, used, MAX_REGENERATIONS),
            cancelDescription = stringResource(R.string.feature_tailor_impl_regen_cancel_description),
            onCancel = { interaction.regenerateCategory = null },
            onConfirm = {
                actions.onRegenerate(category)
                interaction.regenerateCategory = null
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegenerateLimitSheet(interaction: ReviewInteraction) {
    TmrBottomSheet(onDismissRequest = { interaction.limitSheetOpen = false }) {
        StatusPill(
            label = stringResource(R.string.feature_tailor_impl_regen_limit_chip),
            icon = TmrIcons.Refresh,
            color = TmrTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_regen_limit_title),
            style = TmrTheme.typography.headlineM,
            color = TmrTheme.colors.onSurface,
            modifier = Modifier.fillMaxWidth(),
        )
        NoticeStrip(
            text = stringResource(R.string.feature_tailor_impl_regenerations_used),
            icon = TmrIcons.Info,
            tone = BannerTone.Warn,
        )
        TmrPrimaryButton(
            label = stringResource(R.string.feature_tailor_impl_regen_limit_got_it),
            onClick = { interaction.limitSheetOpen = false },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun RegenerateSheet(
    title: String,
    message: String,
    regenerationsLeft: Int,
    confirmLabel: String,
    cancelDescription: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    val colors = TmrTheme.colors
    Text(text = title, style = TmrTheme.typography.headlineM, color = colors.onSurface, modifier = Modifier.fillMaxWidth())
    Text(text = message, style = TmrTheme.typography.bodyL, color = colors.onSurfaceVariant, modifier = Modifier.fillMaxWidth())
    NoticeStrip(
        text = pluralStringResource(
            R.plurals.feature_tailor_impl_regen_left_title,
            regenerationsLeft,
            regenerationsLeft,
        ),
        icon = TmrIcons.Info,
        modifier = Modifier.padding(vertical = TmrTheme.spacing.xs),
    )
    TmrPrimaryButton(
        label = confirmLabel,
        onClick = onConfirm,
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = TmrIcons.Refresh,
    )
    TmrSecondaryButton(label = cancelDescription, onClick = onCancel, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun TailorBottomBar(uiState: TailorUiState, actions: TailorActions, interaction: ReviewInteraction) {
    when (uiState) {
        is TailorUiState.Loading -> TmrBottomActionBar {
            TmrSecondaryButton(
                label = stringResource(R.string.feature_tailor_impl_loading_leave),
                onClick = actions.onBack,
                modifier = Modifier.weight(1f),
            )
        }
        is TailorUiState.Failed -> TmrBottomActionBar(stacked = true) {
            TmrSecondaryButton(
                label = stringResource(R.string.feature_tailor_impl_failed_back),
                onClick = actions.onBack,
                modifier = Modifier.fillMaxWidth(),
            )
            TmrPrimaryButton(
                label = stringResource(R.string.feature_tailor_impl_failed_retry),
                onClick = actions.onRetry,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        TailorUiState.NotFound -> TmrBottomActionBar {
            TmrSecondaryButton(
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
    val hasOpenChanges = state.openCount > 0
    TmrBottomActionBar {
        TmrPrimaryButton(
            label = stringResource(
                when {
                    !hasOpenChanges -> R.string.feature_tailor_impl_preview_and_export
                    state.reviewedCount == 0 -> R.string.feature_tailor_impl_review_changes
                    else -> R.string.feature_tailor_impl_bullet_next_change
                },
            ),
            onClick = {
                if (hasOpenChanges) {
                    state.nextOpenChange()?.let { next -> interaction.openBulletId = next.bullet.id }
                } else {
                    actions.onPreviewExport()
                }
            },
            enabled = !hasOpenChanges || state.canPreviewExport,
            trailingIcon = if (hasOpenChanges) null else TmrIcons.ArrowForward,
            modifier = Modifier.weight(1f),
        )
    }
}
