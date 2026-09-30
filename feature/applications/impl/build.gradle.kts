plugins {
    alias(libs.plugins.hirehop.android.feature.impl)
    alias(libs.plugins.hirehop.android.library.compose)
}

android {
    namespace = "com.hirehop.feature.applications.impl"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.feature.applications.api)
    implementation(projects.feature.analysis.api)

    testImplementation(projects.core.testing)
}
