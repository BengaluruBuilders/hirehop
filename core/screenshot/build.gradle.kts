plugins {
    alias(libs.plugins.hirehop.android.library.screenshot)
}

android {
    namespace = "com.hirehop.core.screenshot"
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui.test)
    implementation(libs.roborazzi.compose)
    implementation(libs.differ)
}

tasks.withType<Test>().configureEach {
    failOnNoDiscoveredTests = false
}
