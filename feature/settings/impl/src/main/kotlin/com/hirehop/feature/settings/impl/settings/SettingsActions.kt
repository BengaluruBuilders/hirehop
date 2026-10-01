package com.hirehop.feature.settings.impl.settings

data class SettingsActions(
    val onRowClick: (SettingsRowState) -> Unit = {},
)
