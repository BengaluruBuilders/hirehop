plugins {
    alias(libs.plugins.tailormyresume.android.feature.impl)
    alias(libs.plugins.tailormyresume.android.library.compose)
}

android {
    namespace = "com.tailormyresume.feature.analysis.impl"
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.domain)
    implementation(projects.core.model)
    implementation(projects.core.navigation)
    implementation(projects.feature.analysis.api)
    implementation(projects.feature.onboarding.api)
    implementation(projects.feature.profile.api)
    implementation(projects.feature.tailor.api)

    testImplementation(projects.core.testing)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
}
