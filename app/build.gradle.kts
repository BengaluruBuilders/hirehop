plugins {
    alias(libs.plugins.hirehop.android.application)
    alias(libs.plugins.hirehop.android.application.compose)
    alias(libs.plugins.hirehop.hilt)
}

android {
    namespace = "com.hirehop.app"

    defaultConfig {
        applicationId = "com.hirehop.app"
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            signingConfig = signingConfigs.named("debug").get()
        }
    }

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
}

dependencies {
    implementation(projects.feature.applications.api)
    implementation(projects.feature.onboarding.api)
    implementation(projects.feature.onboarding.impl)
    implementation(projects.feature.applications.impl)
    implementation(projects.feature.profile.api)
    implementation(projects.feature.profile.impl)
    implementation(projects.feature.analysis.api)
    implementation(projects.feature.analysis.impl)
    implementation(projects.feature.tailor.api)
    implementation(projects.feature.tailor.impl)

    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.core.designsystem)
    implementation(projects.core.domain)
    implementation(projects.core.model)
    implementation(projects.core.navigation)
    implementation(projects.core.ui)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.navigation3.ui)

    debugImplementation(projects.core.testing)

    testImplementation(libs.truth)
}
