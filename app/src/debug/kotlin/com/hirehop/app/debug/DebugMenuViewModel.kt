package com.hirehop.app.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.data.connectivity.ConnectivityMonitor
import com.hirehop.core.data.connectivity.MockConnectivityControl
import com.hirehop.core.domain.sample.SampleDataController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DebugDataMessage { SampleLoaded, DataReset, Failed }

data class DebugMenuUiState(
    val online: Boolean = true,
    val busy: Boolean = false,
    val message: DebugDataMessage? = null,
)

@HiltViewModel
class DebugMenuViewModel @Inject constructor(
    private val sampleDataController: SampleDataController,
    private val connectivityControl: MockConnectivityControl,
    connectivityMonitor: ConnectivityMonitor,
) : ViewModel() {

    private val work = MutableStateFlow(DebugMenuUiState())

    val uiState: StateFlow<DebugMenuUiState> = combine(connectivityMonitor.isOnline, work) { online, state ->
        state.copy(online = online)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = DebugMenuUiState(),
    )

    fun loadSampleData() = run(DebugDataMessage.SampleLoaded) { sampleDataController.load() }

    fun resetAppData() = run(DebugDataMessage.DataReset) { sampleDataController.reset() }

    fun openPreview(target: DebugScenarioTarget, onReady: () -> Unit) {
        viewModelScope.launch {
            if (target.needsSampleJob) sampleDataController.keepSampleJobDescription()
            onReady()
        }
    }

    fun closePreview(target: DebugScenarioTarget) {
        if (target.needsSampleJob) viewModelScope.launch { sampleDataController.clearSampleJobDescription() }
    }

    fun setOnline(online: Boolean) = connectivityControl.setOnline(online)

    private fun run(success: DebugDataMessage, action: suspend () -> Unit) {
        if (work.value.busy) return
        work.update { DebugMenuUiState(busy = true) }
        viewModelScope.launch {
            val message = try {
                action()
                success
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                DebugDataMessage.Failed
            }
            work.update { DebugMenuUiState(message = message) }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
