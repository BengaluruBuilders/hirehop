package com.hirehop.feature.tailor.impl

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhBottomSheet
import com.hirehop.core.designsystem.component.HhContentSwitch
import com.hirehop.core.designsystem.component.HhDecoration
import com.hirehop.core.designsystem.component.HhDecorationKind
import com.hirehop.core.designsystem.component.HhIconActionBar
import com.hirehop.core.designsystem.component.HhIconButton
import com.hirehop.core.designsystem.component.HhInkButton
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhMonogram
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhPillRow
import com.hirehop.core.designsystem.component.HhPillRowStyle
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhSegmentedCounter
import com.hirehop.core.designsystem.component.HhSpotIllustration
import com.hirehop.core.designsystem.component.HhSpotKind
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
                title = headerRole(uiState),
                subtitle = headerCompany(uiState),
                onBack = actions.onBack,
                backContentDescription = stringResource(R.string.feature_tailor_impl_back),
                belowTitle = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
                    ) {
                        HeaderPill(headerPill(uiState))
                        HeaderIdentity(uiState)
                    }
                },
                extended = false,
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

private fun TailorUiState.job(): JobHeader? = when (this) {
    is TailorUiState.Loading -> job
    is TailorUiState.Failed -> job
    is TailorUiState.Success -> job
    TailorUiState.NotFound -> null
}

@Composable
private fun headerRole(state: TailorUiState): String {
    val job = state.job() ?: return stringResource(R.string.feature_tailor_impl_title_review)
    return job.title.trim().ifEmpty { stringResource(R.string.feature_tailor_impl_role_not_set) }
}

@Composable
private fun headerCompany(state: TailorUiState): String? {
    val job = state.job() ?: return null
    return job.company.trim().ifEmpty { stringResource(R.string.feature_tailor_impl_company_not_set) }
}

@Composable
private fun headerPill(state: TailorUiState): String {
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
private fun HeaderPill(text: String) {
    val colors = HhTheme.colors
    Box(
        modifier = Modifier
            .background(colors.brandPressed, HhTheme.shapes.pill)
            .heightIn(min = HhTheme.spacing.d32)
            .padding(horizontal = HhTheme.spacing.md + HhTheme.spacing.d2),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = HhTheme.typography.labelL, color = colors.onBrand)
    }
}

@Composable
private fun HeaderIdentity(state: TailorUiState) {
    val company = state.job()?.company?.trim().orEmpty()
    if (company.isEmpty()) return
    val success = state as? TailorUiState.Success ?: return
    val progressText = if (success.totalCount == 0) {
        stringResource(R.string.feature_tailor_impl_no_changes)
    } else {
        stringResource(
            R.string.feature_tailor_impl_progress_description,
            success.reviewedCount,
            success.totalCount,
        )
    }
    if (LocalDensity.current.fontScale >= LARGE_FONT_SCALE) {
        Text(
            text = progressText,
            style = HhTheme.typography.labelL,
            color = HhTheme.colors.onHeader,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        return
    }
    Row(
        modifier = Modifier.padding(top = HhTheme.spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HhMonogram(text = company, size = HhTheme.spacing.d48)
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs)) {
            Text(
                text = progressText,
                style = HhTheme.typography.labelL,
                color = HhTheme.colors.onHeader,
            )
            HhSegmentedCounter(current = success.reviewedCount, total = success.totalCount)
        }
    }
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
        is TailorUiState.Loading -> LoadingContent(padding)
        is TailorUiState.Failed -> FailedContent(
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
private fun LoadingContent(padding: PaddingValues) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = HhTheme.spacing.gutter,
            end = HhTheme.spacing.gutter,
            top = padding.calculateTopPadding() + HhTheme.spacing.d32 + HhTheme.spacing.sm,
            bottom = padding.calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        item {
            Text(
                text = stringResource(R.string.feature_tailor_impl_loading_title),
                style = HhTheme.typography.headlineM,
                color = HhTheme.colors.onSurface,
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm + HhTheme.spacing.d2)) {
                val pickTitle = stringResource(R.string.feature_tailor_impl_loading_step_pick)
                val pickSubtitle = stringResource(R.string.feature_tailor_impl_status_done)
                HhPillRow(
                    title = pickTitle,
                    onClick = {},
                    style = HhPillRowStyle.Jade,
                    subtitle = pickSubtitle,
                    icon = HhIcons.CheckCircle,
                    trailingIcon = null,
                    modifier = Modifier.clearAndSetSemantics { contentDescription = "$pickTitle. $pickSubtitle" },
                )
                val writeTitle = stringResource(R.string.feature_tailor_impl_loading_step_write)
                val writeSubtitle = stringResource(R.string.feature_tailor_impl_status_in_progress)
                HhPillRow(
                    title = writeTitle,
                    onClick = {},
                    style = HhPillRowStyle.Marigold,
                    subtitle = writeSubtitle,
                    icon = HhIcons.Edit,
                    trailingIcon = null,
                    modifier = Modifier.clearAndSetSemantics { contentDescription = "$writeTitle. $writeSubtitle" },
                )
                val checkTitle = stringResource(R.string.feature_tailor_impl_loading_step_check)
                val checkSubtitle = stringResource(R.string.feature_tailor_impl_status_up_next)
                HhPillRow(
                    title = checkTitle,
                    onClick = {},
                    style = HhPillRowStyle.Neutral,
                    subtitle = checkSubtitle,
                    icon = HhIcons.Clock,
                    trailingIcon = null,
                    modifier = Modifier.clearAndSetSemantics { contentDescription = "$checkTitle. $checkSubtitle" },
                )
            }
        }
        item { LoadingNotice() }
    }
}

@Composable
private fun LoadingNotice() {
    val colors = HhTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.primaryContainer, HhTheme.shapes.card)
            .padding(HhTheme.spacing.cardPadding),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(HhTheme.spacing.d40)
                .background(colors.background, HhTheme.shapes.pill)
                .clearAndSetSemantics {},
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = HhIcons.Bell,
                contentDescription = null,
                tint = colors.onPrimaryContainer,
                modifier = Modifier.size(HhTheme.spacing.d20),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.d2),
        ) {
            Text(
                text = stringResource(R.string.feature_tailor_impl_loading_notice_title),
                style = HhTheme.typography.titleM,
                color = colors.onPrimaryContainer,
            )
            Text(
                text = stringResource(R.string.feature_tailor_impl_loading_notice_body),
                style = HhTheme.typography.bodyM,
                color = colors.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun FailedContent(padding: PaddingValues, title: String, body: String) {
    val colors = HhTheme.colors
    LazyColumn(
        contentPadding = PaddingValues(
            start = HhTheme.spacing.gutter,
            end = HhTheme.spacing.gutter,
            top = padding.calculateTopPadding(),
            bottom = padding.calculateBottomPadding(),
        ),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = HhTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = HhTheme.spacing.d64 * 3),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(HhTheme.spacing.d64 * 2 + HhTheme.spacing.d24)
                            .background(colors.card, HhTheme.shapes.pill)
                            .border(BorderStroke(HhTheme.spacing.d2, colors.outlineVariant), HhTheme.shapes.pill),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        HhSpotIllustration(kind = HhSpotKind.Error, modifier = Modifier.align(Alignment.BottomCenter))
                    }
                    HhDecoration(
                        kind = HhDecorationKind.Zigzag,
                        color = colors.coral,
                        modifier = Modifier.align(Alignment.CenterStart),
                    )
                    HhDecoration(
                        kind = HhDecorationKind.Ring,
                        color = colors.special,
                        modifier = Modifier.align(Alignment.CenterEnd),
                    )
                }
                Row(
                    modifier = Modifier
                        .background(colors.error, HhTheme.shapes.pill)
                        .heightIn(min = HhTheme.spacing.d32 + HhTheme.spacing.d2)
                        .padding(
                            horizontal = HhTheme.spacing.md + HhTheme.spacing.d2,
                            vertical = HhTheme.spacing.xs,
                        ),
                    horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs, Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = HhIcons.Error,
                        contentDescription = null,
                        tint = colors.onError,
                        modifier = Modifier.size(HhTheme.spacing.d20),
                    )
                    Text(
                        text = stringResource(R.string.feature_tailor_impl_failed_chip),
                        style = HhTheme.typography.labelL,
                        color = colors.onError,
                    )
                }
                Text(
                    text = title,
                    style = HhTheme.typography.headlineL,
                    color = colors.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = body,
                    style = HhTheme.typography.bodyL,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun StatusContent(padding: PaddingValues, title: String, body: String) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = HhTheme.spacing.gutter,
            end = HhTheme.spacing.gutter,
            top = padding.calculateTopPadding(),
            bottom = padding.calculateBottomPadding(),
        ),
    ) {
        item { StatusCard(kind = HhSpotKind.Error, title = title, body = body) }
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
        HhBottomSheet(onDismissRequest = closeSheet) {
            BulletReviewSheetContent(
                item = openItem,
                position = state.changeIndexOf(openItem.bullet.id) + 1,
                total = state.totalCount,
                openCount = state.openCount,
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
    HhBottomSheet(onDismissRequest = { interaction.regenerateCategory = null }) {
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
    val colors = HhTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(HhTheme.spacing.d48 + HhTheme.spacing.d8)
                .background(colors.special, HhTheme.shapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = HhIcons.Edit,
                contentDescription = null,
                tint = colors.onSpecial,
                modifier = Modifier.size(HhTheme.spacing.d24),
            )
        }
        HhDecoration(kind = HhDecorationKind.Squiggle, color = colors.coral)
    }
    Text(text = title, style = HhTheme.typography.headlineM, color = colors.onSurface, modifier = Modifier.fillMaxWidth())
    Text(text = message, style = HhTheme.typography.bodyM, color = colors.onSurfaceVariant, modifier = Modifier.fillMaxWidth())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.card, HhTheme.shapes.pill)
            .border(BorderStroke(HhTheme.spacing.d2, colors.outlineVariant), HhTheme.shapes.pill)
            .heightIn(min = HhTheme.spacing.d64)
            .padding(horizontal = HhTheme.spacing.gutter, vertical = HhTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.clearAndSetSemantics {},
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.d4),
        ) {
            repeat(regenerationsLeft) {
                Box(
                    modifier = Modifier
                        .size(HhTheme.spacing.d16 + HhTheme.spacing.d2)
                        .background(colors.special, HhTheme.shapes.pill)
                        .border(BorderStroke(HhTheme.spacing.d2, colors.onSpecial), HhTheme.shapes.pill),
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs),
        ) {
            Text(
                text = pluralStringResource(
                    R.plurals.feature_tailor_impl_regen_left_title,
                    regenerationsLeft,
                    regenerationsLeft,
                ),
                style = HhTheme.typography.titleM,
                color = colors.onSurface,
            )
            Text(
                text = stringResource(R.string.feature_tailor_impl_regen_left_subtitle),
                style = HhTheme.typography.labelM,
                color = colors.onSurfaceVariant,
            )
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = HhTheme.spacing.d4),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm + HhTheme.spacing.d2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HhIconButton(
            icon = HhIcons.Close,
            contentDescription = cancelDescription,
            onClick = onCancel,
            containerColor = colors.card,
            borderColor = colors.outlineVariant,
            size = HhTheme.spacing.d48 + HhTheme.spacing.d12,
        )
        HhPrimaryButton(
            label = confirmLabel,
            onClick = onConfirm,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TailorBottomBar(uiState: TailorUiState, actions: TailorActions, interaction: ReviewInteraction) {
    when (uiState) {
        is TailorUiState.Loading -> HhBottomActionBar {
            HhInkButton(
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
    val hasOpenChanges = state.openCount > 0
    HhIconActionBar(
        secondaryIcon = HhIcons.Edit,
        secondaryContentDescription = if (state.regenerationsLeft == 0) {
            stringResource(R.string.feature_tailor_impl_menu_regenerate_none)
        } else {
            pluralStringResource(
                R.plurals.feature_tailor_impl_regen_open_description,
                state.regenerationsLeft,
                state.regenerationsLeft,
            )
        },
        onSecondaryClick = {
            if (state.regenerationsLeft > 0) {
                state.sections.filterIsInstance<ReviewSection.Entries>()
                    .firstOrNull { it.changeCount > 0 }
                    ?.let { section -> interaction.regenerateCategory = section.category }
            }
        },
        secondaryBadge = state.regenerationsLeft.toString(),
        primaryLabel = if (hasOpenChanges) {
            pluralStringResource(
                R.plurals.feature_tailor_impl_changes_left,
                state.openCount,
                state.openCount,
            )
        } else {
            stringResource(R.string.feature_tailor_impl_preview_and_export)
        },
        onPrimaryClick = {
            if (hasOpenChanges) {
                state.nextOpenChange()?.let { next -> interaction.openBulletId = next.bullet.id }
            } else {
                actions.onPreviewExport()
            }
        },
        primaryEnabled = !hasOpenChanges || state.canPreviewExport,
        primaryTrailingIcon = HhIcons.ArrowForward,
    )
}

private const val LARGE_FONT_SCALE = 1.5f
