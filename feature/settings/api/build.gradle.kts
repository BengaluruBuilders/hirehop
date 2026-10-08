plugins {
    alias(libs.plugins.tailormyresume.android.feature.api)
}

android {
    namespace = "com.tailormyresume.feature.settings.api"
}

dependencies {
    api(projects.core.model)
}
