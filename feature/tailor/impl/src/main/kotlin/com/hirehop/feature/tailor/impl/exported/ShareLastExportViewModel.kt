package com.hirehop.feature.tailor.impl.exported

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.data.repository.ExportHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

internal sealed interface ShareLastExportOutcome {
    data class Share(val request: ExportedFileRequest) : ShareLastExportOutcome

    data object Missing : ShareLastExportOutcome
}

@HiltViewModel
internal class ShareLastExportViewModel @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    private val exportHistoryRepository: ExportHistoryRepository,
    private val fileStore: ExportedFileStore,
) : ViewModel() {

    private val mutableOutcome = MutableStateFlow<ShareLastExportOutcome?>(null)

    private var hasEntered = false

    val outcome: StateFlow<ShareLastExportOutcome?> = mutableOutcome.asStateFlow()

    fun onEnter(applicationId: String) {
        if (hasEntered) return
        hasEntered = true
        viewModelScope.launch {
            val record = exportHistoryRepository.observeExports(applicationId).first().lastOrNull()
            val file = record?.let { fileStore.fileFor(it.fileName) }
            if (record == null || file == null) {
                mutableOutcome.value = ShareLastExportOutcome.Missing
                return@launch
            }
            val job = applicationRepository.observeApplication(applicationId).first()?.job
            mutableOutcome.value = ShareLastExportOutcome.Share(
                ExportedFileRequest(
                    file = file,
                    format = record.format,
                    action = ExportedFileAction.SHARE,
                    jobTitle = job?.title.orEmpty(),
                    jobCompany = job?.company.orEmpty(),
                ),
            )
        }
    }
}
