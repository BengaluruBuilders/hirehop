package com.tailormyresume.feature.onboarding.impl.reading

import androidx.lifecycle.ViewModel
import com.tailormyresume.core.common.network.Dispatcher
import com.tailormyresume.core.common.network.TmrDispatchers
import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.domain.ResumeTextParser
import com.tailormyresume.core.domain.onboarding.SaveImportedProfileUseCase
import com.tailormyresume.feature.onboarding.impl.importresume.ResumeImportDraft
import com.tailormyresume.feature.onboarding.impl.importresume.ResumeTextSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

@HiltViewModel
internal class ReadingViewModel @Inject constructor(
    private val draft: ResumeImportDraft,
    private val textSource: ResumeTextSource,
    private val parser: ResumeTextParser,
    private val saveImported: SaveImportedProfileUseCase,
    private val profileRepository: ProfileRepository,
    @Dispatcher(TmrDispatchers.IO) private val io: CoroutineDispatcher,
) : ViewModel() {

    val uiState: StateFlow<ReadingUiState> = MutableStateFlow(
        ReadingUiState(
            file = ReadingFileUi(name = null, mimeType = null, byteSize = 0L, characters = 0),
            percent = 0,
            rows = ReadingRowKind.entries.map { ReadingRowUi(it, ReadingRowState.Pending) },
        ),
    )

    val events: Flow<ReadingEvent> = emptyFlow()
}
