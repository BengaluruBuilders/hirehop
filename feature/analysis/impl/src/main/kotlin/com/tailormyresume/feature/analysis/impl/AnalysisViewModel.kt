package com.tailormyresume.feature.analysis.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tailormyresume.core.common.network.Dispatcher
import com.tailormyresume.core.common.network.TmrDispatchers
import com.tailormyresume.core.common.network.di.ApplicationScope
import com.tailormyresume.core.data.connectivity.ConnectivityMonitor
import com.tailormyresume.core.data.repository.ContentReportRepository
import com.tailormyresume.core.data.repository.PrepPlanRepository
import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.data.repository.SessionRepository
import com.tailormyresume.core.data.repository.UsageAllowance
import com.tailormyresume.core.domain.AddUserStatedFactUseCase
import com.tailormyresume.core.domain.AiException
import com.tailormyresume.core.domain.AiFailure
import com.tailormyresume.core.domain.AnalyzeJobUseCase
import com.tailormyresume.core.domain.CreateApplicationUseCase
import com.tailormyresume.core.domain.GapMatcher
import com.tailormyresume.core.domain.IdGenerator
import com.tailormyresume.core.domain.JobAnalysisResult
import com.tailormyresume.core.domain.PaymentGateway
import com.tailormyresume.core.domain.SignInGateway
import com.tailormyresume.core.domain.isAiFailure
import com.tailormyresume.core.domain.onboarding.NextOnboardingStepUseCase
import com.tailormyresume.core.domain.onboarding.OnboardingStep
import com.tailormyresume.core.domain.upgradedMatch
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.ContentReport
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.KeptJobDescription
import com.tailormyresume.core.model.KeywordCoverage
import com.tailormyresume.core.model.MatchStatus
import com.tailormyresume.core.model.PrepPlanItem
import com.tailormyresume.core.model.ReportedItemKind
import com.tailormyresume.core.model.factCounts
import com.tailormyresume.core.navigation.PendingNavigation
import com.tailormyresume.feature.tailor.api.navigation.TailorNavKey
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
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val profileRepository: ProfileRepository,
    private val nextOnboardingStep: NextOnboardingStepUseCase,
    private val analyzeJob: AnalyzeJobUseCase,
    private val gapMatcher: GapMatcher,
    private val addUserStatedFact: AddUserStatedFactUseCase,
    private val createApplication: CreateApplicationUseCase,
    private val prepPlanRepository: PrepPlanRepository,
    private val contentReportRepository: ContentReportRepository,
    private val usageAllowance: UsageAllowance,
    private val paymentGateway: PaymentGateway,
    private val signInGateway: SignInGateway,
    private val clock: Clock,
    private val idGenerator: IdGenerator,
    private val savedState: SavedStateHandle,
    connectivityMonitor: ConnectivityMonitor,
    @param:Dispatcher(TmrDispatchers.Default) private val computeDispatcher: CoroutineDispatcher,
    @param:ApplicationScope private val applicationScope: CoroutineScope,
) : ViewModel() {

    private val local = MutableStateFlow(Local())
    private val scenario = MutableStateFlow(DebugScenario.defaultValue)
    private val destinationChannel = Channel<AnalysisDestination>(Channel.BUFFERED)
    private var undoProfile: CandidateProfile? = null
    private var createdApplicationId: String? = null
    private var countedDraftKey: String? = null
    private var submitting = false
    private var entered = false

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
            totalCredits = entitlement.totalCredits,
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
        state.toUiState(env, prep, reported, addUserStatedFact::nextFactId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = AnalysisUiState.Loading,
    )

    fun onEnter(key: DebugScenario) {
        scenario.value = key
        if (entered) return
        entered = true
        load()
    }

    fun onRetry() = load()

    fun onSignInAgain() {
        viewModelScope.launch { signInGateway.signOut() }
    }

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
        val itemText = (uiState.value as? AnalysisUiState.Result)?.itemOrNull(requirementId)?.requirement?.text ?: return
        setOverlay(AnalysisOverlay.None)
        viewModelScope.launch {
            attempt {
                contentReportRepository.report(
                    ContentReport(
                        ready.draftKey,
                        ReportedItemKind.REQUIREMENT,
                        requirementId,
                        itemText,
                        clock.now(),
                        ready.analysis.gap.generationId,
                    ),
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
        if (requirement == null || match.status != MatchStatus.GAP || statement.isBlank() || submitting) return
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
            attempt { tailor(ready) }.onFailure { failure ->
                if (failure.isAiFailure(AiFailure.NoCredit)) {
                    local.update { it.copy(tailoring = false, tailorLimitHit = true) }
                } else {
                    local.update { it.copy(tailoring = false) }
                    showToast(AnalysisToast.TailorFailed)
                }
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
            if (scenario.value in FORCED_WITHOUT_ANALYSIS) return@launch
            attempt { analyze(kept, profile) }.fold(
                onSuccess = { analysis ->
                    if (analysis.gap.keywordCoverage.total > 0) countAnalysisOnce(kept)
                    local.update { it.copy(phase = Phase.Ready(kept, profile, analysis, analysedAt = analysis.analysedAt ?: clock.now())) }
                },
                onFailure = { failure ->
                    val phase = if (failure.isAiFailure(AiFailure.AllowanceExhausted)) {
                        Phase.DailyLimit(kept)
                    } else {
                        Phase.Failed(kept, failure.toFailureCause())
                    }
                    local.update { it.copy(phase = phase) }
                },
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

    private fun applicationIdFor(ready: Phase.Ready): String {
        val key = APPLICATION_ID_KEY + ready.draftKey
        return savedState.get<String>(key) ?: idGenerator.newId().also { savedState[key] = it }
    }

    private suspend fun createApplicationFor(ready: Phase.Ready): String {
        val applicationId = withContext(computeDispatcher) {
            createApplication(ready.profile, ready.analysis, ready.kept, applicationIdFor(ready))
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

    private suspend fun refreshed(ready: Phase.Ready, profile: CandidateProfile): Phase.Ready {
        val previous = ready.serverAnalysis
        val (local, baseline) = withContext(computeDispatcher) {
            gapMatcher.match(profile, previous.job) to gapMatcher.match(ready.baselineProfile, previous.job)
        }
        val localById = local.matches.associateBy { it.requirement.id }
        val baselineById = baseline.matches.associateBy { it.requirement.id }
        val coverage = KeywordCoverage(
            covered = (
                previous.gap.keywordCoverage.covered +
                    maxOf(0, local.keywordCoverage.covered - baseline.keywordCoverage.covered)
                ).coerceAtMost(previous.gap.keywordCoverage.total),
            total = previous.gap.keywordCoverage.total,
        )
        val matches = previous.gap.matches.map { old ->
            upgradedMatch(
                server = old,
                current = localById[old.requirement.id],
                baseline = baselineById[old.requirement.id],
            ) ?: old
        }
        return ready.copy(
            profile = profile,
            analysis = previous.copy(gap = previous.gap.copy(matches = matches, keywordCoverage = coverage)),
        )
    }

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

        data class Failed(val kept: KeptJobDescription, val cause: FailureCause = FailureCause.Generic) : Phase {
            override val label: JobLabel get() = JobLabel(kept.role, kept.company)
        }

        data class DailyLimit(val kept: KeptJobDescription) : Phase {
            override val label: JobLabel get() = JobLabel(kept.role, kept.company)
        }

        data class Ready(
            val kept: KeptJobDescription,
            val profile: CandidateProfile,
            val analysis: JobAnalysisResult,
            val serverAnalysis: JobAnalysisResult = analysis,
            val baselineProfile: CandidateProfile = profile,
            val analysedAt: Instant,
        ) : Phase {
            override val label: JobLabel
                get() = JobLabel(
                    title = kept.resolvedTitle(analysis.job.title),
                    company = kept.resolvedCompany(analysis.job.company),
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
        val totalCredits: Int,
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
        fun toUiState(
            env: Environment,
            prepIds: Set<String>,
            reportedIds: Set<String>,
            nextFactIdOf: (CandidateProfile) -> String,
        ): AnalysisUiState {
            val label = phase.label
            val factCount = phase.facts
            return when {
                env.scenario == DebugScenario.LOADING -> AnalysisUiState.Analyzing(label, factCount)
                phase is Phase.Loading -> AnalysisUiState.Loading
                env.scenario == DebugScenario.ERROR -> AnalysisUiState.Failed(label)
                env.scenario == DebugScenario.EMPTY -> AnalysisUiState.DailyLimit(label)
                phase is Phase.Analyzing -> AnalysisUiState.Analyzing(label, factCount)
                phase is Phase.Failed -> AnalysisUiState.Failed(label, phase.cause)
                phase is Phase.DailyLimit -> AnalysisUiState.DailyLimit(label)
                phase is Phase.Ready -> AnalysisUiState.Result(
                    job = label,
                    keywordCoverage = phase.analysis.gap.keywordCoverage,
                    sections = phase.analysis.gap.matches.toSections(phase.profile, prepIds, reportedIds),
                    totalCredits = env.totalCredits,
                    isOffline = env.scenario == DebugScenario.OFFLINE || !env.online,
                    tailorLimitReached = env.scenario == DebugScenario.PENDING || tailorLimitHit ||
                        (!env.hasCredit && env.freeTailoringsLeft <= 0 && !freeTailoringCounted),
                    isTailoring = tailoring,
                    overlay = overlay,
                    toast = toast,
                    closedRequirementId = closedId,
                    nextFactId = nextFactIdOf(phase.profile),
                    analysedAt = phase.analysedAt,
                )
                else -> AnalysisUiState.Loading
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
        val FORCED_WITHOUT_ANALYSIS = setOf(DebugScenario.ERROR, DebugScenario.EMPTY, DebugScenario.LOADING)
        const val APPLICATION_ID_KEY = "application-id-"
    }
}

private fun Throwable.toFailureCause(): FailureCause {
    val ai = this as? AiException ?: return FailureCause.Generic
    return when (ai.failure) {
        AiFailure.RateLimited -> FailureCause.RateLimited(ai.retryAfterSeconds)
        AiFailure.AnalysisInProgress -> FailureCause.InProgress
        AiFailure.QuotaExceeded -> FailureCause.QuotaReached
        AiFailure.SignInRequired -> FailureCause.SignInRequired
        else -> FailureCause.Generic
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
