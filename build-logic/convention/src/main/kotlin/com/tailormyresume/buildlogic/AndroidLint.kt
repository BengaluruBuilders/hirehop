package com.tailormyresume.buildlogic

import com.android.build.api.dsl.Lint
import org.gradle.api.Project

internal fun Project.configureAndroidLint(lint: Lint) {
    lint.apply {
        warningsAsErrors = true
        abortOnError = true
        checkReleaseBuilds = true
        baseline = file("lint-baseline.xml").takeIf { it.exists() }
        disable += setOf(
            "GradleDependency",
            "NewerVersionAvailable",
            "AndroidGradlePluginVersion",
        )
    }
}
