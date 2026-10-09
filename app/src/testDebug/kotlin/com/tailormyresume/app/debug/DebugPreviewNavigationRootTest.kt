package com.tailormyresume.app.debug

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.app.ui.AppRootState
import com.tailormyresume.app.ui.NavigationRoot
import org.junit.Test

class DebugPreviewNavigationRootTest {

    private fun root(
        opensFirstRunRoot: Boolean = false,
        seenMain: Boolean = false,
        rootState: AppRootState = AppRootState.Main,
        seenAccount: Boolean = false,
        hasAccount: Boolean = false,
    ) = previewNavigationRoot(opensFirstRunRoot, seenMain, rootState, seenAccount, hasAccount)

    @Test
    fun aFirstRunTargetShowsTheFirstRunRoot() {
        assertThat(root(opensFirstRunRoot = true)).isEqualTo(NavigationRoot.FirstRun)
    }

    @Test
    fun aMainTargetShowsTheMainRoot() {
        assertThat(root()).isEqualTo(NavigationRoot.Main)
    }

    @Test
    fun aSeenMainThatBecomesFirstRunShowsTheFirstRunRoot() {
        assertThat(root(seenMain = true, rootState = AppRootState.FirstRun)).isEqualTo(NavigationRoot.FirstRun)
    }

    @Test
    fun anAccountThatGoesAwayShowsTheFirstRunRootEvenWhenOnboardingWasNeverComplete() {
        val shown = root(seenMain = false, rootState = AppRootState.FirstRun, seenAccount = true, hasAccount = false)

        assertThat(shown).isEqualTo(NavigationRoot.FirstRun)
    }

    @Test
    fun anAccountThatIsStillThereKeepsTheMainRoot() {
        assertThat(root(seenAccount = true, hasAccount = true)).isEqualTo(NavigationRoot.Main)
    }

    @Test
    fun aPreviewOpenedSignedOutKeepsTheMainRoot() {
        assertThat(root(seenAccount = false, hasAccount = false)).isEqualTo(NavigationRoot.Main)
    }
}
