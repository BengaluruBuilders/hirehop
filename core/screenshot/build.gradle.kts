plugins {
    alias(libs.plugins.tailormyresume.android.library.screenshot)
}

android {
    namespace = "com.tailormyresume.core.screenshot"
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
