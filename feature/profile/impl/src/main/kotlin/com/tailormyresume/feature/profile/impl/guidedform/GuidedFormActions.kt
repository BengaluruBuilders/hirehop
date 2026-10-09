package com.tailormyresume.feature.profile.impl.guidedform

sealed interface GuidedFormAction {
    data class ValueChanged(
        val field: GuidedField,
        val value: String,
    ) : GuidedFormAction

    data object AddSkill : GuidedFormAction

    data class RemoveSkill(val skill: String) : GuidedFormAction

    data class ChooseExperience(val choice: ExperienceChoice) : GuidedFormAction

    data object StartForm : GuidedFormAction

    data object Next : GuidedFormAction

    data object Back : GuidedFormAction

    data object SaveAndFinishLater : GuidedFormAction

    data object FinishSaved : GuidedFormAction

    data object GoToProjects : GuidedFormAction

    data object NavigationConsumed : GuidedFormAction

    data object DismissMessage : GuidedFormAction
}

data class GuidedFormActions(
    val onValueChange: (GuidedField, String) -> Unit,
    val onAddSkill: () -> Unit,
    val onRemoveSkill: (String) -> Unit,
    val onStartForm: () -> Unit,
    val onNext: () -> Unit,
    val onBack: () -> Unit,
    val onSaveAndFinishLater: () -> Unit,
    val onFinishSaved: () -> Unit,
    val onGoToProjects: () -> Unit,
    val onAddJob: () -> Unit,
    val onChooseExperience: (ExperienceChoice) -> Unit,
    val onEditFact: (entryId: String, entryType: String) -> Unit,
) {
    companion object {
        val None = GuidedFormActions(
            onValueChange = { _, _ -> },
            onAddSkill = {},
            onRemoveSkill = {},
            onStartForm = {},
            onNext = {},
            onBack = {},
            onSaveAndFinishLater = {},
            onFinishSaved = {},
            onGoToProjects = {},
            onAddJob = {},
            onChooseExperience = {},
            onEditFact = { _, _ -> },
        )
    }
}
