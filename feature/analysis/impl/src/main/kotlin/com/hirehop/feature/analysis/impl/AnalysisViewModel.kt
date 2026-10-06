package com.hirehop.feature.analysis.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.common.network.Dispatcher
import com.hirehop.core.common.network.HhDispatchers
import com.hirehop.core.common.network.di.ApplicationScope
import com.hirehop.core.data.connectivity.ConnectivityMonitor
import com.hirehop.core.data.repository.ContentReportRepository
import com.hirehop.core.data.repository.PrepPlanRepository
import com.hirehop.core.data.repository.ProfileRepository
import com.hirehop.core.data.repository.SessionRepository
import com.hirehop.core.data.repository.UsageAllowance
import com.hirehop.core.domain.AddUserStatedFactUseCase
import com.hirehop.core.domain.AnalyzeJobUseCase
import com.hirehop.core.domain.CreateApplicationUseCase
import com.hirehop.core.domain.JobAnalysisResult
import com.hirehop.core.domain.PaymentGateway
import com.hirehop.core.domain.onboarding.NextOnboardingStepUseCase
import com.hirehop.core.domain.onboarding.OnboardingStep
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.ContentReport
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.KeptJobDescription
import com.hirehop.core.model.MatchStatus
import com.hirehop.core.model.PrepPlanItem
import com.hirehop.core.model.ReportedItemKind
import com.hirehop.core.model.factCounts
import com.hirehop.core.navigation.PendingNavigation
import com.hirehop.feature.tailor.api.navigation.TailorNavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val profileRepository: ProfileRepository,
    private val nextOnboardingStep: NextOnboardingStepUseCase,
    private val analyzeJob: AnalyzeJobUseCase,
    private val addUserStatedFact: AddUserStatedFactUseCase,
    private val createApplication: CreateApplicationUseCase,
    private val prepPlanRepository: PrepPlanRepository,
    private val contentReportRepository: ContentReportRepository,
    private val usageAllowance: UsageAllowance,
    private val paymentGateway: PaymentGateway,
    private val clock: Clock,
    connectivityMonitor: ConnectivityMonitor,
    @param:Dispatcher(HhDispatchers.Default) private val computeDispatcher: CoroutineDispatcher,
    @param:ApplicationScope private val applicationScope: CoroutineScope,
) : ViewModel() {

    private val local = MutableStateFlow(Local())
    private val scenario = MutableStateFlow(DebugScenario.defaultValue)
    private val destinationChannel = Channel<AnalysisDestination>(Channel.BUFFERED)
    private var undoProfile: CandidateProfile? = null
    private var createdApplicationId: String? = null
    private var countedDraftKey: String? = null
    private var submitting = false

    val destinations: Flow<AnalysisDestination> = destinationChannel.receiveAsFlow()

    private val draftKey: Flow<String?> = local
        .map { (it.phase as? Phase.Ready)?.draftKey }
        .distinctUntilChanged()

    private val prepIds: Flow<Set<String>> = draftKey.flatMapLatest { key ->
        if (key == null) {
            flowOf(emptySet())
        } else {
            prepPlanRepository.observeItems(key).map { items -> items.map(PrepPlanItem::id).toSet() }
        }
    }

    private val reportedIds: Flow<Set<String>> = draftKey.flatMapLatest { key ->
        if (key == null) {
            flowOf(emptySet())
        } else {
            contentReportRepository.observeReportedIds(key, ReportedItemKind.REQUIREMENT)
        }
    }

    private val environment: Flow<Environment> = combine(
        scenario,
        connectivityMonitor.isOnline,
        paymentGateway.observeEntitlement(),
        usageAllowance.observeFreeTailoringsLeft(),
    ) { activeScenario, online, entitlement, freeTailoringsLeft ->
        Environment(
            scenario = activeScenario,
            online = online,
            freeCredits = entitlement.freeCredits,
            hasCredit = entitlement.totalCredits > 0,
            freeTailoringsLeft = freeTailoringsLeft,
        )
    }

    val uiState: StateFlow<AnalysisUiState> = combine(
        local,
        environment,
        prepIds,
        reportedIds,
    ) { state, env, prep, reported ->
        state.toUiState(env, prep, reported)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = AnalysisUiState.Loading,
    )

    init {
        load()
    }

    fun onEnter(key: DebugScenario) {
        scenario.value = key
    }

    fun onRetry() = load()

    fun onResume() {
        val ready = local.value.phase as? Phase.Ready ?: return
        if (local.value.tailoring || createdApplicationId != null) return
        viewModelScope.launch {
            if (currentProfile() != ready.profile) {
                undoProfile = null
                load()
            }
        }
    }

    fun onBackToJobDescription() {
        destinationChannel.trySend(AnalysisDestination.Leave(OnboardingStep.PasteJobDescription))
    }

    fun onOpenMenu(requirementId: String) = setOverlay(AnalysisOverlay.Menu(requirementId))

    fun onSeeSource(requirementId: String) = setOverlay(AnalysisOverlay.Source(requirementId))

    fun onIHaveThis(requirementId: String) = setOverlay(AnalysisOverlay.Question(requirementId))

    fun onOpenShareCard() = setOverlay(AnalysisOverlay.ShareCard)

    fun onDismissOverlay() = setOverlay(AnalysisOverlay.None)

    fun onReport(requirementId: String) {
        val ready = local.value.phase as? Phase.Ready ?: return
        setOverlay(AnalysisOverlay.None)
        viewModelScope.launch {
            attempt {
                contentReportRepository.report(
                    ContentReport(ready.draftKey, ReportedItemKind.REQUIREMENT, requirementId, clock.now()),
                )
            }.onSuccess { showToast(AnalysisToast.Reported) }
        }
    }

    fun onTogglePrepPlan(requirementId: String) {
        val ready = local.value.phase as? Phase.Ready ?: return
        val item = (uiState.value as? AnalysisUiState.Result)?.itemOrNull(requirementId) ?: return
        viewModelScope.launch {
            if (item.isInPrepPlan) {
                prepPlanRepository.remove(ready.draftKey, requirementId)
            } else {
                prepPlanRepository.add(ready.draftKey, PrepPlanItem(requirementId, item.requirement.text))
                showToast(AnalysisToast.PrepAdded(requirementId, item.requirement.text))
            }
        }
    }

    fun onToastDismiss() {
        local.update { it.copy(toast = null, closedId = null) }
    }

    fun onUndo() {
        when (val toast = local.value.toast) {
            is AnalysisToast.PrepAdded -> {
                val ready = local.value.phase as? Phase.Ready
                if (ready != null) viewModelScope.launch { prepPlanRepository.remove(ready.draftKey, toast.requirementId) }
                onToastDismiss()
            }
            AnalysisToast.GapClosed -> restoreProfile()
            else -> Unit
        }
    }

    fun onSubmitEvidence(requirementId: String, statement: String) {
        val ready = local.value.phase as? Phase.Ready ?: return
        val match = ready.analysis.gap.matches.firstOrNull { it.requirement.id == requirementId }
        val requirement = match?.requirement
        if (requirement == null || match.status == MatchStatus.MET || statement.isBlank() || submitting) return
        submitting = true
        viewModelScope.launch {
            try {
                attempt {
                    val preview = checkNotNull(addUserStatedFact.preview(requirement, statement)) { "Profile is missing" }
                    val previewed = refreshed(ready, preview)
                    val closed = previewed.analysis.gap.matches
                        .firstOrNull { it.requirement.id == requirementId }
                        ?.status != MatchStatus.GAP
                    if (closed) {
                        addUserStatedFact(requirement, statement)
                        refreshed(ready, checkNotNull(currentProfile()) { "Profile is missing" })
                    } else {
                        null
                    }
                }.fold(
                    onSuccess = { fresh ->
                        if (fresh != null) {
                            applyEvidence(ready, fresh, requirementId)
                        } else {
                            local.update {
                                it.copy(overlay = AnalysisOverlay.Question(requirementId, notClosed = true))
                            }
                        }
                    },
                    onFailure = { showToast(AnalysisToast.EvidenceFailed) },
                )
            } finally {
                submitting = false
            }
        }
    }

    fun onTailor() {
        val ready = local.value.phase as? Phase.Ready ?: return
        val state = uiState.value as? AnalysisUiState.Result ?: return
        if (!state.canTailor) return
        local.update { it.copy(tailoring = true, overlay = AnalysisOverlay.None) }
        applicationScope.launch {
            attempt { tailor(ready) }.onFailure {
                local.update { it.copy(tailoring = false) }
                showToast(AnalysisToast.TailorFailed)
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            local.update { it.copy(phase = Phase.Loading) }
            val kept = sessionRepository.observeKeptJobDescription().first()
            if (kept == null) {
                leave(OnboardingStep.PasteJobDescription)
                return@launch
            }
            val step = nextOnboardingStep()
            if (step !is OnboardingStep.GapAnalysis) {
                leave(step)
                return@launch
            }
            val profile = currentProfile()
            if (profile == null) {
                local.update { it.copy(phase = Phase.Failed(kept)) }
                return@launch
            }
            local.update { it.copy(phase = Phase.Analyzing(kept, profile.confirmedFactCount())) }
            attempt { analyze(kept, profile) }.fold(
                onSuccess = { analysis ->
                    if (analysis.gap.keywordCoverage.total > 0) countAnalysisOnce(kept)
                    local.update { it.copy(phase = Phase.Ready(kept, profile, analysis)) }
                },
                onFailure = { local.update { it.copy(phase = Phase.Failed(kept)) } },
            )
        }
    }

    private suspend fun countAnalysisOnce(kept: KeptJobDescription) {
        if (countedDraftKey == kept.draftKey) return
        countedDraftKey = kept.draftKey
        usageAllowance.consumeAnalysis()
    }

    private fun leave(step: OnboardingStep) {
        destinationChannel.trySend(AnalysisDestination.Leave(step))
    }

    private suspend fun tailor(ready: Phase.Ready) {
        if (!claimFreeTailoring()) {
            local.update { it.copy(tailoring = false, tailorLimitHit = true) }
            return
        }
        val applicationId = createdApplicationId ?: createApplicationFor(ready).also { createdApplicationId = it }
        sessionRepository.clearKeptJobDescription()
        if (sessionRepository.observeOnboardingComplete().first()) {
            destinationChannel.send(AnalysisDestination.Tailor(applicationId))
        } else {
            PendingNavigation.set(listOf(TailorNavKey(applicationId)))
            sessionRepository.markOnboardingComplete()
        }
    }

    private suspend fun claimFreeTailoring(): Boolean {
        if (local.value.freeTailoringCounted) return true
        if (paymentGateway.observeEntitlement().first().totalCredits > 0) return true
        val allowed = usageAllowance.consumeFreeTailoring()
        if (allowed) local.update { it.copy(freeTailoringCounted = true) }
        return allowed
    }

    private suspend fun createApplicationFor(ready: Phase.Ready): String {
        val applicationId = withContext(computeDispatcher) {
            createApplication(ready.profile, ready.analysis, ready.kept)
        }
        prepPlanRepository.observeItems(ready.draftKey).first().forEach { item ->
            prepPlanRepository.add(applicationId, item)
        }
        prepPlanRepository.clearFor(ready.draftKey)
        contentReportRepository.observeReports(ready.draftKey).first().forEach { report ->
            contentReportRepository.report(report.copy(applicationId = applicationId))
        }
        contentReportRepository.clearFor(ready.draftKey)
        return applicationId
    }

    private suspend fun applyEvidence(before: Phase.Ready, fresh: Phase.Ready, requirementId: String) {
        undoProfile = before.profile
        dropClosedGapsFromPrepPlan(fresh)
        local.update {
            it.copy(
                phase = fresh,
                overlay = AnalysisOverlay.None,
                closedId = requirementId,
            )
        }
        showToast(AnalysisToast.GapClosed)
    }

    private suspend fun dropClosedGapsFromPrepPlan(fresh: Phase.Ready) {
        val gapIds = fresh.gapIds()
        prepPlanRepository.observeItems(fresh.draftKey).first()
            .filter { it.id !in gapIds }
            .forEach { prepPlanRepository.remove(fresh.draftKey, it.id) }
    }

    private fun restoreProfile() {
        val ready = local.value.phase as? Phase.Ready ?: return
        val snapshot = undoProfile ?: return
        undoProfile = null
        viewModelScope.launch {
            attempt {
                profileRepository.saveProfile(snapshot)
                refreshed(ready, snapshot)
            }.fold(
                onSuccess = { fresh ->
                    dropClosedGapsFromPrepPlan(fresh)
                    local.update { it.copy(phase = fresh) }
                    onToastDismiss()
                },
                onFailure = { showToast(AnalysisToast.EvidenceFailed) },
            )
        }
    }

    private suspend fun refreshed(ready: Phase.Ready, profile: CandidateProfile): Phase.Ready =
        ready.copy(profile = profile, analysis = analyze(ready.kept, profile))

    private suspend fun analyze(kept: KeptJobDescription, profile: CandidateProfile): JobAnalysisResult =
        withContext(computeDispatcher) { analyzeJob(profile, kept.text) }

    private suspend fun currentProfile(): CandidateProfile? = profileRepository.observeProfile().first()

    private fun setOverlay(overlay: AnalysisOverlay) {
        local.update { it.copy(overlay = overlay) }
    }

    private fun showToast(toast: AnalysisToast) {
        local.update { it.copy(toast = toast) }
    }

    private sealed interface Phase {
        val label: JobLabel get() = JobLabel()
        val facts: Int get() = 0

        data object Loading : Phase

        data class Analyzing(val kept: KeptJobDescription, val factCount: Int) : Phase {
            override val label: JobLabel get() = JobLabel(kept.role, kept.company)
            override val facts: Int get() = factCount
        }

        data class Failed(val kept: KeptJobDescription) : Phase {
            override val label: JobLabel get() = JobLabel(kept.role, kept.company)
        }

        data class Ready(
            val kept: KeptJobDescription,
            val profile: CandidateProfile,
            val analysis: JobAnalysisResult,
        ) : Phase {
            override val label: JobLabel
                get() = JobLabel(
                    title = kept.role.ifBlank { analysis.job.title },
                    company = kept.company.ifBlank { analysis.job.company },
                )
            override val facts: Int get() = profile.confirmedFactCount()

            val draftKey: String get() = kept.draftKey

            fun gapIds(): Set<String> = analysis.gap.matches
                .filter { it.status == MatchStatus.GAP }
                .map { it.requirement.id }
                .toSet()
        }
    }

    private data class Environment(
        val scenario: DebugScenario,
        val online: Boolean,
        val freeCredits: Int,
        val hasCredit: Boolean,
        val freeTailoringsLeft: Int,
    )

    private data class Local(
        val phase: Phase = Phase.Loading,
        val overlay: AnalysisOverlay = AnalysisOverlay.None,
        val toast: AnalysisToast? = null,
        val closedId: String? = null,
        val tailoring: Boolean = false,
        val freeTailoringCounted: Boolean = false,
        val tailorLimitHit: Boolean = false,
    ) {
        fun toUiState(env: Environment, prepIds: Set<String>, reportedIds: Set<String>): AnalysisUiState {
            val label = phase.label
            val factCount = phase.facts
            return when {
                env.scenario == DebugScenario.LOADING -> AnalysisUiState.Analyzing(label, factCount)
                phase is Phase.Loading -> AnalysisUiState.Loading
                env.scenario == DebugScenario.ERROR -> AnalysisUiState.Failed(label)
                env.scenario == DebugScenario.EMPTY -> AnalysisUiState.DailyLimit(label)
                phase is Phase.Analyzing -> AnalysisUiState.Analyzing(label, factCount)
                phase is Phase.Failed -> AnalysisUiState.Failed(label)
                phase is Phase.Ready -> AnalysisUiState.Result(
                    job = label,
                    keywordCoverage = phase.analysis.gap.keywordCoverage,
                    sections = phase.analysis.gap.matches.toSections(phase.profile, prepIds, reportedIds),
                    freeCredits = env.freeCredits,
                    isOffline = env.scenario == DebugScenario.OFFLINE || !env.online,
                    tailorLimitReached = env.scenario == DebugScenario.PENDING || tailorLimitHit ||
                        (!env.hasCredit && env.freeTailoringsLeft <= 0 && !freeTailoringCounted),
                    isTailoring = tailoring,
                    overlay = overlay,
                    toast = toast,
                    closedRequirementId = closedId,
                )
                else -> AnalysisUiState.Loading
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

private fun CandidateProfile.confirmedFactCount(): Int = factCounts().confirmed

private suspend inline fun <T> attempt(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (failure: Exception) {
    Result.failure(failure)
}
