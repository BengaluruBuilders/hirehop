plugins {
    alias(libs.plugins.tailormyresume.android.library)
    alias(libs.plugins.compose)
}

android {
    namespace = "com.tailormyresume.core.navigation"
}

dependencies {
    api(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.savedstate.compose)
    implementation(libs.androidx.lifecycle.viewModel.navigation3)

    testImplementation(libs.truth)
}
