package com.hirehop.feature.settings.impl.settings

import com.hirehop.core.model.DebugScenario

enum class SettingsGroupLabel { ACCOUNT, CREDITS, YOUR_DATA, PRIVACY, ABOUT, DELETE_ACCOUNT }

enum class SettingsRowStyle { NORMAL, BIG, SMALL }

enum class SettingsRowKey {
    ACCOUNT,
    SIGN_OUT,
    CREDITS_AND_HELP,
    YOUR_DATA,
    PRIVACY_POLICY,
    CONSENT_NOTICE,
    GRIEVANCE_CONTACT,
    PROMISE,
    VERSION,
    DELETE_ACCOUNT,
    DELETE_ACCOUNT_WEB,
}

enum class SettingsSlotKind {
    PRIVACY_POLICY_ADDRESS,
    GRIEVANCE_CONTACT_ADDRESS,
    DELETE_ACCOUNT_WEB_ADDRESS,
}

enum class SettingsDestination {
    SIGN_OUT,
    CREDITS_AND_HELP,
    YOUR_DATA,
    CONSENT_NOTICE,
    DELETE_ACCOUNT,
}

data class SettingsRowState(
    val key: SettingsRowKey,
    val style: SettingsRowStyle = SettingsRowStyle.NORMAL,
    val supporting: String? = null,
    val slot: SettingsSlotKind? = null,
    val trailing: String? = null,
    val showChevron: Boolean = false,
    val isEnabled: Boolean = true,
    val destination: SettingsDestination? = null,
)

data class SettingsGroupState(
    val label: SettingsGroupLabel,
    val rows: List<SettingsRowState>,
)

data class SettingsUiState(
    val groups: List<SettingsGroupState> = settingsGroups(),
    val accountDisplayName: String? = null,
    val creditsLeft: Int = 0,
    val isOffline: Boolean = false,
    val destination: SettingsDestination? = null,
) {
    val isEveryRowEnabled: Boolean get() = groups.all { group -> group.rows.all { row -> row.isEnabled } }
}

sealed interface SettingsAction {
    data class DestinationSelected(val destination: SettingsDestination) : SettingsAction

    data object DestinationConsumed : SettingsAction
}

fun settingsIsOffline(scenario: DebugScenario): Boolean = scenario == DebugScenario.OFFLINE

fun settingsGroupsFor(
    accountDisplayName: String?,
    creditsLeft: Int,
    isOffline: Boolean,
): List<SettingsGroupState> = listOf(
    SettingsGroupState(
        label = SettingsGroupLabel.ACCOUNT,
        rows = listOf(
            SettingsRowState(
                key = SettingsRowKey.ACCOUNT,
                supporting = accountDisplayName,
            ),
            SettingsRowState(
                key = SettingsRowKey.SIGN_OUT,
                supporting = SUPPORTING_SIGN_OUT,
                destination = SettingsDestination.SIGN_OUT,
            ),
        ),
    ),
    SettingsGroupState(
        label = SettingsGroupLabel.CREDITS,
        rows = listOf(
            SettingsRowState(
                key = SettingsRowKey.CREDITS_AND_HELP,
                supporting = SUPPORTING_CREDITS,
                trailing = creditsLeft.toString(),
                showChevron = true,
                destination = SettingsDestination.CREDITS_AND_HELP,
            ),
        ),
    ),
    SettingsGroupState(
        label = SettingsGroupLabel.YOUR_DATA,
        rows = listOf(
            SettingsRowState(
                key = SettingsRowKey.YOUR_DATA,
                supporting = SUPPORTING_YOUR_DATA,
                showChevron = true,
                destination = SettingsDestination.YOUR_DATA,
            ),
        ),
    ),
    SettingsGroupState(
        label = SettingsGroupLabel.PRIVACY,
        rows = listOf(
            SettingsRowState(
                key = SettingsRowKey.PRIVACY_POLICY,
                supporting = if (isOffline) SUPPORTING_PRIVACY_OFFLINE else null,
                slot = SettingsSlotKind.PRIVACY_POLICY_ADDRESS,
            ),
            SettingsRowState(
                key = SettingsRowKey.CONSENT_NOTICE,
                supporting = SUPPORTING_CONSENT,
                showChevron = true,
                destination = SettingsDestination.CONSENT_NOTICE,
            ),
            SettingsRowState(
                key = SettingsRowKey.GRIEVANCE_CONTACT,
                slot = SettingsSlotKind.GRIEVANCE_CONTACT_ADDRESS,
            ),
        ),
    ),
    SettingsGroupState(
        label = SettingsGroupLabel.ABOUT,
        rows = listOf(
            SettingsRowState(
                key = SettingsRowKey.PROMISE,
                style = SettingsRowStyle.BIG,
            ),
            SettingsRowState(key = SettingsRowKey.VERSION),
        ),
    ),
    SettingsGroupState(
        label = SettingsGroupLabel.LEL,
        rows = settingsDeleteAccountRows(isOffline = isOffline),
    ),
)

private fun settingsDeleteAccountRows(isOffline: Boolean): List<SettingsRowState> = listOf(
    SettingsRowState(
        key = SettingsRowKey.DELETE_ACCOUNT,
        supporting = if (isOffline) SUPPORTING_DELETE_OFFLINE else SUPPORTING_DELETE,
        showChevron = true,
        isEnabled = !isOffline,
        destination = SettingsDestination.DELETE_ACCOUNT,
    ),
    SettingsRowState(
        key = SettingsRowKey.DELETE_ACCOUNT_WEB,
        style = SettingsRowStyle.SMALL,
        slot = SettingsSlotKind.DELETE_ACCOUNT_WEB_ADDRESS,
    ),
)

fun settingsGroups(): List<SettingsGroupState> = settingsGroupsFor(
    accountDisplayName = null,
    creditsLeft = 0,
    isOffline = false,
)

fun settingsStateFor(
    accountDisplayName: String?,
    creditsLeft: Int,
    isOffline: Boolean,
): SettingsUiState = SettingsUiState(
    groups = settingsGroupsFor(
        accountDisplayName = accountDisplayName,
        creditsLeft = creditsLeft,
        isOffline = isOffline,
    ),
    accountDisplayName = accountDisplayName,
    creditsLeft = creditsLeft,
    isOffline = isOffline,
)

private const val SUPPORTING_SIGN_OUT = "Removes the account from this phone. Your data stays until you delete it."
private const val SUPPORTING_CREDITS = "Purchases, refunds, and help"
private const val SUPPORTING_YOUR_DATA = "See, correct, download, or delete it"
private const val SUPPORTING_PRIVACY_OFFLINE = "Opens in your browser. Needs a connection."
private const val SUPPORTING_CONSENT = "Read only. The notice you agreed to."
private const val SUPPORTING_DELETE = "Your profile, applications, and credits"
private const val SUPPORTING_DELETE_OFFLINE = "Needs a connection."
