package com.tailormyresume.feature.tailor.impl.exported

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tailormyresume.core.common.network.Dispatcher
import com.tailormyresume.core.common.network.TmrDispatchers
import com.tailormyresume.core.data.repository.ApplicationRepository
import com.tailormyresume.core.data.repository.CreditsRepository
import com.tailormyresume.core.domain.SetApplicationStatusUseCase
import com.tailormyresume.core.domain.StatusChange
import com.tailormyresume.core.model.ApplicationStatus
import com.tailormyresume.core.model.CreditLedgerEntry
import com.tailormyresume.core.model.CreditLedgerKind
import com.tailormyresume.core.model.JobApplication
import com.tailormyresume.feature.tailor.impl.export.ExportOutcome
import com.tailormyresume.feature.tailor.impl.export.ExportResumeUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Instant as KotlinInstant

@HiltViewModel(assistedFactory = ExportedViewModel.Factory::class)
internal class ExportedViewModel @AssistedInject constructor(
    private val applicationRepository: ApplicationRepository,
    private val creditsRepository: CreditsRepository,
    private val exportResume: ExportResumeUseCase,
    private val setApplicationStatus: SetApplicationStatusUseCase,
    private val fileStore: ExportedFileStore,
    @param:Dispatcher(TmrDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    @Assisted val applicationId: String,
) : ViewModel() {

    private val outcome = MutableStateFlow<ExportOutcome?>(null)

    private val statusSheet = MutableStateFlow<ApplicationStatus?>(null)

    private val eventChannel = Channel<ExportedEvent>(Channel.BUFFERED)

    val events: Flow<ExportedEvent> = eventChannel.receiveAsFlow()

    val state: StateFlow<ExportedUiState> = combine(
        applicationRepository.observeApplication(applicationId),
        creditsRepository.observeLedger(),
        outcome,
        statusSheet,
        ::stateFor,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExportedUiState.Loading,
    )

    private var lastChange: StatusChange? = null

    init {
        viewModelScope.launch {
            outcome.value = try {
                exportResume(applicationId)
            } catch (_: IOException) {
                ExportOutcome.Failed
            } catch (_: IllegalStateException) {
                ExportOutcome.Failed
            }
        }
    }

    fun onShare() = resolveFile { file -> ExportedEvent.Share(file) }

    fun onOpen() = resolveFile { file -> ExportedEvent.Open(file) }

    fun onMarkApplied() {
        viewModelScope.launch {
            val previous = setApplicationStatus(applicationId, ApplicationStatus.APPLIED)
            lastChange = previous
            val appliedOn = applicationRepository.observeApplication(applicationId).first()?.appliedOn
            eventChannel.send(ExportedEvent.MarkedApplied(appliedOn?.let(::formatDay) ?: ""))
        }
    }

    fun onUndoApplied() {
        viewModelScope.launch {
            val previous = lastChange ?: return@launch
            setApplicationStatus.restore(applicationId, previous)
            lastChange = null
        }
    }

    fun onChangeStatus() {
        val current = state.value as? ExportedUiState.Ready ?: return
        statusSheet.value = current.status
    }

    fun onPickStatus(status: ApplicationStatus) {
        statusSheet.value = status
    }

    fun onDismissStatusSheet() {
        statusSheet.value = null
    }

    fun onSaveStatus() {
        viewModelScope.launch {
            val pick = statusSheet.value ?: return@launch
            setApplicationStatus(applicationId, pick)
            statusSheet.value = null
            eventChannel.send(ExportedEvent.StatusSet(pick))
        }
    }

    fun onSoon() {
        eventChannel.trySend(ExportedEvent.ComingSoon)
    }

    private fun resolveFile(event: (File) -> ExportedEvent) {
        viewModelScope.launch {
            val ready = state.value as? ExportedUiState.Ready ?: return@launch
            val file = withContext(ioDispatcher) { fileStore.fileFor(ready.fileName) }
            eventChannel.send(file?.let(event) ?: ExportedEvent.FileMissing)
        }
    }

    private fun stateFor(
        application: JobApplication?,
        ledger: List<CreditLedgerEntry>,
        outcome: ExportOutcome?,
        sheet: ApplicationStatus?,
    ): ExportedUiState {
        val exported = outcome as? ExportOutcome.Exported ?: return when (outcome) {
            null -> ExportedUiState.Loading
            ExportOutcome.Failed -> ExportedUiState.ExportFailed
            else -> ExportedUiState.NothingExported
        }
        if (application == null) return ExportedUiState.NothingExported
        return ExportedUiState.Ready(
            fileName = exported.fileName,
            pageCount = exported.pageCount,
            sizeKb = wholeKb(exported.sizeBytes),
            creditsLeft = ledger.sumOf(CreditLedgerEntry::amount).coerceAtLeast(0),
            freeResumeUsed = ledger.count { it.kind == CreditLedgerKind.SPEND } == 1 &&
                ledger.none { it.kind == CreditLedgerKind.PURCHASE },
            jobTitle = application.job.title,
            company = application.job.company,
            status = application.status,
            markedOn = markedOnOf(application),
            statusSheet = sheet,
        )
    }

    private fun markedOnOf(application: JobApplication): String? {
        val appliedOn = application.appliedOn ?: return null
        if (application.status == ApplicationStatus.SAVED) return null
        return formatDay(appliedOn)
    }

    private fun formatDay(instant: KotlinInstant): String =
        DateTimeFormatter.ofPattern(DAY_PATTERN, Locale.ENGLISH)
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochMilli(instant.toEpochMilliseconds()))

    private fun wholeKb(sizeBytes: Long): Int =
        ((sizeBytes + BYTES_PER_KB - 1) / BYTES_PER_KB).coerceAtLeast(1L).toInt()

    private companion object {
        const val BYTES_PER_KB = 1024L
        const val DAY_PATTERN = "d MMM"
    }

    @AssistedFactory
    interface Factory {
        fun create(applicationId: String): ExportedViewModel
    }
}
