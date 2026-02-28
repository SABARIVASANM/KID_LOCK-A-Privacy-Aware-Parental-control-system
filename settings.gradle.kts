pluginManagement {
    repositories {
        google()            // ✅ Required for Android and Firebase plugins
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()            // ✅ Required for Firebase SDKs
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "PrivacyAwareInterface"
include(":app")
