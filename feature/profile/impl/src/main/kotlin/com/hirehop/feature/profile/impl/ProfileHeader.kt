package com.hirehop.feature.profile.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.theme.HhTheme

internal data class ProfileHeaderState(
    val name: String,
    val role: String,
    val factCount: Int,
    val confirmedCount: Int,
    val userStatedCount: Int,
    val toConfirmCount: Int,
)

@Composable
internal fun ProfileFrame(
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit,
) {
    HhScreen(
        modifier = modifier,
        sheet = false,
        header = { HhInnerHeader(title = stringResource(R.string.feature_profile_impl_title)) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = HhTheme.spacing.gutter,
                end = HhTheme.spacing.gutter,
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding(),
            ),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
            content = content,
        )
    }
}
