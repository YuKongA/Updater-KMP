import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

plugins {
    id("updater.multiplatform")
    id("updater.serialization")
}

kotlin {
    fun KotlinNativeTarget.cliBinary(vararg stripArgs: String) {
        binaries.executable {
            entryPoint = "main"
            baseName = "updater"
            if (buildType == NativeBuildType.RELEASE) linkerOpts(*stripArgs)
        }
    }
    mingwX64 { cliBinary("-Wl,-s", "-Wl,--gc-sections", "-Wl,--icf=all") }
    linuxX64 { cliBinary("-Wl,-s", "-Wl,--no-export-dynamic", "-Wl,--gc-sections", "-Wl,--icf=all") }
    macosArm64 { cliBinary("-Wl,-x") }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.data)
            implementation(libs.koin.core)
            implementation(libs.clikt)
            implementation(libs.mordant)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
