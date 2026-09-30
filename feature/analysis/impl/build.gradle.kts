plugins {
    alias(libs.plugins.hirehop.android.feature.impl)
    alias(libs.plugins.hirehop.android.library.compose)
}

android {
    namespace = "com.hirehop.feature.analysis.impl"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.domain)
    implementation(projects.core.model)
    implementation(projects.core.navigation)
    implementation(projects.feature.analysis.api)
    implementation(projects.feature.profile.api)
    implementation(projects.feature.tailor.api)

    testImplementation(projects.core.testing)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
}
