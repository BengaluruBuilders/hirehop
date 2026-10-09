package com.tailormyresume.app.debug

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class DebugManifestConfigChangesTest {

    private val androidNamespace = "http://schemas.android.com/apk/res/android"

    private fun configChanges(): Set<String> {
        val manifest = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
            .newDocumentBuilder()
            .parse(File("src/debug/AndroidManifest.xml"))
        val activities = manifest.getElementsByTagName("activity")
        val scenario = (0 until activities.length)
            .map { activities.item(it) as org.w3c.dom.Element }
            .single { it.getAttributeNS(androidNamespace, "name") == ".debug.DebugScenarioActivity" }
        return scenario.getAttributeNS(androidNamespace, "configChanges").split("|").filter { it.isNotBlank() }.toSet()
    }

    @Test
    fun theDevLauncherHandlesFontScaleAndDensityChangesItself() {
        assertThat(configChanges()).containsAtLeast("fontScale", "density")
    }
}
