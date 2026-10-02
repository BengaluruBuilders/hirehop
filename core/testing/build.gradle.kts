plugins {
    alias(libs.plugins.hirehop.android.library)
}

android {
    namespace = "com.hirehop.core.testing"
}

dependencies {
    api(libs.kotlinx.coroutines.test)
    api(libs.truth)
    api(libs.turbine)
    api(projects.core.common)
    api(projects.core.data)
    api(projects.core.domain)
    api(projects.core.model)

    implementation(libs.junit)

    testImplementation(libs.truth)
    testImplementation(libs.turbine)
}
