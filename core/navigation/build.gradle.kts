plugins {
    alias(libs.plugins.hirehop.android.library)
    alias(libs.plugins.compose)
}

android {
    namespace = "com.hirehop.core.navigation"
}

dependencies {
    api(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.savedstate.compose)
    implementation(libs.androidx.lifecycle.viewModel.navigation3)

    testImplementation(libs.truth)
}
