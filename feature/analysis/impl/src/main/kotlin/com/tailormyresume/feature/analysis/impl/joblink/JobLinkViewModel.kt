package com.tailormyresume.feature.analysis.impl.joblink

import androidx.lifecycle.ViewModel
import com.tailormyresume.core.domain.JobImporter
import com.tailormyresume.feature.analysis.impl.job.JobDraftStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

@HiltViewModel
internal class JobLinkViewModel @Inject constructor(
    private val importer: JobImporter,
    private val draftStore: JobDraftStore,
) : ViewModel() {

    val uiState: StateFlow<JobLinkUiState> = MutableStateFlow(JobLinkUiState())

    val events: Flow<JobLinkEvent> = emptyFlow()

    fun onLinkChange(link: String) = Unit

    fun onImport() = Unit
}
