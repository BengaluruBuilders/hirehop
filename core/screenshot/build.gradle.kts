plugins {
    alias(libs.plugins.hirehop.android.library.screenshot)
}

android {
    namespace = "com.hirehop.core.screenshot"
}

tasks.withType<Test>().configureEach {
    failOnNoDiscoveredTests = false
}
