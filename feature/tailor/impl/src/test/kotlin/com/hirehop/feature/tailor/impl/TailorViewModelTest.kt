package com.hirehop.feature.tailor.impl

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.UpdateBulletDecisionUseCase
import com.hirehop.core.model.BulletDecision
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.GuardrailViolation
import com.hirehop.core.model.TailoredBullet
import com.hirehop.core.testing.repository.TestApplicationRepository
import com.hirehop.core.testing.repository.TestProfileRepository
import com.hirehop.core.testing.util.MainDispatcherRule
import com.hirehop.feature.tailor.impl.document.ResumeDocument
import com.hirehop.feature.tailor.impl.document.ResumeDocumentAssembler
import com.hirehop.feature.tailor.impl.export.ResumePdfRenderer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.IOException
import kotlin.time.Clock

class TailorViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val applicationRepository = TestApplicationRepository()
    private val profileRepository = TestProfileRepository()
    private val renderer = FakeResumePdfRenderer()
    private lateinit var viewModel: TailorViewModel

    private val reviewable = testBullet("r1", original = "Built a tool", proposed = "Developed a tool")
    private val alreadyRejected = testBullet(
        id = "r2",
        original = "Wrote tests",
        proposed = "Authored tests",
        decision = BulletDecision.REJECTED,
    )
    private val unchanged = testBullet("u1", original = "Wrote SQL", proposed = "Wrote SQL")
    private val violating = testBullet(
        id = "v1",
        original = "Helped a team",
        proposed = "Led a team of 30",
        violations = listOf(GuardrailViolation.UnsupportedNumber("30")),
    )
    private val hidden = testBullet("h1", entryId = "exp-hidden", original = "Old job", proposed = "Older job")

    private val profile = testProfile(
        listOf(
            testEntry("exp-1", bullets = sourceBullets("r1", "r2", "u1", "v1")),
            testEntry("exp-hidden", isConfirmed = false, bullets = sourceBullets("h1")),
            testEntry("edu-1", EntryCategory.EDUCATION),
        ),
    )

    @Before
    fun setup() {
        viewModel = TailorViewModel(
            applicationRepository = applicationRepository,
            profileRepository = profileRepository,
            updateBulletDecision = UpdateBulletDecisionUseCase(applicationRepository, Clock.System),
            assembler = ResumeDocumentAssembler(),
            pdfRenderer = renderer,
            applicationId = "app-1",
        )
    }

    private fun TestScope.collectUiState() {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
    }

    private fun sendData(bullets: List<TailoredBullet>) {
        applicationRepository.sendApplications(listOf(testApplication(bullets)))
        profileRepository.sendProfile(profile)
    }

    private fun success(): TailorUiState.Success {
        val state = viewModel.uiState.value
        assertThat(state).isInstanceOf(TailorUiState.Success::class.java)
        return state as TailorUiState.Success
    }

    private fun decisionOf(bulletId: String): BulletDecision =
        success().entries.flatMap { it.bullets }.first { it.bullet.id == bulletId }.bullet.decision

    @Test
    fun applicationId_isTheAssistedKey() {
        assertThat(viewModel.applicationId).isEqualTo("app-1")
    }

    @Test
    fun uiState_startsLoading() {
        assertThat(viewModel.uiState.value).isEqualTo(TailorUiState.Loading)
    }

    @Test
    fun uiState_isNotFoundWhenTheApplicationIsMissing() = runTest {
        collectUiState()

        applicationRepository.sendApplications(emptyList())
        profileRepository.sendProfile(profile)

        assertThat(viewModel.uiState.value).isEqualTo(TailorUiState.NotFound)
    }

    @Test
    fun uiState_groupsBulletsByConfirmedEntryOnly() = runTest {
        collectUiState()

        sendData(listOf(reviewable, unchanged, hidden))

        assertThat(success().entries.map { it.entryId }).containsExactly("exp-1")
        assertThat(success().entries.single().bullets.map { it.bullet.id }).containsExactly("r1", "u1").inOrder()
    }

    @Test
    fun uiState_countsOnlyBulletsThatNeedAReview() = runTest {
        collectUiState()

        sendData(listOf(reviewable, alreadyRejected, unchanged, violating))

        assertThat(success().totalCount).isEqualTo(2)
        assertThat(success().reviewedCount).isEqualTo(1)
    }

    @Test
    fun uiState_resolvesSourceTextForEachBullet() = runTest {
        collectUiState()

        sendData(listOf(reviewable))

        assertThat(success().entries.single().bullets.single().sourceTexts).containsExactly("Source text of r1")
    }

    @Test
    fun uiState_classifiesBulletKinds() = runTest {
        collectUiState()

        sendData(listOf(reviewable, unchanged, violating))

        val kinds = success().entries.single().bullets.associate { it.bullet.id to it.kind }
        assertThat(kinds).containsExactly(
            "r1", BulletReviewKind.REVIEWABLE,
            "u1", BulletReviewKind.UNCHANGED,
            "v1", BulletReviewKind.VIOLATION,
        )
    }

    @Test
    fun onAccept_marksTheBulletAcceptedAndCountsItAsReviewed() = runTest {
        collectUiState()
        sendData(listOf(reviewable, unchanged))

        viewModel.onAccept("r1")

        assertThat(decisionOf("r1")).isEqualTo(BulletDecision.ACCEPTED)
        assertThat(success().reviewedCount).isEqualTo(1)
        assertThat(success().totalCount).isEqualTo(1)
    }

    @Test
    fun onReject_marksTheBulletRejectedAndCountsItAsReviewed() = runTest {
        collectUiState()
        sendData(listOf(reviewable, unchanged))

        viewModel.onReject("r1")

        assertThat(decisionOf("r1")).isEqualTo(BulletDecision.REJECTED)
        assertThat(success().reviewedCount).isEqualTo(1)
    }

    @Test
    fun onAcceptAllSafeChanges_skipsViolatingUnchangedAndRejectedBullets() = runTest {
        collectUiState()
        sendData(listOf(reviewable, alreadyRejected, unchanged, violating))

        viewModel.onAcceptAllSafeChanges()

        assertThat(decisionOf("r1")).isEqualTo(BulletDecision.ACCEPTED)
        assertThat(decisionOf("r2")).isEqualTo(BulletDecision.REJECTED)
        assertThat(decisionOf("u1")).isEqualTo(BulletDecision.PENDING)
        assertThat(decisionOf("v1")).isEqualTo(BulletDecision.PENDING)
        assertThat(success().reviewedCount).isEqualTo(success().totalCount)
    }

    @Test
    fun documentInState_followsTheCurrentDecisions() = runTest {
        collectUiState()
        sendData(listOf(reviewable))

        viewModel.onAccept("r1")

        val bullets = success().document.sections.flatMap { it.entries }.flatMap { it.bullets }
        assertThat(bullets).contains("Developed a tool")
    }

    @Test
    fun onExport_rendersTheDocumentAndPublishesTheFile() = runTest {
        collectUiState()
        sendData(listOf(reviewable))
        viewModel.onAccept("r1")

        viewModel.onExport()

        val exportState = viewModel.exportState.value
        assertThat(exportState).isInstanceOf(ExportUiState.Ready::class.java)
        assertThat((exportState as ExportUiState.Ready).file.name).isEqualTo("Priya_Sharma_Acme_Backend_Engineer.pdf")
        assertThat(renderer.lastDocument?.sections?.flatMap { it.entries }?.flatMap { it.bullets })
            .contains("Developed a tool")
    }

    @Test
    fun onExport_reportsFailureWhenTheRendererFails() = runTest {
        collectUiState()
        sendData(listOf(reviewable))
        renderer.failWith = IOException("disk full")

        viewModel.onExport()

        assertThat(viewModel.exportState.value).isEqualTo(ExportUiState.Failed)
    }

    @Test
    fun onExportHandled_resetsTheExportState() = runTest {
        collectUiState()
        sendData(listOf(reviewable))
        viewModel.onExport()

        viewModel.onExportHandled()

        assertThat(viewModel.exportState.value).isEqualTo(ExportUiState.Idle)
    }

    @Test
    fun onExport_doesNothingBeforeTheStateLoads() = runTest {
        collectUiState()

        viewModel.onExport()

        assertThat(viewModel.exportState.value).isEqualTo(ExportUiState.Idle)
        assertThat(renderer.lastDocument).isNull()
    }
}

private class FakeResumePdfRenderer : ResumePdfRenderer {
    var lastDocument: ResumeDocument? = null
    var failWith: IOException? = null

    override suspend fun render(document: ResumeDocument, fileName: String): File {
        failWith?.let { throw it }
        lastDocument = document
        return File(fileName)
    }
}
