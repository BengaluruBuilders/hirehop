plugins {
    alias(libs.plugins.hirehop.android.library)
    alias(libs.plugins.hirehop.android.room)
    alias(libs.plugins.hirehop.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.hirehop.core.database"
}

dependencies {
    api(projects.core.model)
    implementation(projects.core.common)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.truth)

    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.kotlin.test)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
