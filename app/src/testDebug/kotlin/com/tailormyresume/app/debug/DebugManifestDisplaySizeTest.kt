package com.tailormyresume.app.debug

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

class DebugManifestDisplaySizeTest {

    @Test
    fun theDevLauncherStaysOpenWhenTheDisplaySizeChanges() {
        val manifest = File("src/debug/AndroidManifest.xml").readText()
        val activity = manifest.substringAfter(".debug.DebugScenarioActivity").substringBefore(">")
        val handled = activity.substringAfter("android:configChanges=\"").substringBefore("\"").split("|").toSet()

        assertThat(handled).containsAtLeast("screenSize", "smallestScreenSize", "screenLayout", "orientation")
    }
}
