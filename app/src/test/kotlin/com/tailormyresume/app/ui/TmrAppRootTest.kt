package com.tailormyresume.app.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.app.navigation.registerComposeActivity
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.domain.onboarding.StartDestination
import com.tailormyresume.core.navigation.Navigator
import com.tailormyresume.feature.applications.api.navigation.DefaultApplicationsNavKey
import com.tailormyresume.feature.onboarding.api.navigation.DefaultSignInNavKey
import com.tailormyresume.feature.onboarding.api.navigation.UploadNavKey
import com.tailormyresume.feature.profile.api.navigation.DefaultProfileNavKey
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TmrAppRootTest {

    init {
        registerComposeActivity()
    }

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private var ready by mutableStateOf<AppRootState.Ready>(AppRootState.Ready(StartDestination.SignIn, null))
    private var navigator: Navigator? = null

    private fun show() {
        rule.setContent {
            TmrTheme {
                TmrAccountRoot(
                    ready = ready,
                    entryProvider = { shellNavigator ->
                        navigator = shellNavigator
                        ({ key: NavKey -> NavEntry(key) {} })
                    },
                )
            }
        }
        rule.waitForIdle()
    }

    private fun startKeys(): List<NavKey> = rule.runOnIdle { navigator?.state?.stack?.toList().orEmpty() }

    @Test
    fun startsAtTheKeyOfTheStartDestination() {
        ready = AppRootState.Ready(StartDestination.Upload, "account-1")
        show()

        assertThat(startKeys()).containsExactly(UploadNavKey())
    }

    @Test
    fun accountChange_resetsBackStackToNewStart() {
        ready = AppRootState.Ready(StartDestination.Applications, "account-1")
        show()
        rule.runOnIdle { navigator?.navigate(DefaultProfileNavKey) }
        rule.waitForIdle()
        assertThat(startKeys()).containsExactly(DefaultApplicationsNavKey, DefaultProfileNavKey).inOrder()

        ready = AppRootState.Ready(StartDestination.SignIn, null)
        rule.waitForIdle()

        assertThat(startKeys()).containsExactly(DefaultSignInNavKey)
    }
}
