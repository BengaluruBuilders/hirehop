package com.hirehop.feature.profile.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.FactSource

private fun unconfirmedDemoProfile(): CandidateProfile {
    val demo = DemoProfileProvider.profile()
    return demo.copy(
        entries = demo.entries.mapIndexed { index, entry ->
            if (index % 2 == 0) entry.copy(source = FactSource.IMPORTED, isConfirmed = false) else entry
        },
    )
}

private fun successState(profile: CandidateProfile) = ProfileUiState.Success(
    profile = profile,
    unconfirmedCount = profile.entries.count { !it.isConfirmed },
)

@Preview(showBackground = true)
@Composable
private fun ProfileScreenSuccessPreview() {
    HhTheme {
        ProfileScreen(
            uiState = successState(unconfirmedDemoProfile()),
            importState = ResumeImportState(),
            actions = ProfileActions.None,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenConfirmedPreview() {
    HhTheme {
        ProfileScreen(
            uiState = successState(DemoProfileProvider.profile()),
            importState = ResumeImportState(),
            actions = ProfileActions.None,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenEmptyPreview() {
    HhTheme {
        ProfileScreen(
            uiState = ProfileUiState.Empty,
            importState = ResumeImportState(),
            actions = ProfileActions.None,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenLoadingPreview() {
    HhTheme {
        ProfileScreen(
            uiState = ProfileUiState.Loading,
            importState = ResumeImportState(),
            actions = ProfileActions.None,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EntryCardUnconfirmedPreview() {
    HhTheme {
        EntryCard(
            entry = unconfirmedDemoProfile().entries.first(),
            onConfirm = {},
            onEdit = {},
            onDelete = {},
        )
    }
}
