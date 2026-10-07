package com.hirehop.feature.tailor.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.data.connectivity.ConnectivityMonitor
import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.data.repository.ContentReportRepository
import com.hirehop.core.data.repository.ProfileRepository
import com.hirehop.core.data.repository.TailoringReviewStateRepository
import com.hirehop.core.domain.UpdateBulletDecisionUseCase
import com.hirehop.core.model.BulletDecision
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.ContentReport
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.JobApplication
import com.hirehop.core.model.ReportedItemKind
import com.hirehop.core.model.TailoringReviewState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock

@HiltViewModel(assistedFactory = TailorViewModel.Factory::class)
internal class TailorViewModel @AssistedInject constructor(
    private val applicationRepository: ApplicationRepository,
    profileRepository: ProfileRepository,
    connectivityMonitor: ConnectivityMonitor,
    private val reviewStateRepository: TailoringReviewStateRepository,
    private val contentReportRepository: ContentReportRepository,
    private val clock: Clock,
    private val updateBulletDecision: UpdateBulletDecisionUseCase,
    private val handEditBullet: HandEditBulletUseCase,
    private val regenerateSection: RegenerateSectionUseCase,
    @Assisted val applicationId: String,
    @Assisted scenario: DebugScenario,
) : ViewModel() {

    private val decisionMutex = Mutex()

    private val activeScenario = MutableStateFlow(scenario)

    private val sessionState = combine(
        reviewStateRepository.observe(applicationId),
        contentReportRepository.observeReportedIds(applicationId, ReportedItemKind.RESUME_BULLET),
        contentReportRepository.observeReportedIds(applicationId, ReportedItemKind.SECTION),
    ) { review, bullets, sections -> ReviewSession(review, bullets + sections.map(::sectionReportId)) }

    val uiState: StateFlow<TailorUiState> = combine(
        applicationRepository.observeApplication(applicationId),
        profileRepository.observeProfile(),
        sessionState,
        connectivityMonitor.isOnline,
        activeScenario,
    ) { application, profile, session, online, forced ->
        stateFor(application, profile, session, online, forced)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TailorUiState.Loading(),
    )

    fun onAccept(bulletId: String) = setDecision(bulletId, BulletDecision.ACCEPTED)

    fun onKeepOriginal(bulletId: String) = setDecision(bulletId, BulletDecision.REJECTED)

    fun onUndo(bulletId: String) = setDecision(bulletId, BulletDecision.PENDING)

    fun onEditByHand(bulletId: String, text: String) {
        viewModelScope.launch {
            decisionMutex.withLock { handEditBullet(applicationId, bulletId, text) }
        }
    }

    fun onRegenerate(category: EntryCategory) {
        viewModelScope.launch {
            decisionMutex.withLock { regenerateSection(applicationId, category) }
        }
    }

    fun onReportBullet(bulletId: String) = report(ReportedItemKind.RESUME_BULLET, bulletId)

    fun onReportSection(sectionKey: String) = report(ReportedItemKind.SECTION, sectionKey)

    fun onRetry() {
        activeScenario.value = DebugScenario.DEFAULT
    }

    private fun report(kind: ReportedItemKind, itemId: String) {
        val success = uiState.value as? TailorUiState.Success ?: return
        val itemText = success.reportedText(kind, itemId) ?: return
        viewModelScope.launch {
            contentReportRepository.report(
                ContentReport(
                    applicationId = applicationId,
                    itemKind = kind,
                    itemId = itemId,
                    itemText = itemText,
                    reportedAt = clock.now(),
                    generationId = success.reportedGenerationId(kind, itemId),
                ),
            )
        }
    }

    private fun setDecision(bulletId: String, decision: BulletDecision) {
        viewModelScope.launch {
            decisionMutex.withLock { updateBulletDecision(applicationId, bulletId, decision) }
        }
    }

    private fun stateFor(
        application: JobApplication?,
        profile: CandidateProfile?,
        session: ReviewSession,
        online: Boolean,
        forced: DebugScenario,
    ): TailorUiState {
        val job = application?.job?.let { JobHeader(title = it.title, company = it.company) }
        return when (forced) {
            DebugScenario.LOADING -> TailorUiState.Loading(
                job = job,
                factCount = application?.tailoredResume?.bullets?.flatMap { it.sourceIds }?.distinct()?.size,
                lineCount = application?.tailoredResume?.bullets?.size,
            )
            DebugScenario.ERROR -> TailorUiState.Failed(job)
            else -> buildTailorUiState(
                TailorInputs(
                    application = application,
                    profile = profile,
                    isOffline = !online || forced == DebugScenario.OFFLINE,
                    regenerationsUsed = session.review.regenerationsUsed,
                    editedBulletIds = session.review.editedBulletIds,
                    reportedIds = session.reportedIds,
                ),
            )
        }
    }

    private data class ReviewSession(val review: TailoringReviewState, val reportedIds: Set<String>)

    @AssistedFactory
    interface Factory {
        fun create(applicationId: String, scenario: DebugScenario): TailorViewModel
    }
}
