package com.tailormyresume.feature.profile.impl

internal data class ProfileActions(
    val onConfirmEntry: (String) -> Unit,
    val onUpdateContact: (ContactDraft) -> Unit,
    val onAddSkill: (String) -> Unit,
    val onRemoveSkill: (String) -> Unit,
) {
    companion object {
        val None = ProfileActions(
            onConfirmEntry = {},
            onUpdateContact = {},
            onAddSkill = {},
            onRemoveSkill = {},
        )
    }
}

internal data class ProfileNavigation(
    val onOpenFact: (entryId: String) -> Unit,
    val onAddFact: (entryType: String) -> Unit,
    val onAddEvidence: () -> Unit,
    val onBuildStepByStep: () -> Unit,
    val onImportResume: () -> Unit,
) {
    companion object {
        val None = ProfileNavigation(
            onOpenFact = {},
            onAddFact = {},
            onAddEvidence = {},
            onBuildStepByStep = {},
            onImportResume = {},
        )
    }
}

internal fun ProfileViewModel.toActions(): ProfileActions = ProfileActions(
    onConfirmEntry = this::confirmEntry,
    onUpdateContact = this::updateContact,
    onAddSkill = this::addSkill,
    onRemoveSkill = this::removeSkill,
)
