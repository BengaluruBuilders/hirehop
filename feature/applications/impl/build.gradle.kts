plugins {
    alias(libs.plugins.tailormyresume.android.feature.impl)
    alias(libs.plugins.tailormyresume.android.library.compose)
}

android {
    namespace = "com.tailormyresume.feature.applications.impl"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.domain)
    implementation(projects.core.navigation)
    implementation(projects.feature.applications.api)
    implementation(projects.feature.onboarding.api)
    implementation(projects.feature.tailor.api)

    testImplementation(projects.core.testing)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
}
