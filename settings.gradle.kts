pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
    }
}
rootProject.name = "hirehop"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":app")
include(":core:common")
include(":core:data")
include(":core:database")
include(":core:designsystem")
include(":core:domain")
include(":core:model")
include(":core:navigation")
include(":core:screenshot")
include(":core:testing")
include(":core:ui")

include(":feature:applications:api")
include(":feature:applications:impl")
include(":feature:profile:api")
include(":feature:profile:impl")
include(":feature:analysis:api")
include(":feature:analysis:impl")
include(":feature:tailor:api")
include(":feature:tailor:impl")

check(JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
    """
    HireHop requires JDK 17+ but it is currently using JDK ${JavaVersion.current()}.
    Java Home: [${System.getProperty("java.home")}]
    """.trimIndent()
}
