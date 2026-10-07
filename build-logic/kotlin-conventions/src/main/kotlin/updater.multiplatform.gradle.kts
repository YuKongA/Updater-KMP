import top.yukonga.updater.buildlogic.ProjectConfig

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    jvmToolchain(ProjectConfig.JVM_VERSION)
}
