plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    api(project(":core:protocol-api"))
    testImplementation(libs.kotlin.test)
    testImplementation(project(":core:capture"))
}
