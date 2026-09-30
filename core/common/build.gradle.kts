plugins {
    alias(libs.plugins.hirehop.android.library)
    alias(libs.plugins.hirehop.hilt)
}

android {
    namespace = "com.hirehop.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
