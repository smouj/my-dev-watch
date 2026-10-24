pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
    }
}

rootProject.name = "my-dev-watch"

include(":core:model")
include(":core:protocol-api")
include(":core:protocol-gloryfit")
include(":core:watchface")
include(":core:capture")
include(":core:firmware")
include(":tools:cli")

/**
 * The Android application is part of this build, but it is only configured when an
 * Android SDK is actually available. That keeps `./gradlew test` usable on a plain
 * JDK (CI core job, contributors without the SDK) without silently skipping work:
 * the Android CI job always provides ANDROID_HOME and therefore always builds it.
 */
val androidSdkAvailable: Boolean = when (providers.gradleProperty("withAndroid").orNull) {
    "true" -> true
    "false" -> false
    else -> System.getenv("ANDROID_HOME") != null || System.getenv("ANDROID_SDK_ROOT") != null
}

if (androidSdkAvailable) {
    include(":android:app")
} else {
    logger.lifecycle("Android SDK not found: skipping :android:app (set ANDROID_HOME or -PwithAndroid=true)")
}
