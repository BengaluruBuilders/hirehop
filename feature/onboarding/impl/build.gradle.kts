plugins {
    alias(libs.plugins.hirehop.android.feature.impl)
    alias(libs.plugins.hirehop.android.library.compose)
}

android {
    namespace = "com.hirehop.feature.onboarding.impl"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.domain)
    implementation(projects.feature.onboarding.api)
    implementation(projects.feature.profile.api)
    implementation(projects.feature.analysis.api)
    implementation(projects.feature.applications.api)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(projects.core.testing)
}
