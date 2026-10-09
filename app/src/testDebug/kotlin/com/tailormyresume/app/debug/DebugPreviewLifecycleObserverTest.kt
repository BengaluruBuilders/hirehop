package com.tailormyresume.app.debug

import android.app.Application
import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.app.ai.DebugPreviewMode
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class DebugPreviewLifecycleObserverTest {

    private val previewMode = DebugPreviewMode()
    private var closed = 0

    @Before
    fun registerActivity() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        shadowOf(application.packageManager)
            .addActivityIfNotPresent(ComponentName(application, ComponentActivity::class.java))
    }

    private fun ComponentActivity.observe(opened: Boolean) {
        lifecycle.addObserver(
            DebugPreviewLifecycleObserver(
                activity = this,
                lifecycle = DebugPreviewLifecycle(previewMode, forcePayment = {}, releasePayment = {}),
                opened = { opened },
                closePreview = { closed++ },
            ),
        )
    }

    @Test
    fun createdToResumedKeepsTheFlagWhileAPreviewIsOpen() {
        ActivityScenario.launch(ComponentActivity::class.java).use { scenario ->
            scenario.moveToState(Lifecycle.State.CREATED)
            scenario.onActivity { it.observe(opened = true) }

            scenario.moveToState(Lifecycle.State.RESUMED)

            assertThat(previewMode.active).isTrue()
        }
    }

    @Test
    fun movingToTheBackgroundDropsTheFlagAndComingBackRaisesItAgain() {
        ActivityScenario.launch(ComponentActivity::class.java).use { scenario ->
            scenario.onActivity { it.observe(opened = true) }

            scenario.moveToState(Lifecycle.State.CREATED)
            assertThat(previewMode.active).isFalse()

            scenario.moveToState(Lifecycle.State.RESUMED)
            assertThat(previewMode.active).isTrue()
        }
    }

    @Test
    fun recreateClearsTheFlagAndDoesNotClosePreview() {
        ActivityScenario.launch(ComponentActivity::class.java).use { scenario ->
            scenario.onActivity { it.observe(opened = true) }
            assertThat(previewMode.active).isTrue()

            scenario.recreate()

            assertThat(previewMode.active).isFalse()
            assertThat(closed).isEqualTo(0)
        }
    }

    @Test
    fun finishingAnOpenPreviewClosesIt() {
        ActivityScenario.launch(ComponentActivity::class.java).use { scenario ->
            scenario.onActivity { it.observe(opened = true) }

            scenario.onActivity { it.finish() }
            scenario.moveToState(Lifecycle.State.DESTROYED)

            assertThat(closed).isEqualTo(1)
        }
    }

    @Test
    fun finishingWithoutAPreviewClosesNothing() {
        ActivityScenario.launch(ComponentActivity::class.java).use { scenario ->
            scenario.onActivity { it.observe(opened = false) }

            scenario.onActivity { it.finish() }
            scenario.moveToState(Lifecycle.State.DESTROYED)

            assertThat(closed).isEqualTo(0)
        }
    }
}
