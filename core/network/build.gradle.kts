plugins {
    alias(libs.plugins.tailormyresume.android.library)
    alias(libs.plugins.tailormyresume.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.tailormyresume.core.network"
}

dependencies {
    api(projects.core.model)
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.serialization.json)
    api(libs.okhttp)
    api(libs.retrofit)
    implementation(libs.retrofit.kotlinxSerialization)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.truth)
}
