plugins {
    alias(libs.plugins.hirehop.android.library)
    alias(libs.plugins.hirehop.android.room)
    alias(libs.plugins.hirehop.hilt)
}

android {
    namespace = "com.hirehop.core.database"
}

dependencies {
    api(projects.core.model)
    implementation(projects.core.common)
    implementation(libs.kotlinx.coroutines.core)
}
