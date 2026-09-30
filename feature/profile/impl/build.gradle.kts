plugins {
    alias(libs.plugins.hirehop.android.feature.impl)
    alias(libs.plugins.hirehop.android.library.compose)
}

android {
    namespace = "com.hirehop.feature.profile.impl"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.feature.profile.api)

    testImplementation(projects.core.testing)
}
