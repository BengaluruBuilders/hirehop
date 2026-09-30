plugins {
    alias(libs.plugins.hirehop.android.feature.impl)
    alias(libs.plugins.hirehop.android.library.compose)
}

android {
    namespace = "com.hirehop.feature.tailor.impl"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.domain)
    implementation(projects.core.model)
    implementation(projects.core.navigation)
    implementation(projects.feature.tailor.api)
    implementation(libs.androidx.core)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(projects.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
}
