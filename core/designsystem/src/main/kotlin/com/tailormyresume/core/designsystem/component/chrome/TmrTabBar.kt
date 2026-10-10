package com.tailormyresume.core.designsystem.component.chrome

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class TmrTab { Applications, Profile }

@Composable
fun TmrTabBar(
    selected: TmrTab,
    onApplications: () -> Unit,
    onAdd: () -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier,
) = Unit
