package com.tailormyresume.feature.profile.impl.experience

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tailormyresume.core.navigation.Navigator

internal data class EditRoleActions(
    val onTitleChange: (String) -> Unit = {},
    val onCompanyChange: (String) -> Unit = {},
    val onStartChange: (String) -> Unit = {},
    val onEndChange: (String) -> Unit = {},
    val onCurrentChange: (Boolean) -> Unit = {},
    val onBulletChange: (Int, String) -> Unit = { _, _ -> },
    val onAddBullet: () -> Unit = {},
    val onDelete: () -> Unit = {},
)

@Composable
internal fun EditRoleScreen(
    state: EditRoleUiState,
    actions: EditRoleActions,
    modifier: Modifier = Modifier,
) {
}

@Composable
internal fun EditRoleRoute(
    navigator: Navigator,
    entryId: String?,
    modifier: Modifier = Modifier,
) {
}
