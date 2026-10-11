package com.tailormyresume.feature.tailor.impl.exported

import androidx.lifecycle.ViewModel
import com.tailormyresume.core.common.network.Dispatcher
import com.tailormyresume.core.common.network.TmrDispatchers
import com.tailormyresume.core.data.repository.ApplicationRepository
import com.tailormyresume.core.data.repository.CreditsRepository
import com.tailormyresume.core.domain.SetApplicationStatusUseCase
import com.tailormyresume.core.model.ApplicationStatus
import com.tailormyresume.feature.tailor.impl.export.ExportResumeUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel(assistedFactory = ExportedViewModel.Factory::class)
internal class ExportedViewModel @AssistedInject constructor(
    applicationRepository: ApplicationRepository,
    creditsRepository: CreditsRepository,
    exportResume: ExportResumeUseCase,
    setApplicationStatus: SetApplicationStatusUseCase,
    fileStore: ExportedFileStore,
    @param:Dispatcher(TmrDispatchers.IO) ioDispatcher: CoroutineDispatcher,
    @Assisted val applicationId: String,
) : ViewModel() {

    val state: StateFlow<ExportedUiState> get() = TODO()

    val events: Flow<ExportedEvent> get() = TODO()

    fun onShare() { TODO() }

    fun onOpen() { TODO() }

    fun onMarkApplied() { TODO() }

    fun onUndoApplied() { TODO() }

    fun onChangeStatus() { TODO() }

    fun onPickStatus(status: ApplicationStatus) { TODO() }

    fun onDismissStatusSheet() { TODO() }

    fun onSaveStatus() { TODO() }

    fun onSoon() { TODO() }

    @AssistedFactory
    interface Factory {
        fun create(applicationId: String): ExportedViewModel
    }
}
