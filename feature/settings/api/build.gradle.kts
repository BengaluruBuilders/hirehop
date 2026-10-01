plugins {
    alias(libs.plugins.hirehop.android.feature.api)
}

android {
    namespace = "com.hirehop.feature.settings.api"
}

dependencies {
    api(projects.core.model)
}
