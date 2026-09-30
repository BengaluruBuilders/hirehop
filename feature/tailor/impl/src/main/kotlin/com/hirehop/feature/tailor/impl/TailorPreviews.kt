package com.hirehop.feature.tailor.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.hirehop.core.designsystem.component.HhScaffold
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.BulletDecision
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.EditType
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.EvidenceBullet
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.GuardrailViolation
import com.hirehop.core.model.JobApplication
import com.hirehop.core.model.JobDescription
import com.hirehop.core.model.ProfileEntry
import com.hirehop.core.model.TailoredBullet
import com.hirehop.core.model.TailoredResume
import com.hirehop.feature.tailor.impl.document.ResumeDocumentAssembler
import kotlin.time.Instant

private val previewProfile = CandidateProfile(
    fullName = "Priya Sharma",
    email = "priya@example.com",
    phone = "+91 98765 43210",
    headline = "Final-year computer science student",
    skills = listOf("Kotlin", "Android", "SQL"),
    entries = listOf(
        ProfileEntry(
            id = "project-1",
            category = EntryCategory.PROJECT,
            title = "Campus Events App",
            organization = "Personal project",
            startDate = "Jan 2025",
            endDate = "Apr 2025",
            bullets = listOf(
                EvidenceBullet("b1", "Built an Android app that lists campus events"),
                EvidenceBullet("b2", "Wrote SQL queries for the event database"),
                EvidenceBullet("b3", "Helped 3 friends test the app"),
                EvidenceBullet("b4", "Presented the app at the college fest"),
            ),
            source = FactSource.IMPORTED,
            isConfirmed = true,
        ),
    ),
)

private val previewBullets = listOf(
    TailoredBullet(
        id = "t1",
        entryId = "project-1",
        originalText = "Built an Android app that lists campus events",
        proposedText = "Developed an Android app in Kotlin that lists campus events",
        sourceIds = listOf("b1"),
        editTypes = listOf(EditType.REWORD, EditType.EMPHASISE),
        keywordsUsed = listOf("Kotlin"),
        violations = emptyList(),
        decision = BulletDecision.PENDING,
    ),
    TailoredBullet(
        id = "t2",
        entryId = "project-1",
        originalText = "Wrote SQL queries for the event database",
        proposedText = "Wrote SQL queries for the event database",
        sourceIds = listOf("b2"),
        editTypes = emptyList(),
        keywordsUsed = emptyList(),
        violations = emptyList(),
        decision = BulletDecision.PENDING,
    ),
    TailoredBullet(
        id = "t3",
        entryId = "project-1",
        originalText = "Helped 3 friends test the app",
        proposedText = "Led a team of 30 testers",
        sourceIds = listOf("b3"),
        editTypes = emptyList(),
        keywordsUsed = emptyList(),
        violations = listOf(
            GuardrailViolation.UnsupportedNumber("30"),
            GuardrailViolation.VerbEscalation(from = "Helped", to = "Led"),
        ),
        decision = BulletDecision.PENDING,
    ),
    TailoredBullet(
        id = "t4",
        entryId = "project-1",
        originalText = "Presented the app at the college fest",
        proposedText = "Presented the app at the college fest",
        sourceIds = listOf("b4"),
        editTypes = listOf(EditType.REORDER),
        keywordsUsed = emptyList(),
        violations = emptyList(),
        decision = BulletDecision.PENDING,
    ),
    TailoredBullet(
        id = "t5",
        entryId = "project-1",
        originalText = "Old text that the user has since edited",
        proposedText = "Reworded old text",
        sourceIds = listOf("b5"),
        editTypes = listOf(EditType.REWORD),
        keywordsUsed = emptyList(),
        violations = emptyList(),
        decision = BulletDecision.PENDING,
    ),
)

private fun previewSuccess(decision: BulletDecision): TailorUiState.Success {
    val bullets = previewBullets.map { if (it.id == "t1") it.copy(decision = decision) else it }
    val application = JobApplication(
        id = "app-1",
        job = JobDescription("Android Developer", "Acme", "", emptyList()),
        status = ApplicationStatus.SAVED,
        notes = "",
        gapAnalysis = null,
        tailoredResume = TailoredResume(bullets),
        createdAt = Instant.fromEpochSeconds(0),
        updatedAt = Instant.fromEpochSeconds(0),
    )
    val state = buildTailorUiState(application, previewProfile, ResumeDocumentAssembler())
    return state as TailorUiState.Success
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ReviewPendingPreview() {
    HhTheme {
        HhScaffold { padding ->
            ReviewContent(
                state = previewSuccess(BulletDecision.PENDING),
                onAccept = {},
                onReject = {},
                onAcceptAllSafeChanges = {},
                contentPadding = padding,
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ReviewAcceptedPreview() {
    HhTheme {
        HhScaffold { padding ->
            ReviewContent(
                state = previewSuccess(BulletDecision.ACCEPTED),
                onAccept = {},
                onReject = {},
                onAcceptAllSafeChanges = {},
                contentPadding = padding,
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ReviewRejectedPreview() {
    HhTheme {
        HhScaffold { padding ->
            ReviewContent(
                state = previewSuccess(BulletDecision.REJECTED),
                onAccept = {},
                onReject = {},
                onAcceptAllSafeChanges = {},
                contentPadding = padding,
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 700)
@Composable
private fun ResumePreviewPreview() {
    HhTheme {
        HhScaffold { padding ->
            ResumePreviewContent(document = previewSuccess(BulletDecision.ACCEPTED).document, contentPadding = padding)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TailorScreenLoadingPreview() {
    HhTheme {
        TailorScreen(
            uiState = TailorUiState.Loading,
            exportState = ExportUiState.Idle,
            onBackClick = {},
            onAccept = {},
            onReject = {},
            onAcceptAllSafeChanges = {},
            onExport = {},
            onExportFailureShown = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TailorScreenNotFoundPreview() {
    HhTheme {
        TailorScreen(
            uiState = TailorUiState.NotFound,
            exportState = ExportUiState.Idle,
            onBackClick = {},
            onAccept = {},
            onReject = {},
            onAcceptAllSafeChanges = {},
            onExport = {},
            onExportFailureShown = {},
        )
    }
}
