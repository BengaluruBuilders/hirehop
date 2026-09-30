package com.hirehop.feature.tailor.impl

import com.hirehop.core.model.BulletDecision
import com.hirehop.core.model.EditType
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.TailoredBullet
import com.hirehop.feature.tailor.impl.document.ResumeDocument
import java.io.File

internal sealed interface TailorUiState {
    data object Loading : TailorUiState

    data object NotFound : TailorUiState

    data class Success(
        val entries: List<TailorEntryUi>,
        val reviewedCount: Int,
        val totalCount: Int,
        val canExport: Boolean,
        val document: ResumeDocument,
        val exportFileName: String,
    ) : TailorUiState {
        val safeChangeBulletIds: List<String>
            get() = entries.flatMap { it.bullets }
                .filter { it.kind == BulletReviewKind.REVIEWABLE && it.bullet.decision == BulletDecision.PENDING }
                .map { it.bullet.id }
    }
}

internal data class TailorEntryUi(
    val entryId: String,
    val category: EntryCategory,
    val title: String,
    val organization: String,
    val bullets: List<TailorBulletUi>,
)

internal data class TailoredBulletSource(
    val id: String,
    val text: String,
    val source: FactSource,
)

internal data class TailorBulletUi(
    val bullet: TailoredBullet,
    val sourceTexts: List<String>,
    val sources: List<TailoredBulletSource>,
    val isStale: Boolean,
) {
    val kind: BulletReviewKind = bullet.reviewKind(isStale)
}

internal enum class BulletReviewKind { STALE, VIOLATION, UNCHANGED, REVIEWABLE }

internal fun TailoredBullet.reviewKind(isStale: Boolean): BulletReviewKind = when {
    isStale -> BulletReviewKind.STALE
    violations.isNotEmpty() -> BulletReviewKind.VIOLATION
    proposedText.trim() == originalText.trim() && EditType.REORDER !in editTypes -> BulletReviewKind.UNCHANGED
    else -> BulletReviewKind.REVIEWABLE
}

internal sealed interface ExportUiState {
    data object Idle : ExportUiState

    data object Exporting : ExportUiState

    data class Ready(val file: File) : ExportUiState

    data object Failed : ExportUiState
}
