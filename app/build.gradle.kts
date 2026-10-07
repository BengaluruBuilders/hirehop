plugins {
    alias(libs.plugins.hirehop.android.application)
    alias(libs.plugins.hirehop.android.application.compose)
    alias(libs.plugins.hirehop.hilt)
    alias(libs.plugins.dependency.guard)
}

android {
    namespace = "com.hirehop.app"

    defaultConfig {
        applicationId = "com.hirehop.app"
        versionCode = 1
        versionName = "0.1.0"
    }

    buildFeatures {
        buildConfig = true
    }

    flavorDimensions += "backend"
    productFlavors {
        create("demo") {
            dimension = "backend"
        }
        create("prod") {
            dimension = "backend"
            buildConfigField("String", "HIREHOP_API_BASE_URL", "\"https://apps-backend.fly.dev\"")
            buildConfigField(
                "String",
                "HIREHOP_WEB_CLIENT_ID",
                "\"${providers.gradleProperty("hirehopWebClientId").getOrElse("")}\"",
            )
        }
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
    implementation(projects.feature.settings.api)
    implementation(projects.feature.settings.impl)
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

    "prodImplementation"(projects.core.network)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.lifecycle.viewModelCompose)
    implementation(libs.androidx.navigation3.ui)

    testImplementation(projects.core.testing)
    testImplementation(libs.truth)
}

dependencyGuard {
    configuration("demoReleaseRuntimeClasspath")
    configuration("prodReleaseRuntimeClasspath")
}
