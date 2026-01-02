pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Adding the specific Google Maven repository URL for beta/specialized libraries
        maven { url = uri("https://maven.google.com/") }
    }
}

rootProject.name = "Focus Filter"
include(":app")
