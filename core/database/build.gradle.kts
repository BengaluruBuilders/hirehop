plugins {
    alias(libs.plugins.tailormyresume.android.library)
    alias(libs.plugins.tailormyresume.android.room)
    alias(libs.plugins.tailormyresume.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.tailormyresume.core.database"
    testOptions.unitTests.isIncludeAndroidResources = true
}

androidComponents {
    onVariants { variant ->
        variant.hostTests["UnitTest"]?.sources?.assets?.addStaticSourceDirectory("schemas")
    }
}

dependencies {
    api(projects.core.model)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.truth)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.room.testing)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)

    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.kotlin.test)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
