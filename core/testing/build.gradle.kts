plugins {
    alias(libs.plugins.tailormyresume.android.library)
}

android {
    namespace = "com.tailormyresume.core.testing"
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
