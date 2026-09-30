plugins {
    alias(libs.plugins.hirehop.android.library)
    alias(libs.plugins.hirehop.hilt)
}

android {
    namespace = "com.hirehop.core.data"
}

dependencies {
    api(projects.core.common)
    api(projects.core.database)
    api(projects.core.model)
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(projects.core.testing)
}
