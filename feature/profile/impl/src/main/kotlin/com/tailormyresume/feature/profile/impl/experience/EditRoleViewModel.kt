package com.tailormyresume.feature.profile.impl.experience

import androidx.lifecycle.ViewModel
import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.domain.fact.FactIdAllocator
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

internal data class EditRoleDraft(
    val title: String = "",
    val company: String = "",
    val start: String = "",
    val end: String = "",
    val current: Boolean = false,
    val bullets: List<String> = listOf(""),
)

internal sealed interface EditRoleUiState {
    data object Loading : EditRoleUiState

    data class Editing(
        val isNew: Boolean,
        val draft: EditRoleDraft,
        val canSave: Boolean,
        val canAddBullet: Boolean,
    ) : EditRoleUiState
}

internal enum class EditRoleEvent { Saved, Deleted }

@HiltViewModel(assistedFactory = EditRoleViewModel.Factory::class)
internal class EditRoleViewModel @AssistedInject constructor(
    profileRepository: ProfileRepository,
    idAllocator: FactIdAllocator,
    @Assisted entryId: String?,
) : ViewModel() {
    val uiState: StateFlow<EditRoleUiState> = TODO()

    val events: Flow<EditRoleEvent> = TODO()

    fun onTitleChange(value: String): Unit = TODO()

    fun onCompanyChange(value: String): Unit = TODO()

    fun onStartChange(value: String): Unit = TODO()

    fun onEndChange(value: String): Unit = TODO()

    fun onCurrentChange(value: Boolean): Unit = TODO()

    fun onBulletChange(index: Int, value: String): Unit = TODO()

    fun onAddBullet(): Unit = TODO()

    fun save(): Unit = TODO()

    fun delete(): Unit = TODO()

    @AssistedFactory
    interface Factory {
        fun create(entryId: String?): EditRoleViewModel
    }
}
