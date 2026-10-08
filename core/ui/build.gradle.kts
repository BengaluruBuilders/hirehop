plugins {
    alias(libs.plugins.tailormyresume.android.library)
    alias(libs.plugins.tailormyresume.android.library.compose)
}

android {
    namespace = "com.tailormyresume.core.ui"
}

dependencies {
    api(projects.core.designsystem)
    api(projects.core.model)
}
