package com.tailormyresume.core.designsystem.component.hero

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class TmrHeroColor { Blue, Amber, Lime }

@Composable
fun TmrHeroCard(
    color: TmrHeroColor,
    label: String,
    headline: String,
    modifier: Modifier = Modifier,
    decoration: @Composable BoxScope.() -> Unit = {},
) = Unit
