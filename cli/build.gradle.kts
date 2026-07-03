import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    fun KotlinNativeTarget.cliBinary() {
        binaries.executable {
            entryPoint = "main"
            baseName = "updater"
        }
    }
    mingwX64 { cliBinary() }
    linuxX64 { cliBinary() }
    macosArm64 { cliBinary() }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core)
            implementation(libs.clikt)
            implementation(libs.mordant)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
