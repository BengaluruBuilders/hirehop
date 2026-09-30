plugins {
    alias(libs.plugins.hirehop.android.library)
    alias(libs.plugins.hirehop.hilt)
}

android {
    namespace = "com.hirehop.core.domain"
}

dependencies {
    api(projects.core.data)
    api(projects.core.model)
    implementation(projects.core.common)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(projects.core.testing)
}
