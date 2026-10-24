plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("io.github.smouj.mydevwatch.cli.MainKt")
    applicationName = "my-dev-watch"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:protocol-api"))
    implementation(project(":core:protocol-gloryfit"))
    implementation(project(":core:watchface"))
    implementation(project(":core:capture"))
    implementation(project(":core:firmware"))
    testImplementation(libs.kotlin.test)
}
