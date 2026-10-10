package com.tailormyresume.feature.analysis.impl.job

import androidx.lifecycle.ViewModel
import com.tailormyresume.core.common.network.Dispatcher
import com.tailormyresume.core.common.network.TmrDispatchers
import com.tailormyresume.core.data.repository.ApplicationRepository
import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.domain.AnalyzeJobUseCase
import com.tailormyresume.core.domain.IdGenerator
import com.tailormyresume.core.domain.ProposeJobLabelUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject
import kotlin.time.Clock

@HiltViewModel
internal class JobViewModel @Inject constructor(
    private val analyzeJob: AnalyzeJobUseCase,
    private val proposeJobLabel: ProposeJobLabelUseCase,
    private val applicationRepository: ApplicationRepository,
    private val profileRepository: ProfileRepository,
    private val draftStore: JobDraftStore,
    private val progressTicker: AnalysisProgressTicker,
    private val idGenerator: IdGenerator,
    private val clock: Clock,
    @Dispatcher(TmrDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    val uiState: StateFlow<JobUiState> = MutableStateFlow(JobUiState.Empty)

    val events: Flow<JobEvent> = emptyFlow()

    fun onTextChange(text: String) = Unit

    fun onPaste(clipboardText: String) = Unit

    fun onClear() = Unit

    fun onAnalyze() = Unit
}
