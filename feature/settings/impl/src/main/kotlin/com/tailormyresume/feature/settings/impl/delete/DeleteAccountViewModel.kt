package com.tailormyresume.feature.settings.impl.delete

import androidx.lifecycle.ViewModel
import com.tailormyresume.core.domain.account.DeleteAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

internal sealed interface DeleteAccountUiState {
    data object Loading : DeleteAccountUiState

    data class Content(
        val applications: Int,
        val unusedCredits: Int,
        val typed: String,
        val canDelete: Boolean,
        val deleting: Boolean,
    ) : DeleteAccountUiState
}

internal sealed interface DeleteAccountEvent {
    data object DeleteFailed : DeleteAccountEvent
}

@HiltViewModel
internal class DeleteAccountViewModel @Inject constructor(
    deleteAccount: DeleteAccountUseCase,
) : ViewModel() {

    val uiState: StateFlow<DeleteAccountUiState> = MutableStateFlow(DeleteAccountUiState.Loading)

    val events: Flow<DeleteAccountEvent> = emptyFlow()

    fun onTextChanged(text: String) = Unit

    fun onDelete() = Unit

    fun onSheetClosed() = Unit
}
