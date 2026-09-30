package com.hirehop.feature.profile.impl

internal data class ProfileActions(
    val onConfirmEntry: (String) -> Unit,
    val onConfirmAll: () -> Unit,
    val onDeleteEntry: (String) -> Unit,
    val onSaveEntry: (String?, EntryDraft) -> Unit,
    val onUpdateContact: (ContactDraft) -> Unit,
    val onAddSkill: (String) -> Unit,
    val onRemoveSkill: (String) -> Unit,
    val onLoadDemo: () -> Unit,
    val onStartManual: () -> Unit,
    val onClearProfile: () -> Unit,
    val onResumeTextChange: (String) -> Unit,
    val onParseResume: () -> Unit,
    val onSavePreview: () -> Unit,
    val onResetImport: () -> Unit,
) {
    companion object {
        val None = ProfileActions(
            onConfirmEntry = {},
            onConfirmAll = {},
            onDeleteEntry = {},
            onSaveEntry = { _, _ -> },
            onUpdateContact = {},
            onAddSkill = {},
            onRemoveSkill = {},
            onLoadDemo = {},
            onStartManual = {},
            onClearProfile = {},
            onResumeTextChange = {},
            onParseResume = {},
            onSavePreview = {},
            onResetImport = {},
        )
    }
}

internal fun ProfileViewModel.toActions(): ProfileActions = ProfileActions(
    onConfirmEntry = this::confirmEntry,
    onConfirmAll = this::confirmAll,
    onDeleteEntry = this::deleteEntry,
    onSaveEntry = this::saveEntry,
    onUpdateContact = this::updateContact,
    onAddSkill = this::addSkill,
    onRemoveSkill = this::removeSkill,
    onLoadDemo = this::loadDemoProfile,
    onStartManual = this::startManualProfile,
    onClearProfile = this::clearProfile,
    onResumeTextChange = this::onResumeTextChange,
    onParseResume = this::parseResume,
    onSavePreview = this::savePreview,
    onResetImport = this::resetImport,
)
