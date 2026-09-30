plugins {
    alias(libs.plugins.hirehop.android.library)
    alias(libs.plugins.hirehop.android.library.compose)
}

android {
    namespace = "com.hirehop.core.ui"
}

dependencies {
    api(projects.core.designsystem)
    api(projects.core.model)
}
